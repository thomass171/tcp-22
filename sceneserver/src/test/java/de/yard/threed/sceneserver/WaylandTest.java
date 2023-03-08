package de.yard.threed.sceneserver;

import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.SceneServerTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static de.yard.threed.sceneserver.testutils.SceneServerTestUtils.waitForClientConnected;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WaylandTest {

    static final int INITIAL_FRAMES = 10;

    SceneServer sceneServer;

    @BeforeEach
    public void setup() throws Exception {

        HashMap<String, String> properties = new HashMap<String, String>();
        boolean wayland=true;
        if (wayland) {
            properties.put("basename", "traffic:tiles/Wayland.xml");
            properties.put("enableAutomove", "true");
            System.setProperty("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }

        sceneServer = SceneServerTestUtils.setupServerForScene("de.yard.threed.traffic.apps.BasicTravelScene", INITIAL_FRAMES,properties,200);
    }

    @AfterEach
    public void tearDown(){
        sceneServer.stopServer();
    }

    /**
     * Currently without login/join
     */
    @Test
    public void testLaunch() throws IOException {

        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        // no user/avatar and graph yet.
        assertEquals(0, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");

        SystemState.state=SystemState.STATE_READY_TO_JOIN;

        TestClient testClient = new TestClient("carl");
        testClient.connectAndLogin();
        waitForClientConnected();

        SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(),5);
        assertEquals(INITIAL_FRAMES + 5, sceneServer.getSceneRunner().getFrameCount());

       /*
        // Movements also should arrive in client
        TestUtils.assertPacket(BASE_EVENT_ENTITY_CHANGE.getLabel(), new Pair[]{
                new Pair("position","*")
        }, packets);
*/
    }




}
