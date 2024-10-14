package de.yard.threed.engine.apps;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.ConfigurationByEnv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class ModelPreviewSceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 30;

    /**
     *
     */
    @Test
    public void testGalleryScene() throws Exception {
        HashMap<String, String> customProperties = new HashMap<String, String>();
        setup(customProperties);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());

        assertEquals(2, BundleRegistry.getBundleNames().length);
        assertNotNull(BundleRegistry.getBundle("engine"));
        assertNotNull(BundleRegistry.getBundle("data"));

        ModelPreviewScene modelPreviewScene = (ModelPreviewScene) Scene.getCurrent();
        String majorModelPhrase = modelPreviewScene.modellist[modelPreviewScene.major];
        String expectedName = StringUtils.substringAfter(majorModelPhrase, ":");
        expectedName = StringUtils.substringBeforeLast(expectedName, ";");
        log.debug("expectedName={}", expectedName);
        assertEquals(1, SceneNode.findByName(expectedName).size());

    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(HashMap<String, String> customProperties) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.engine.apps.ModelPreviewScene");
        properties.putAll(customProperties);
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties), null);
    }
}
