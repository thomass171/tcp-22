package de.yard.threed.server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        try {

            Options options = new Options();
            options.addOption("s", "scene", true, "scene class");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String sceneName = cmd.getOptionValue("s");
            String subdir = cmd.getOptionValue("d");
            Configuration customconfig = new BaseConfiguration();

            HashMap<String, String> properties = new HashMap<String, String>();
            SceneServer sceneServer = new SceneServer(subdir, sceneName,properties);
            // start (blocking) render loop
            sceneServer.runServer();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }


}
