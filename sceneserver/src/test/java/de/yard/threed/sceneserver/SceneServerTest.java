package de.yard.threed.sceneserver;


import de.yard.threed.engine.testutil.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class SceneServerTest {
    SceneServer sceneServer;
    ServerSceneRunner sceneRunner;

    @BeforeEach
    public void setup() throws Exception {

        TestFactory.resetInit();
        ServerSceneRunner.dropInstance();
    }

    @Test
    public void testLaunch() throws Exception {
       // what to test here? Scenes are tested in dedicated tests.
    }
}
