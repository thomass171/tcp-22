package de.yard.threed.platform.jme;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.*;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.test.MainTest;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlatformJMETest {

    PlatformJme platformJme;

    @BeforeEach
    void setup() {
        // EngineTestFactory cannot be used because it inits a SceneRunnerForTesting but no JmeSceneRunner
        // Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"data", "engine"}, new JmePlatformFactory());
        EngineTestFactory.resetInit();

        Configuration configuration = ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>());
        PlatformFactory platformFactory = new JmePlatformFactory();
        platformFactory.createPlatform(configuration);
        JmeSceneRunner nsr = JmeSceneRunner.getInstance();

        //TODO tricky JmeResourceManager rm = new JmeResourceManager(assetManager);
        //((PlatformJme) Platform.getInstance()).postInit(rm);

        nsr.initAbstract(null, new Scene() {
            @Override
            public void init(SceneMode sceneMode) {

            }

            @Override
            public void update() {

            }
        });
        nsr.enterInitChain( new String[] {"data", "engine"});
        platformJme = (PlatformJme) Platform.getInstance();
        nsr.scsettings = new Settings();
    }

    @Test
    public void testPlatform() {
        MainTest.runTest();
    }

    @Test
    public void testTextures() {

        Texture river = Texture.buildBundleTexture("data", "images/river.jpg");
        assertNotNull(river);
    }

   /*TODO needs assetmanager @Test
    public void testAudio() {

        URL soundURL = new URL("https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool/fgdatabasic",new ResourcePath("Sounds"),"jet.wav");
        AudioClip jetAudioClip = new AudioClip(soundURL);
        Audio jetAudio = Audio.buildAudio(jetAudioClip);
        assertNotNull(jetAudio);
    }*/

}
