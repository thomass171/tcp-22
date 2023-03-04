package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.Setup;

import java.util.HashMap;


/**
 * Main for platform Homebrew (eg. Android)
 * <p>
 * Created by thomass on 22.01.16.
 */
public class Main {
    static Log logger;

    public static void main(String[] args) {
        HashMap<String, String> properties = Setup.setUp();

        //10.7.21 NativeSceneRunner nsr = OpenGlSceneRunner.init(properties);
        ;
        HomeBrewSceneRunner nsr = HomeBrewSceneRunner.init(ConfigurationByEnv.buildDefaultConfigurationWithArgsAndEnv(args, properties), new OpenGlRenderer(/*PlatformHomeBrew/*OpenGlContext* /.getGlContext()*/), SceneMode.forMonolith());

        logger = Platform.getInstance().getLog(Main.class);

        logger.info("Loading OpenGL Client");
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

        } catch (Throwable t) {
            logger.error("Throwable occured:" + t.getMessage() + t.getStackTrace()[0]);
            // Hier kann man gut einen Breakpint setzen, um einen Stacktrace zu bekommen
            t.printStackTrace();
        }
    }
}