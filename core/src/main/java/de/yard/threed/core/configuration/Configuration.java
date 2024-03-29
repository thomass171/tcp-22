package de.yard.threed.core.configuration;

import de.yard.threed.core.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Intended to replace properties map platform init?
 * 6.2.23: No longer needed as singleton since provided by platform. Instantiated now by some extending class.
 */
public abstract class Configuration {

    List<Configuration> configurationList = new ArrayList<Configuration>();

    public abstract String getPropertyString(String property);

    public abstract List<String> getPropertiesList();

    public String getString(String property) {

        String s;
        if ((s = getPropertyString(property)) != null) {
            return s;
        }
        for (Configuration configuration : configurationList) {
            if ((s = configuration.getPropertyString(property)) != null) {
                return s;
            }
        }
        return null;
    }

    public String getString(String property, String defaultValue) {
        String s;
        if ((s = getString(property)) == null) {
            s = defaultValue;
        }
        return s;
    }

    public Integer getInt(String property) {
        return getInt(property, null);
    }

    public Integer getInt(String property, Integer defaultValue) {
        String s;
        if ((s = getString(property)) == null) {
            return defaultValue;
        }
        return new Integer(Util.parseInt(s));
    }

    public Boolean getBoolean(String property) {
        return getBoolean(property, null);
    }

    public Boolean getBoolean(String property, Boolean defaultValue) {
        String s;
        if ((s = getString(property)) == null) {
            return defaultValue;
        }
        if (Util.isTrue(s)) {
            return new Boolean(true);
        }
        return new Boolean(false);
    }

    public Double getDouble(String property) {
        return getDouble(property, null);
    }

    public Double getDouble(String property, Double defaultValue) {
        String s;
        if ((s = getString(property)) == null) {
            return defaultValue;
        }
        return new Double(Util.parseDouble(s));
    }

    public Configuration addConfiguration(Configuration configuration, boolean after) {
        if (after) {
            configurationList.add(configuration);
        } else {
            configurationList.add(0, configuration);
        }
        return this;
    }

    /**
     * Also count myself.
     *
     * @return
     */
    public int size() {
        return 1 + configurationList.size();
    }

    public List<String> getProperties() {

        List<String> properties=new ArrayList<String>();
        properties.addAll(getPropertiesList());
        for (Configuration configuration : configurationList) {
            properties.addAll(configuration.getPropertiesList());
        }
        return properties;
    }

    public static Configuration buildDefaultConfigurationWithArgs(String[] args, Map<String, String> properties) {
        return new ConfigurationByArgs(args).addConfiguration(
                new ConfigurationByProperties(properties), true);
    }
}
