package de.yard.threed.core.configuration;

import de.yard.threed.core.platform.Platform;

public class ConfigurationByArgs extends Configuration {

    @Override
    public String getPropertyString(String property) {

        return  Platform.getInstance().getSystemProperty("argv." + property);
    }
}
