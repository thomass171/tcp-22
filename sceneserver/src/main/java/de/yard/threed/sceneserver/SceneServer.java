package de.yard.threed.sceneserver;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.javanative.ConfigurationHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;

import java.util.HashMap;

/**
 * Extracted from Main for better testing.
 * <p>
 * Creates the scene. The scenerunner inits the platform.
 * <p>
 * Corresponds functionally to JMEs or homebrew {@link de.yard.threed.platform.homebrew.Main}.
 */
public class SceneServer {
    public /*Native*/ AbstractSceneRunner nsr;
    Scene updater;

    public SceneServer(String subdir, String sceneName, Configuration configuration) throws ClassNotFoundException, IllegalAccessException, InstantiationException {


        //loadConfig(subdir);

        //nsr = ServerSceneRunner.init(properties, SceneMode.forServer());
        nsr = HomeBrewSceneRunner.init(configuration, new SceneServerRenderer(),SceneMode.forServer());

        // here reflection can be used.
        try {
            Class clazz = Class.forName(sceneName);
            updater = (Scene) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            Platform.getInstance().getLog(SceneServer.class).error("class not found:" + sceneName);
        }
    }

    /**
     * Blocking endless render loop
     */
    public void runServer() {
        nsr.runScene(updater);
    }

    /*private static Configuration loadConfig(String subdir) {
        String configfilename = subdir + "/configuration.properties";
        return ConfigurationHelper.loadSingleConfigFromClasspath(configfilename);
    }*/

    /**
     * Only for testing?
     */
    public HomeBrewSceneRunner getSceneRunner() {
        return (HomeBrewSceneRunner) nsr;
    }
}
