package de.yard.threed.traffic.flight;

import de.yard.threed.graph.GraphComponent;
import de.yard.threed.graph.GraphEdge;

/**
 * Created by thomass on 28.03.17.
 */
public class TaxiwaySegment extends GraphComponent {
    // never null, but maybe empty
    public String name;
    boolean isPushBackRoute;
    GraphEdge edge;
    
    public TaxiwaySegment(GraphEdge edge, String name, boolean isPushBackRoute) {
        this.name = name;
        this.isPushBackRoute=isPushBackRoute;
        this.edge = edge;
    }
}
