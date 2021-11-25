package de.yard.threed.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 27.05.17.
 */
public class DefaultGraphWeightProvider implements GraphWeightProvider {
    public List<GraphEdge> voidedges = new ArrayList<GraphEdge>();
    Graph graph;
    public List<Integer> validlayer = null;

    public DefaultGraphWeightProvider(Graph graph, List<GraphEdge> voidedges) {
        this.graph = graph;
        this.voidedges = voidedges;
    }

    public DefaultGraphWeightProvider(Graph graph, int validlayer) {
        this.graph = graph;
        this.validlayer = new ArrayList<Integer>();
        this.validlayer.add(validlayer);
    }

    public DefaultGraphWeightProvider(Graph graph, GraphEdge[] voidedges) {
        this.graph=graph;
        for (GraphEdge e:voidedges){
            this.voidedges.add(e);
        }
    }

    @Override
    public double getWeight(GraphNode n1, GraphNode n2) {
        if (voidedges != null) {
            for (GraphEdge e : voidedges) {
                if (e.from == n1 && e.to == n2) {
                    return java.lang.Double.MAX_VALUE;
                }
                if (e.to == n1 && e.from == n2) {
                    return java.lang.Double.MAX_VALUE;
                }
            }
        }
        GraphEdge e1 = graph.findConnection(n1, n2);
        if (!isvalid(e1.getLayer())) {
            return java.lang.Double.MAX_VALUE;
        }
        return graph.getWeight(n1, n2);
    }

    private boolean isvalid(int layer) {
        if (validlayer == null) {
            return true;
        }
        for (int l : validlayer) {
            if (l == layer) {
                return true;
            }
        }
        return false;
    }
}
