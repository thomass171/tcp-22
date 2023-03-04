package de.yard.threed.sceneserver;


import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SceneServerTest {
    SceneServer sceneServer;
    HomeBrewSceneRunner sceneRunner;

    @BeforeEach
    public void setup() throws Exception {

        EngineTestFactory.resetInit();
        HomeBrewSceneRunner.dropInstance();
    }

    @Test
    public void testLaunch() throws Exception {
       // what to test here? Scenes are tested in dedicated tests.
    }
}
