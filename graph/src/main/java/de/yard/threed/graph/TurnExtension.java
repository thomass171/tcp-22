package de.yard.threed.graph;
//liegt in graph, weil GraphUtils es verwendet.


/**
 * Graphextension fuer Teardrop, turnloop, back, etc.
 * 
 * Created by thomass on 04.05.17.
 */
public class TurnExtension {
    public GraphEdge edge;
    public GraphEdge branch;
    public GraphEdge arc;

    public TurnExtension(GraphEdge edge, GraphEdge branch, GraphEdge arc) {
        this.edge = edge;
        this.branch = branch;
        this.arc = arc;
    }
    

    /**
     * Same for branch and arc (but not edge).
     *
     * @return
     */
    public int getLayer() {
        if (branch!=null) {
            return branch.getLayer();
        }
        if (edge!=null) {
            return edge.getLayer();
        }
        if (arc!=null) {
            return arc.getLayer();
        }
        return -1;
    }
}
