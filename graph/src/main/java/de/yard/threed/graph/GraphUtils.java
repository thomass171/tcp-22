package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.MathUtil2;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Graph methods.
 * Not using attribute heading.
 * <p>
 * Created by thomass on 29.03.17.
 */
public class GraphUtils {
    static Log logger = Platform.getInstance().getLog(GraphUtils.class);
    private static boolean graphutilsdebuglog = false;
    public static boolean strict = false;

    /**
     * Die zu winkeligen Kanten an einer Node durch das Einfügen von Bögen glätten. Die gesmoothten Kanten werden nachgehalten und nicht mehrfach
     * gesmoothed.
     * <p>
     * Skizze 29
     * <p>
     * Liefert Liste der eingebauten Bogenstuecke.
     *
     * @param node
     */
    public static List<GraphEdge> smoothNode(Graph graph, GraphNode node, double radius, int layer) {
        GraphSmoothing smoothing = graph.getSmoothing();
        List<GraphEdge> arcs = new ArrayList<GraphEdge>();
        for (GraphEdge edge : node.edges) {

            Vector3 effectiveincomingdir = edge.getEffectiveInboundDirection(node);
            for (int i = 0; i < node.edges.size(); i++) {
                GraphEdge e = node.edges.get(i);
                if (e != edge) {
                    Vector3 effectivedir = e.getEffectiveOutboundDirection(node);
                    double angle = Vector3.getAngleBetween(effectiveincomingdir, effectivedir);
                    // erstmal willkürliches unteres Limit 0.1 . Und zu spitze Winkel (>135 Grad) uebgergehn. Dafür gibt es (noch) keine Lösung.
                    // 3.5.17: mal ca. 160 Grad nehmen.
                    if (angle > 0.1f && angle < MathUtil2.PI_2 * 1.8f/*1.5f*/) {
                        // Pruefung ob Gegenrichtung schon gesmoothed ist
                        if (!smoothing.areSmoothed(e, edge)) {
                            GraphEdge a = addAlternateRouteByArc(graph, edge.getOppositeNode(node), edge, node, e, e.getOppositeNode(node), radius, layer);
                            if (a != null) {
                                arcs.add(a);
                                smoothing.addSmoothedEdges(e, edge);
                            }
                        }
                        // fuer Tests if (arcs.size() >= 3)
                        //   return arcs;
                    }
                }

            }
        }
        return arcs;
    }

    /**
     * Den Pfad zwischen zwei Nodes über eine dritte Node durch einen zusätzlichen neuen Pfad abbilden, der die Zwischennode auslässt und einen Bogen verwendet.
     * Bleibt erstmal eingeschraenkt auf die z=0 Ebene. TODO checken dass z überall 0 ist?.
     * Skizze 29.
     * <p>
     * Liefert das eingebaute Bogenstueck oder null, wenn es nicht angelegt werden kann.
     */
    public static GraphEdge addAlternateRouteByArc(Graph graph, GraphNode start, GraphEdge e1, GraphNode mid, GraphEdge e2, GraphNode end, double radius, int layer) {
        //logger.debug("building alternate route from " + start.getName() + start.getLocation() + " by " + mid.getName() + mid.getLocation() + " to " + end.getName() + end.getLocation());
        return addArcToAngleSimple(graph, start, e1, mid.getLocation(), e2, end, radius, true, false, layer, false);
    }


    /**
     * deprecated weil die Turns temporär sind. Von daher fuerfte es keine geben.
     *
     * @param entrypoint
     * @return
     */
    @Deprecated
    public static GraphNode getOrCreateTearDropTurn(GraphNode entrypoint) {

        return null;
    }

    /**
     * Die Kante an ihrer to node mit einer geraden Kante erweitern in die Richtung der Kante an der to node.
     */
    public static GraphEdge extendWithEdge(Graph graph, GraphEdge edge, double len, int layer) {
        Vector3 toloc = edge.to.getLocation();
        // TODO Bei Bögen ist die direction am to die Tangente, nicht die from/to direction.
        // TODO gehört das deswegen in die Railingfactory?
        Vector3 dir = edge.getDirection();
        Vector3 destination = toloc.add(dir.normalize().multiply(len));
        GraphNode destnode = graph.addNode("", destination);
        GraphEdge e = graph.connectNodes(edge.to, destnode, "", layer);
        return e;
    }

    /**
     * An node mit einem Bogen aus Richtung von inbound fortsetzen.
     * nicht fertig!
     *
     * @param graph
     * @param node
     * @param inbound
     * @param radius
     * @param left
     * @return
     */
    public static GraphNode extend(Graph graph, GraphNode node, GraphEdge inbound, double radius, boolean left, int layer) {
        Vector3 dir = inbound.getEffectiveDirectionAtNode(node, false).negate();
        Vector3 upVector = new Vector3(0, 0, 1);
        if (!left) {
            // dann muss der Bogen auf die andere Seite
            upVector = new Vector3(0, 0, -1);
        }
        Vector3 arcdir;// = Vector3.getCrossProduct(dir, upVector).normalize();
        arcdir = dir.rotate(Quaternion.buildRotationZ(new Degree((left) ? 117 : -117)));
        Vector3 nodelocation = node.getLocation().add(arcdir.multiply(2 * radius));
        GraphNode newnode = graph.addNode("", nodelocation);
        GraphEdge arc = graph.connectNodes(node, newnode, "", layer);
        arcdir = dir.rotate(Quaternion.buildRotationZ(new Degree((left) ? 90 : -90)));
        Vector3 arccenter = node.getLocation().add(arcdir.multiply(radius));
        //8.5.18: arc.setArc(arccenter, radius, MathUtil2.PI * 1.3f);
        GraphArc ga = new GraphArc(arccenter, radius, arc.from.getLocation().subtract(arccenter), new Vector3(0, 1, 0), MathUtil2.PI * 1.3f);
        arc.setArc(ga);
        return newnode;
    }

    /**
     * Caclulation of an circle embedded in an angle of two edges. Either inner arc (covering beta) shortening v1->v2 or outer arc (covering alpha) reconnecting v2->v1.
     * The edges do not need to be connected in a mid node. The intersection might also be a virtual one.
     * <p>
     * Bleibt erstmal eingeschraenkt auf die z=0 Ebene. TODO checken dass z überall 0 ist?.
     * Als radius kann auch 'd' uebergeben werden, markiert mit radiusisdistance.
     * Skizze 29.
     * Die Arc Edge geht immer von start->end. D.h. arcbeginloc ist immer auf e1.
     * <p>
     * Returns null if edges are parallel (and only in this case)
     * Geht auch in 3D.
     * 9.2.18: For 3D (z!=0) only used for connected edges.
     * Wegen Rueckwaertskompatibilät (Transisiotn) lass ich not connected erstmal doch zu. Auch nicht conneted koennen ja einen Schnittpunkt haben.
     * <p>
     * Die Orientierung der Edges speilt keine Rolle. Die Berechnung basiert auf start->mid->end, hier wird die effective direction verwendet.
     *
     * @return
     */
    public static GraphArcParameter calcArcParameter(GraphNode start, GraphEdge e1, Vector3 intersection, GraphEdge e2, GraphNode end, double radius, boolean inner, boolean radiusisdistance) {
        if (graphutilsdebuglog) {
            logger.debug("calc arc from " + start.getName() + start.getLocation() + " by " + intersection + " to " + end.getName() + end.getLocation());
        }
        //suitable for meter dimensions
        //v1 und v2 erstmal nur als normalisierte Richtung
        Vector3 v1 = e1.getEffectiveOutboundDirection(start);
        Vector3 v2 = e2.getEffectiveInboundDirection(end);
        // mit getAngleBetween erkennt man nicht die Richtung (alpha>180) bzw dann gegenüberliegenden KReis. Aber toangles ist viel zu anfaellig.
        double alpha = MathUtil.PI - Vector3.getAngleBetween(v1, v2);
        double beta = MathUtil.PI - alpha;
        double distancefromintersection;
        if (radiusisdistance) {
            distancefromintersection = radius;
            radius = (Math.tan(alpha / 2) * distancefromintersection);
        } else {
            distancefromintersection = (radius * Math.tan(beta / 2));
        }
        // Über das Kreuzprodukt die Orientierung ermitteln (auf welcher Seite liegt der KReis).
        // Der Knackpunkt, warum es auf z=0 Eben beschraenkt ist.?
        //15.3.18: Fuer das KP muss die Orientierung gedreht werden. Aus Kompatitbilaet mit Tests negiere ich den jetzt. Oder besser nicht?
        Vector3 kp = Vector3.getCrossProduct(v1, v2);//.negate();
        Vector3 upVector = new Vector3(0, 0, 1);
        //MA21: wegen 3D darum jetzt kp als upVector.
        //Hmmm upVector=kp;
        //15.3.18 z darf bestimmt nicht geprüft werden
        /*if (/*angles[2] > 0* /kp.getZ() > 0) {
            // dann muss der Bogen auf die andere Seite
            upVector = new Vector3(0, 0, -1);
            // 15.3.18: kp wird ja auch der Berechnung später verwendet und enthäelt ja schon die andere Drehung. Darum beta nicht negieren. Wohl doch.
            //15.3.18: 
            beta = -beta;
            //alpha = -alpha;
        }*/
        // security checks for 'd'. Required due to possible rounding problems? Matehmaticcally impossible(??).
        // 3.5.17: Indeed these might occur, of course. And than drawing the arc doesn't make any sense or isType impossible respectively.
        // but for outer arcs this might also happen due to rounding. So be more tolerant.
        if (graphutilsdebuglog) {
            logger.debug("v1=" + v1);
            logger.debug("v2=" + v2);
            logger.debug("distancefromintersection=" + distancefromintersection + ",radius=" + radius + ",alpha=" + alpha + ",beta=" + beta);
        }
        Vector3 radiusvector;// = Vector3.getCrossProduct(v1, upVector).normalize().multiply(radius);
        Vector3 ex = Vector3.getCrossProduct(v1, kp).normalize();
        Vector3 ey = Vector3.getCrossProduct(v2, kp).normalize();
        //v1 = v1.multiply(e1len - distancefromintersection);
        v2 = v2.multiply(distancefromintersection);

        Vector3 arcbeginloc;// = start.getLocation().add(v1);
        arcbeginloc = intersection.subtract(v1.multiply(distancefromintersection));
        //MA21: radisuvector ohne up berechnen
        radiusvector = ex.negate().multiply(radius);
        Vector3 arccenter = arcbeginloc.add(radiusvector);
        if (graphutilsdebuglog) {
            logger.debug("arccenter=" + arccenter);
            logger.debug("ex=" + ex);
            logger.debug("radiusvector=" + radiusvector);
        }
        if (!inner) {
            beta = beta - MathUtil2.PI2;
        }
        return new GraphArcParameter(arccenter, radius, distancefromintersection, arcbeginloc, beta, v2, kp, ex, kp);
    }

    /**
     * Arc zwischen zwei connected edges.
     * Liefert null, wenn der arc nicht moeglich ist.
     */
    public static GraphArcParameter calcArcParameterAtConnectedEdges(GraphEdge e1, GraphEdge e2, double radius, boolean inner, boolean radiusisdistance) {

        GraphNode intersectionnode = e1.getNodeToEdge(e2);
        if (intersectionnode == null) {
            logger.error("edges not connected");
            return null;
        }
        GraphNode start = e1.getOppositeNode(intersectionnode);
        Vector3 intersection = intersectionnode.getLocation();
        GraphNode end = e2.getOppositeNode(intersectionnode);
        return calcArcParameter(start, e1, intersection, e2, end, radius, inner, radiusisdistance);
    }

    /**
     * Liefert null, wenn der Bogen nicht passt.
     * TODO doofe Lösung wegen Fehlerbahdnlung. Evt. radius verkleinern.
     * 10.8.17: Das ist wirklich doof, denn damit gibt es häufig posiitonen ohne teardrop und dann bleiben Vehicle da schlicht hängen (und fressen CPU)
     */
    public static GraphEdge addArcToAngleSimple(Graph graph, GraphNode start, GraphEdge e1, Vector3 mid, GraphEdge e2, GraphNode end, double radius, boolean inner, boolean radiusisdistance, int layer, boolean nonregular) {
        GraphArcParameter para = calcArcParameter(start, e1, mid, e2, end, radius, inner, radiusisdistance);
        double e1len = e1.getLength();
        double e2len = e2.getLength();

        if (para.distancefromintersection > e1len + 0.0001f) {
            // not possible to draw arc
            logger.warn("skipping arc because of d=" + para.distancefromintersection + ", e1len=" + e1len);
            return null;
        }
        if (para.distancefromintersection > e2len + 0.0001f) {
            // not possible to draw arc
            logger.warn("skipping arc because of d=" + para.distancefromintersection + ", e2len=" + e2len);
            return null;
        }

        return addArcToAngle(graph, start, e1, mid, e2, end, para, layer, nonregular);
    }

    /**
     * Return edge of arc
     * 28.7.17: Wenn die Schenkel zu kurz sind (bzw. der Winkel zu spitz), nicht einfach arcbegin/end auf edges start/end setzen. Das führt doch nie zu einem Bodegn!
     * Dann kann es halt keine arc geben. Pruefung aber wegen starker Auswirkung schaltbar machen.
     * <p>
     * Liefert null, wenn der Bogen nicht passt. TODO doofe Lösung wegen Fehlerbahdnlung. Die paras sollten schlüssig sein.
     */
    public static GraphEdge addArcToAngle(Graph graph, GraphNode start, GraphEdge e1, Vector3 mid, GraphEdge e2, GraphNode end, GraphArcParameter para, int layer, boolean nonregular) {
        if (graphutilsdebuglog) {
            logger.debug("building arc from " + start.getName() + start.getLocation() + " by " + mid + " to " + end.getName() + end.getLocation());
        }
        double mindistancefornewnode = 0.1f;
        double e2len = e2.getLength();

        GraphNode arcbegin;
        if (Vector3.getDistance(para.arcbeginloc, start.getLocation()) > mindistancefornewnode) {
            //27.4.18: umbenannt smoothbegin->smootharcfrom um Namensgleichiet mit Edge zu vermeiden. ist confusing. Der Name der Node braucht nicht dran, der ist am arc.
            arcbegin = graph.addNode("smootharcfrom", para.arcbeginloc);
            graph.connectNodes(start, arcbegin, "smoothbegin", layer);
        } else {
            arcbegin = start;
            logger.warn("arc low distance to start");
            if (strict) {
                return null;
            }
        }
        GraphNode arcend;
        if (e2len - para.distancefromintersection > mindistancefornewnode) {
            //27.4.18: umbenannt smoothend->smootharcto um Namensgleichiet mit Edge zu vermeiden. ist confusing. Der Name der Node braucht nicht dran, der ist am arc.
            arcend = graph.addNode("smootharcto", mid/*.getLocation()*/.add(para.v2));
            graph.connectNodes(arcend, end, "smoothend", layer);
        } else {
            logger.warn("arc low distance to end");
            if (strict) {
                return null;
            }
            // 16.5.18: Skizze 11d: Nicht einfach arcend auf end setzen, weil die nachher connected werden und dann eine Rekursion entsteht. Besser duplizieren.
            // Das gilt aber nicht bei "back" und "terdrop". Da wird die Anomalie ganz bewusst so genutzt. Ob es ein Problem mit der Edge mit
            //LAenge 0 gibt, ist unklar.
            if (nonregular) {
                arcend = end;
            } else {
                arcend = graph.addNode("smootharcto", end.getLocation());
            }
        }

        GraphEdge arc;
        arc = graph.connectNodes(arcbegin, arcend, "smootharc", layer);

        //graph.connectNodes(arcend, end, "smoothend", layer);

        //15.3.18: Nicht mehr negieren. Überhaupt, das ist ja furchtbar
       /* if (para.inner) {
            arc.setArc(para.arccenter, para.radius, -para.beta);
            
        } else {
            // 18.7.17: Ein woodoo(?) Behelf um die Winkelrichtung zu ermitteln (wegen turnloop C_4).
            if (para.crossproduct.getZ() < 0) {
                // dann muss der Bogen auf die andere Seite
                arc.setArc(para.arccenter, para.radius, MathUtil2.PI2 - para.beta);
            } else {
                arc.setArc(para.arccenter, para.radius, -MathUtil2.PI2 - para.beta);
            }
        }*/
        //9.2.18: MA21: Parameter als Objekt
        arc.setArc(para.arc);
        return arc;
    }

    /**
     * An einer node eine neue edge anhaengen, die in einem bestimmten Winkel abzweigt.
     *
     * @return
     */
    public static GraphEdge createBranch(Graph graph, GraphNode node, GraphEdge edge, double branchlen, Degree angle, int layer) {
        Vector3 branchdir = edge.getEffectiveOutboundDirection(node).rotate(Quaternion.buildRotationZ(angle));
        return extend(graph, node, branchdir, branchlen, layer);
    }

    /**
     * An einer node eine neue edge anhaengen in bestimmter Richtung anhaengen.
     *
     * @return
     */
    public static GraphEdge extend(Graph graph, GraphNode node, Vector3 dir, double len, int layer) {
        dir = dir.multiply(len);
        GraphNode destination = graph.addNode("ex", node.getLocation().add(dir));
        GraphEdge branch = graph.connectNodes(node, destination, "e", layer);
        return branch;
    }

    public static GraphEdge extend2(Graph graph, GraphNode node, Vector3 location, String nodename, String edgename, int layer) {
        GraphNode destination = graph.addNode(nodename, location);
        GraphEdge branch = graph.connectNodes(node, destination, edgename, layer);
        return branch;
    }

    /**
     * Create teardrop by extending edge at node by an arc back to the opposite node on edge including smoothing of the intersection point.
     * <p>
     * Auch erstmal nur in der z0 Ebene.
     * For now leads to the opposite node of inbound.
     * Liefert den Bogen. Oder null, wenn kein Turn angelegt werden konnte.
     * layerid isType created internally.
     * <p>
     * 24.5.17: smoothnode jetzt wegen transition optional/deprecated. Fuer Railing koennte es aber sinnvoll sein.
     *
     * @return
     */
    public static TurnExtension addTearDropTurn(Graph graph, GraphNode node, GraphEdge edge, boolean left, double smoothingradius, int layer, boolean smoothnode) {
        if (graphutilsdebuglog) {
            logger.debug("creating teardrop turn");
        }

        GraphEdge approach = edge;
        GraphNode vertex = approach.getOppositeNode(/*parking.*/node);
        // erstmal ganz grob abhaengig von der Edgelänge  
        Degree angle = new Degree(((left) ? 1 : -1) * 90 / (approach.getLength() / 5));
        GraphEdge branch = GraphUtils.createBranch(graph, vertex, approach, approach.getLength(), angle, layer);
        branch.setName("teardrop.branch");
        GraphEdge teardrop = GraphUtils.addArcToAngleSimple(graph, branch.getOppositeNode(vertex), branch, vertex.getLocation(), approach, /*parking.*/node, approach.getLength(), false, true, layer, true);
        if (teardrop == null) {
            // scheitern ist vielleicht eher unwahrscheinlich. Naja, je nach Laenge
            logger.warn("teardrop failed. skipping");
            teardrop = graph.connectNodes(node, branch.getOppositeNode(vertex));
        } else {
            teardrop.setName("teardrop.smootharc");
        }
        if (smoothnode) {
            GraphUtils.smoothNode(graph, vertex, smoothingradius, layer);
        }
        return new TurnExtension(edge, branch, teardrop);
    }

    /**
     * Create uturn from nextnode.
     * Skizze xx
     * <p>
     * Auch nur in der z0 Ebene. Aber das bleibt so auch.
     * Liefert den turn als edge->arc->branch in TurnExtension.
     *
     * @return
     */
    public static TurnExtension addUTurn(Graph graph, GraphNode nextnode, GraphEdge fromedge, GraphNode destnode, GraphEdge destination, double distance, double smoothingradius, int layer) {
        if (graphutilsdebuglog) {
            logger.debug("creating U turn");
        }

        double r = smoothingradius;
        double d = distance;
        Vector3 normal = new Vector3(0, 0, 1);
        Vector3 maindir = fromedge.getEffectiveInboundDirection(nextnode);
        Vector3 vn = Vector3.getCrossProduct(maindir, normal).normalize();
        Vector3 vo = vn.multiply(smoothingradius);
        double s = Math.sqrt(3 * r * r - r * d - d * d / 4);
        if (java.lang.Double.isNaN(s)) {
            logger.warn("inconsistent uturn wird radius " + r + " and distance " + d);
            return null;
        }
        Vector3 vs = maindir.multiply(s);
        double angle = -Math.atan(s / (r + (d / 2)));

        Vector3 arc0center = nextnode.getLocation().add(vo);
        Vector3 arc1center = nextnode.getLocation().add(vn.negate().multiply(d / 2)).add(vs);
        Vector3 arc2center = destnode.getLocation().add(vo.negate());

        Vector3 e = arc1center.subtract(arc0center).multiply(0.5f);
        Vector3 n0 = arc0center.add(e);
        GraphEdge e0 = extend2(graph, nextnode, n0, "n0", "uturn0", layer);
        e0.setArc(new GraphArc(arc0center, r, vo.negate(), normal, angle));

        Vector3 ex2 = arc1center.subtract(arc2center).multiply(0.5f);
        Vector3 n1 = arc2center.add(ex2);
        GraphEdge e1 = extend2(graph, e0.getTo(), n1, "n1", "uturn1", layer);
        e1.setArc(new GraphArc(arc1center, r, e.negate(), normal, MathUtil2.PI2 - 2 * (MathUtil.PI_2 - Math.abs(angle))));

        GraphEdge e2 = graph.connectNodes(e1.getTo(), destnode, "uturn2", layer);
        e2.setArc(new GraphArc(arc2center, r, ex2, normal, angle));

        return new TurnExtension(e0, e2, e1);
    }

    /**
     * Turn loop just on one node without intersection node.
     * By extending edge at node by a ahort edge, an arc and an edge back to the same node.
     * TODO 15.8.17: Wenn der Winkel zu klein ist, führt das zu zu engen turns.
     */
    public static TurnExtension addTurnLoop(Graph graph, GraphNode node, GraphEdge incoming, GraphEdge outcoming, int layer) {
        if (graphutilsdebuglog) {
            logger.debug("creating turn loop at node=" + node + ",incoming=" + incoming + ",outcoming=" + outcoming);
        }
        double len = 20;//willkuerlich
        //Confusing: both entend end notes have name "ex". TODO rename
        GraphEdge e1 = extend(graph, node, incoming.getEffectiveInboundDirection(node), len, layer);
        e1.setName("e1");
        GraphEdge e2 = extend(graph, node, outcoming.getEffectiveInboundDirection(node), len, layer);
        e2.setName("e2");
        GraphEdge turnloop = GraphUtils.addArcToAngleSimple(graph, e1.getTo(), e1, node.getLocation(), e2, e2.getTo(), len, false, true, layer, true);
        if (turnloop == null) {
            // scheitern ist vielleicht eher unwahrscheinlich. Naja, je nach Laenge
            logger.warn("turnloop failed. skipping");
            turnloop = graph.connectNodes(e1.getTo(), e2.getTo());
        } else {
            turnloop.setName("turnloop.smootharc");
        }
        return new TurnExtension(e1, e2, turnloop);
    }

    /**
     * Create loop turn at a node for having a smooth transition from inbound to some other edge. Needed when a smoothing arc doesn't exist or isType
     * not reachable.
     * By extending edge at node by a ahort edge, an arc and an edge back through the intersection node.
     * <p>
     * Auch erstmal nur in der z0 Ebene.
     * <p>
     * Skizze 13 und 29c
     *
     * @return
     */
    /*9.5.18 public static GraphTransition createTurnLoop(Graph graph, GraphNode node, GraphEdge incoming, Vector3 intersection, GraphEdge destinationedge, GraphNode destinationnode, GraphArcParameter arcpara, int layer) {
        if (graphutilsdebuglog) {
            logger.debug("addTurnLoop: node=" + node + ",incoming=" + incoming + ",destinationedge=" + destinationedge);
        }
        double len = 20;//willkuerlich
        GraphEdge e1 = extend(graph, node, incoming.getEffectiveInboundDirection(node), len, layer);
        e1.setName("e1");
        GraphEdge e2 = extend(graph, node, destinationedge.getEffectiveInboundDirection(node), len, layer);
        e2.setName("e2");
        //GraphEdge turnloop = GraphUtils.addArcToAngle(graph, e1.getTo(), e1, node.getLocation(), e2, e2.getTo(), len, false, true, layer);
        GraphEdge arc = addArcToAngle(graph, e1.getTo(), e1, intersection, destinationedge, destinationnode, arcpara, layer);
        if (arc == null) {
            //TODO
        }
        return null;//TODO new GraphTransition(arc);
        //return new TearDropTurn(e1, e2, turnloop);
    }*/

    /**
     * Von einer Node im Bogen "zurücksetzen" und vorwärts ans andere Ende. Der Winkel und Bogen sind so gestaltet, dass das zweite Stück gerade auf den
     * Nachfolger stoesst. Von da kann dann ein smoothed path gebildet werden.
     * 2 Fälle:
     * a) Winkel zum Nachfolger ist zu klein. Dann muss die zweite Edge auch ein Bogen sein (ungelöste Geometriefragen).
     * b) Winkel zum Nachfolger ist gross genug. Dann kann die zweite Edge eine Gerade sein und die erste ist ein inner arc.
     * Man kann aber auch immer einfach 90 Grad zurücksetzen (und noch ein Stück weiter) und dann eine Transition zur Zieledge bauen.
     * Die Gefahr von Atrefakten bei Transitions ist aber sehr gross.
     * Erstmal immer b). Im Zweifel ist der Bogen halt sehr eng. Das kann man dann immer noch mal angehen.
     * <p>
     * name isType "create" instead of "find" because a temporary arc isType added.
     *
     * @return
     */
    public static TurnExtension createBack(Graph graph, GraphNode node, GraphEdge dooredge, GraphEdge successor, int layer) {
        // Den succesor um die Länge der aktuellen Edge verlängern.
        GraphEdge ext = extend(graph, dooredge.getOppositeNode(node), successor.getEffectiveInboundDirection(dooredge.getOppositeNode(node)), dooredge.getLength(), layer);
        GraphEdge arc = addArcToAngleSimple(graph, node, dooredge, dooredge.getOppositeNode(node).getLocation(), ext, ext.to, dooredge.getLength(), true, true, layer, true);
        if (arc == null) {
            // scheitern ist vielleicht eher unwahrscheinlich. Naja, je nach Laenge
            logger.warn("createBack failed. skipping");
            arc = graph.connectNodes(node, ext.to);
        }
        return new TurnExtension(ext, null, arc);
    }

    /**
     * Return path from some graph position to some node by avoiding edges "voidedges".
     * name isType "create" instead of "find" because a teardrop or other return path might be created.
     * <p>
     * Return null if no path isType found.
     * Several solutions:
     * 1) Try node in direction of orientation of current edge. If getFirst segment isType a current edge, a teardrop return isType added.
     * <p>
     * Der geliefert Path ist ab der nextnode! D.h. die aktuelle Position bleibt wie sie ist und führt auf den Path beginn.
     * Parameter smoothpath ist eigentlich nur fuer Tests sinnvoll.
     * 15.8.17: Als final position wird die "to" node bzw. die letzte unsmoothed edge eingetragen.
     * 12.2.18: Splitted to two methods
     * 16.2.18: Optional mit Relocation, um turnloop an nextnode zu vermeiden.
     * 10.4.18: Optional mit laneinfo fuer multilane.
     *
     * @return
     */
    public static GraphPath createPathFromGraphPosition(Graph graph, GraphPosition from, GraphNode to, GraphWeightProvider graphWeightProvider, GraphPathConstraintProvider graphPathConstraintProvider, int layer,
                                                        boolean smoothpath, boolean allowrelocation, GraphLane lane) {
        GraphNode nextnode = from.getNodeInDirectionOfOrientation();
        if (graphWeightProvider == null) {
            // dann nur Layer 0 zulassen
            graphWeightProvider = new DefaultGraphWeightProvider(graph, 0);
        }
        GraphPath path = graph.findPath(nextnode, to, graphWeightProvider);
        if (path == null) {
            //warning only
            logger.warn("no path found from " + from + " to " + to);
            return null;
        }
        // 3.11.17: Den Pfad als info loggen, weil das ein zentrlaer Punkt ist. Wenn der Pfad falsch ist, hat das grosse Auswirkung
        //if (graphutilsdebuglog) {

        logger.info("createPathFromGraphPosition: from " + from + ",nextnode=" + nextnode + ",path=" + path);
        //}
        if (path.getSegmentCount() == 0) {
            return null;
        }
        return createPathFromGraphPositionAndPath(graph, path, nextnode, from, to, graphPathConstraintProvider, layer, smoothpath, allowrelocation, lane);
    }


    /*13.4.18 public static GraphPath createPathFromGraphPosition(Graph graph, GraphPosition from, GraphNode to, GraphWeightProvider graphWeightProvider, double smoothingradius, int layer,
                                                        boolean smoothpath, double minimumlen) {
        return createPathFromGraphPosition(graph, from, to, graphWeightProvider, smoothingradius, layer, smoothpath, minimumlen, false, null);
    }*/

    /**
     * Return optimized/smotthed path for a path.
     * name isType "create" instead of "find" because a teardrop or other return path might be created.
     * <p>
     * Der geliefert Path ist ab der nextnode! D.h. die aktuelle Position bleibt wie sie ist und führt auf den Path beginn.
     * Parameter smoothpath ist eigentlich nur fuer Tests sinnvoll.
     * 15.8.17: Als final position wird die "to" node bzw. die letzte unsmoothed edge eingetragen.
     * 12.2.18: Splitted to two methods
     * 15.2.18: "from" kann auch null sein. Dann wird ab "nextnode" gesmoothed
     * 16.2.18: Optional mit Relocation, um z.B. turnloop an nextnode zu vermeiden.
     * 11.4.18: uturn indicator als Flag, weil das scher zu erkennen ist.
     *
     * @return
     */
    public static GraphPath createPathFromGraphPositionAndPath(Graph graph, GraphPath path, GraphNode nextnode, GraphPosition from, GraphNode to, GraphPathConstraintProvider graphPathConstraintProvider, int layer,
                                                               boolean smoothpath, boolean allowrelocation, GraphLane lane) {
        // TODO: Plausi, dass nextnode die naechste von from ist?
        //11.4.18 bei uturn gilt das aber nicht
        if (lane == null && path.getStart() != nextnode) {
            throw new RuntimeException("start != nextnode");
            //logger.warn("start != nextnode");
        }
        path = GraphUtils.bypassShorties(graph, path, graphPathConstraintProvider.getMinimumLength(), layer);
        if (path.getSegmentCount() == 0) {
            logger.warn("bypassShorties returned empty path");
            return null;
        }
        // outline path ist erst nach bypasshorties sinnvoll, weil die Shorties da ja noch viel mehr Auswirkung haben.
        boolean useuturn = false;
        if (lane != null) {
            //die Strategie hier ist, per U-Turn auf eine Lane der from edge zu kommen, auch wenn der...
            //oder Nee, einfach(?) auch einen turnloop, allerdings auf die outline. Oder halber teardrop?
            //besser direkt eine "extended U-Turn" Wende von outline auf outline, denn teardrop und turnloop basieren
            //beide auf outer arc im Winkel, was es bei U-Turn nicht gibt. Bei U-Turn ist die nextnode aber eine andere als sonst.
            //das ganz ist abhaengig von "from" Orientierung.
            if (from.currentedge.equals(path.getSegment(0).edge)) {
                useuturn = true;
            }
            path = createOutlinePath(graph, path, lane, layer, useuturn);
            //    uturnneeded = true;
        }

        GraphPath smoothedpath = new GraphPath(layer);
        smoothedpath.finalposition = GraphPosition.buildPositionAtNode(path.getLast().edge, to, false);
        if (graphutilsdebuglog) {
            logger.debug("smoothing path " + path);
        }
        int startpos = 1;
        // startsegment so lassen, wenn es das erste ist. Das Segment koennte zwar auch durch smoothing ersetzt werden, das erfodert dann aber
        // einen Reposition. Das lass ich erstmal weg.
        // Weil die from edge wegen der aktuellen Position erhalten bleiben muss, kann noch keine Transition verwendet werden. 
        // Am Einstieg in den Pfad einen loop- oder teardrop turn machen, um smooth in den Pfad zu kommen.
        // 17.7.17: Je stumpfer der Winkel zwischen den beiden Edges ist, umso merkwürdiger sieht der Turnloop dann aus.
        if (path.getSegmentCount() > 0) {
            GraphPathSegment firstsegment = path.getSegment(0);

            if (from != null) {
                if (firstsegment.edge == from.currentedge) {
                    // need to turn back to my current edge. Add teardrop for turning at the end of the current edge. 
                    TurnExtension turn = addTearDropTurn(graph, nextnode, from.currentedge, true, graphPathConstraintProvider.getSmoothingRadius(nextnode), layer, false);
                    // recalculate path ignroing the current edge and all other edges that are not the teardrop entry.
                    smoothedpath.addSegment(new GraphPathSegment(turn.arc, nextnode));
                    smoothedpath.addSegment(new GraphPathSegment(turn.branch, turn.arc.getOppositeNode(nextnode)));
                } else {
                    // getFirst segment isType not my current one. Need to find a smooth path into getFirst segment.
                    // current solution isType turnloop.
                    // das geht warum auch immer nicht ueber Transition unten in der Schleife. Das passt hier einfach nicht rein.
                    // 17.7.17: Je stumpfer der Winkel zwischen den beiden Edges ist, umso merkwürdiger (weil sehr gross) sieht der Turnloop dann aus.
                    // Darum bei sehr stumpfen Winkeln keinen turn machen. 1.8.17: Von 0.01->0.05
                    // 18.8.17: umgekehrt wird der turnloop seeehr klein, wenn der Winkel zu spitz ist (z.B. >3.0 an C_4). Das ist auch nicht gut. Aber mal kein
                    // TO DO, denn mit strict mode löst sich das viuelleicht.
                    // 30.11.17: REQ1? Gehoert aber nicht hier hin. Hier gehts nur ums erste Segment.
                    // 16.2.18: Optional mit Relocation, um turnloop zu vermeiden: wenn ich weit genug weg bin von nextnode so dass ein inner arc passt, an nextnode smoothen
                    // und eine Startposition setzen.
                    // 11.4.18: Kann auch UTurn werden, aber nur, wenn ich in "Gegenrichtung" Richtung nextnode stehe.

                    if (lane != null && useuturn) {

                        TurnExtension uturn = addUTurn(graph, nextnode, from.currentedge, firstsegment.getEnterNode(), firstsegment.edge, lane.offset,
                                graphPathConstraintProvider.getSmoothingRadius(nextnode), layer);
                        if (uturn == null) {
                            return null;
                        }
                        smoothedpath.addSegment(new GraphPathSegment(uturn.edge, nextnode));
                        smoothedpath.addSegment(new GraphPathSegment(uturn.arc, uturn.edge.getOppositeNode(nextnode)));
                        smoothedpath.addSegment(new GraphPathSegment(uturn.branch, uturn.branch.getOppositeNode(firstsegment.getEnterNode())));
                        //firstsegment bleibt erhalten. Es muss hier aber schon uebernommen werden, damit beim Smoothing nicht der letzt turnloop arc zu ersetzen versucht wird.
                        smoothedpath.addSegment(firstsegment);
                        startpos = 1;
                    } else {
                        GraphTransition relocationgt = null;
                        if (allowrelocation) {
                            // mal sehen, ob ein inner arc passt. Ich unterstelle mal, dass nextnode bzw destinationedge connected sind. Ist das hier überhaupt relevant? 
                            relocationgt = buildInnerArcOrTurnloopTransition(graph, from, nextnode, nextnode, nextnode.getLocation(), firstsegment.edge,
                                    firstsegment.edge.getOppositeNode(nextnode), graphPathConstraintProvider, layer);
                            if (graphutilsdebuglog) {
                                logger.debug("relocation gt=" + relocationgt);
                            }
                        }
                        if (relocationgt != null) {
                            smoothedpath.replaceLast(relocationgt);
                            //die neue aktuelle Position ist nie reverse, weil es eine neues Edge ist. Oder?
                            smoothedpath.startposition = new GraphPosition(relocationgt.seg.get(0).edge, from.edgeposition, false);
                            startpos = 1;
                        } else {
                            double angle = GraphEdge.getAngleBetweenEdges(from.currentedge, nextnode, firstsegment.edge);
                            if (graphutilsdebuglog) {
                                logger.debug("angle=" + angle);
                            }
                            if (angle < 0.05f || angle > 3.14) {
                                // hier ist dann nichts zu tun. Doch, ich muss das erste ruebernehmen, damit es nachher ein last gibt.
                                smoothedpath.addSegment(firstsegment);
                                startpos = 1;
                            } else {

                                //GraphTransition transition = createTransition(graph, from, firstsegment.edge, firstsegment.getLeaveNode(), GroundNet.SMOOTHINGRADIUS, layer);
                                TurnExtension turnloop = addTurnLoop(graph, nextnode, from.currentedge, firstsegment.edge, layer);
                                if (turnloop == null) {
                                    return null;
                                }
                                smoothedpath.addSegment(new GraphPathSegment(turnloop.edge, nextnode));
                                smoothedpath.addSegment(new GraphPathSegment(turnloop.arc, turnloop.edge.getOppositeNode(nextnode)));
                                smoothedpath.addSegment(new GraphPathSegment(turnloop.branch, turnloop.branch.getOppositeNode(nextnode)));
                                //mal ja mal nein. Kann aber nicht erforderlich sein, weil das ja schon oben dazukommt.
                                //smoothedpath.addSegment(firstsegment);
                                // das zweite auch uebernehmen, weil es durhc den turnloop schon richtig getroffen wird und damit es durch die folgene Transition ersetzt werden kann.
                                //smoothedpath.addSegment(path.getSegment(1));
                                //firstsegment muss erhalten bleiben.
                                startpos = 0;
                            }
                        }
                    }
                }
            } else {
                // kein "from". Ich lege die erste Edge provisorisch als erstes Segment an. Das wird dann unten beim Smoothing wieder entfernt.
                smoothedpath.addSegment(path.getSegment(0));
                startpos = 1;
            }
        }
        // Smoothing. Bypass ist schon gelaufen.
        // Das erste Segment wurde schon behandelt, darum beim zweiten weitermachen
        // zu kurze Segmente werden verworfen.

        for (int i = startpos; i < path.getSegmentCount(); i++) {
            GraphPathSegment segment = path.getSegment(i);
            GraphPathSegment lastsegment = smoothedpath.getLast();
            //short segments werden nicht mehr geskipped, sondern jetzt bypass
            if (true || segment.edge.getLength() >= graphPathConstraintProvider.getMinimumLength()) {

                if (smoothpath) {
                    // ich gehe davon aus, am Anfang des letzten Segemtns zu stehen.Vielleicht muss es dann noch ersetzt werden.
                    GraphPosition lastposition = GraphPosition.buildPositionAtNode(lastsegment.edge, lastsegment.enternode, true);
                    GraphTransition transition = createTransition(graph, lastposition, segment.edge, segment.getLeaveNode(), graphPathConstraintProvider, layer);
                    // }
                    if (transition == null) {
                        // dann halt ohne transition
                        smoothedpath.addSegment(segment);
                    } else {
                        //TODO ueber laenge pruefen, dass die Transisiotn nicht entartet ist. 13.3.19: Oder sonstwie. Bei der Transition kann immer Murks rauskommen
                        // zB EDDK am "B2->Home Knick" mit multilane.
                        //Avoid inconsitsnet GraphPAths, eg EDDK at "B2->Home" with multilane.
                        //13.3.19 replaceLast macht jetzt erstmal auch eine Validierung
                        if (!smoothedpath.replaceLast(transition)) {
                            // dann Segment ohne Smoothing uebernehmen.
                            logger.error("inconsistent transition found and ignored");
                            smoothedpath.addSegment(segment);
                        }
                    }
                } else {
                    // Segment ohne Smoothing uebernehmen
                    smoothedpath.addSegment(segment);
                }

                //smoothedpath.addSegment(segment);
                //lastsegment=segment;
            } else {
                if (graphutilsdebuglog) {
                    logger.debug("skipping short segment " + segment.edge);
                }
            }
        }
        if (graphutilsdebuglog) {
            logger.debug("smoothed path: " + smoothedpath);
        }
        //13.3.19: Hier gibt es wohl schon mal Inkonsistenzen
        String msg = smoothedpath.validate();
        if (msg != null) {
            logger.warn("returning inconsistent path: "+msg);
        }
        return smoothedpath;
    }

    /**
     * bypass too short edges.
     * 28.7.17: ich lass mal den bypass von layer!=0, weil ich davon ausgehe, dass solche Segmente bewusst eingebaut sind.
     *
     * @return
     */
    public static GraphPath bypassShorties(Graph graph, GraphPath path, double minimumlen, int layer) {
        GraphPath np = new GraphPath(path.layer);
        GraphPathSegment lastsegment = null;
        for (int i = 0; i < path.getSegmentCount(); i++) {
            GraphPathSegment segment = path.getSegment(i);
            // das erste Segment bleibt immer erhalten. Warum eigentlich? Vielleicht damit ich headingkonform aus einer
            // parkpos fahren kann. Dann scheitern aber Tests (123->C_4). Mal anders versuchen. Ja, dann scheitert PAthFromC4.
            //Also doch besser erstes erhalten? Ach nee, das headingkonforma ausfahren wäre nur für aircraft, und die machen eh pushback, der anders läuft.
            // Aber fuer das Erreichen der ersten Node wurde schon eine Wende konstruiert, die im Pfad ist. Und das heisst, das erste Segment muss bleiben.
            // Aber was ist, wenn das zweite auch das vorletzte ist? Bei so kurzen Pfaden geht es eigentlich gar nicht, denn dass vorletzte muesste back gebypassed
            // werden, um headingkonform einzufahren. * Quatsch, der turn wird doch nach dem bypass erstellt.
            // Das headin gkonforme einfahren ist doch wichtioger als das ausfahren (wegen pushback und turn).
            if (segment.edge.getLayer() == 0 && segment.edge.getLength() < minimumlen && /*i > 0 &&*/ i < path.getSegmentCount() - 1 /*&& path.getSegmentCount()>3*/) {
                if (i == 0) {
                    // bypass nach vorne und naechste Segment uebergehen
                    if (graphutilsdebuglog) {
                        logger.debug("bypass ahead. segment " + i);
                    }
                    GraphPathSegment nextsegment = path.getSegment(i + 1);
                    GraphEdge bypass = graph.connectNodes(path.getSegment(0).enternode, nextsegment.getLeaveNode(), "bypass", layer);
                    lastsegment = new GraphPathSegment(bypass, path.getSegment(0).enternode);
                    np.addSegment(lastsegment);
                    i++;
                } else {
                    // von hinten
                    if (graphutilsdebuglog) {
                        logger.debug("bypass back segment " + i);
                    }
                    GraphEdge bypass = graph.connectNodes(lastsegment.enternode, segment.getLeaveNode(), "bypass", layer);
                    lastsegment = new GraphPathSegment(bypass, lastsegment.enternode);
                    np.replaceLast(new GraphTransition(lastsegment));
                }
            } else {
                //uebernehmen
                if (graphutilsdebuglog) {
                    logger.debug("no bypass segment " + i);
                }
                if (lastsegment != null) {
                    // GraphTransition transition = createTransition()
                }
                np.addSegment(segment);
                lastsegment = segment;
            }
        }
        return np;
    }

    /**
     * 2D, nur z=0!
     *
     * @param e1
     * @param e2
     * @return
     */
    public static Vector3 getIntersection(GraphEdge e1, GraphEdge e2) {
        Vector2 line1start = new Vector2(e1.getFrom().getLocation().getX(), e1.getFrom().getLocation().getY());
        Vector2 line1end = new Vector2(e1.getTo().getLocation().getX(), e1.getTo().getLocation().getY());
        Vector2 line2start = new Vector2(e2.getFrom().getLocation().getX(), e2.getFrom().getLocation().getY());
        Vector2 line2end = new Vector2(e2.getTo().getLocation().getX(), e2.getTo().getLocation().getY());
        Vector2 intersection = MathUtil2.getLineIntersection(line1start, line1end, line2start, line2end);
        if (intersection == null) {
            return null;
        }
        return new Vector3(intersection.getX(), intersection.getY(), 0);
    }

    /**
     * create a smooth transition from edge fromedge to some other edge (which might be identical to "from") by adding one or two arcs and maybe some helper edges.
     * Variants:
     * a) add branch on current edge if the current position provides enough space.
     * b) if a isn't possible, extend the current edge at the next node and add a turn loop.
     * <p>
     * only 2D, z=0.
     * Wiki, Skizze 29d/29e
     * Die Transition geht davon aus, dass das from Segment mit dem ersten der Transition ersetzt wird.
     * 18.7.17: Ob dieser allgemeingültige Ansatz wirklich tauglich ist? Die Gefahr von Atrefakten ist sehr gross.
     * 14.4.18: MA25: Das ist wirklich sehr abstrakt und vage. Zur Vereinfachung setze ich erstmal voraus, dass die Edges connected sind.
     * 17.4.18: Andererseits ist die Logik zum Teil aber fertig, und laesst sih nicht eihfach durch einfürgen einer Node ändern, denn dann passt die
     * neue Node nicht zu "from" Position.
     *
     * @return
     */
    public static GraphTransition createTransition(Graph graph, GraphPosition from, GraphEdge destinationedge, GraphNode destinationnode, GraphPathConstraintProvider graphPathConstraintProvider, int layer) {
        GraphNode connectingnode = null;
        if (from.currentedge.getCenter() != null) {
            logger.warn("not yet from arcs");
            return null;
        }
        GraphNode nextnode = from.getNodeInDirectionOfOrientation();
        if (graphutilsdebuglog) {
            logger.debug("createTransition from position " + from + " heading " + nextnode + " to " + destinationnode + " on " + destinationedge);
        }
        /* GraphNode intersectionnode = null;
        if (destinationedge.getFrom().equals(nextnode)) {
            intersectionnode = nextnode;
        }
        if (destinationedge.getTo().equals(nextnode)) {
            intersectionnode = nextnode;
        }
        if (intersectionnode != null) {
            // edges are connected
            //if (positionRequiresExtension)
            //GraphEdge arc = addAlternateRouteByArc( graph, from.currentedge.getOppositeNode(nextnode), from.currentedge, nextnode, destinationedge,  destinationedge.getOppositeNode(nextnode), smoothingradius,  layer);
        }*/
        if (from.currentedge == destinationedge) {
            // need to turn back to my current edge. Add teardrop for turning at the end of the current edge. 
            TurnExtension turn = addTearDropTurn(graph, nextnode, from.currentedge, true, graphPathConstraintProvider.getSmoothingRadius(nextnode), layer, false);
            if (turn == null) {
                return null;
            }
            GraphTransition gt = new GraphTransition();
            gt.add(new GraphPathSegment(turn.arc, nextnode));
            gt.add(new GraphPathSegment(turn.branch, turn.arc.getOppositeNode(nextnode)));
            return gt;
        }
        boolean destinationisconnected = false;
        if (nextnode == destinationedge.getOppositeNode(destinationnode)) {
            destinationisconnected = true;
            connectingnode = nextnode;
        }
        /*17.4.18 doch nicht das wahre. if (!destinationisconnected) {
            //MA25
            //Dann erfolgt eine temporaere Verbindung
            Vector3 intersection = getIntersection(from.currentedge, destinationedge);

            // wenn keine intersection gefunden wurde, kann da was faul sein.
            if (intersection == null) {
                logger.warn("no intersection");
                return null;
            }

            GraphNode intersectionnode = extend(graph, destinationnode, intersection, "intersection", "c0", layer).getTo();
            graph.connectNodes(nextnode, intersectionnode, "c1", layer);
            // So, jetzt sind sie verbunden
            destinationisconnected = true;
        }*/
        // intersection ist für die parallelbestimmung nicht geeignet (Rundungsfehler?).
        boolean isparallel = false;
        double angle = Vector3.getAngleBetween(from.currentedge.getDirection(), destinationedge.getDirection());
        if (angle < 0.0001f || angle > MathUtil2.PI - 0.0001f) {
            isparallel = true;
        }
        if (isparallel/*intersection == null*/) {
            // parallel destination edge. Depending on ahead od behind: s-turn; if edge disnace > ?? simple turnloop otherwise teardrop
            if (nextnode == destinationnode || destinationisconnected) {
                // auf der anderen Seit gehts weiter. Koennte ein Turnloop Ausgang sein. Das bleibt dann so.
                // obwohl eine inner arc Berechnung auch nur zu einem ganz kleinen Bogen führen muesste. Aber vielleicht zu klein mit Artefakten.
                return null;
            }

            //TODO nicht fertig
            TurnExtension turn = addTearDropTurn(graph, nextnode, from.currentedge, true, graphPathConstraintProvider.getSmoothingRadius(nextnode), layer, false);
            GraphTransition gt = new GraphTransition();
            gt.add(new GraphPathSegment(turn.arc, nextnode));
            gt.add(new GraphPathSegment(turn.branch, turn.arc.getOppositeNode(nextnode)));
            return gt;
        }
        Vector3 intersection;
        if (destinationisconnected) {
            // macht einiges einfacher, auch z.B. wegen Rundungsproblemen bei intersection.
            intersection = nextnode.getLocation();
        } else {
            intersection = getIntersection(from.currentedge, destinationedge);

            // wenn keine intersection gefunden wurde, kann da was faul sein.
            if (intersection == null) {
                logger.warn("no intersection");
                return null;
            }
        }
        GraphTransition gt1 = buildInnerArcOrTurnloopTransition(graph, from, nextnode, connectingnode, intersection, destinationedge, destinationnode, graphPathConstraintProvider, layer);
        if (gt1 != null) {
            return gt1;
        }
        if (graphutilsdebuglog) {
            logger.warn("created no transition");
        }
        return null;
    }

    /**
     * ausgelagert von oben, um universeller nutzbar zu sein.
     * 14.4.18: Iterativ kleineren Radius versuchen, bevor turnloop angelegt wird.
     * 16.2.2018
     */
    private static GraphTransition buildInnerArcOrTurnloopTransition(Graph graph, GraphPosition from, GraphNode nextnode, GraphNode connectingnode, Vector3 intersection, GraphEdge destinationedge, GraphNode destinationnode, GraphPathConstraintProvider graphPathConstraintProvider, int layer) {
        boolean destinationisconnected = connectingnode != null;
        double relpos = 0;
        double smoothingradius = graphPathConstraintProvider.getSmoothingRadius(nextnode);
        for (int i = 0; i < 4; i++) {
            GraphNode start = from.currentedge.getOppositeNode(nextnode);
            GraphEdge e1 = from.currentedge;
            GraphArcParameter arcpara;
            if (destinationisconnected) {
                // 11.2.18: geht auch als arc in 3D
                arcpara = calcArcParameterAtConnectedEdges(e1, destinationedge, smoothingradius, true, false);
                if (arcpara == null) {
                    //already logged
                    return null;
                }
                arcpara.arc.origin = connectingnode;
            } else {
                arcpara = calcArcParameter(start, e1, intersection, destinationedge, destinationnode, smoothingradius, true, false);
                if (arcpara == null) {
                    //already logged
                    return null;
                }
            }
            relpos = compareEdgePosition(from, arcpara.arcbeginloc);
            if (graphutilsdebuglog) {
                logger.debug("relpos=" + relpos);
            }
            if (relpos > 0) {
                // arc ahead of current position. inner arc can be used.
                GraphEdge arc = addArcToAngle(graph, start, e1, intersection, destinationedge, destinationnode, arcpara, layer, false);
                if (arc == null) {
                    logger.warn("createTransition: inner arc failed. Too large?");
                    return null;
                }
                GraphTransition gt = new GraphTransition();
                //TODO oder arc.to? oder from?
                gt.add(new GraphPathSegment(graph.connectNodes(start, arc.from, "smoothbegin." + nextnode.getName(), layer), start));
                gt.add(new GraphPathSegment(arc, arc.from/*27.4.18destinationedge.getOppositeNode(destinationnode))*/));
                gt.add(new GraphPathSegment(graph.connectNodes(arc.to, destinationnode, "smoothend." + nextnode.getName(), layer), arc.getTo()));
                if (graphutilsdebuglog) {
                    logger.debug("created inner arc with " + gt.seg.size() + " transition edges");
                }
                return gt;
            } /*else*/
            smoothingradius *= 0.8f;
        }
        if (relpos < 0) {
            //behind. Turnloop transition at the end of current edge. Logging because this might be ugly?
            logger.warn("building turn loop transition");
            double len = 5;
            //??arcpara = calcArcParameter( start, e1, intersection, destinationedge, destinationnode, len, false, true);
            //??return createTurnLoop(graph,nextnode,e1,intersection,destinationedge,destinationnode,arcpara,layer);
            TurnExtension turnloop = addTurnLoop(graph, nextnode, from.currentedge, destinationedge, layer);
            GraphTransition gt = new GraphTransition();
            gt.add(new GraphPathSegment(turnloop.edge, nextnode));
            gt.add(new GraphPathSegment(turnloop.arc, turnloop.edge.getOppositeNode(nextnode)));
            gt.add(new GraphPathSegment(turnloop.branch, turnloop.edge.getOppositeNode(nextnode)));
            if (graphutilsdebuglog) {
                logger.debug("created turnloop");
            }
            return gt;
        }
        // no transition possible. Probably never reached here.
        return null;
    }

    /**
     * isType "v" logically ahead (>0), on (0), or behind (<0) the current position?
     * v must be on the same line as the edge!
     */
    public static double compareEdgePosition(GraphPosition position, Vector3 v) {
        if (position.currentedge.getCenter() != null) {
            logger.warn("not yet from arcs");
            return 0;
        }
        Vector3 location = position.get3DPosition();
        Vector3 diff = v.subtract(location);
        double difflen = diff.length();
        // value fitting for groundnet only?
        if (difflen < 0.01f) {
            return 0;
        }
        Vector3 effdir = position.currentedge.getEffectiveInboundDirection(position.getNodeInDirectionOfOrientation());
        double angle = Vector3.getAngleBetween(effdir, diff);
        if (angle < MathUtil2.PI_2) {
            return difflen;
        }
        return -difflen;
    }


    /**
     * The getFirst segment will be the "back" segment for moving back. No, successor?
     * Die Position sollte auf der startnode sein, sonst gibt es Sprünge durch den Wechsel der
     * edge.
     * Der reversegear muss abhängig von der aktuellen Position eingetragen werden. Ach was, die Methode ist ja speziell zum zurücksetzen. D.h.
     * nur das erste Segment mir reversegear.
     * Skizze 32
     *
     * @return
     */
    public static GraphPath createBackPathFromGraphPosition(Graph graph, TurnExtension turn, GraphNode to, GraphWeightProvider graphWeightProvider,
                                                            GraphPathConstraintProvider graphPathConstraintProvider, int layer, boolean smoothpath, boolean allowrelocation, GraphLane lane) {
        //TurnExtension turn = createBack(graph, startnode, startedge, successor, layer);
        if (allowrelocation) {
            //aeusserst fraglich.
            logger.warn("allowrelocation valid?");
        }
        GraphPath path = createPathFromGraphPosition(graph, new GraphPosition(turn.edge, turn.edge.getLength(), true), to, graphWeightProvider, graphPathConstraintProvider, layer, smoothpath, allowrelocation, lane);
        if (path == null) {
            return null;
        }
        // Der Pfad ist ab successor. Die turn edge noch davorhanegen. Und da einen orientation definieren.
        GraphPathSegment s = new GraphPathSegment(turn.edge, turn.edge.to);
        s.changeorientation = true;
        path.backward = true;
        path.insertSegment(0, s);
        //path.start = turn.edge.to;
        // from des arc ist die Startnode, aber  reverse und ich fahre rückwärts.
        path.startposition = new GraphPosition(turn.arc, turn.arc.getLength(), true);
        return path;
    }

    /**
     * Einen Outline Path zu einem Path erstellen.
     * Der erste outline wird Start des neuen Path.
     * Der "path" sollte nicht gesmoothed sein. Das wird sonst zu unuebersichtlich.
     * Outline wird zunächst mal nur an layer 0 gemacht.
     * Fuer die letzte Edge wird aber kein Outline verwendet, damit der Pfad nicht im Nirgendwo endet,
     * sondern in Deckung mit einer regulaeren Edge. Man koennte alternativ auch einen UTurn anhaengen.
     * Am Beginn optional auch. Sonst muss es vielleicht einen turnloop auf eine outline geben. Das geht nicht.
     * 15.5.18: Das ist aber verwirrend, auch ausserhalb von Layer 0 outline Points zu bilden, die auf dem Graph liegen.
     * Vielleicht dann doch besser die Originalpunkte verwenden. TODO
     */
    private static GraphPath createOutlinePath(Graph graph, GraphPath path, GraphLane graphlane, int layer, boolean beginwithoutline) {
        double offset = graphlane.offset;

        List<Vector3> outline = graph.orientation.getOutline(path.path, offset, 0);
        GraphNode from;
        if (beginwithoutline) {
            from = graph.addNode("outline0", outline.get(0));
        } else {
            from = path.getSegment(0).getEnterNode();
        }

        //GraphNode from = path.start;
        GraphEdge e;
        GraphPath newpath = new GraphPath(layer);
        for (int i = 1; i < outline.size() - 2; i++) {
            GraphNode destnode = graph.addNode("outline" + i, outline.get(i));
            destnode.parent = path.getSegment(i).getEnterNode();
            //Edge Namen kann man sich eigentlich sparen, denn die fallen beim Smoothing ja wieder weg.
            //fuer Tests aber ganz hilfreich. Gibt es da nicht eine Defaultmethode fuer die Namenbildung? Das ist aber eh ungeeignet.
            e = graph.connectNodes(from, destnode, "toOutline" + i, layer);
            newpath.addSegment(new GraphPathSegment(e, from));
            from = destnode;
        }
        // zureuck auf die letzte regulaere Edge
        GraphNode reenternode = path.getLast().getEnterNode();
        //Edge Namen kann man sich sparen, denn die fallen beim Smoothing ja wieder weg.
        e = graph.connectNodes(from, reenternode, "reenter", layer);
        newpath.addSegment(new GraphPathSegment(e, from));
        e = graph.connectNodes(reenternode, path.getLast().getLeaveNode(), "last", layer);
        newpath.addSegment(new GraphPathSegment(e, reenternode));
        //Die finalposition wird vom Aufrufer eingetragen
        if (graphutilsdebuglog) {
            logger.debug("outline path created:" + newpath);
        }
        return newpath;
    }
}
