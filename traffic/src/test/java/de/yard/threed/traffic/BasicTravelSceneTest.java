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

import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import de.yard.threed.graph.GraphMovingComponent;

import static org.junit.Assert.*;


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
        assertEquals("EVENT_LOCATIONCHANGED.size", 1, completeEvents.size());

        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals("TRAFFIC_EVENT_GRAPHLOADED.size", 2, completeEvents.size());

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull("railwayGraph", railwayGraph);

        assertFalse(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);
    }

    @Test
    public void testDemo() throws Exception {
        setup("traffic:tiles/Demo.xml");

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertNotNull("user entity", UserSystem.getInitialUser());

        // "Wayland" has two graph files that should have been loaded finally (via EVENT_LOCATIONCHANGED)
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals("EVENT_LOCATIONCHANGED.size", 1, completeEvents.size());

        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals("TRAFFIC_EVENT_GRAPHLOADED.size", 1, completeEvents.size());

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull("railwayGraph", railwayGraph);

        assertEquals("number of entities (user)", 1, SystemManager.findEntities((EntityFilter) null).size());

        // test GraphTerrainSystem
        List<NativeSceneNode> tracksNode = SceneNode.findByName("tracks");
        assertEquals("tracksNode", 1, tracksNode.size());

        assertTrue(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);

        EcsEntity player = UserSystem.getInitialUser();
        TeleportComponent tc = TeleportComponent.getTeleportComponent(player);
        // vehicle not yet loaded. So only 3 outside viewpoints.
        assertEquals("teleport destinations", 3, tc.getPointCount());

        sceneRunner.runLimitedFrames(50);
        // now 'loc' should have been loaded.
        assertEquals("teleport destinations", 3 + 2, tc.getPointCount());
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String tileName) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        properties.put("argv.basename", tileName);
        sceneRunner = TrafficTestUtils.setupForScene(INITIAL_FRAMES, properties);
    }
}
