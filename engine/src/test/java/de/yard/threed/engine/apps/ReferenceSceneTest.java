package de.yard.threed.engine.apps;

import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.ConfigurationByEnv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Depends on external bundle 'nasa'.
 */
@Slf4j
public class ReferenceSceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 30;

    /**
     * 13.12.23 disabled because breaks GuiGridTest.
     */
    @ParameterizedTest
    @CsvSource({"false"})
    @Disabled
    public void testReferenceScene(boolean enableFPC) throws Exception {
        HashMap<String, String> customProperties = new HashMap<String, String>();
        customProperties.put("enableFPC", Boolean.toString(enableFPC));
        setup(customProperties);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());

        assertEquals(2, BundleRegistry.getBundleNames().length);
        //order is not deterministic assertEquals("data", BundleRegistry.getBundleNames()[0]);
        assertNotNull(BundleRegistry.getBundle("engine"));
        assertNotNull(BundleRegistry.getBundle("data"));

        // could be optimized for speed
        for (int i = 0; i < 50; i++) {
            sceneRunner.runLimitedFrames(1, 0.1);
            sleepMs(100);
        }
        //nasa not ending in registry??
        //TestUtils.waitUntil(BundleRegistry.getBundle("nasa")!=null);
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(HashMap<String, String> customProperties) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.engine.apps.reference.ReferenceScene");
        properties.putAll(customProperties);
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties), null);
    }
}
