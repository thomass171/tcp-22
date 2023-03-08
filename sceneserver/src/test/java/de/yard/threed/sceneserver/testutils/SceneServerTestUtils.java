package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Server;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.testutil.PayloadHook;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.maze.MazeDataProvider;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import de.yard.threed.platform.homebrew.PlatformHomeBrew;
import de.yard.threed.sceneserver.ClientListener;
import de.yard.threed.sceneserver.SceneServer;
import de.yard.threed.sceneserver.SceneServerRenderer;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;


public class SceneServerTestUtils {

    /**
     * Not for real server!
     */
    public static void waitForClientConnected() {

        int cnt = 0;

        ClientListener clientListener = ClientListener.getInstance();

        while (clientListener.getClientConnections().size() == 0) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
               fail("no client connected");
            }
        }
    }

    public static void assertEventPacket(EventType expectedEventType, Pair<String, String>[] expectedProperties, List<Packet> packets, int expectedCount) {
        int found = 0;
        for (Packet packet : packets) {
            if (("" + expectedEventType.getType()).equals(packet.getValue("event"))) {
                found++;
                if (expectedProperties != null) {
                    for (Pair p : expectedProperties) {
                        String value = packet.getValue((String) p.getFirst());
                        if (value == null) {
                            fail("property not found:" + p.getFirst());
                        }
                        //TODO assert value
                    }
                }
            }
        }
        if (found == 0) {
            fail("Event " + expectedEventType + " not found in " + packets.size() + " packets");
        } else {
            if (expectedCount != -1 && found != expectedCount) {
                fail("Event " + expectedEventType + " found " + found + " times in " + packets.size() + " packets");
            }
        }
    }

    public static void assertEvents(EventType expectedEventType, List<Event> events, int expectedCount, PayloadHook payloadHook) {
        int found = 0;
        for (Event event : events) {
            if (event == null) {
                Platform.getInstance().getLog(SceneServerTestUtils.class).warn("event is null");
            }
            if (expectedEventType.getType() == event.getType().getType()) {
                found++;
                if (payloadHook != null) {
                    payloadHook.handle(event.getPayload());
                }
            }
        }
        if (found == 0) {
            fail("Event " + expectedEventType + " not found in " + events.size() + " events");
        } else {
            if (expectedCount != -1 && found != expectedCount) {
                fail("Event " + expectedEventType + " found " + found + " times in " + events.size() + " events");
            }
        }
    }

    /**
     * Setup scene like it is done in main and render "initialFrames" frames.
     *
     * @param sceneclass
     * @throws Exception
     */
    public static SceneServer setupServerForScene(String sceneclass, int initialFrames, HashMap<String, String> properties, int renderbrake) throws Exception {

        EngineTestFactory.resetInit();
        HomeBrewSceneRunner.dropInstance();
        MazeDataProvider.reset();
        // reset of Grid Instance not needed

        SceneServer sceneServer = new SceneServer("subdir", sceneclass, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties));
        HomeBrewSceneRunner sceneRunner = (HomeBrewSceneRunner) sceneServer.nsr;
        //why should we throttle that much? why anyway? Because some movement depends on tpf, which might be too low to detect it.
        //also maze movement needs some time
        //sceneRunner.renderbrake=200;
        sceneRunner.renderbrake = renderbrake;
        // but don't save (waste) time during tests
        ((SceneServerRenderer)((PlatformHomeBrew)Platform.getInstance()).renderer).noClientCpuSaveDelay=0;

        sceneRunner.frameLimit = initialFrames;

        sceneServer.runServer(Server.DEFAULT_BASE_PORT);

        return sceneServer;
    }

    public static void runAdditionalFrames(HomeBrewSceneRunner sceneRunner, int frames) {
        long before = sceneRunner.getFrameCount();
        sceneRunner.frameLimit = frames;
        sceneRunner.startRenderloop();
        assertEquals(before + frames, sceneRunner.getFrameCount());
    }

    /**
     * Validate all
     * Entity change events should be complete. The total number might vary.
     */
    public static void assertAllEventEntityState(List<Event> allEvents) {
        List<Event> eventlist = EcsTestHelper.filterEventList(allEvents, (e) -> e.getType().getType() == BaseEventRegistry.EVENT_ENTITYSTATE.getType());
        for (Event e : eventlist) {
            Payload payload = e.getPayload();
            Integer entityid = (Integer) payload.get("entityid");
            String buildername = (String) payload.get("buildername");
            Vector3 position = (Vector3) payload.get("position");
            Vector3 scale = (Vector3) payload.get("scale");
            Quaternion rotation = (Quaternion) payload.get("rotation");
            assertNotNull(entityid, "entityid");
            /*not contained always assertNotNull(buildername, "buildername:" + e);
            assertNotNull(position, "position");
            assertNotNull(rotation, "rotation");
            assertNotNull(scale, "scale");*/
        }
    }
}
