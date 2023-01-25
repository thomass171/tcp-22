package de.yard.threed.sceneserver;


import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class SceneServerTest {
    SceneServer sceneServer;
    HomeBrewSceneRunner sceneRunner;

    @BeforeEach
    public void setup() throws Exception {

        TestFactory.resetInit();
        HomeBrewSceneRunner.dropInstance();
    }

    @Test
    public void testLaunch() throws Exception {
       // what to test here? Scenes are tested in dedicated tests.
    }
}
