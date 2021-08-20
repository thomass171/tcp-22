package de.yard.threed.engine.test;

import de.yard.threed.core.Color;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.core.testutil.TestUtil;

/**
 * Ein Test speziell fuer "echte" Async Funktionalitäten. Kann deshalb nicht in Unittests laufen, sondern
 * nur in (Reference)Scene.
 * Arbeitet mit einem 3D Objekt zur Ergebnisanzeige (gruen/rot).
 */
public class AsyncTest {
    Log logger = Platform.getInstance().getLog(AsyncTest.class);

    boolean success = false;

    public void runtest(Scene scene) {
        EngineHelper.addAsyncJob(new EvaluationJob(scene, this), 3000);
        testCorrupted();
    }

    /**
     *
     */
    private void testCorrupted() {
        logger.info("test corrupted: Up to 4 reported errors are intended (missing.ac,ControlLight.bin,gltf:nobin,gltf failed) . Check final green or red cube.");
        String BUNDLECORRUPTED = "corrupted";
        BundleRegistry.unregister(BUNDLECORRUPTED);
        TestUtil.assertNull("", BundleRegistry.getBundle(BUNDLECORRUPTED));
        AbstractSceneRunner.instance.loadBundle(BUNDLECORRUPTED, (r) -> {
            logger.debug("bundle load completed");

            Bundle cb = BundleRegistry.getBundle(BUNDLECORRUPTED);
            TestUtil.assertNotNull("", cb);
            BundleResource missing = new BundleResource(cb, "missing.ac");
            TestUtil.assertTrue("failure", cb.failed(missing));
            TestUtil.assertFalse("contains", cb.contains(missing));
            TestUtil.assertFalse("exists", cb.exists(missing));
            BundleResource readme = new BundleResource(cb, "Readme.txt");
            TestUtil.assertFalse("failure", cb.failed(readme));
            TestUtil.assertTrue("contains", cb.contains(readme));
            TestUtil.assertTrue("exists", cb.exists(readme));
            BundleResource controllight = new BundleResource(cb, "ControlLight.gltf");
            TestUtil.assertFalse("failure", cb.failed(controllight));
            TestUtil.assertFalse("contains", cb.contains(controllight));
            TestUtil.assertTrue("exists", cb.exists(controllight));
            BundleResource controllightbin = new BundleResource(cb, "ControlLight.bin");
            TestUtil.assertFalse("failure", cb.failed(controllightbin));
            TestUtil.assertFalse("contains", cb.contains(controllightbin));
            TestUtil.assertTrue("exists", cb.exists(controllightbin));
            EngineHelper.buildNativeModel(controllight, null, (r1) -> {
                // das fehlende bin muss jetzt aufgefallen sein.
                logger.debug("model build completed");
                TestUtil.assertFalse("failure", cb.failed(controllight));
                TestUtil.assertTrue("contains", cb.contains(controllight));
                TestUtil.assertTrue("exists", cb.exists(controllight));
                TestUtil.assertTrue("failure", cb.failed(controllightbin));
                TestUtil.assertFalse("contains", cb.contains(controllightbin));
                TestUtil.assertFalse("exists", cb.exists(controllightbin));
                success = true;
                //green/red cube sind als Indikator wichtig, nicht die message, denn die kann einfach fehlen bei Fehler.
                logger.debug("testCorrupted successfully completed.");
                

            });

        }, true);

    }

}

class EvaluationJob implements AsyncJob {
    Scene scene;
    AsyncTest asynctest;

    EvaluationJob(Scene scene, AsyncTest asynctest) {
        this.scene = scene;
        this.asynctest = asynctest;
    }

    @Override
    public String execute() {
        SceneNode cube;
        if (asynctest.success) {
            cube = ModelSamples.buildCube(2, Color.GREEN);
        } else {
            cube = ModelSamples.buildCube(2, Color.RED);
        }
        scene.addToWorld(cube);
        return null;
    }

    @Override
    public AsyncJobCallback getCallback() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}