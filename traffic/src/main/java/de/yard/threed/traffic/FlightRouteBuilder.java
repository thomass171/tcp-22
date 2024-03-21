package de.yard.threed.traffic;

import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.traffic.VehicleComponent;

/**
 * We also have BasicFlightRouteBuilder.
 */
public interface FlightRouteBuilder {
    /**
     * Returns true when RoutBuilder finally completed.
     *
     * @return
     * @param gmc
     * @param vhc
     */
    boolean apply(GraphMovingComponent gmc, VehicleComponent vhc);
}
