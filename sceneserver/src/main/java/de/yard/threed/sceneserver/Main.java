package de.yard.threed.sceneserver;

import de.yard.threed.core.Server;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByArgs;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {


    public static void main(String[] args) {

        //Logger logger = LoggerFactory.getLogger(Main.class.getName());

        try {

            // basePort is the bus connector port
            int basePort = Server.DEFAULT_BASE_PORT;
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


            SceneServer sceneServer = new SceneServer(subdir, scene, configuration);
            if (configuration.getString("systemtracker") != null) {
                // TODO load custom class
                SystemManager.setSystemTracker(new LoggingSystemTracker());
            }
            // start (blocking) render loop
            sceneServer.runServer(basePort);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: Exiting");
            System.exit(1);
        }
        System.exit(0);
    }


}
