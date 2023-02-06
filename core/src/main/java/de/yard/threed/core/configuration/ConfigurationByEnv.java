package de.yard.threed.core.configuration;

/**
 * configuration from a shell environment..
 */
public class ConfigurationByEnv extends Configuration {

    public ConfigurationByEnv() {
    }

    @Override
    public String getPropertyString(String property) {
        return System.getenv(property);
    }
}
