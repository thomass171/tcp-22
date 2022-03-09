package de.yard.threed.javanative;

import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigurationHelperTest {

    @Test
    public void testSampleConfig() {
        Configuration configuration = ConfigurationHelper.loadSingleConfigFromClasspath("sample-config1.txt");

        assertEquals("value20", configuration.getString("prop1"));
        assertEquals("900", configuration.getString("prop20"));
        assertNull(configuration.getString("prop1xxx"));
    }

    @Test
    public void testConfigNotFound() {
        Configuration configuration = ConfigurationHelper.loadSingleConfigFromClasspath("sXXX");

        assertNull(configuration.getString("prop1"));
        assertNull(configuration.getString("prop1xxx"));
    }

    @Test
    public void testSampleConfigWithArgs() {
        String[] args = new String[]{
                "prop2=3",
                "-prop2=4",
                "--prop2=5",
                "--prop20=905"
        };
        Configuration configuration = ConfigurationHelper.fromArgsAndClasspath(args,"sample-config1.txt");

        assertEquals("value20", configuration.getString("prop1"));
        assertNull(configuration.getString("prop1xxx"));

        assertEquals("5", configuration.getString("prop2"));
        assertEquals("905", configuration.getString("prop20"));
    }
}
