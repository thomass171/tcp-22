package de.yard.threed.sceneserver;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByArgs;
import de.yard.threed.core.configuration.ConfigurationByEnv;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.javanative.SocketClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

@Slf4j
public class Main {
    public static void main(String[] args) {

        //Logger logger = LoggerFactory.getLogger(Main.class.getName());

        try {
            // A dedicated logger; helpful to diff client/server logs in mixed operation like tests from IDE?
            // No, better rely on log4j auto configuration. An absolute path doesn't make things easier.
            // Console output should be sufficient (no file) because of docker.
            //System.setProperty("log4j2.configurationFile", System.getProperty("user.dir") + "/src/main/resources/log4j2-server.xml");
            //System.setProperty("log4j2.debug", "true");

            log.debug("Working Directory = " + System.getProperty("user.dir"));

            for (String arg : args) {
                log.debug("arg=" + arg);
            }
            String subdir = null;//cmd.getOptionValue("d");

            // args have more prio than env
            Configuration configuration = new ConfigurationByArgs(args).addConfiguration(new ConfigurationByEnv(), true);
            String scene = configuration.getString("scene");

            HashMap<String, String> properties = new HashMap<String, String>();
            SceneServer sceneServer = new SceneServer(subdir, scene, configuration);
            if (configuration.getString("systemtracker") != null) {
                // TODO load custom class
                SystemManager.setSystemTracker(new LoggingSystemTracker());
            }
            // start (blocking) render loop
            sceneServer.runServer();


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: Exiting");
            System.exit(1);
        }
        System.exit(0);
    }


}
