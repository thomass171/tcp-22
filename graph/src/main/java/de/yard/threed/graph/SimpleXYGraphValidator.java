package de.yard.threed.graph;

/**
 * Created by thomass on 08.02.17.
 */
public class SimpleXYGraphValidator implements GraphValidator {
    public SimpleXYGraphValidator(){
        
    }

    @Override
    public boolean nodesValidForEdge(GraphNode n1, GraphNode n2) {
        return true;
    }
}
