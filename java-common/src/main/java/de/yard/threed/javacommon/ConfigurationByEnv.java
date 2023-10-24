package de.yard.threed.javacommon;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByArgs;
import de.yard.threed.core.configuration.ConfigurationByProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * configuration from a shell environment.
 * Must reside in a native java module, not in core.
 */
public class ConfigurationByEnv extends Configuration {

    public ConfigurationByEnv() {
    }

    @Override
    public String getPropertyString(String property) {
        return System.getenv(property);
    }

    /**
     * A ConfigurationByArgs should be added in related main classes with top prio.
     * ConfigurationByEnv is only available in Java envs!
     */
    public static Configuration buildDefaultConfigurationWithArgsAndEnv(String[] args, Map<String, String> properties) {
        return new ConfigurationByArgs(args).addConfiguration(new ConfigurationByEnv(), true).addConfiguration(
                new ConfigurationByProperties(properties), true);
    }

    /**
     * The default configuration is always a command line configuration initially.
     * 5.2.23: "byargs" needs args!! So better have a separate explicit init.
     * 6.2.23: Now build a typical default configuration with
     * 1) ByEnv (top prio), eg. for "HOSTDIR", "ADDITIONALBUNDLE". ConfigurationByEnv is only available in Java envs!
     * 2) by properties
     *
     */
    public static Configuration buildDefaultConfigurationWithEnv(Map<String, String> properties) {
        return new ConfigurationByEnv().addConfiguration(
                new ConfigurationByProperties(properties), true);
    }

    @Override
    public List<String> getProperties() {
        return new ArrayList<String>(System.getenv().keySet());
    }
}
