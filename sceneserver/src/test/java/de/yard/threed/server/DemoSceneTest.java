package de.yard.threed.server;

import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.server.testutils.TestClient;
import de.yard.threed.server.testutils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.engine.BaseEventRegistry.BASE_EVENT_ENTITY_CHANGE;
import static de.yard.threed.server.testutils.TestUtils.waitForClientConnected;
import static de.yard.threed.server.testutils.TestUtils.waitForClientPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for traffic "Demo.xml" scene (traffic scene definition "traffic:tiles/Demo.xml").
 */
public class DemoSceneTest {

    static final int INITIAL_FRAMES = 10;

    SceneServer sceneServer;

    @BeforeEach
    public void setup() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("argv.basename", "traffic:tiles/Demo.xml");
        properties.put("argv.enableAutomove", "true");
        System.setProperty("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        sceneServer = TestUtils.setupServerForScene("de.yard.threed.traffic.apps.BasicTravelScene", INITIAL_FRAMES, properties);
    }

    @Test
    public void testLaunch() throws IOException {
        //?assertRunningThreads(); läuft docvh nur der clientlistener?
        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        assertEquals(1, SystemManager.findEntities((EntityFilter) null).size(), "number of entities (ball)");
        // "ball" should be synced
        //assertEquals("number of scene synced nodes",1, mpServer.getSceneRunner().getSyncedSceneNodeCount());
        //steht aber nicht drin
        //assertEquals("lok",  sceneRunner.getSyncedSceneNode(0).getName());

        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient = new TestClient();
        testClient.connectAndLogin();
        waitForClientConnected();
        waitForClientPacket();

        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 5);
        assertEquals(INITIAL_FRAMES + 5, sceneServer.getSceneRunner().getFrameCount());

        // Check login succeeded.
        // possible race condition with movements arriving before login/joined event
        List<Packet> packets = testClient.getAllPackets();
        assertTrue(packets.size() > 0);
        TestUtils.assertPacket(UserSystem.USER_EVENT_LOGGEDIN.getLabel(), null, packets);

        // join happened implicitly, so Avatar should exist.
        TestUtils.assertPacket(UserSystem.USER_EVENT_JOINED.getLabel(), null, packets);
        assertEquals(1 + 1, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (ball+avatar)");

        // Movements also should arrive in client
        TestUtils.assertPacket(BASE_EVENT_ENTITY_CHANGE.getLabel(), new Pair[]{
                new Pair("position", "*")
        }, packets);

    }


}
