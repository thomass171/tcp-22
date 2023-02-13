package de.yard.threed.sceneserver;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByArgs;
import de.yard.threed.core.configuration.ConfigurationByEnv;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        try {

            for (String arg : args) {
                System.out.println("arg=" + arg);
            }
            String subdir = null;//cmd.getOptionValue("d");

            // args have more prio than env
            Configuration configuration = new ConfigurationByArgs(args).addConfiguration(new ConfigurationByEnv(), true);
            String scene = configuration.getString("scene");

            HashMap<String, String> properties = new HashMap<String, String>();
            SceneServer sceneServer = new SceneServer(subdir, scene, configuration);
            SystemManager.setSystemTracker(new LoggingSystemTracker());
            // start (blocking) render loop
            sceneServer.runServer();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }


}
