package de.yard.threed.core;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByArgs;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigurationTest {

    String[] args = new String[]{
            "prop2=3",
            "-prop2=4",
            "--prop2=5",
            "--scene=major",
            "--prop20=905"
    };

    @Test
    public void testSampleConfigWithArgs() {

        Configuration configuration = new ConfigurationByArgs(args);

        assertNull(configuration.getString("prop1xxx"));

        assertEquals("5", configuration.getString("prop2"));
        assertEquals("905", configuration.getString("prop20"));
    }

    @Test
    public void testSampleConfig() {
        Map<String,String> properties = new HashMap<>();
        properties.put("scene","minor");
        Configuration configuration = Configuration.buildDefaultConfigurationWithArgsAndEnv(args, properties);

        assertEquals("major", configuration.getString("scene"));
    }
}
