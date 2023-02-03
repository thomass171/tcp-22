package de.yard.threed.sceneserver;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByArgs;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        try {

            for (String arg : args) {
                System.out.println("arg=" + arg);
            }
            String sceneName = args[args.length - 1];
            String subdir = null;//cmd.getOptionValue("d");

            Configuration configuration = Configuration.init();
            configuration.addConfiguration(new ConfigurationByArgs(args), false);

            HashMap<String, String> properties = new HashMap<String, String>();
            SceneServer sceneServer = new SceneServer(subdir, sceneName, properties);
            // start (blocking) render loop
            sceneServer.runServer();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }


}
