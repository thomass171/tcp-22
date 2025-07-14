package de.yard.threed.trafficcore;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
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
        return rbc.applyCourseDistance(runway.getFrom(), getHeading(), 65);
    }

    /**
     * der entrypoint ist zur Nutzung im groundnet um den groundnet graph rein zu verlängern.
     *
     * @return
     */
    public LatLon getEntrypoint() {
        return rbc.applyCourseDistance(getHoldingPoint(), getHeading().reverse(), 15);
    }

    public LatLon getTakeoffPoint() {
        return rbc.applyCourseDistance(runway.getFrom(), getHeading(), 600);
    }

    public LatLon getTouchdownpoint() {
        return rbc.applyCourseDistance(runway.getFrom(), getHeading(), 200);
    }
}
