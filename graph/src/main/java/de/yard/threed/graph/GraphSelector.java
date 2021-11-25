package de.yard.threed.graph;

/**
 * Created by thomass on 20.12.16.
 */
public interface GraphSelector {
    /**
     * Die Successor Edge an einem Knoten liefern, der ueber incomingedge erreicht wird.
     * Liefert null, wenn es keine gibt. Es kann auch die incomingedge geliefert werden.
     * Das ist alles Sache der Implementierung.
     */
    GraphPathSegment findNextEdgeAtNode(GraphEdge incomingedge, GraphNode node);
}
