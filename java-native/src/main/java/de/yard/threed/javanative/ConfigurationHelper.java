package de.yard.threed.javanative;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class ConfigurationHelper {
    static Logger logger = Logger.getLogger(ConfigurationHelper.class.getName());

    /**
     * If the file cannot be loaded, this is just logged
     * 5.2.23: Should use own configuration instead of apache
     * @param configfilename
     * @return
     */
    @Deprecated
    public static Configuration loadSingleConfigFromClasspath(String configfilename) {
        Configuration config = new BaseConfiguration();
        InputStream configFile = null;
        try {
            logger.debug("trying to load configuration from classpath file " + configfilename);
            configFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(configfilename);
            config = loadSingleConfig(configFile);

        } catch (Exception e) {
            logger.error("could not read " + configfilename + ", ignoring it: " + e.getMessage());
        }
        return config;
    }

    public static Configuration loadSingleConfig(InputStream configFile) {
        Configuration config = new BaseConfiguration();
        try {
            //Configurations configs = new Configurations();
            PropertiesConfiguration fileConfig = new PropertiesConfiguration();//configs.properties(fileHandler);

            fileConfig.read(new InputStreamReader(configFile));
            //TODO  fileConfig.setListDelimiter(';');
            config = fileConfig;
        } catch (Exception e) {
            logger.error("could not read config stream, ignoring it: " + e.getMessage());
        }
        return config;
    }

    /**
     * Heads up: c2 will not override c1! So c1 has higher prio.
     *
     * @return
     */
    public static Configuration addConfiguration(Configuration c1, Configuration c2) {
        CompositeConfiguration conf = new CompositeConfiguration();
        conf.addConfiguration(c1);
        conf.addConfiguration(c2);
        return conf;
    }

    public static Configuration loadSingleConfigFromArgs(String[] args) {
        Configuration config = new PropertiesConfiguration();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.split("=");
                config.setProperty(parts[0].substring(2), (parts.length == 1) ? null : parts[1]);
            }
        }
        return config;
    }

    public static Configuration fromArgsAndClasspath(String[] args, String filename) {

        return ConfigurationHelper.addConfiguration(
                ConfigurationHelper.loadSingleConfigFromArgs(args),
                ConfigurationHelper.loadSingleConfigFromClasspath(filename));
    }
}
