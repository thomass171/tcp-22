package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PayloadHook;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.maze.MazeDataProvider;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import de.yard.threed.sceneserver.ClientConnection;
import de.yard.threed.sceneserver.ClientListener;
import de.yard.threed.sceneserver.SceneServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;


public class TestUtils {

    public static void waitForClientConnected() {

        int cnt = 0;

        ClientListener clientListener = ClientListener.getInstance();

        while (clientListener.getClientConnections().size() == 0) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
                throw new RuntimeException("no client connected");
            }
        }
    }

    public static void waitForClientPacketAvailableInServer() {

        int cnt = 0;

        ClientListener clientListener = ClientListener.getInstance();
        ClientConnection clientConnection = clientListener.getClientConnections().get(0);
        while (!clientConnection.hasPacket()) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
                throw new RuntimeException("no packet after 5 seconds. no client connected?");
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

    public static void assertEvent(EventType expectedEventType, List<Event> events, int expectedCount, PayloadHook payloadHook) {
        int found = 0;
        for (Event event : events) {
            if (event == null) {
                Platform.getInstance().getLog(TestUtils.class).warn("event is null");
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

        TestFactory.resetInit();
        HomeBrewSceneRunner.dropInstance();
        MazeDataProvider.reset();

        SceneServer sceneServer = new SceneServer("subdir", sceneclass, Configuration.buildDefaultConfigurationWithEnv(properties));
        HomeBrewSceneRunner sceneRunner = (HomeBrewSceneRunner) sceneServer.nsr;
        //why should we throttle that much? why anyway? Because some movement depends on tpf, which might be too low to detect it.
        //also maze movement needs some time
        //sceneRunner.renderbrake=200;
        sceneRunner.renderbrake = renderbrake;

        sceneRunner.frameLimit = initialFrames;

        sceneServer.runServer();

        return sceneServer;
    }

    public static void runAdditionalFrames(HomeBrewSceneRunner sceneRunner, int frames) {
        long before = sceneRunner.getFrameCount();
        sceneRunner.frameLimit = frames;
        sceneRunner.startRenderloop();
        assertEquals(before + frames, sceneRunner.getFrameCount());
    }


}
