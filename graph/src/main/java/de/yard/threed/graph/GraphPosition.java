package de.yard.threed.graph;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Zunächst mal rein abstrakt, denn die exakte 3D Position ist von der Darstellung abhängig. 7.12.16: Jetzt nicht mehr,
 * weil eine Kante schon ueber ihre Darstellung Bescheid wissen muss.
 * <p>
 * Once (2017) had an up vector, but it turned out better not to have it.
 * <p>
 * Created by thomass on 24.11.16.
 */
public class GraphPosition {
    private Log logger = Platform.getInstance().getLog(GraphPosition.class);

    public GraphEdge currentedge = null;
    // Absolute Position on graph from "from". For reverse from "to". 18.7.17: really? Yes, really!
    // Once (2016) is was in percent.
    public double edgeposition = 0;
    // 6.12.16: Die Orientierung, auch wenn das steng genommen kein Attribut der Position ist. boolean, weil es nur zwei Zustände gibt.
    // Die Wertefestlegung ist rein zufällig.
    public boolean reverseorientation = false;
    public boolean reversegear = false;

    /**
     * Position on from node with default orientation (not reverse).
     */
    public GraphPosition(GraphEdge edge) {
        this(edge, 0);
    }

    /**
     * Position somewhere on the edge with distance from "from".
     */
    public GraphPosition(GraphEdge edge, double position) {
        this(edge, position, false);
    }

    public GraphPosition(GraphEdge edge, double position, boolean reverseorientation) {
        if (edge == null) {
            logger.warn("no edge");
        }
        currentedge = edge;
        edgeposition = position;
        this.reverseorientation = reverseorientation;
    }

    /**
     * Mapping fron an abstract graph position to a real 3D position.
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

    public boolean isReverseOrientation() {
        return reverseorientation;
    }

    /**
     * The position, always from "from".
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

    public GraphEdge getEdge() {
        return currentedge;
    }
}
