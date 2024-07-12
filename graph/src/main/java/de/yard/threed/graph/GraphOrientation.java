package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Ich weiss erstmal keinen besseren Namen.
 * 22.2.2020:Es ist ein konzeptionelles Problem, dass der Graph dafür sorgen soll, dass Vehicle richtig stehen. Dann gehen nested Graphs(Orbit) nicht.
 * Es müsste aber doch gehen, dass ein EDDK Vehicle von einer Kugel umrundet wird.
 * Ich fürchte, diese Klasse ist krude.
 * 4.7.24: Still doubt this class has a valid concept? After extracting 3Drotation() appears ok now for outline.
 * <p>
 * Created on 16.03.18.
 */
public abstract class GraphOrientation {
    Log logger = Platform.getInstance().getLog(GraphOrientation.class);

    public GraphOrientation() {
    }

    public static GraphOrientation buildForZ0() {
        return new GraphOrientationZ0();
    }

    public static GraphOrientation buildForY0() {
        return new GraphOrientationY0()/*Default()*/;
    }

    public static GraphOrientation buildDefault() {
        return new GraphOrientationY0()/*Default()*/;
    }

    public static GraphOrientation buildForFG() {
        return new GraphOrientationFG();
    }

    /**
     * 29.3.18 Der upVector kann/muss abhaengig von der Edge sein.
     *
     * @return
     */
    public abstract Vector3 getUpVector(GraphEdge edge);

    public abstract String getName();

    /**
     * Eine Outline entlang eines Graphen erzeugen.
     * Liefert die Liste der NodePunkte ab hier bei offset=0, sonst eine etwas versetzte Linie links (offset negativ) oder rechts (offset positiv) davon.
     * Das duerfte aber relativ sein.
     * Geht (erstmal) nicht mit Verzweigungen und nur in 2D. In 3D bräuchte man einen upVector? Es wird von y=0 ausgegangen.
     * Geht auch noch nicht mit Bögen.
     * Berechnungen sind aber in 3D.
     * Wenn es keine edge gibt, wird ueberhaupt kein Punkt geliefert, weil keine Orinetierung berechnet werden kann.
     * Es kann bei zu grossem offset bzw zu kleinem nodeabstand oder zu starken Kruemmungen die Linepunkte "die falsche Reihenfolge" haben.
     * 7.2.18: Wenn auf einen Abzweiger (Knoten mit mehr als einem Nachfolger) getroffen wird, wird abgebrochen. Das scheint schlüssig.
     * 9.2.18: Jetzt auch für einfache Bögen.
     * 16.3.18: Aus GraphNode hierhin verschoben, um den Graph von der Orientierung zu entkoppeln.
     * 10.4.18: Andere Signatur: Jetzt kommt hier ein Path bzw. der Kern davon rein. Das macht es vielleicht etwas eingaengiger. Mit layer laesst sich auf ein LAyer beschraenken (-1 fuer alle).
     * 30.8.18: Flexibler, z.B. über interface (GraphPathSegment->LineSegment) geht das nur schwer, weil arcs auch behandelt werden, zumindest rusimentär.
     * 04.04.19: Es gibt jetzt auch eine 2D Variante (OutlineBuilder)
     *
     * @param offset
     * @return
     */
    public List<Vector3> getOutline(List<GraphPathSegment> path, double offset, int layer) {
        GraphNode lastnode;
        GraphEdge edge;
        if (path != null && path.size() == 1 && path.get(0).edge.getArc() != null) {
            GraphPathSegment arcsegment = path.get(0);
            edge = arcsegment.edge;
            lastnode = arcsegment.getLeaveNode();
            //9.2.18: Reichlich Sonderlocke fuer einfachen arc. Geht nur mit neuer Definition.
            if (lastnode != null && edge.getArc() != null) {
                return buildArcOutline(edge, offset, 16, arcsegment.getEnterNode().equals(edge.getTo()));
            }
        }

        List<Vector3> line = new ArrayList<Vector3>();
        if (path.size() == 0) {
            return line;
        }

        GraphNode startnode;
        int idx = 0;

        edge = path.get(0).edge;
        idx++;
        startnode = path.get(0).enternode;

        // erster Punkt
        Quaternion rotation;
        Vector3 dir = edge.getEffectiveOutboundDirection(startnode);

        double offsettouse = getOffsetToUse(edge, offset, layer);
        Vector3 outpoint = getEndOutlinePoint(startnode, edge, dir, offsettouse);
        line.add(outpoint);


        while (edge != null) {
            //7.2.18: Nicht immer auf to gehen.
            GraphNode nextnode = edge.getOppositeNode(startnode);
            // TODO: besserer Notaus
            if (nextnode == null/*basenode*/) {
                //circle?
                break;
            }
            if (path != null && idx > path.size()) {
                break;
            }
            // Winkel aus letztem dir und aktuellem

            Vector3 nextdir = null;
            GraphEdge nextedge = null;
            if (idx < path.size()) {
                nextedge = path.get(idx).edge;
                idx++;
            }
            if (nextedge != null) {
                //13.4.18: nextdir ist doch voellig falsch!
                //nextdir = edge.getEffectiveBeginDirection();
                nextdir = nextedge.getEffectiveOutboundDirection(nextnode);
                Degree angle = Degree.buildFromRadians((float) Vector3.getAngleBetween(dir, nextdir) / 2);
                Vector3 kp = null;
                if (angle.getDegree() > 0.05f) {
                    kp = Vector3.getCrossProduct(dir, nextdir).normalize();
                }
                //angle = new Degree(angle.getDegree() - 90);

                if (MathUtil.mathvalidate) {
                    if (!nextdir.isValid()) {
                        throw new RuntimeException("NaN in nextdir");
                    }
                    if (!dir.isValid()) {
                        throw new RuntimeException("NaN in dir");
                    }
                    if (Float.isNaN((float) angle.getDegree())) {
                        throw new RuntimeException("NaN in angle.degree");
                    }
                }

                // Wenn es eine Layereinschraenkung gibt, echten outline nur erstellen, wenn beide Edges zu dem Layer gehören.
                offsettouse = offset;
                if (layer != -1 && (edge.getLayer() != layer || nextedge.getLayer() != layer)) {
                    offsettouse = 0;
                }
                //11.4.18: Um den Upvector der GraphRotation rotieren. Oder besser das Kreuzprodukt wegen Winkel > 180.
                //Ausser bei kleinen Winkeln. Dann doch per up. 
                Vector3 offsetv;
                if (kp == null) {
                    //dir/nextdir parallel
                    rotation = Quaternion.buildQuaternionFromAngleAxis(angle, this.getUpVector(edge));
                    offsetv = Vector3.getCrossProduct(dir, this.getUpVector(edge));
                } else {
                    rotation = Quaternion.buildQuaternionFromAngleAxis(angle, kp);
                    //offsetv = Vector3.getCrossProduct(dir, kp);
                    offsetv = Vector3.getCrossProduct(dir, this.getUpVector(edge));
                }
                offsetv = offsetv.normalize().multiply(offsettouse);
                Vector3 outlinepoint = nextnode.getLocation().add(offsetv.rotate(rotation));
                if (MathUtil.mathvalidate) {
                    if (!outlinepoint.isValid()) {
                        throw new RuntimeException("NaN in outlinepoint");
                    }
                }
                line.add(outlinepoint);
                //logger.debug("outline at " + nextnode.getLocation() + " isType " + outlinepoint + " with angle " + angle);
            } else {
                // letzter Punkt
                dir = edge.getEffectiveInboundDirection(nextnode);
                offsettouse = getOffsetToUse(edge, offset, layer);
                outpoint = getEndOutlinePoint(nextnode, edge, dir, offsettouse);
                line.add(outpoint);
            }
            // naechsten Schritt vorbereiten.
            dir = nextdir;
            edge = nextedge;
            startnode = nextnode;
        }

        if (MathUtil.mathvalidate) {
            //validieren
            for (int i = 0; i < line.size(); i++) {
                Vector3 v = line.get(i);
                if (!v.isValid()) {
                    throw new RuntimeException("NaN in line.i=" + i);
                }
            }
        }
        return line;
    }

    private double getOffsetToUse(GraphEdge edge, double offset, int layer) {
        double offsettouse = offset;
        if (layer != -1 && edge.getLayer() != layer) {
            offsettouse = 0;
        }
        return offsettouse;
    }

    /**
     * offset positiv->rechts, zumindest in y0 Ebene.
     */
    public List<Vector3> getOutlineFromNode(GraphNode basenode, float offset) {
        GraphNode startnode = basenode;
        GraphEdge edge = startnode.getFirstFromEdge();
        List<GraphPathSegment> path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(edge, startnode));
        while (edge != null) {

            //7.2.18: Nicht immer auf to gehen.
            GraphNode nextnode = edge.getOppositeNode(startnode);
            // TODO: besserer Notaus
            if (nextnode == basenode) {
                //circle?
                break;
            }
            GraphEdge nextedge = null;
            List<GraphEdge> nextedges = nextnode.getEdgesExcept(edge);
            if (nextedges.size() == 1) {
                //nur wenn es genau eine gibt
                nextedge = nextedges.get(0);
                path.add(new GraphPathSegment(nextedge, nextnode));
            } else {
                // letzter Punkt
            }
            // naechsten Schritt vorbereiten.
            edge = nextedge;
            startnode = nextnode;
        }
        return getOutline(path, offset, -1);
    }

    /**
     * public fuer Test
     */
    public Vector3 getEndOutlinePoint(GraphNode node, GraphEdge edge, Vector3 dir, double offset) {
        //16.3.18: Edge in Referenceorientierung anlegen und dann "einfach" rotieren.
        Vector3 outv = new Vector3(offset, 0, 0);
        boolean reverseorientation = false;
        Quaternion rotation = DefaultEdgeBasedRotationProvider.get3DRotation(reverseorientation, dir, this.getUpVector(edge));
        return node.getLocation().add(outv.rotate(rotation));
    }

    List<Vector3> buildArcOutline(GraphEdge edge, double offset, int segments, boolean reverse) {
        GraphArc p = edge.getArc();
        List<Vector3> line = new ArrayList<Vector3>();

        Vector3 center = edge.getCenter();
        float step = 1f / segments;

        int index;
        for (int i = 0; i <= segments; i++) {
            index = i;
            if (reverse) {
                index = segments - i;
            }
            Vector3 v = p.getRotatedEx(index * step, 0);
            v = v.normalize().multiply(p.getRadius() - (reverse ? -1 : 1) * offset);
            v = center.add(v);
            line.add(v);
        }
        return line;
    }

    public static GraphOrientation buildByName(String orientation) {
        if (orientation.equals(GraphOrientationZ0.NAME)) {
            return buildForZ0();
        }
        if (orientation.equals(GraphOrientationY0.NAME)) {
            return buildForY0();
        }
        if (orientation.equals(GraphOrientationFG.NAME)) {
            return buildForFG();
        }
        throw new RuntimeException("unknown orientation " + orientation);
    }
}

/**
 * upVector ist dann (0,0,1)
 */
class GraphOrientationZ0 extends GraphOrientation {

    public static String NAME = "z0";

    @Override
    public Vector3 getUpVector(GraphEdge edge) {
        Vector3 up = new Vector3(0, 0, 1);
        //up.rotate(getBaseRotation());
        return up;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

/**
 * In der XZ Ebene.
 * upVector ist dann (0,1,0)
 */
class GraphOrientationY0/*Default*/ extends GraphOrientation {

    public static String NAME = "y0";

    @Override
    public Vector3 getUpVector(GraphEdge edge) {
        return new Vector3(0, 1, 0);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

/**
 * 22.2.2020: Das ist doch eigentlich ein universeller 3D Graph? Nee, der bezieht seinen up-Vektor von einem Mittelpunkt.
 */
class GraphOrientationFG extends GraphOrientation {

    public static String NAME = "fg";

    /**
     * kann man das staendige Rechnen nicht optimieren? TODO
     *
     * @return
     */
    @Override
    public Vector3 getUpVector(GraphEdge edge) {
        Vector3 up = edge.from.getLocation()./*negate().*/normalize();
        Quaternion baserotation = new Quaternion();
        return up.rotate(baserotation);
    }

    @Override
    public String getName() {
        return NAME;
    }
}