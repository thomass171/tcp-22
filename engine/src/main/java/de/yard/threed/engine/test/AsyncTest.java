package de.yard.threed.engine.test;

import de.yard.threed.core.CharsetException;
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
import de.yard.threed.core.testutil.RuntimeTestUtil;

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
        RuntimeTestUtil.assertNull("", BundleRegistry.getBundle(BUNDLECORRUPTED));
        AbstractSceneRunner.instance.loadBundle(BUNDLECORRUPTED, (r) -> {
            logger.debug("bundle " + BUNDLECORRUPTED + " load completed");

            Bundle cb = BundleRegistry.getBundle(BUNDLECORRUPTED);
            RuntimeTestUtil.assertNotNull("", cb);
            BundleResource missing = new BundleResource(cb, "missing.ac");
            RuntimeTestUtil.assertTrue("failure", cb.failed(missing));
            RuntimeTestUtil.assertFalse("contains", cb.contains(missing));
            RuntimeTestUtil.assertFalse("exists", cb.exists(missing));
            BundleResource readme = new BundleResource(cb, "Readme.txt");
            RuntimeTestUtil.assertFalse("failure", cb.failed(readme));
            RuntimeTestUtil.assertTrue("contains", cb.contains(readme));
            RuntimeTestUtil.assertTrue("exists", cb.exists(readme));
            BundleResource controllight = new BundleResource(cb, "ControlLight.gltf");
            RuntimeTestUtil.assertFalse("failure", cb.failed(controllight));
            // 15.12.23: After removing 'delayed' three value changed
            RuntimeTestUtil.assertTrue/*False*/("contains", cb.contains(controllight));
            RuntimeTestUtil.assertTrue("exists", cb.exists(controllight));
            BundleResource controllightbin = new BundleResource(cb, "ControlLight.bin");
            RuntimeTestUtil.assertTrue/*False*/("failure", cb.failed(controllightbin));
            RuntimeTestUtil.assertFalse("contains", cb.contains(controllightbin));
            RuntimeTestUtil.assertFalse/*True*/("exists", cb.exists(controllightbin));
            BundleResource nonutf8 = new BundleResource(cb, "non-utf8.txt");
            RuntimeTestUtil.assertFalse("failure", cb.failed(nonutf8));
            RuntimeTestUtil.assertTrue("contains", cb.contains(nonutf8));
            RuntimeTestUtil.assertTrue("exists", cb.exists(nonutf8));
            boolean gotException = false;
            try {
                cb.getResource(nonutf8).getContentAsString();
            } catch (CharsetException e) {
                gotException=true;
            }
            RuntimeTestUtil.assertTrue("gotException", gotException);

            EngineHelper.buildNativeModel(controllight, null, (r1) -> {
                // das fehlende bin muss jetzt aufgefallen sein.
                logger.debug("model build completed");
                RuntimeTestUtil.assertFalse("failure", cb.failed(controllight));
                RuntimeTestUtil.assertTrue("contains", cb.contains(controllight));
                RuntimeTestUtil.assertTrue("exists", cb.exists(controllight));
                RuntimeTestUtil.assertTrue("failure", cb.failed(controllightbin));
                RuntimeTestUtil.assertFalse("contains", cb.contains(controllightbin));
                RuntimeTestUtil.assertFalse("exists", cb.exists(controllightbin));
                success = true;
                //green/red cube sind als Indikator wichtig, nicht die message, denn die kann einfach fehlen bei Fehler.
                logger.debug("testCorrupted successfully completed.");
            });
        });

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