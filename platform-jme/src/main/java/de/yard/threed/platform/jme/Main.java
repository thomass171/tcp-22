package de.yard.threed.platform.jme;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.Scene;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.Setup;

import java.util.HashMap;


/**
 * Main for platform JME (not OpenGL, that leads to Classpath problems when loading eg. shader)
 * <p/>
 * 20.9.23: Now with instance of main to be ready to be extended in other modules/projects.
 * <p/>
 * Created by thomass on 30.05.15.
 */
public class Main {
    static Log logger;

    public Main(String[] args) {

        Configuration configuration = ConfigurationByEnv.buildDefaultConfigurationWithArgsAndEnv(args, getInitialProperties());
        PlatformFactory platformFactory=getPlatformFactory(configuration);
        JmeSceneRunner nsr = JmeSceneRunner.init(platformFactory.createPlatform(configuration));

        logger = Platform.getInstance().getLog(Main.class);
        logger.info("Loading JME Client");
        String scene = Platform.getInstance().getConfiguration().getString("scene");
        logger.debug("Parameter:");
        logger.debug("scene=" + scene);

        // exit is done by JME when ESC is pressed?
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("shutdown detected");
            // cleanup also closes busconnector socket
            nsr.cleanup();
            System.out.println("socket closed");

           /* try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);*/
        }));

        try {
            if (scene == null) {
                logger.warn("No scene");
            } else {
                Scene updater = (Scene) Class.forName(scene).newInstance();
                nsr.runScene(updater);
            }
        } catch (Exception t) {
            // probably never reached because other thread is running the app?
            logger.error("Exception occured:" + t.getMessage() + t.getStackTrace()[0]);
            throw new RuntimeException(t);
        }
        // no exit() here. It will terminate process immediately. But main thread is in ...
    }

    /**
     * To be overridden.
     */
    protected PlatformFactory getPlatformFactory(Configuration configuration) {
        return new JmePlatformFactory();
    }

    public static void main(String[] args) {
        new Main(args);
    }

    /**
     * Ready to be overridden.
     */
    public HashMap<String, String> getInitialProperties() {
        return Setup.setUp();
    }
}
