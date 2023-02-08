package de.yard.threed.platform.jme;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.javacommon.Setup;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;


/**
 * Main for platform JME (not OpenGL, that leads to Classpath problems when loading eg. shader)
 * <p/>
 * Warum gibt es die Scene eigentlich nicht mehr als Property? 28.10.15: Vielleicht wegen der Klasse ScenePool?
 * 22.1.16: Vielleicht weil GWT eine solche Klasse nicht per Reflection instatieeren kann?
 * <p/>
 * Created by thomass on 30.05.15.
 */
public class Main {
    static Log logger;

    public static void main(String[] args) {

        JmeSceneRunner nsr = JmeSceneRunner.init(Configuration.buildDefaultConfigurationWithArgsAndEnv(args, Setup.setUp()));

        logger = Platform.getInstance().getLog(Main.class);
        logger.info("Loading JME Client");
        String scene = Platform.getInstance().getConfiguration().getString("scene");
        logger.debug("Parameter:");
        logger.debug("scene=" + scene);

        try {
            if (scene == null) {
                logger.warn("No scene");
            } else {
                Scene updater = (Scene) Class.forName(scene).newInstance();
                nsr.runScene(updater);

            }
        } catch (Exception t) {
            logger.error("Exception occured:" + t.getMessage() + t.getStackTrace()[0]);
            throw new RuntimeException(t);
        }
    }


    private static void encodeBase64(String path, String filename) {
        try {
            byte[] buf = FileUtils.readFileToByteArray(new File(path + "/" + filename));
            buf = Base64.encodeBase64(buf);
            File outfile = new File(path + "/" + filename + ".b64");
            FileUtils.writeByteArrayToFile(outfile, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

/**
 * Nurmal ein Versuch. Muss im Prinzip in die Platform.
 */
class GaCo extends Thread {
    @Override
    public void run() {
        while (true) {
            System.gc();
            try {
                sleep(200);
            } catch (InterruptedException e) {

            }
        }
    }
}