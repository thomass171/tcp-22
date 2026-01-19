package de.yard.threed.traffic;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.graph.*;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.flight.AircraftVehicleRotation;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.GeoRoute;
import de.yard.threed.trafficcore.SimpleEllipsoidCalculations;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class AircraftVehicleRotationTest {
    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory());

    @Test
    void testWithRailingSample() {
        float innerradius = RailingDimensions.innerarcradius;

        Graph rails = RailingFactory.buildRailSample1();
        GraphEdge firstArcEdge;
        int i = 0;
        do {
            firstArcEdge = rails.getEdge(i++);
        } while (!firstArcEdge.isArc());

        GraphPosition railingpos = new GraphPosition(firstArcEdge);

        AircraftVehicleRotation aircraftVehicleRotation = new AircraftVehicleRotation(new Degree(45), 10.0);

        Quaternion vehicleRotation = aircraftVehicleRotation.getVehicleOrientation( railingpos, rails);
    }

    @Test
    void testRollForPosition() {
        AircraftVehicleRotation aircraftVehicleRotation = new AircraftVehicleRotation(new Degree(45), 10.0);
        assertEquals(0.0, aircraftVehicleRotation.getRollForPosition(0, 100, true, 20.0).getDegree());
        assertEquals(4.5 * 3, aircraftVehicleRotation.getRollForPosition(3, 100, true, 20.0).getDegree());
        assertEquals(45.0, aircraftVehicleRotation.getRollForPosition(10, 100, true, 20.0).getDegree());
        assertEquals(45.0, aircraftVehicleRotation.getRollForPosition(50, 100, true, 20.0).getDegree());
        assertEquals(45.0, aircraftVehicleRotation.getRollForPosition(90, 100, true, 20.0).getDegree());
        assertEquals(45.0 - 2 * 4.5, aircraftVehicleRotation.getRollForPosition(92, 100, true, 20.0).getDegree());
        assertEquals(0.0, aircraftVehicleRotation.getRollForPosition(100, 100, true, 20.0).getDegree());
    }

    @Test
    void testRollForPositionOnShortArc() {
        AircraftVehicleRotation aircraftVehicleRotation = new AircraftVehicleRotation(new Degree(45), 8.0);
        assertEquals(45.0*0.5, aircraftVehicleRotation.getRollForPosition(4.0, 12, true, 20.0).getDegree());
        assertEquals(45.0*0.75, aircraftVehicleRotation.getRollForPosition(6.0, 12, true, 20.0).getDegree());
        assertEquals(45.0*0.5, aircraftVehicleRotation.getRollForPosition(8.0, 12, true, 20.0).getDegree());
        assertEquals(0.0, aircraftVehicleRotation.getRollForPosition(11.99999, 12, true, 20.0).getDegree(),0.0001);
    }

    @Test
    void testRollForPositionOnSuperShortArc() {
        AircraftVehicleRotation aircraftVehicleRotation = new AircraftVehicleRotation(new Degree(45), 12.0);
        assertEquals(45.0 / 6.0, aircraftVehicleRotation.getRollForPosition(2.0, 8, true, 20.0).getDegree());
        assertEquals(45.0 / 3.0, aircraftVehicleRotation.getRollForPosition(4.0, 8, true, 20.0).getDegree());
        assertEquals(45.0 / 6.0, aircraftVehicleRotation.getRollForPosition(6.0, 8, true, 20.0).getDegree());
        assertEquals(0.0, aircraftVehicleRotation.getRollForPosition(7.99999, 8, true, 20.0).getDegree(),0.0001);
    }

    @Test
    void testFlightRouteEDKB_EDDK() throws Exception {

        FlightRouteGraph flightRoute = TrafficTestUtils.buildFlightRouteGraph(GeoRoute.parse(GeoRoute.SAMPLE_EDKB_EDDK));
        GraphPath path=flightRoute.getPath();

        GraphPathSegment firstArcToRight=path.getSegment(3);

        AircraftVehicleRotation aircraftVehicleRotation = new AircraftVehicleRotation(new Degree(45), 10.0);
        assertEquals(-45.0, aircraftVehicleRotation.getRollForPosition(
                new GraphPosition(firstArcToRight.edge, firstArcToRight.edge.getLength()/2.0), flightRoute.getGraph()).getDegree());
    }

    @Test
    void testFlightRouteEGPH06_EGPF05() throws Exception {

        FlightRouteGraph flightRoute = TrafficTestUtils.buildFlightRouteGraph(GeoRoute.parse(GeoRoute.SAMPLE_EGPH06_EGPF05));
        GraphPath path=flightRoute.getPath();

        GraphPathSegment firstArcToLeft=path.getSegment(3);

        AircraftVehicleRotation aircraftVehicleRotation = new AircraftVehicleRotation(new Degree(45), 10.0);
        assertEquals(45.0, aircraftVehicleRotation.getRollForPosition(
                new GraphPosition(firstArcToLeft.edge, firstArcToLeft.edge.getLength()/2.0), flightRoute.getGraph()).getDegree());
    }
}
