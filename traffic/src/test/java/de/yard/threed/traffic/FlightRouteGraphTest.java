package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPathSegment;
import de.yard.threed.graph.GraphTestUtil;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.flight.AircraftVehicleRotation;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficcore.GeoRoute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlightRouteGraphTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testEDKB_EDDK() throws Exception {

        FlightRouteGraph flightRoute = TrafficTestUtils.buildFlightRouteGraph(GeoRoute.parse(GeoRoute.SAMPLE_EDKB_EDDK));
        GraphPath path=flightRoute.getPath();

        // The smoothing result looks strange, maybe wrong. Too few/short/long arcs, missing arc near end?, 0-length end.
        GraphTestUtil.assertGraphPath(
                new boolean[]{false, true, false, true, false, true, false, false, true, false},
                new double[]{408.9, 1.3, 426.3, 31.2, 6517.68, 49.95, 3831.9, 923.2, 4198.05, 0.0},
                path);

        GraphPathSegment firstArcToRight=path.getSegment(3);
        assertFalse(firstArcToRight.edge.arcDirectionLeft(true, flightRoute.getGraph().getGraphOrientation()));
    }

    @Test
    public void testEGPH06_EGPF05() throws Exception {

        FlightRouteGraph flightRoute = TrafficTestUtils.buildFlightRouteGraph(GeoRoute.parse(GeoRoute.SAMPLE_EGPH06_EGPF05));
        GraphPath path=flightRoute.getPath();

        GraphPathSegment firstArcToLeft=path.getSegment(3);
        assertTrue(firstArcToLeft.edge.arcDirectionLeft(true, flightRoute.getGraph().getGraphOrientation()));
    }
}
