package de.yard.threed.engine.testutil;

import de.yard.threed.core.Event;
import de.yard.threed.core.Pair;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Degree;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.SceneAnimationController;
import de.yard.threed.engine.SegmentedPath;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.platform.common.AsyncHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 14.9.15: Das mit dem InputStream ist noch nicht ganz das wahre. Da muss mehr in die Plattform.
 * <p/>
 * Created by thomass on 17.07.15.
 */
public class TestHelper {
    /**
     * Ein Gearde, ein Viertelkreis und wieder eine Gerade.
     * <p>
     * Der Radius des Viertelkreises ist 2,die Bogenlaenge ist genau PI.
     * Laenge des Pfads ist 2 + PI + 3
     * bewegt sich nur in der y0 Ebene.
     * Nach hinten in den raum ist negatives z.
     *
     * @return
     */
    static public SegmentedPath buildTestPath() {
        SegmentedPath path = new SegmentedPath(new Vector3(0, 0, 0));
        path.addLine(new Vector3(0, 0, -2));
        path.addArc(new Vector3(-2, 0, -2), new Degree(90), 4);
        path.addLine(new Vector3(-5, 0, -4));
        return path;

    }

    /**
     * Alle Animations ablaufen lassen.
     */
    public static void runAnimations(/*Animation animation*/) {
        SceneAnimationController ac = SceneAnimationController.getInstance();

        while (ac.getRunningAnimationCnt() > 0) {
            //TODO Notaus
            ac.update();
        }
    }

    /**
     * Das, was sonst im Runnerhelper laeuft.
     */
    public static void processAsync() {
        /*13.12.23 not sure whether still needed. But AsyncHelper should trigger BundleLoadDelegate
        AsyncHelper.processAsync(AbstractSceneRunner.getInstance().getBundleLoader());
        List<Pair<BundleLoadDelegate, Bundle>> loadresult = Platform.getInstance().bundleLoader.processAsync();
        AbstractSceneRunner.getInstance().processDelegates(loadresult);*/
        AbstractSceneRunner.getInstance().processDelegates();

        // trigger BundleLoadDelegate.
        AsyncHelper.processAsync();
        // 15.12.23 Those extracted from AsyncHelper
        AbstractSceneRunner.getInstance().processFutures();
        AbstractSceneRunner.getInstance().processInvokeLaters();
    }

    /**
     * 10.4.21: Das muss doch an eine zentrale Stelle.
     */
    @Deprecated
    public static void cleanupAsync() {
        AsyncHelper.cleanup();
        if (AbstractSceneRunner.getInstance() != null) {
            AbstractSceneRunner.getInstance().cleanup();
        }
    }


    public static String getDataBundleString(String bundle, String resource) {
        BundleResource br = new BundleResource(BundleRegistry.getBundle(bundle), resource);
        return br.bundle.getResource(resource).getContentAsString();

    }

    public static String loadFileFromClasspath(String fileName) throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
