package de.yard.threed.traffic;

import de.yard.threed.graph.GraphMovingComponent;

/**
 * localOrbit->globalOrbit->travel->globalOrbit->localOrbit
 */
public class OrbitToOrbitRouteBuilder implements FlightRouteBuilder {
    @Override
    public boolean apply(GraphMovingComponent gmc, VehicleComponent vhc) {
        return false;
    }
}
