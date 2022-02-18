package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Payload;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.traffic.SphereSystem.USER_REQUEST_SPHERE;
import static org.junit.Assert.*;

/**
 * <p>
 * Created by thomass on 7.10.21.
 */
public class SphereSystemTest {

    SceneNode world;


    @Test
    public void testFlatWaylandWithoutConfigXml() throws Exception {
        //DefaultTrafficWorld.instance = null;
        //assertNull("", DefaultTrafficWorld.getInstance());

        startSimpleTest(/*"Desdorf"*/"traffic:Wayland");

        // 0 because of no TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals("requests ", 0, SystemManager.getRequestCount());
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals("completeEvents.size", 1, completeEvents.size());
        //assertNull("", DefaultTrafficWorld.getInstance());
        SphereProjections projections = TrafficHelper.getProjectionByDataprovider();
        assertNotNull("", projections);
        assertNotNull("", projections.projection);
        assertNull("", projections.backProjection);

        List<ViewPoint> viewpoints = TrafficHelper.getViewpointsByDataprovider();
        // 9 "oben*" viewpoints from osmscenery". Vehicle not loaded because of missing system.
        assertEquals("viewpoints", 9, viewpoints.size());
        ViewPoint viewPoint = viewpoints.get(0);
    }



    @Test
    public void testDemoWithConfigXml() throws Exception {
        //DefaultTrafficWorld.instance = null;
        //assertNull("", DefaultTrafficWorld.getInstance());

        startSimpleTest("traffic:tiles/Demo.xml");

        // 0 because of no TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals("requests ", 0, SystemManager.getRequestCount());
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals("completeEvents.size", 1, completeEvents.size());
        //assertNull("", DefaultTrafficWorld.getInstance());
        assertNotNull("", TrafficHelper.getProjectionByDataprovider());

        List<ViewPoint> viewpoints = TrafficHelper.getViewpointsByDataprovider();

        assertEquals("viewpoints", 1, viewpoints.size());
        assertEquals("viewpoint[0].y", 100, viewpoints.get(0).transform.position.getY(),0.0001);
        ViewPoint viewPoint = viewpoints.get(0);

        assertEquals("vehiclelist", 1, TrafficSystem.vehiclelist.size());
        assertEquals("vehiclelist[0].name", "loc", TrafficSystem.vehiclelist.get(0).getName());

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
        assertEquals("completeEvents.size", 1, locEvents.size());
        assertNull("", TrafficHelper.getProjectionByDataprovider().projection);
        // 1 because of TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals("requests ", 1, SystemManager.getRequestCount());
        Request request = SystemManager.getRequest(0);
        assertEquals("", "TRAFFIC_REQUEST_LOADGROUNDNET", request.getType().getLabel());

    }

    private void runSimpleTest(String tilename) {
    }


    private void startSimpleTest(String tilename) {

        if (tilename == null || tilename.endsWith("EDDK")) {
            //DefaultTrafficWorld.instance = null;
            //assertNull("", DefaultTrafficWorld.getInstance());

            //TrafficWorldConfig liegt in "ext"
            setup(GeoCoordinate.fromLatLon(new LatLon(new Degree(50.86538f), new Degree(7.139103f)),0),null);
        }else{
            setup(null,null);
        }
        SystemManager.putRequest(new Request(USER_REQUEST_SPHERE, new Payload(tilename,new ArrayList())));
        //ein Request muss anliegen
        assertEquals("requests ", 1, SystemManager.getRequestCount());
        //EcsTestHelper.processRequests();
        EcsTestHelper.processSeconds(2);
    }

    /**
     *
     */
    private void setup(GeoCoordinate center, SceneConfig sceneConfig) {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                world = new SceneNode();
                SystemManager.addSystem(new SphereSystem(null,null,center,sceneConfig));

                //ohne Elevation wird kein groundnet geladen
                //??SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, TerrainElevationProvider.buildForStaticAltitude(17));

            }
        };

        TestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod);
        //16.12.21 AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
    }
}
