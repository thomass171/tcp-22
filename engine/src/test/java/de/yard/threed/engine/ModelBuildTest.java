package de.yard.threed.engine;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.AdvancedHeadlessPlatformFactory;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@Slf4j
public class ModelBuildTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new AdvancedHeadlessPlatformFactory());


    @Test
    public void testLoadModelInBundleLoadDelegate() throws Exception {

        String bundleName = "data";
        if (BundleRegistry.getBundle(bundleName) != null) {
            BundleRegistry.unregister(bundleName);
        }

        BooleanHolder modelLaunched = new BooleanHolder();
        // list just as holder
        List<SceneNode> destinationNodes = new ArrayList<>();

        AbstractSceneRunner.getInstance().loadBundle(bundleName, new BundleLoadDelegate() {
            @Override
            public void bundleLoad(Bundle bundle) {
                // This gltf is from earlier bundle build.
                assertNotNull(BundleRegistry.getBundle(bundleName));
                BundleResource resource = new BundleResource(BundleRegistry.getBundle(bundleName), "models/loc.gltf");

                log.debug("Starting loader");

                SceneNode node = ModelFactory.asyncModelLoad(resource);
                destinationNodes.add(node);
                modelLaunched.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            TestHelper.processAsync();
            return modelLaunched.getValue();
        }, 10000);

        // modelbuildvalues not available for checking
        TestHelper.processAsync();
        assertEquals(1, destinationNodes.size());
        //rootnodename is full name instead of just egkk_tower
        NativeSceneNode modelNode = destinationNodes.get(0).findNodeByName("models/loc.gltf", true).get(0);
        assertNotNull(modelNode);
    }

}
