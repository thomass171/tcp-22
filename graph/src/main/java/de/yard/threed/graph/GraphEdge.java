package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Auch wenn das abstrakt eine Kante ist, muss hier doch eine Info über die Ausgestaltung vorliegen. Es kann auch ein Bogen oder eine Kurve sein.
 * 13.12.16: Man koennte die Info vielleicht in ein Interface oder eine customdata Klasse auslagern; oder eine Propertylist.
 * Dann wäre es doch entkoppelt. Andererseits muss GraphPosition schon genau die Länge wissen.
 * 07.02.2018: Zur Visualisierung/3D Movement muss jede Edge einen eigenen up-Vector haben (evtl. auch zwei), wenn nicht immer alles in einer Ebene liegt.
 * <p>
 * Created by thomass on 13.09.16.
 */
public class GraphEdge {
    private Log logger = Platform.getInstance().getLog(GraphEdge.class);
    private String name = "";
    public GraphNode from;
    public GraphNode to;
    // dir ist die Differenz to-from. Nicht normalisiert.
    Vector3 dir;
    // center != null heisst Bogen
    //12.2.18 private Vector3 center;
    //12.2.18public float radius;
    //12.2.18 private Vector3 vf, vt;
    //29.3.17: rot statt angle speichern. Macht es allgemeingültiger. Aber ob slerp wirklich besser geeignet ist. Lieber mal angle, aber alle drei Winkel.
    //Nee, erstmal nicht, das ist reichlich tricky, auch wegen z.B. der Länge des Bogens. Erstmal nur Ebeneunterscheidung.
    //Der angle negativ für CW Bögen, sonst positiv.
    //17.4.18 jetzt in arc float angle;
    //8.5.18 Quaternion arcrotation;
    // Die Laenge ist im Prinzip redundant. Aber man spart schon einiges an ständigen Berechnungen, vor allem
    // wenn es keine Gerade ist.
    double len;
    static private int uniqueid = 1;
    private int id = uniqueid++;
    //29.3.17: bloeder hack
    //15.3.18 boolean iszEbene = false;
    public GraphComponent customdata;
    // Eine Edge gehört zu genau einem Layer. Soll sie doch zu mehreren gehören, muss die Edge dupliziert werden.
    private int layer;
    //16.3.18  private Vector3 upVector;
    //9.2.18: Neue 3D faehige Arc Definition
    public GraphArc arcParameter = null;

    /**
     * Constructor nur für Aufruf aus Graph
     *
     * @param from
     * @param to
     */
    public GraphEdge(GraphNode from, GraphNode to, String name, /*boolean iszEbene,*/ int layer/*, Vector3 upVector*/) {
        this.name = name;
        this.from = from;
        this.to = to;
        // this.iszEbene = iszEbene;
        this.layer = layer;
        //this.upVector = upVector;
        this.dir = to.getLocation().subtract(from.getLocation());
        len = to.getLocation().subtract(from.getLocation()).length();
        checklen();
    }

    public GraphNode getFrom() {
        return from;
    }

    public GraphNode getTo() {
        return to;
    }

    public double getLength() {
        return len;
    }

    /**
     * Ist auch halbkeisfähig, bekommt dafür aber die Normale uebergeben, weil die bei Halbkreisen nicht berechenbar ist.
     * 11.4.18: Deprecated zugunsten der naechsten setArc. Jetzt Convenience wegen from.
     */
    public void setArcAtFrom(Vector3 center, double radius, double angle, Vector3 normal) {
        Vector3 ex = from.getLocation().subtract(center);
        setArc(new GraphArc(center, radius, ex, normal, angle));
    }

    public void setArc(GraphArc arc) {
        arcParameter = arc;
        float umfang = (float) (2 * Math.PI * arc.getRadius());
        len = Math.abs((float) (umfang * arc.beta / (2 * Math.PI)));
        checklen();
    }

    private void checklen() {
        if (len < 0.000001f) {
            logger.warn(name + ": adjusting too low len to 0.000001");
            len = 0.000001f;
        }
    }

    /**
     * Mapping von einer abstrakten Graph Position zu einer echten 3D Position.
     * edgeposition ist hier schon reverseneutral. Der Aufrufer muss revers beachten.
     *
     * @return
     */
    public Vector3 get3DPosition(double edgeposition) {
        if (arcParameter == null) {
            // Gerade
            return from.getLocation().add(dir.multiply(edgeposition / len));
        }
        // Bogen. Die Rotation ist erstmal nur 2D um die y-Achse.
        // 29.3.17: Jetzt row statt angle.Nee?

        Vector3 v = arcParameter.getRotatedEx(edgeposition / len, 0);
        v = arcParameter.arccenter.add(v);
        //if (v.getY() < 0 && v.getY() > -5.12124E-7) {
        //    logger.debug("3DPosition=" + v);
        //}
        return v;
    }

    /**
     * Der Winkel, wenn es ein Bogen ist, sonst null.
     *
     * @return
     */
    public Degree getAngle() {
        if (arcParameter == null) {
            return null;
        }
        return Degree.buildFromRadians(arcParameter.beta);
    }

    public Vector3 getCenter() {
        if (arcParameter == null) {
            return null;
        }
        return arcParameter.arccenter;
    }

    /**
     * Gar nicht so einfach. Es könnte Positionen darauf geben, die danach ungültig sind.
     * Ansonsten aber ziemlich straightforward. Obwohl: Bögen! Erstmal nicht für Bögen.
     *
     * @param offset
     * @return
     */
    public GraphNode split(Graph graph, double offset) {
        if (arcParameter != null) {
            //TODO
            throw new RuntimeException("isType arc");
        }
        Vector3 splitlocation = from.getLocation().add(dir.multiply(offset / len));

        GraphNode splitnode = graph.addNode("", splitlocation);
        // no need to connect 'from' to 'splitnode', because 'this' becomes the connection. But it needs to be added.
        GraphEdge n1 = graph.connectNodes(splitnode, to);
        to.removeEdge(this);
        splitnode.addEdge(this);
        to = splitnode;
        this.dir = to.getLocation().subtract(from.getLocation());
        len = to.getLocation().subtract(from.getLocation()).length();
        return splitnode;
    }

    /**
     * Durch Duplizieren dieser Kante mit einer bestimmten Länge und etwas rotiert eine quasi
     * als Abzweigung verwendbare neu Kante erstellen. Der Aufrufer muss die dann an denselben
     * from Knoten hängen.
     *
     * @param brancharc
     * @param branchlen
     */
    public void branch(Degree brancharc, float branchlen) {

    }

    /**
     * Die Nominalrichtung der Kante. Nicht normalisiert.
     *
     * @return
     */
    public Vector3 getDirection() {
        return dir;
    }

    /**
     * Die tatsächliche 3D Richtung der Kante abhängig von der Position. Liefert normalisiert.
     * Orientierung ist immer from->to. Die Orientierung einer GraphPosition spielt hier keine Rolle.
     * 19.4.17: Aber reverse muss beachtet werden, weil position dann vom anderen Ende zählt. Macht Aufrufer.
     * 15.3.18: Liefert Direction in Worldspace mit baseprojection? Kann man "world space" ueberhaupt sagen?
     * Eigentlich ist es doch eher im Graph space, so wie "dir" bei Geraden. NeeNee, FG Graphs haben alle World Coordinates.
     * Das "space" Gerede koennte Unsinn sein.
     * Z0 Prüfung hier ist doof.
     * Und im arc soll die Rotation doch geslerpt werden. Aber das ist doch ein bischen was anderes,denn
     * das bezieht sich auf die Orientierung eines Objektes auf dem Graph. Vielleicht deprecated? Nee, warum?
     * Das hier ist einfach die Richtung der Edge im world space. Das sagt nichts ueber die Rotation eines
     * Objekts auf dem Graph aus, denn da spielt die baserotation mit rein.
     *
     * @return
     */
    public Vector3 getEffectiveDirection(double edgeposition) {

        if (arcParameter == null) {
            // Gerade. Das ist trivial.
            return dir.normalize();
        }

        Vector3 v = arcParameter.getRotatedEx(edgeposition / len, 0);
        //Tja, das mit dem angle und cross geht so, aber warum man das so machen muss? Ob das der wahre Jakob ist?
        if (arcParameter.getBeta() < 0) {
            v = Vector3.getCrossProduct(v, arcParameter.n).normalize();
        } else {
            v = Vector3.getCrossProduct(arcParameter.n, v).normalize();
        }
        return v;
        

    }

    public Vector3 getEffectiveBeginDirection() {
        return getEffectiveDirection(0);
    }

    public Vector3 getEffectiveEndDirection() {
        return getEffectiveDirection(len);
    }

    /**
     * Orientierung ist hier je nach opt NICHT immer from->to, sondern aus Sicht von "node".
     * Die Option ist irgendwie doof. 17.5.17: Deswegen deprecated
     * normalized.
     *
     * @param node
     * @return
     */
    @Deprecated
    public Vector3 getEffectiveDirectionAtNode(GraphNode node, boolean immerfroto) {
        Vector3 effectiveincomingdir;
        if (from == node) {
            effectiveincomingdir = getEffectiveBeginDirection()/*.negate()*/;
        } else {
            //TODO checken dass es ende ist.
            //29.3.17: negate, weil immer aus Sicht von "node".
            if (immerfroto) {
                effectiveincomingdir = getEffectiveEndDirection().negate();
            } else {
                effectiveincomingdir = getEffectiveEndDirection();
            }
        }
        return effectiveincomingdir;
    }

    /**
     * "node" isType not checked for validness.
     * normalized.
     *
     * @param node
     * @return
     */
    public Vector3 getEffectiveInboundDirection(GraphNode node) {
        return getEffectiveOutboundDirection(node).negate();
    }

    /**
     * "node" isType not checked for validness.
     * normalized.
     * 18.7.17: Das ist doch genau verkehrt rum. Outbound ist "abgehend", also an der Node weg von der Edge. Also ist das hier eigentlich inbound?
     * Wird aber schon ueberall falsch verwenden.
     * 26.7.17: Hmmm. Die abgehende Richtung der edge von "node". Scheint mir jetzt doch richtig.
     * 12.4.18: Ja, wenn man sagt, das ist dir Richtung DIESER edge ausgehend aus node.
     *
     * @param node
     * @return
     */
    public Vector3 getEffectiveOutboundDirection(GraphNode node) {
        Vector3 effectivedir;
        if (from == node) {
            effectivedir = getEffectiveBeginDirection();
        } else {
            effectivedir = getEffectiveEndDirection().negate();
        }
        return effectivedir;
    }

    /**
     * Laesst sich nur fuer Edges ermitteln, die an einer node zusammenkommen. Sonst gaebe es zwei Ergebnisse.
     *
     * @return
     */
    public static double getAngleBetweenEdges(GraphEdge i, GraphNode node, GraphEdge o) {
        return Vector3.getAngleBetween(i.getEffectiveInboundDirection(node), o.getEffectiveOutboundDirection(node));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Wichtig fuer remove.
     *
     * @param e
     * @return
     */
    @Override
    public boolean equals(Object e) {
        return ((GraphEdge) e).id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    /**
     * liefert die Node auf der anderen Seite von node.
     *
     * @param node
     * @return
     */
    public GraphNode getOppositeNode(GraphNode node) {
        if (node == from) {
            return to;
        }
        return from;
    }

    public int getId() {
        return id;
    }

    public int getLayer() {
        return layer;
    }

    public void removeFromNodes() {
        removeFromNode(from);
        removeFromNode(to);
    }

    public void removeFromNode(GraphNode n) {
        // //C# kann keine Iterator wie Java
        /*for (Iterator<GraphEdge> iter = n.edges.listIterator(); iter.hasNext(); ) {
            GraphEdge e = iter.next();
            if (e.equals(this)){
                iter.remove();
            }
        }*/
        for (int i = n.edges.size() - 1; i >= 0; i--) {
            GraphEdge e = n.edges.get(i);
            if (e.equals(this)) {
                n.edges.remove(i);
            }
        }
    }

    @Override
    public String toString() {
        return getName() + "(" + from.getName() + "->" + to.getName() + ")";
    }


    public boolean isArc() {
        return arcParameter != null;
    }

   /* public Vector3 getUpVector() {
        return upVector;
    }*/

    public GraphArc getArc() {
        return arcParameter;
    }

    /**
     * Return the node connecting this edge to "e".
     */
    public GraphNode getNodeToEdge(GraphEdge e) {
        if (from.findEdge(e) != null) {
            return from;
        }
        if (to.findEdge(e) != null) {
            return to;
        }
        return null;
    }

    /**
     * Wie der Aufrufer schon sagt, das ist haarig
     * brauchts nicht in FG? erst fuer Platrunde?
     */
    public void recalcForProjection() {
        if (isArc()) {
            throw new RuntimeException("so nicht");
        }
        this.dir = to.getLocation().subtract(from.getLocation());
        len = to.getLocation().subtract(from.getLocation()).length();
        checklen();
    }
}
