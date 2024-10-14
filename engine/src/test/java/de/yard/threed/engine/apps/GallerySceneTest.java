package de.yard.threed.engine.apps;

import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.ConfigurationByEnv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;
import java.util.function.BooleanSupplier;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class GallerySceneTest {

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

        // wait for entities. No load error will occur for 'yy.gltf' in SimpleHeadlessPlatform!
        TestUtils.waitUntil(() -> SystemManager.findEntities(null).size() >= 8, 10000);

        List<EcsEntity> entities = SystemManager.findEntities(null);
        assertEquals(6/*objects*/ + 1/*yy.gltf*/ + 1/*user*/, entities.size());

        EcsEntity locEntity = SystemManager.findEntities(e -> "loc".equals(e.getName())).get(0);
        assertNotNull(locEntity, "loc entity");

    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(HashMap<String, String> customProperties) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.engine.apps.GalleryScene");
        properties.putAll(customProperties);
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties), null);
    }
}
