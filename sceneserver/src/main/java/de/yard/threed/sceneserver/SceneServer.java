package de.yard.threed.sceneserver;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.javanative.ConfigurationHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;

import java.util.HashMap;

/**
 * Extracted from Main for better testing.
 * <p>
 * Creates the scene. The scenerunner inits the platform.
 * <p>
 * Corresponds functionally to JMEs or homebrew {@link de.yard.threed.platform.homebrew.Main}.
 */
@Slf4j
public class SceneServer {
    public /*Native*/ AbstractSceneRunner nsr;
    Scene updater;
    Server jettyServer;

    public SceneServer(String subdir, String sceneName, Configuration configuration) throws ClassNotFoundException, IllegalAccessException, InstantiationException {


        //loadConfig(subdir);

        //nsr = ServerSceneRunner.init(properties, SceneMode.forServer());
        nsr = HomeBrewSceneRunner.init(configuration, new SceneServerRenderer(), SceneMode.forServer());

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
    public void runServer(int port) throws Exception {

        log.info("Starting SceneServer on base port {}", port);
        // exceptions while starting jetty will be catched/handled by main catch.
        jettyServer = JettyServer.startJettyServer(port + 1);

        // Even though the server is currently starting, allow clients to connect (async/MT).
        // Network events will be queued anyway. Doing initializations here is better then doing
        // it deep in SceneServerRenderer.init().
        //28.2.23 ClientListener.dropInstance();

        ClientListener.init("", port);
        ClientListener clientListener = ClientListener.getInstance();
        clientListener.start();

        SystemManager.setBusConnector(new SceneServerBusConnector());
        nsr.runScene(updater);
    }

    public void stopServer(){
        try {
            //log.debug("Stopping jetty");
            jettyServer.stop();
            // need to wait?
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientListener.dropInstance();
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
