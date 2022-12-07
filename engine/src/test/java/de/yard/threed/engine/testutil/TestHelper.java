package de.yard.threed.engine.testutil;

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
        AsyncHelper.processAsync(AbstractSceneRunner.getInstance().getBundleLoader());
        List<Pair<BundleLoadDelegate, Bundle>> loadresult = Platform.getInstance().bundleLoader.processAsync();
        AbstractSceneRunner.getInstance().processDelegates(loadresult);
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

    /**
     * Dummy Bundle bauen
     */
    public static Bundle buildDummyBundleModel777() {
        Bundle my777 = new Bundle("My-777", "Models/777-200.ac\n", false);
        my777.addResource("Models/777-200.ac", new BundleData(""));
        return my777;
    }

    public static Bundle buildDummyBundleModelbasic() {
        Bundle fgdatabasicmodel = new Bundle("fgdatabasicmodel", "AI/Aircraft/737/Models/B737-300.ac\nAI/Aircraft/737/737-AirBerlin.xml\n", false);
        fgdatabasicmodel.addResource("AI/Aircraft/737/Models/B737-300.ac", new BundleData(""));
        fgdatabasicmodel.addResource("AI/Aircraft/737/737-AirBerlin.xml", new BundleData(""));
        return fgdatabasicmodel;
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

    /*12.2.16: jetzt in Platform public static InputStream getFileStream(String ressource) throws FileNotFoundException {
        final java.io.InputStream ins = new FileInputStream(ressource);//ClassLoader.getFileStream(ressource);
        return new InputStream() {
            @Override
            public int read() {
                try {
                    return ins.read();
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }
            }

            @Override
            public void read(byte[] buf, int size) {
                try {
                    ins.read(buf, 0, size);
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }

            }
        };

    }

    public static byte[] readFileStream(final InputStream ins) {
        byte[] bytebuf;
        try {

            bytebuf = IOUtils.toByteArray(new java.io.InputStream() {
                @Override
                public int read() throws IOException {
                    return ins.read();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("io", e);
        }
        return bytebuf;
    }*/
}
