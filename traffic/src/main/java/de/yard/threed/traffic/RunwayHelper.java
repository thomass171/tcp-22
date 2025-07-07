package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.trafficcore.model.Runway;

public class RunwayHelper {

    Runway runway;
    EllipsoidCalculations rbc;

    public RunwayHelper(Runway runway, EllipsoidCalculations rbc) {
        this.runway = runway;
        this.rbc = rbc;
    }

    public Degree getHeading() {
        return rbc.courseTo(runway.getFrom(), runway.getTo());
    }

    public LatLon getHoldingPoint() {
        // distance is 'km', not 'm'!
        return rbc.applyCourseDistance(runway.getFrom(), getHeading(), /*65*/0.065);
    }

    /**
     * der entrypoint ist zur Nutzung im groundnet um den groundnet graph rein zu verlängern.
     *
     * @return
     */
    public LatLon getEntrypoint() {
        // distance is 'km', not 'm'!
        return rbc.applyCourseDistance(getHoldingPoint(), getHeading().reverse(), /*15*/0.015);
    }

    public LatLon getTakeoffPoint() {
        // distance is 'km', not 'm'!
        return rbc.applyCourseDistance(runway.getFrom(), getHeading(), /*600*/0.6);
    }

    public LatLon getTouchdownpoint() {
        // distance is 'km', not 'm'!
        return rbc.applyCourseDistance(runway.getFrom(), getHeading(), /*200*/0.2);
    }
}
