package de.yard.threed.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Difference to GraphPath? A transition isType a "short path" used for connecting edges.
 * 
 * Created by thomass on 24.05.17.
 */
public class GraphTransition {
    public List<GraphPathSegment> seg = new ArrayList<GraphPathSegment>();

    public GraphTransition() {
        
    }
    public GraphTransition(GraphPathSegment s) {
        this.seg.add(s);
    }

    public GraphTransition(GraphPathSegment s0, GraphPathSegment s1, GraphPathSegment s2) {
        this.seg.add(s0);
        this.seg.add(s1);
        this.seg.add(s2);
    }

    public void add(GraphPathSegment graphPathSegement) {
        seg.add(graphPathSegement);
    }
}
