package de.yard.threed.server;

import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.server.testutils.TestClient;
import de.yard.threed.server.testutils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static de.yard.threed.server.testutils.TestUtils.waitForClientConnected;
import static de.yard.threed.server.testutils.TestUtils.waitForClientPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WaylandTest {

    static final int INITIAL_FRAMES = 10;

    SceneServer sceneServer;

    @BeforeEach
    public void setup() throws Exception {

        HashMap<String, String> properties = new HashMap<String, String>();
        boolean wayland=true;
        if (wayland) {
            properties.put("argv.basename", "traffic:tiles/Wayland.xml");
            properties.put("argv.enableAutomove", "true");
            System.setProperty("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }

        sceneServer = TestUtils.setupServerForScene("de.yard.threed.traffic.apps.BasicTravelScene", INITIAL_FRAMES,properties);
    }

    /**
     * Currently without login/join
     */
    @Test
    public void testLaunch() throws IOException {

        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        assertEquals(1, SystemManager.findEntities((EntityFilter) null).size(), "number of entities (ball)");
        // "ball" should be synced
        //assertEquals("number of scene synced nodes",1, mpServer.getSceneRunner().getSyncedSceneNodeCount());
        //steht aber nicht drin
        //assertEquals("lok",  sceneRunner.getSyncedSceneNode(0).getName());

        SystemState.state=SystemState.STATE_READY_TO_JOIN;

        TestClient testClient = new TestClient();
        testClient.connectAndLogin();
        waitForClientConnected();
        waitForClientPacket();

        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(),5);
        assertEquals(INITIAL_FRAMES + 5, sceneServer.getSceneRunner().getFrameCount());

       /*
        // Movements also should arrive in client
        TestUtils.assertPacket(BASE_EVENT_ENTITY_CHANGE.getLabel(), new Pair[]{
                new Pair("position","*")
        }, packets);
*/
    }




}
