package de.yard.threed.javacommon.commondesktop;

import de.yard.threed.core.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static de.yard.threed.javacommon.ConfigurationByEnv.buildDefaultConfigurationWithEnv;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationTest {

    @Test
    void testDefaultConfigurationWithEnv() {

        Map<String, String> properties = new HashMap<>();
        properties.put("HOSTDIR", "xx");
        Configuration configuration = buildDefaultConfigurationWithEnv(properties);

        String hostdir = configuration.getString("HOSTDIR");
        // env should have prio over properties
        assertTrue(hostdir.contains("tcp-22"), hostdir);
    }
}
