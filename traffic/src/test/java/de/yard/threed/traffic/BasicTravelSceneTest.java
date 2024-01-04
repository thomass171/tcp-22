package de.yard.threed.traffic;


import de.yard.threed.core.Event;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeSceneNode;
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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import de.yard.threed.graph.GraphMovingComponent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static de.yard.threed.engine.testutil.TestUtils.assertViewPoint;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
    GraphMovingComponent gmc;
    VehicleComponent vhc;
    VelocityComponent vc;
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    /**
     * "Wayland" is loaded "by convention". 16.12.21: No longer by convention
     */
    @ParameterizedTest
    @CsvSource({"false", "true"})
    public void testWayland(boolean enableFPC) throws Exception {
        HashMap<String, String> customProperties = new HashMap<String, String>();
        customProperties.put("enableFPC", Boolean.toString(enableFPC));
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
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals(1, completeEvents.size(), "EVENT_LOCATIONCHANGED.size");
        Event evtLocationChanged = completeEvents.get(0);

        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals(2, completeEvents.size(), "TRAFFIC_EVENT_GRAPHLOADED.size");

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull(railwayGraph, "railwayGraph");

        assertFalse(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);

        sceneRunner.runLimitedFrames(50);
        // now 'loc' should have been loaded.
        assertEquals(1, EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_VEHICLELOADED).size(), "TRAFFIC_EVENT_VEHICLELOADED.size");
        // vehicle loaded for each of the railway and road graphs
        assertEquals(2, ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG)).vehiclesLoaded);

        if (enableFPC) {
            assertNull(TeleportComponent.getTeleportComponent(userEntity));
            assertEquals(5, ((FirstPersonMovingSystem) SystemManager.findSystem(FirstPersonMovingSystem.TAG)).viewPoints.size());
            assertEquals("oben5", ((FirstPersonMovingSystem) SystemManager.findSystem(FirstPersonMovingSystem.TAG)).viewPoints.get(4).name);
        } else {
            // 'loc' has two viewpoints. TODO check why is position
            EcsTestHelper.assertTeleportComponent(userEntity, 5 + 2, 6, new Vector3());
        }

        List<ViewPoint> vps = TrafficHelper.getViewpointsByDataprovider();
        assertNotNull(vps);
        assertEquals(5, vps.size());
        assertViewPoint("oben5",new LocalTransform(new Vector3(0, 0, 4000),new Quaternion()), vps.get(4));

        assertNotNull(TrafficHelper.getVehicleConfigByDataprovider("loc", null));
        assertNull(TrafficHelper.getVehicleConfigByDataprovider("xx", null));
    }

    @Test
    public void testDemo() throws Exception {
        setup("traffic:tiles/Demo.xml", new HashMap<String, String>());

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertNotNull(UserSystem.getInitialUser(), "user entity");

        // "Wayland" has two graph files that should have been loaded finally (via EVENT_LOCATIONCHANGED)
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals(1, completeEvents.size(), "EVENT_LOCATIONCHANGED.size");

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
        // now 'loc' should have been loaded.
        // should start at externel overview point. For now its in vehicle. TODO: Check why 0,0,0 is correct position
        EcsTestHelper.assertTeleportComponent(player, 1 + 2, 2, new Vector3());

        EcsEntity userEntity = SystemManager.findEntities(e -> BasicTravelScene.DEFAULT_USER_NAME.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        EcsEntity locEntity = SystemManager.findEntities(e -> "loc".equals(e.getName())).get(0);
        assertNotNull(locEntity, "loc entity");

        SceneNode locNode = locEntity.getSceneNode();
        double xpos0 = locNode.getTransform().getPosition().getX();
        sceneRunner.runLimitedFrames(50);
        double xpos1 = locNode.getTransform().getPosition().getX();
        double xdiff = Math.abs(xpos0 - xpos1);
        log.debug("xdiff={}", xdiff);
        assertTrue(xdiff > 3.0);

        // 28.11.23: would be nice to test effect of 'baseTransformForVehicleOnGraph' But how, hmm?
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String tileName, HashMap<String, String> customProperties) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        properties.put("basename", tileName);
        properties.putAll(customProperties);
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties),
                /*1.10.23 provided by scene new String[]{"engine", "data", "traffic"}*/null);
    }
}
