package de.yard.threed.graph;


import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Threshold;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.PositionUpdateTrigger;
import de.yard.threed.engine.Transform;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.util.RandomIntProvider;

/**
 * A component for an entity that is attached or moves on a graph.
 * The graph doens't affect velocity.
 * <p>
 * In 2018 we attempted a flag 'antijitter' to avoid jittering, but it not only was no solution but also broke functionality.
 * <p>
 * 21.3.24: Projection (for groundnet and thus icao and graph dependent?) removed from here because having it here makes things really complex.
 * Instead 'graph' might be a ProjectedGraph.
 * <p>
 * 20.6.24: Flag 'automoveenabled' removed
 * Created by thomass on 24.11.16.
 */
public class GraphMovingComponent extends EcsComponent {
    static Log logger = Platform.getInstance().getLog(GraphMovingComponent.class);
    public static boolean graphmovementdebuglog = false;
    // Soll eine Bewegung, sofern prinzipiell moeglich,  ohne Benutzersteuerung automatisch erfolgen. Das laesst sich disablen, um
    // Benutzersteuerung zu aktivieren. 9.10.19: Auf was bezieht sich das denn? automove ist entweder entlang eines path (wenn er gesetzt ist)
    // oder sonst einfach so im Graph.
    private boolean automove = false;
    // keycontrolled sollte nicht gleichzeitig zu automove an sein.
    // 9.10.19: Sollte nicht key bezogen sein sondern eher Request (wegen gui). Evtl. ist das ganze obsolet
    // zugunsten pick animation/menu.
    // 15.11.23: deprecated. TODO 3.4.25 change to requests (keys via InputtorequestSystem)
    public boolean keycontrolled = false;

    //16.2.18: Indeed unusual, but might be null. After a regular "path completed" it remains set.
    private GraphPosition currentposition;
    private Transform mover;
    // Temporarily set when the entity is moving. null when not moving?
    private GraphPath path;
    public static String TAG = "GraphMovingComponent";
    private GraphSelector selector;
    //wo ist "vorne"
    public long statechangetimestamp = 0;
    // The graph to which the entity is attached. During moving 'path' is set/used temporarily.
    // 31.3.20: Once we thought it might not be a TrafficGraph (for passing properties like 'icao', which indeed is bad coupling)
    // but of cource it is valid for sub classes. However TrafficGraph no longer is a sub class of Graph.
    // 22.3.24 Might also be a ProjectedGraph.
    private Graph graph;
    public boolean unscheduledmoving;
    // (vehicle)model specific rotation
    private GraphVehicleRotation customModelRotation;
    private PositionUpdateTrigger positionUpdateTrigger = new PositionUpdateTrigger();
    // Execute each 10th of code reaches
    private Threshold positionCheckThreshold = new Threshold(10);

    /**
     * mover darf null sein, z.B. fuer Tests. Aber auch fuer etwas unsichtbares. visualizer natuerlich auch.
     * 29.5.17: Mir dünkt, dass mover hier doch obselet ist.
     * 13.2.18: Wird aber vom System verwendet.
     * 1.3.18: kein visualizer, der wird nicht gebraucht.
     * 15.3.18: Aber der Graph muss bekannt sein. Das duerfte aber nicht problematisch sein im Sinne von zu viel Referenzen. Der wird aber spaeter gesetzt.
     *
     * @param mover
     */
    public GraphMovingComponent(/*Graph graph,*/ Transform mover /*GraphVisualizer visualizer,* / GraphPosition currentposition*/) {
        //super(mover, observer);
        //this.graph = graph;
        this.mover = mover;
        //this.visualizer = visualizer;
        this.currentposition = currentposition;
        // ein Default Selector
        selector = new RandomGraphSelector(new RandomIntProvider());
    }
    /*public GraphMovingComponent(/*Graph graph,* /Transform mover/*, GraphVisualizer visualizer, GraphPosition currentposition* /) {
        this(mover/*graph,mover, currentposition* /);
    }*/

    public GraphMovingComponent(/*GraphPosition currentposition*/) {
        mover = null;
        // 10.4.20 auch ein Default Selector
        selector = new RandomGraphSelector(new RandomIntProvider());
    }

    //@Override
   /* public void doinit() {
        mover.setPosition(currentposition.get3DPosition().add(visualizer.getPositionOffset()));
        mover.setRotation(currentposition.get3DRotation());
    }*/

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Die aktuelle Position bleibt erhalten. Von dort erfolgt die Bewegung erstmal
     * bis zum Pfadbeginn. Dann erst greift der Selektor. Der Pfad sollte natürlich dann da auch wirklich beginnen.
     * Optional kann der Path eine startposition enthalten. Da wird sofort hingesprungen.
     * Beginnt optional die Bewegung.
     *
     * @param path
     */
    public void setPath(GraphPath path, boolean enableAutomove) {
        this.path = path;
        automove = enableAutomove;
        selector = new GraphPathSelector(path);
        if (graphmovementdebuglog) {
            logger.debug("setPath: " + path + " with startposition " + path.startposition);
        }
        if (path.startposition != null) {
            currentposition = path.startposition;
            currentposition.reversegear = path.backward;
        }
        statechangetimestamp = Platform.getInstance().currentTimeMillis();
    }

    /**
     * Wenn sich der Graph aendert. Aber fuer Einheitlichkeit auch bei Erstnutzung.
     */
    public void setGraph(Graph graph, GraphPosition position) {
        this.graph = graph;
        this.currentposition = position;
    }

    /**
     * 28.3.20: Ist schon praktisch um zu ermitteln, wo sich ein Vehicle befindet
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Die Position vorsetzen. Positiver Wert vorwaerte, negativer rückwärts. Dabei ist die Richtung
     * aber auch von der Orientierung abhängig.
     * 3.4.17: Aus Graphposition hierhin verschoben. Ist zwar auch (noch) nicht schön, aber der selector gehört wirklich nicht in die Position.
     * Liefert den path, wenn er completed wurde (und wirklich nur in dem Aufruf)
     * 26.4.18: Bei Movement mit 'W' kann es passieren, dass nach einem completedPath an der finalpositiuon weitergefahren wird?
     *
     * @param amount
     */
    public GraphPath moveForward(double amount) {
        if (currentposition == null) {
            logger.error("no currentposition. Probably setGraph() not called");
            return null;
        }
        GraphPath pathcompleted = null;
        if (graphmovementdebuglog) {
            logger.debug("moveForward: amount=" + amount + ", path " + ((path != null) ? "isType set" : "isType not set") + ",currentposition=" + currentposition);
        }
        // 18.7.17: Wenn ich auf einem Gleis eine Lok, die vorwärts Richtung links fahrt,  anhebe, umgekehrt draufsetze und wieder vorwärts fahren lasse, fährt sie nach rechts (oder wieder links?
        // könnte vom Strom abhängen). Hier sollte aber mal festgelegt werden, dass vorwaerts immer in Richtung der Orienierung ist. Also evtl. amount umkehren.
        // Das ist aber schon dadurch berücksichtigt, dass bei reverse die edgeposition von "to" gemwessen wird. Aber dann brauch ich wohl ein weiteres Attrbiut Rückwärtsgang.
        // Der Aufrufer kann ja nicht mit negativen Werten bei rückwärts fahrenden aufrufen. Der Name moveForward ist daher etwas irreführend.
        if (currentposition.reversegear) {
            amount = -amount;
        }
        // evtl. mehrfach auf Nachfolgekante wechseln.
        if (amount > 0) {
           /* if (currentposition.reverseorientation) {
                stepsize= -888;
            } else {
                stepsize=currentposition.currentedge.getLength() - currentposition.edgeposition;
                currentposition.edgeposition += stepsize;*/
            int iterations = 0;

            currentposition.edgeposition += amount;
            while (currentposition.edgeposition > currentposition.currentedge.len) {
                // Ich stehe auf der to-Node. Nö, so kann man das nicht sagen.
                // Bis zur to-Node kann ich erstmal gehen. Eine neue Edge auswaehlen und dort weitergehen.
                // 3.4.17: Die Entscheidung liegt komplett im Selector. Wenn der so will, gehe ich auch dieselbe Edge wieder zurück.
                GraphNode switchnode = (currentposition.reverseorientation) ? currentposition.currentedge.from : currentposition.currentedge.to;
                GraphPathSegment newsegment = selector.findNextEdgeAtNode(currentposition.currentedge, switchnode);
                if (newsegment == null) {
                    // Es gibt keine Nachfolgekante. auf der aktuellen Kante am Ende stehen bleiben. Der path kann jetzt weg.
                    currentposition.edgeposition = currentposition.currentedge.len;
                    if (path != null) {
                        pathcompleted = movepathCompleted();
                    }
                } else {
                    if (graphmovementdebuglog) {
                        logger.debug("new segment isType " + newsegment);
                        if (newsegment.edge.getName().equals("")) {
                            newsegment = newsegment;
                        }
                    }
                    adjustPositionOnNewEdge(switchnode, newsegment, currentposition.edgeposition - currentposition.currentedge.len);

                    /*GraphEdge newedge = newsegment.edge;
                    if (outbound == newedge.from) {
                        currentposition.reverseorientation = false;
                    } else {
                        currentposition.reverseorientation = true;
                    }
                    currentposition.edgeposition -= currentposition.currentedge.getLength();
                    currentposition.currentedge = newedge;
                    currentposition.reversegear = false;//newsegment.backward;*/
                }
                if (iterations++ > 10000) {
                    logger.warn("aborting after " + iterations + " with amount " + amount + ". Might be not fitting to dimensions.");
                    break;
                }
            }
        }

        if (/*4.4.17 currentposition.edgeposition > 0 &&*/ amount < 0) {
            //addieren ist hier richtig, weils negativ ist
            currentposition.edgeposition += amount;
            while (currentposition.edgeposition < 0) {
                // Underflow auf Vorgaenger
                GraphNode switchnode = (currentposition.reverseorientation) ? currentposition.currentedge.to : currentposition.currentedge.from;
                GraphPathSegment newsegment = selector.findNextEdgeAtNode(currentposition.currentedge, switchnode);
                if (newsegment == null) {
                    // Es gibt keine Nachfolgekante. auf der aktuellen Kante am Anfang/Ende stehen bleiben
                    //      currentposition.edgeposition = currentposition.currentedge.len;
                    //27.4.18: das scheint nicht ganz sauber. Ich bin den Path zurueckgefahren und sollte doch am Anfang stehen.Bzw einfach stehen bleiben. 
                    //Darum reverse beachten und denn path nicht completen, denn vielleicht geh ich ja wieder vorwaerts.
                    currentposition.edgeposition = (currentposition.reverseorientation) ? currentposition.currentedge.getLength() : 0;
                    /*27.4.18if (path != null) {
                        pathcompleted = movepathCompleted();
                    }*/
                } else {
                    //GraphEdge newedge = newsegment.edge;
                    adjustPositionOnNewEdge(switchnode, newsegment, Math.abs(currentposition.edgeposition));
                    /*if (switchnode == newedge.to) {
                        //19.7.17: isType doch vertauscht? Zumindest kommt es einem so vor.
                        currentposition.reverseorientation = false;
                    } else {
                        currentposition.reverseorientation = true;
                    }
                    currentposition.edgeposition = newedge.len + currentposition.edgeposition;
                    currentposition.currentedge = newedge;                    
                    currentposition.reversegear = false;//newsegment.backward;*/
                }
            }
        }
        if (graphmovementdebuglog) {
            logger.debug("moveForward: completed,currentposition=" + currentposition);
        }
        return pathcompleted;
    }

    public GraphPath getPath() {
        return path;//state.equals(MOVING);
    }

    /**
     * static zur Entkopplung (und Test).
     *
     * @return
     */
    private void adjustPositionOnNewEdge(GraphNode switchnode, GraphPathSegment newsegment, double remaining) {
        /*if (switchnode == newedge.to) {
            //19.7.17: isType doch vertauscht? Zumindest kommt es einem so vor.
            currentposition.reverseorientation = false;
        } else {
            currentposition.reverseorientation = true;
        }*/
        // newpos.reverseorientation = (switchnode == newedge.to) && oldposition.reverseorientation;
        GraphEdge newedge = newsegment.edge;
        if ((switchnode == newedge.to && switchnode == currentposition.currentedge.from) ||
                (switchnode == newedge.from && switchnode == currentposition.currentedge.to)) {
            currentposition.reverseorientation = currentposition.reverseorientation;
        } else {
            currentposition.reverseorientation = !currentposition.reverseorientation;
        }
        if (newsegment.changeorientation) {
            currentposition.reverseorientation = !currentposition.reverseorientation;
        }

        if (newedge.to == switchnode) {
            // entering through to
            if (currentposition.reverseorientation) {
                currentposition.edgeposition = remaining/*newedge.len + currentposition.edgeposition*/;
            } else {
                currentposition.edgeposition = newedge.len - remaining;//currentposition.edgeposition*/;
            }
        } else {
            // entering through from
            if (currentposition.reverseorientation) {
                //OK
                currentposition.edgeposition = newedge.len - remaining/*newedge.len + currentposition.edgeposition*/;
            } else {
                currentposition.edgeposition = remaining;//currentposition.edgeposition*/;
            }
        }
        currentposition.currentedge = newedge;
        currentposition.reversegear = false;//newsegment.backward;
    }

    /**
     * ich geh davon aus, dass ich am Ende einer Edge und damit an einer Node stehe.
     * 3.8.17: Das muss aber nicht in Layer 0 sein, z.B. doorapproach. Darum bleibe
     * ich einfach da stehen wo ich bin. Die Problematik, dass vielleicht jemand (m)einen Teilgraph
     * entfernt, gibt es ja immr.(Wiki)
     * 15.8.17: Das ist zu sehr Servicepoint orientiert. Am Ende eines Move Path sollte die Position irgendwo definiert
     * sein. Ob das Layer 0 oder ein anderes ist, ist eigentlich egal. Aber eher nichts tempoärese wie ein smooth path.
     * Da kann man die Dinger ja nie freigeben. Weil das hier nicht entscheidbar ist, bekommt der Path eine final position.
     *
     * @return
     */
    private GraphPath movepathCompleted() {
        if (graphmovementdebuglog) {
            logger.debug("move path completed with final position " + path.finalposition);
        }
        // find corresponding position in layer 0 if a path was used.
        /*GraphNode nextnode = currentposition.getNodeInDirectionOfOrientation();
        Vector3 dir = currentposition.currentedge.getEffectiveInboundDirection(nextnode);
        GraphPosition newpos = null;
        for (GraphEdge e : nextnode.edges) {
            // be quite tolerant
            if (e.getLayer() == 0 && Vector3.getAngleBetween(e.getEffectiveInboundDirection(nextnode), dir) < 0.01f) {
                newpos = GraphPosition.buildPositionAtNode(e, nextnode, false);
                if (graphmovementdebuglog) {
                    logger.debug("switching to new current position in layer 0:" + newpos);
                }
                break;
            }
        }
        if (newpos == null) {
            logger.error("No corresponding position in layer 0");
        } else {
            currentposition = newpos;
        }*/
        if (path.finalposition != null) {
            //logger.debug("switching to new current position :" + path.finalposition);
            currentposition = path.finalposition;
        }
        GraphPath pathforreturn = path;
        path = null;
        automove = false;
        statechangetimestamp = Platform.getInstance().currentTimeMillis();
        return pathforreturn;
    }

    public void setSelector(GraphSelector selector) {
        this.selector = selector;
    }

    /*
    @Override
    public void moveForward(float amount) {
        target.translateOnAxis(new Vector3(0, 0, -1), amount);
    }

    }*/

    public static GraphMovingComponent getGraphMovingComponent(EcsEntity e) {
        GraphMovingComponent gmc = (GraphMovingComponent) e.getComponent(GraphMovingComponent.TAG);
        return gmc;
    }

    public boolean pathCompleted() {
        return path == null;
    }

    /**
     * Liefert die Laenge des noch zu movenden Path. MAXFLOAT, wenn es keinen path gibt.
     *
     * @return
     */
    public double getRemainingPathLen() {
        if (path == null) {
            return java.lang.Double.MAX_VALUE;
        }
        return path.getLength(currentposition);
    }

    public GraphPosition getCurrentposition() {
        return currentposition;
    }

    /**
     * muesste ich doch ohne auskommen. Dafer gibt es setPath mit startposition. Darum private.
     * 16.5.18: Obwohl das tricky sein kann, weil dann die Edge der startposition die erste im (smoothed) path sein muss.
     *
     * @param currentposition
     */
    private void setCurrentposition(GraphPosition currentposition) {
        this.currentposition = currentposition;
    }

    public void setAutomove(boolean enabled) {
        automove = enabled;
    }

    /**
     * 9.1.19: Ist das nicht viel zu GroundServices spezifisch? Mal deprecated.
     *
     * @param unscheduledmoving
     */
    @Deprecated
    public void setUnscheduledmoving(boolean unscheduledmoving) {
        this.unscheduledmoving = unscheduledmoving;
    }


    public void setPosRot(LocalTransform posRot) {
        mover.setPosRot(posRot);
    }

    public Vector3 getPosition() {
        return mover.getPosition();
    }

    public boolean hasAutomove() {
        return automove;
    }

    public void checkForPositionUpdate() {
        // don't execute each frame
        if (positionCheckThreshold.reached(1)) {
            positionUpdateTrigger.checkForPositionUpdate(mover);
        }
    }

    public Quaternion getModelRotation() {
        if (customModelRotation == null) {
            return new Quaternion();
        }
        return customModelRotation.getVehicleOrientation(getCurrentposition(), graph);
    }

    public void setVehicleRotation(GraphVehicleRotation graphVehicleRotation) {
        customModelRotation = graphVehicleRotation;
    }
}

class GraphPathSelector implements GraphSelector {
    Log logger = Platform.getInstance().getLog(GraphPathSelector.class);
    private GraphPath path;
    boolean initial = true;

    GraphPathSelector(GraphPath path) {
        this.path = path;
    }

    /**
     * Liefert die naechste Edge die "incomingedge" nachfolgt. Sonst null.
     * 27.4.18: Ein GraphSelector darf nicht wieder vorne beginnen, wenn er einmal abgefahren wurde.
     * Beim ersten Aufruf wird die incomingedge i.d.R. nicht gefunden, weil der Path noch nicht eingefahren wurde. Dann
     * kommt das erst Element.
     */
    @Override
    public GraphPathSegment findNextEdgeAtNode(GraphEdge incomingedge, GraphNode node) {
        int index = -1;//path.path.indexOf(incomingedge);

        for (int i = 0; i < path.getSegmentCount(); i++) {
            GraphEdge e = path.getSegment(i).edge;
            if (e.equals(incomingedge)) {
                index = i;
            }
        }
        //26.4.18: Das muessen wir aber beachten, sonst steh ich wieder vorne. Aber nicht beim ersten Durchlauf
        if (index == -1) {
            if (!initial) {
                return null;
            } else {
                return path.getSegment(0);
            }
        }
        initial = false;

        GraphPathSegment seg = path.getSegment(index);
        // die node beachten um backward erkennen zu koennen.
        if (node.equals(seg.getEnterNode())) {
            // das ist ein backward
            //logger.debug("found backward at index "+index+",seg="+seg.edge.getName()+",node="+node.name);
            if (index > 0) {
                seg = path.getSegment(index - 1);
            } else {
                // end reached
                seg = null;
            }
        } else {
            //logger.debug("found forward at index "+index+",seg="+seg.edge.getName()+",node="+node.name);

            if (index < path.getSegmentCount() - 1) {
                seg = path.getSegment(index + 1);
            } else {
                // end reached
                seg = null;
            }
        }
        return seg;
    }
}