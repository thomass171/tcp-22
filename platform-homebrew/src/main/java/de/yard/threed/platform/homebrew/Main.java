package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.core.platform.Log;
import de.yard.threed.javacommon.Setup;

import java.util.HashMap;


/**
 * Hauptprogramm fuer Platform Opengl (z.B. Android)
 * <p>
 * Warum gibt es die Scene eigentlich nicht mehr als Property? 28.10.15: Vielleicht wegen der Klasse ScenePool?
 * 22.1.16: Vielleicht weil GWT eine solche Klasse nicht per Reflection instatieeren kann?
 * <p>
 * Created by thomass on 22.01.16.
 */
public class Main {
    static Log logger;

    public static void main(String[] args) {
        HashMap<String, String> properties = Setup.setUp(args);

        //TODO allgemeingültiges cachedir
        properties.put("CACHEDIR", "/Users/thomas/Projekte/Granada/ncache");
        properties.put("BUNDLEDIR", "/Users/thomas/Projekte/Granada/bundles");

        //10.7.21 NativeSceneRunner nsr = OpenGlSceneRunner.init(properties);
        HomeBrewSceneRunner nsr = HomeBrewSceneRunner.init(properties);

        logger = Platform.getInstance().getLog(Main.class);

        logger.info("Loading OpenGL Client");
        String scene = System.getProperty("scene");
        logger.debug("Parameter:");
        logger.debug("scene=" + scene);

        try {
            if (scene == null) {
                logger.warn("No scene");
            } else {
                Scene updater = (Scene) Class.forName(scene).newInstance();
                nsr.runScene(updater);
            }

        } catch (Throwable t) {
            logger.error("Throwable occured:" + t.getMessage() + t.getStackTrace()[0]);
            // Hier kann man gut einen Breakpint setzen, um einen Stacktrace zu bekommen
            t.printStackTrace();
        }
    }
}