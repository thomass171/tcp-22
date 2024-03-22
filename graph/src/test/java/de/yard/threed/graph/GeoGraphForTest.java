package de.yard.threed.graph;

import de.yard.threed.core.Vector3;

/**
 * With double coordinates corresponding to geo coordinates for better
 * intuitation.
 */
public class GeoGraphForTest {

    public Graph graph;
    public GraphNode n0, n1, n2;
    public GraphEdge e0, e1;

    public Vector3 v0 = new Vector3(50.76, 7.16, 0);
    public Vector3 v1 = new Vector3(50.76, 7.15, 0);
    public Vector3 v2 = new Vector3(50.77, 7.15, 0);

    GeoGraphForTest(GraphProjection projection) {
        if (projection == null) {
            graph = new Graph();
        } else {
            graph = new ProjectedGraph(projection);
        }

        n0 = graph.addNode("", v0);
        n1 = graph.addNode("", v1);
        n2 = graph.addNode("", v2);
        e0 = graph.connectNodes(n0, n1);
        e1 = graph.connectNodes(n1, n2);


    }
}
