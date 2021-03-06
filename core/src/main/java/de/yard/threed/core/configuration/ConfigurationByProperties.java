package de.yard.threed.core.configuration;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleResource;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationByProperties extends Configuration {

    private Map<String, String> properties = new HashMap<String, String>();

    public ConfigurationByProperties(BundleResource br) {
        BundleData data = br.bundle.getResource(br);
        String[] rows = StringUtils.splitByLineBreak(data.getContentAsString());
        for (String row : rows) {
            if (StringUtils.contains(row, "=")) {
                String[] parts = StringUtils.split(row, "=");
                properties.put(parts[0], parts[1]);
            }
        }
    }

    @Override
    public String getPropertyString(String property) {
        return properties.get(property);
    }
}
