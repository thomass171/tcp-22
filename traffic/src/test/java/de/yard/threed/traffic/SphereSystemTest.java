package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.engine.testutil.TestUtils.assertViewPoint;
import static de.yard.threed.traffic.SphereSystem.USER_REQUEST_SPHERE;
import static org.junit.jupiter.api.Assertions.*;


/**
 * <p>
 * Created by thomass on 7.10.21.
 */
public class SphereSystemTest {

    SceneNode world;


    /**
     * 16.3.24: Was testFlatWaylandWithoutConfigXml once, but (XML) configs are
     * standard meanwhile. At least the legacy options are no longer used.
     */
    @Test
    public void testFlatWaylandWithConfigXml() throws Exception {

        startSimpleTest("traffic:tiles/Wayland.xml");

        // 0 because of no TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals(0, SystemManager.getRequestCount(), "requests ");
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals(1, completeEvents.size(), "completeEvents.size");
        SphereProjections projections = TrafficHelper.getProjectionByDataprovider();
        assertNotNull(projections);
        assertNotNull(projections.projection);
        assertNull(projections.backProjection);

        List<ViewPoint> viewpoints = TrafficHelper.getViewpointsByDataprovider();
        // Vehicle not loaded because of missing system.
        assertEquals(5, viewpoints.size(), "viewpoints");
        ViewPoint viewPoint = viewpoints.get(0);
        assertViewPoint("oben1", new LocalTransform(new Vector3(0, 0, 137), Quaternion.buildRotationX(new Degree(0))), viewPoint);
    }


    @Test
    public void testDemoWithConfigXml() throws Exception {
        //DefaultTrafficWorld.instance = null;
        //assertNull("", DefaultTrafficWorld.getInstance());

        startSimpleTest("traffic:tiles/Demo.xml");

        // 0 because of no TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals(0, SystemManager.getRequestCount(), "requests ");
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals(1, completeEvents.size(), "completeEvents.size");
        //assertNull("", DefaultTrafficWorld.getInstance());
        assertNotNull(TrafficHelper.getProjectionByDataprovider());

        List<ViewPoint> viewpoints = TrafficHelper.getViewpointsByDataprovider();

        assertEquals(1, viewpoints.size(), "viewpoints");
        assertEquals(100, viewpoints.get(0).transform.position.getY(), 0.0001, "viewpoint[0].y");
        ViewPoint viewPoint = viewpoints.get(0);

        List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();
        assertEquals(1, vehiclelist.size(), "vehiclelist");
        assertEquals("loc", vehiclelist.get(0).getName(), "vehiclelist[0].name");

    }

    /*geht noch nicht wegen TrafficWorldSingleton @Test
    public void testWithAirportService() throws Exception {
        assertNull("",DefaultTrafficWorld.getInstance());

        runSimpleTest();

        assertNull("",DefaultTrafficWorld.getInstance());
    }*/

    /**
     * Without tilename request is consumed but with no action?
     * No, tilename=null leads to 3D for now
     * Und laedt aus BackwardCompat EDDK groundnet.
     */
    @Test
    public void testWithoutTilename() throws Exception {

        startSimpleTest(null);

        List<Event> completeEvents = EcsTestHelper.getEventHistory();
        //Hmm. es gibt ja so viele Events .assertEquals("completeEvents.size", 4/*??*/, completeEvents.size());

        List<Event> locEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals(1, locEvents.size(), "completeEvents.size");
        assertNull(TrafficHelper.getProjectionByDataprovider().projection);
        // 1 because of TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals(1, SystemManager.getRequestCount(), "requests ");
        Request request = SystemManager.getRequest(0);
        assertEquals("TRAFFIC_REQUEST_LOADGROUNDNET", request.getType().getLabel());

    }

    private void runSimpleTest(String tilename) {
    }


    private void startSimpleTest(String tilename) {

        if (tilename == null || tilename.endsWith("EDDK")) {
            //DefaultTrafficWorld.instance = null;
            //assertNull("", DefaultTrafficWorld.getInstance());

            //TrafficWorldConfig liegt in "ext"
            setup(GeoCoordinate.fromLatLon(new LatLon(new Degree(50.86538f), new Degree(7.139103f)), 0));
        } else {
            setup(null);
        }
        SystemManager.putRequest(new Request(USER_REQUEST_SPHERE, new Payload(tilename, new ArrayList())));
        //ein Request muss anliegen
        assertEquals(1, SystemManager.getRequestCount(), "requests ");
        //EcsTestHelper.processRequests();
        EcsTestHelper.processSeconds(2);
    }

    /**
     *
     */
    private void setup(GeoCoordinate center) {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                world = new SceneNode();
                SystemManager.addSystem(new SphereSystem(null, null));

                //ohne Elevation wird kein groundnet geladen
                //??SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, TerrainElevationProvider.buildForStaticAltitude(17));

            }
        };

        EngineTestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));
        //16.12.21 AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
    }
}
