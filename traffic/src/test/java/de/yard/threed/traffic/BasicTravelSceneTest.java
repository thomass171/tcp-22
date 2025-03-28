package de.yard.threed.traffic;


import de.yard.threed.core.Color;
import de.yard.threed.core.Event;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeLight;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.FirstPersonMovingSystem;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.traffic.apps.BasicTravelScene;

import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import de.yard.threed.graph.GraphMovingComponent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static de.yard.threed.core.testutil.TestUtils.*;
import static de.yard.threed.engine.test.testutil.TestUtil.assertColor;
import static de.yard.threed.engine.testutil.EngineTestUtils.assertViewPoint;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration test.
 * Putting it all together and test interaction.
 * <p>
 * Derived from FlightSystemTest/DemoSceneTest.
 * <p>
 * Created by thomass on 29.11.21.
 */
@Slf4j
public class BasicTravelSceneTest {

    SceneNode world;

    EcsEntity aircraft;
    VehicleComponent vhc;
    VelocityComponent vc;
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    /**
     * "Wayland" is loaded "by convention". 16.12.21: No longer by convention
     * A georoute cannot be used here because Wayland is native 2D without MapProjection.
     */
    @ParameterizedTest
    @CsvSource(value = {
            "false;;",
            "true;;",
            // coordinates just arbitrary
            "false;mobi;coordinate:90.0,110.5,76.0",
    }, delimiter = ';')
    public void testWayland(boolean enableFPC, String initialVehicle, String initialLocation) throws Exception {
        HashMap<String, String> customProperties = new HashMap<String, String>();
        customProperties.put("enableFPC", Boolean.toString(enableFPC));
        if (initialVehicle != null) {
            customProperties.put("initialVehicle", initialVehicle);
            customProperties.put("initialLocation", initialLocation);
        }
        setup("traffic:tiles/Wayland.xml", customProperties);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        if (enableFPC) {
            assertNotNull(SystemManager.findSystem(FirstPersonMovingSystem.TAG));
            assertNull(SystemManager.findSystem(TeleporterSystem.TAG));
        } else {
            assertNull(SystemManager.findSystem(FirstPersonMovingSystem.TAG));
            assertNotNull(SystemManager.findSystem(TeleporterSystem.TAG));
        }
        assertNotNull(Observer.getInstance(), "observer");
        // Should be assigned to ?
        Transform observerParent = Observer.getInstance().getTransform().getParent();
        assertNotNull(observerParent, "observerParent");

        EcsEntity userEntity = UserSystem.getInitialUser();
        assertNotNull(userEntity, "user entity");

        // "Wayland" has two graph files that should have been loaded finally (via EVENT_LOCATIONCHANGED)
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED);
        assertEquals(1, completeEvents.size(), "TRAFFIC_EVENT_SPHERE_LOADED.size");
        Event evtLocationChanged = completeEvents.get(0);

        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals(2, completeEvents.size(), "TRAFFIC_EVENT_GRAPHLOADED.size");

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull(railwayGraph, "railwayGraph");

        assertFalse(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);

        sceneRunner.runLimitedFrames(50);
        TrafficSystem trafficSystem = ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG));
        SphereSystem sphereSystem = ((SphereSystem) SystemManager.findSystem(SphereSystem.TAG));
        // no projection in wayland, it is native.
        assertNull(sphereSystem.projection);
        testLights();

        // now 'loc' should have been loaded.
        assertEquals(1 + ((initialVehicle == null ? 0 : 1)), EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_VEHICLELOADED).size(), "TRAFFIC_EVENT_VEHICLELOADED.size");
        // vehicle loaded for each of the railway and road graphs
        assertEquals(2, trafficSystem.vehiclesLoaded);
        // 10.3.25 'mobi' added. It's known independent from launching it.
        assertEquals(1 + 1, trafficSystem.getKnownVehicles().size());

        EcsEntity locEntity = SystemManager.findEntities(e -> "loc".equals(e.getName())).get(0);
        assertNotNull(locEntity, "loc entity");
        // position is random so cannot be tested
        TrafficTestUtils.assertVehicleEntity(locEntity, "loc", 0.0, null, "TravelSphere", log);

        if (enableFPC) {
            assertNull(TeleportComponent.getTeleportComponent(userEntity));
            assertEquals(5, ((FirstPersonMovingSystem) SystemManager.findSystem(FirstPersonMovingSystem.TAG)).viewPoints.size());
            assertEquals("oben5", ((FirstPersonMovingSystem) SystemManager.findSystem(FirstPersonMovingSystem.TAG)).viewPoints.get(4).name);
        } else {
            // 'loc' has two viewpoints. TODO check why is position
            EcsTestHelper.assertTeleportComponent(userEntity, 5 + 2 + ((initialVehicle == null ? 0 : 2)), 6 + ((initialVehicle == null ? 0 : 2)), new Vector3());
        }

        List<ViewPoint> vps = TrafficHelper.getViewpointsByDataprovider();
        assertNotNull(vps);
        assertEquals(5, vps.size());
        assertViewPoint("oben5", new LocalTransform(new Vector3(0, 0, 4000), new Quaternion()), vps.get(4));

        assertNotNull(trafficSystem.getVehicleConfig("loc", null));
        assertNull(trafficSystem.getVehicleConfig("xx", null));

        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(locEntity);
        // loc should immediately move (without path, only by property automove set in config 'Wayland.xml' for vehicle 'loc')
        assertNotNull(gmc.getGraph());
        assertTrue(gmc.hasAutomove());
        assertNull(gmc.getPath());

        if (initialVehicle != null) {
            EcsEntity mobiEntity = SystemManager.findEntities(e -> "mobi".equals(e.getName())).get(0);
            assertNotNull(mobiEntity, "mobiEntity");
            TrafficTestUtils.assertVehicleEntity(mobiEntity, "mobi", 0.0, new Vector3(90.0, 110.5, 76.0), "TravelSphere"/*"sphereTransform"*/, log);
            FreeFlyingComponent bmc = FreeFlyingComponent.getFreeFlyingComponent(mobiEntity);
            assertNotNull(bmc);

        }
    }

    @Test
    public void testDemo() throws Exception {
        setup("traffic:tiles/Demo.xml", new HashMap<String, String>());

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertNotNull(UserSystem.getInitialUser(), "user entity");

        // "Wayland" has two graph files that should have been loaded finally (via EVENT_LOCATIONCHANGED)
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED);
        assertEquals(1, completeEvents.size(), "TRAFFIC_EVENT_SPHERE_LOADED.size");

        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals(1, completeEvents.size(), "TRAFFIC_EVENT_GRAPHLOADED.size");

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull(railwayGraph, "railwayGraph");

        assertEquals(1, SystemManager.findEntities((EntityFilter) null).size(), "number of entities (user)");

        // test GraphTerrainSystem
        List<NativeSceneNode> tracksNode = SceneNode.findByName("tracks");
        assertEquals(1, tracksNode.size(), "tracksNode");

        assertTrue(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);

        EcsEntity player = UserSystem.getInitialUser();
        // vehicle not yet loaded. So only 1 outside viewpoints.
        EcsTestHelper.assertTeleportComponent(player, 1, 0, null);

        sceneRunner.runLimitedFrames(50);
        TrafficSystem trafficSystem = ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG));
        SphereSystem sphereSystem = ((SphereSystem) SystemManager.findSystem(SphereSystem.TAG));
        // no projection in demo, it is native.
        assertNull(sphereSystem.projection);
        testLights();

        // now 'loc' should have been loaded.
        // should start at externel overview point. For now its in vehicle. TODO: Check why 0,0,0 is correct position
        EcsTestHelper.assertTeleportComponent(player, 1 + 2, 2, new Vector3());

        EcsEntity userEntity = SystemManager.findEntities(e -> BasicTravelScene.DEFAULT_USER_NAME.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        EcsEntity locEntity = SystemManager.findEntities(e -> "loc".equals(e.getName())).get(0);
        assertNotNull(locEntity, "loc entity");
        // assume position fits
        TrafficTestUtils.assertVehicleEntity(locEntity, "loc", 0.0, new Vector3(48.56055289994228, 0.0, -20.03455336481473), "TravelSphere", log);

        SceneNode locNode = locEntity.getSceneNode();
        double xpos0 = locNode.getTransform().getPosition().getX();
        sceneRunner.runLimitedFrames(50);
        double xpos1 = locNode.getTransform().getPosition().getX();
        double xdiff = Math.abs(xpos0 - xpos1);
        log.debug("xdiff={}", xdiff);
        assertTrue(xdiff > 3.0);

        // 28.11.23: would be nice to test effect of 'baseTransformForVehicleOnGraph' But how, hmm?

        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(locEntity);
        // loc should immediately move (without path, only by property automove set in config 'Demo.xml' for vehicle 'loc')
        assertNotNull(gmc.getGraph());
        assertTrue(gmc.hasAutomove());
        assertNull(gmc.getPath());
    }

    /**
     * 21.3.24: 3D scene with initial route by geolocation.
     * Not possible until we have EllipsoidCalculations implementation?
     * The route is the SAMPLE_EDKB_EDDK
     */
    @ParameterizedTest
    @CsvSource(value = {"loc;wp:50.768,7.1672000->takeoff:50.7692,7.1617000->wp:50.7704,7.1557->wp:50.8176,7.0999->wp:50.8519,7.0921->touchdown:50.8625,7.1317000->wp:50.8662999,7.1443999"}, delimiter = ';')
    public void testMoon(String initialVehicle, String initialRoute) throws Exception {
        HashMap<String, String> customProperties = new HashMap<String, String>();
        //if (initialVehicle!=null){
        customProperties.put("initialVehicle", initialVehicle);
        customProperties.put("initialRoute", initialRoute);
        //}

        setup("traffic:tiles/Moon.xml", customProperties);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertNotNull(UserSystem.getInitialUser(), "user entity");

        // "Wayland" has two graph files that should have been loaded finally (via EVENT_LOCATIONCHANGED)
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED);
        assertEquals(1, completeEvents.size(), "TRAFFIC_EVENT_SPHERE_LOADED.size");

        // Now we expect loc on the starting point of the graph of initialRoute
        EcsEntity locEntity = SystemManager.findEntities(e -> "loc".equals(e.getName())).get(0);
        assertNotNull(locEntity, "loc entity");

        SceneNode locNode = locEntity.getSceneNode();
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(locEntity);
        assertNotNull(gmc, "gmc");
        assertNotNull(gmc.getCurrentposition(), "getCurrentposition");

        Vector3 locPosition = locNode.getTransform().getPosition();
        // there is no elevation currently(?), so the position should be quite simple to validate
        //TODO other test due to movement 26.6.24:there shoud be no movement?? assertVector3(new SimpleEllipsoidCalculations(MoonSceneryBuilder.MOON_RADIUS).toCart(new GeoCoordinate(new Degree(50.768), new Degree(7.1672000))), locPosition);

        // loc should *not* immediately move (even though a path there is no property automove set in config 'Moon.xml' for vehicle 'loc')
        TrafficTestUtils.assertEntityOnGraph(locEntity, true, VehicleLauncher.locToGraphRotation());

        LocalTransform posrot = gmc.getGraph().getPosRot(gmc.getCurrentposition(), new Quaternion());
        log.debug("posrot={}", posrot);

        EllipsoidCalculations elliCalcs = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        TrafficTestUtils.assertGeoGraphRotation(gmc.getGraph(), gmc.getGraph().getEdge(0), 100);

        // double check to TrafficTestUtils.assertGeoGraphRotation()
        GeoCoordinate first = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.768, 7.1672000), 60);
        GeoCoordinate second = GeoCoordinate.fromLatLon(LatLon.fromDegrees(50.7692, 7.1617000), 60);
        double distance = 100;//elliCalcs.distanceTo(first, second);
        // negate due to typical forward orientation confusion
        Vector3 rotatedForward = MathUtil2.DEFAULT_FORWARD.negate().rotate(posrot.rotation);
        // rotatedForward should roughly have direction -x, -y, +z
        log.debug("distance={},rotatedForward={}", distance, rotatedForward);
        GeoCoordinate rotTarget = elliCalcs.fromCart(elliCalcs.toCart(first).add(rotatedForward.multiply(distance)));
        // 50.7690336,7.1622472 is correct (appx. 100m from first on runway) according to visual map check
        assertLatLon(LatLon.fromDegrees(50.7690336, 7.1622472), rotTarget, 0.001, "rotTarget");


    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String tileName, HashMap<String, String> customProperties) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        if (tileName != null) {
            properties.put("basename", tileName);
        }
        properties.putAll(customProperties);
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties),
                /*1.10.23 provided by scene new String[]{"engine", "data", "traffic"}*/null);
    }

    public void testLights() {

        List<NativeLight> lights = Platform.getInstance().getLights();
        assertEquals(2, lights.size(), "lights.size");
        // directional
        assertVector3(new Vector3(1, 1, 1).normalize(), lights.get(0).getDirectionalDirection());
        assertColor("", Color.WHITE, lights.get(0).getDirectionalColor());
        // ambient
        assertNull(lights.get(1).getDirectionalDirection());
        assertColor("", Color.WHITE, lights.get(1).getAmbientColor());
    }
}
