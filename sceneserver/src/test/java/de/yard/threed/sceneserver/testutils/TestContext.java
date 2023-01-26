package de.yard.threed.sceneserver.testutils;

import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.maze.MazeUtils;
import de.yard.threed.sceneserver.SceneServer;



public class TestContext {
    public SceneServer sceneServer;
    public TestClient testClient;

    public TestContext(SceneServer sceneServer, TestClient testClient) {
        this.sceneServer = sceneServer;
        this.testClient = testClient;
    }

    /**
     * difficult to make this reliable.
     *
     * @param request
     */
    public void sendRequestAndWait(Request request) {
        while (MazeUtils.isAnyMoving() != null) {
            TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 1);
        }

        testClient.sendRequest(request);

        do {
            TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 3);
        } while (SystemManager.getRequestCount() > 0);

        while (MazeUtils.isAnyMoving() != null) {
            TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 3);
        }
    }

}
