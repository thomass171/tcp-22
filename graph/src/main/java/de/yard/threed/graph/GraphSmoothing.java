package de.yard.threed.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 05.04.17.
 */
public class GraphSmoothing {
    private Graph graph;
    // nachhalten, welche Paare gesmoothed wurden, um die Gegenrichtung zu skippen
    //List<Pair<GraphEdge,GraphEdge>> smoothededges = new ArrayList<Pair<GraphEdge,GraphEdge>>();
    List<String> smoothededges = new ArrayList<String>();


    public GraphSmoothing(Graph graph) {
        this.graph = graph;
    }

    public boolean areSmoothed(GraphEdge e1, GraphEdge e2) {
        if (smoothededges.contains("" + e1.getId() + "+" + e2.getId())) {
            return true;
        }
        if (smoothededges.contains("" + e2.getId() + "+" + e1.getId())) {
            return true;
        }
        return false;
    }

    public void addSmoothedEdges(GraphEdge e1, GraphEdge e2) {
        smoothededges.add("" + e1.getId() + "+" + e2.getId());
    }
}
