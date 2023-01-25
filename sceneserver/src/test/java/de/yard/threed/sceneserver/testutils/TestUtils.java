package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import de.yard.threed.sceneserver.ClientConnection;
import de.yard.threed.sceneserver.ClientListener;
import de.yard.threed.sceneserver.SceneServer;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.fail;


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

    public static void waitForClientPacket() {

        int cnt = 0;

        ClientListener clientListener = ClientListener.getInstance();
        ClientConnection clientConnection = clientListener.getClientConnections().get(0);
        while (!clientConnection.hasPacket()) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
                throw new RuntimeException("no client connected");
            }
        }
    }

    public static void assertEventPacket(EventType expectedEventType, Pair<String, String>[] expectedProperties, List<Packet> packets) {
        boolean found = false;
        for (Packet packet : packets) {
            if ((""+expectedEventType.getType()).equals(packet.getValue("event"))) {
                found = true;
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
        if (!found) {
            fail("Event " + expectedEventType + " not found in " + packets.size() + " packets");
        }
    }

    /**
     * Setup scene like it is done in main and render "initialFrames" frames.
     *
     * @param sceneclass
     * @throws Exception
     */
    public static SceneServer setupServerForScene(String sceneclass, int initialFrames, HashMap<String, String> properties) throws Exception {

        TestFactory.resetInit();
        HomeBrewSceneRunner.dropInstance();

        SceneServer sceneServer = new SceneServer("subdir", sceneclass, properties);
        HomeBrewSceneRunner sceneRunner = (HomeBrewSceneRunner) sceneServer.nsr;
        sceneRunner.renderbrake=200;

        sceneRunner.frameLimit = initialFrames;

        sceneServer.runServer();

        return sceneServer;
    }

    public static void runAdditionalFrames(HomeBrewSceneRunner sceneRunner, int frames) {
        sceneRunner.frameLimit = frames;
        sceneRunner.startRenderloop();
    }
}
