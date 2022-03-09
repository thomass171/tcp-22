package de.yard.threed.traffic;


import de.yard.threed.core.Event;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.traffic.testutils.TrafficTestUtils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import de.yard.threed.graph.GraphMovingComponent;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration test.
 * Putting it all together and test interaction.
 * <p>
 * Derived from FlightSystemTest/DemoSceneTest.
 * <p>
 * Created by thomass on 29.11.21.
 */
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
    @Test
    public void testWayland() throws Exception {
        setup("traffic:tiles/Wayland.xml");

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());

        // "Wayland" has two graph files that should have been loaded finally (via EVENT_LOCATIONCHANGED)
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals(1, completeEvents.size(), "EVENT_LOCATIONCHANGED.size");

        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals(2, completeEvents.size(), "TRAFFIC_EVENT_GRAPHLOADED.size");

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull(railwayGraph, "railwayGraph");

        assertFalse(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);
    }

    @Test
    public void testDemo() throws Exception {
        setup("traffic:tiles/Demo.xml");

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
        TeleportComponent tc = TeleportComponent.getTeleportComponent(player);
        // vehicle not yet loaded. So only 1 outside viewpoints.
        assertEquals(1, tc.getPointCount(), "teleport destinations");

        sceneRunner.runLimitedFrames(50);
        // now 'loc' should have been loaded.
        assertEquals(1 + 2, tc.getPointCount(), "teleport destinations");
        // should start at externel overview point. For now its in vehicle.
        assertEquals(2, tc.getIndex(), "teleport index");
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String tileName) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        properties.put("argv.basename", tileName);
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, properties, new String[]{"engine", "data", "traffic"});
    }
}
