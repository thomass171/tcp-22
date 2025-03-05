package de.yard.threed.engine;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.CharsetException;
import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.loader.DefaultMaterialFactory;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.AdvancedHeadlessPlatformFactory;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.TestHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * See also PlatformModelLoaderTest
 * Also for ModelLoader.
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

                SceneNode node = ModelFactory.asyncModelLoad(new ResourceLoaderFromBundle(resource));
                destinationNodes.add(node);
                modelLaunched.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return modelLaunched.getValue() && destinationNodes.get(0).findNodeByName("models/loc.gltf").size() > 0;
        }, 10000);

        assertEquals(1, destinationNodes.size());
        //rootnodename is full name instead of just egkk_tower
        log.debug(destinationNodes.get(0).dump("  ", 0));
        SceneNode modelNode = destinationNodes.get(0).findNodeByName("models/loc.gltf").get(0);
        assertNotNull(modelNode);

        List<String> modelsBuilt = AbstractSceneRunner.getInstance().systemTracker.getModelsBuilt();
        assertEquals(1, modelsBuilt.size());
        assertEquals("data:models/loc.gltf", modelsBuilt.get(0));
    }

    /**
     * Derived from runtime AsyncTest.
     */
    @Test
    public void testCorrupted() throws Exception {
        String BUNDLECORRUPTED = "corrupted";
        BundleRegistry.unregister(BUNDLECORRUPTED);
        assertNull(BundleRegistry.getBundle(BUNDLECORRUPTED));

        BooleanHolder success = new BooleanHolder(false);

        AbstractSceneRunner.instance.loadBundle(BUNDLECORRUPTED, (r) -> {
            log.debug("bundle " + BUNDLECORRUPTED + " load completed");

            Bundle cb = BundleRegistry.getBundle(BUNDLECORRUPTED);
            assertNotNull(cb);
            BundleResource missing = new BundleResource(cb, "missing.ac");
            assertTrue(cb.failed(missing));
            assertFalse(cb.contains(missing));
            assertFalse(cb.exists(missing));
            BundleResource readme = new BundleResource(cb, "Readme.txt");
            assertFalse(cb.failed(readme));
            assertTrue(cb.contains(readme));
            assertTrue(cb.exists(readme));
            BundleResource controllight = new BundleResource(cb, "ControlLight.gltf");
            assertFalse(cb.failed(controllight));
            // 15.12.23: After removing 'delayed' three value changed
            assertTrue/*False*/(cb.contains(controllight));
            assertTrue(cb.exists(controllight));
            BundleResource controllightbin = new BundleResource(cb, "ControlLight.bin");
            assertTrue/*False*/(cb.failed(controllightbin));
            assertFalse(cb.contains(controllightbin));
            assertFalse/*True*/(cb.exists(controllightbin));
            BundleResource nonutf8 = new BundleResource(cb, "non-utf8.txt");
            assertFalse(cb.failed(nonutf8));
            assertTrue(cb.contains(nonutf8));
            assertTrue(cb.exists(nonutf8));
            boolean gotException = false;
            try {
                cb.getResource(nonutf8).getContentAsString();
            } catch (CharsetException e) {
                gotException = true;
            }
            assertTrue(gotException);

            Platform.getInstance().buildNativeModelPlain(new ResourceLoaderFromBundle(controllight), null, (r1) -> {
                // missing bin should have been realized
                log.debug("model build completed. node=" + r1.getNode());
                assertNull(r1.getNode());
                success.setValue(true);
            }, 0);
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return success.getValue();
        }, 10000);

    }

    @Test
    public void testModelLoader() throws Exception {
        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

        BundleResource bundleResource = new BundleResource(BundleRegistry.getBundle("engine"), "cesiumbox/BoxTextured.gltf");

        List<NativeSceneNode> builtNodes = new ArrayList<>();
        ModelLoader.buildModel(new ResourceLoaderFromBundle(bundleResource), null, 0, new ModelBuildDelegate() {
            @Override
            public void modelBuilt(BuildResult result) {
                if (result.getNode() == null) {
                    fail("no node");
                }
                builtNodes.add(result.getNode());
            }
        }, new DefaultMaterialFactory());

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return builtNodes.size() > 0;
        }, 10000);

        assertEquals(1, builtNodes.size());
        assertTrue(Texture.hasTexture("CesiumLogoFlat.png"), "CesiumLogoFlat.texture");

        SceneNode cesiumBox = new SceneNode(builtNodes.get(0));
    }

    /**
     * Maybe not the best location here, but which is better?
     */
    @Test
    public void testTexturedCube() {
        EngineTestFactory.loadBundleAndWait("data");
        SceneNode n = ModelSamples.buildTexturedCube(2, new DefaultMaterialFactory());
        Assertions.assertEquals(0, PortableModelBuilder.dummyMaterialReasons.size(), "dummymaterialused");
        EngineTestUtils.processAsync();
        EngineTestUtils.processAsync();
        Assertions.assertEquals(1, Texture.texturePool.size(), "texturePoolsize");
        Assertions.assertTrue(Texture.texturePool.hasTexture("texturedcube-atlas.jpg"), "texture");
        Material material = n.getMesh().getMaterial();
        assertNotNull(material);

    }

}
