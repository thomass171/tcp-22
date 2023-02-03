package de.yard.threed.core.configuration;

import de.yard.threed.core.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Intended to replace properties map platform init?
 */
public class Configuration {

    List<Configuration> configurationList = new ArrayList<Configuration>();
    static Configuration defaultConfiguration = null;

    public String getPropertyString(String property) {
        return null;
    }

    public String getString(String property) {
        for (Configuration configuration : configurationList) {
            String s;
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

    Integer getInt(String property, Integer defaultValue) {
        String s;
        if ((s = getString(property)) == null) {
            return defaultValue;
        }
        return new Integer(Util.parseInt(s));
    }

    public Boolean getBoolean(String property) {
        return getBoolean(property, null);
    }

    Boolean getBoolean(String property, Boolean defaultValue) {
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

    Double getDouble(String property, Double defaultValue) {
        String s;
        if ((s = getString(property)) == null) {
            return defaultValue;
        }
        return new Double(Util.parseDouble(s));
    }

    public void addConfiguration(Configuration configuration, boolean after) {
        if (after) {
            configurationList.add(configuration);
        } else {
            configurationList.add(0, configuration);
        }
    }

    public int size() {
        return configurationList.size();
    }

    /**
     * The default configuration is always a command line configuration initially.
     * 5.2.23: "byargs" needs args!! So better have a separate explicit init.
     */
    public static Configuration getDefaultConfiguration() {
        if (defaultConfiguration == null) {
            throw new RuntimeException("not inited");
            //defaultConfiguration = new Configuration();
            //defaultConfiguration.addConfiguration(new ConfigurationByArgs(), false);
        }
        return defaultConfiguration;
    }

    public static Configuration init() {
        if (defaultConfiguration != null) {
            throw new RuntimeException("already inited");
        }
        defaultConfiguration = new Configuration();
        return defaultConfiguration;
    }

    /**
     * Needed for testing.
     */
    public static void reset() {
        defaultConfiguration = null;
    }
}
