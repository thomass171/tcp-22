package de.yard.threed.sceneserver;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class SceneServerTest {
    SceneServer sceneServer;
    ServerSceneRunner sceneRunner;

    @BeforeEach
    public void setup() throws Exception {

        ServerSceneRunner.dropInstance();
    }

    @Test
    public void testLaunch() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("argv.basename", "traffic:tiles/Demo.xml");
        properties.put("argv.enableAutomove", "true");
        System.setProperty("scene", "de.yard.threed.traffic.apps.BasicTravelScene");

        SceneServer sceneServer = new SceneServer("subdir", "de.yard.threed.traffic.apps.BasicTravelScene", properties);
        sceneRunner = (ServerSceneRunner) sceneServer.nsr;

        sceneRunner.frameLimit = 300;
        sceneServer.runServer();
    }
}
