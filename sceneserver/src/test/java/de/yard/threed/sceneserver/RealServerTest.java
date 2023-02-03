package de.yard.threed.sceneserver;

import de.yard.threed.core.Point;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.MazeUtils;
import de.yard.threed.maze.MoverComponent;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.maze.RequestRegistry.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests with a real standalone scene server.
 */
@Slf4j
public class RealServerTest {



    public void setup(String gridname) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        //Use a deterministic grid without bot/monster automovement
        //properties.put("argv.initialMaze", "maze/Maze-P-Simple.txt");
        properties.put("argv.initialMaze", gridname);

        List<String> args = new ArrayList<>();
        args.add("-s");
        args.add("de.yard.threed.maze.MazeScene");
        TestUtils.execJavaProcess(de.yard.threed.sceneserver.Main.class,new ArrayList(), args);


    }


    //@Test
    public void testMultiUser() throws Exception {
        setup("maze/Area15x10.txt");
        log.debug("testMultiUser");

        //SystemState.state = SystemState.STATE_READY_TO_JOIN;

       /* TestClient testClient0 = new TestClient(TestClient.USER_NAME0);
        testClient0.assertConnectAndLogin(sceneServer);

        TestClient testClient1 = new TestClient(TestClient.USER_NAME1);
        testClient1.assertConnectAndLogin(sceneServer);

        // EVENT_MAZE_LOADED should have been resent after login, but only to the new client
        testClient0.assertEventMazeLoaded("maze/Area15x10.txt");
        testClient1.assertEventMazeLoaded("maze/Area15x10.txt");*/

    }
}
