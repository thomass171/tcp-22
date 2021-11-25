package de.yard.threed.graph;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Zunächst mal rein abstrakt, denn die exakte 3D Position ist von der Darstellung abhängig. 7.12.16: Jetzt nicht mehr,
 * weil eine Kante schon ueber ihre Darstellung Bescheid wissen muss.
 * <p>
 * Created by thomass on 24.11.16.
 */
public class GraphPosition {
    private Log logger = Platform.getInstance().getLog(GraphPosition.class);

    //GraphNode currentnode=null;
    public GraphEdge currentedge = null;
    // in Prozent von from entfernt. 7.12.16: Aber warum Prozent? Das erfordert doch nur zusätzliche Berechnung. Eine Entkopplung von der
    // Frage der Darstellung einer Kante ist wenn überhaupt eh nur aufwändig möglich. Auch für eine Neuberechnung des Prozentwertes nach
    // einer Bewegung muss man die absoluten Werte kennen. Sonst variiert die Geschwindigkeit abhängig von der Kantenlänge.
    // Also, es ist die absolute Position auf dem Graph von "from" aus.  Bei reverse von "to" aus. 18.7.17: wirklich? Ja, wirklich!
    public double edgeposition = 0;
    // 6.12.16: Die Orientierung, auch wenn das steng genommen kein Attribut der Position ist. boolean, weil es nur zwei Zustände gibt.
    // Die Wertefestlegung ist rein zufällig.
    public boolean reverseorientation = false;
    public boolean reversegear = false;
    // 26.4.17:Eigentlich steh tder upvector bei zebene Graphen ja schon fest.
    //17.5.17 public Vector3 upVector;

    /**
     * Position unmittelbar auf der from node mit Defaultorientierung (nicht reverse).
     *
     * @param edge
     */
    public GraphPosition(GraphEdge edge) {
        this(edge, 0);
    }

    /**
     * Position somewhere on the edge with distance from "from".
     *
     * @param edge
     */
    public GraphPosition(GraphEdge edge, double position) {
        this(edge,position,false);
    }

    public GraphPosition(GraphEdge edge, double position, boolean reverseorientation) {
        if (edge==null){
            logger.warn("no edge");
        }
        currentedge = edge;
        edgeposition = position;
        this.reverseorientation = reverseorientation;
    }

    /**
     * Mapping von einer abstrakten Graph Position zu einer echten 3D Position.
     *
     * @return
     */
    public Vector3 get3DPosition() {
        if (reverseorientation) {
            return currentedge.get3DPosition(currentedge.getLength() - edgeposition);
        }
        return currentedge.get3DPosition(edgeposition);
    }

    

    public Vector3 getDirection() {
        Vector3 dir = currentedge.getEffectiveDirection(getAbsolutePosition());
        if (reverseorientation) {
            return dir.negate();
        }
        return dir;
    }
    
    /*public void setUpVector(Vector3 upVector) {
        this.upVector = upVector;
    }*/

    public boolean isReverseOrientation() {
        return reverseorientation;
    }

    /**
     * Die Position immer von from aus.
     * @return
     */
    public double getAbsolutePosition() {
        double absoluteedgeposition = edgeposition;
        if (reverseorientation) {
            absoluteedgeposition = currentedge.getLength() - edgeposition;
        }
        return absoluteedgeposition;
    }

    public GraphNode getNodeInDirectionOfOrientation() {
        if (reverseorientation) {
            return currentedge.getFrom();
        }
        return currentedge.getTo();
    }

    /**
     * Die Position in die Edge hinein oder hinaus.
     *
     * @param edge
     * @param intoedge ist die edge die edge in die node rein?
     * @return
     */
    public static GraphPosition buildPositionAtNode(GraphEdge edge, GraphNode node, boolean intoedge) {
        if (edge.from == node) {
            if (intoedge) {
                return new GraphPosition(edge);
            } else {
                return new GraphPosition(edge, edge.getLength(), true);
            }
        }
        if (intoedge) {
            return new GraphPosition(edge, 0, true);
        } else {
            return new GraphPosition(edge, edge.getLength(), false);
        }
    }

    @Override
    public String toString() {
        return currentedge + "@" + ((reverseorientation) ? "-" : "") + edgeposition;
    }

    public GraphEdge getEdge(){
        return currentedge;
    }
}
