package de.yard.threed.core;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.DefaultLog;
import de.yard.threed.core.platform.LevelLogFactory;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestLogFactory;
import de.yard.threed.core.testutil.TestLogger;;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogFactoryTest {

    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    //no JALog available
    Log nativeLog = new TestLogger("LogFactoryTest");

    @BeforeEach
    void setup() {

    }

    @Test
    public void testDefaultLevel() {

        Map<String, String> properties = new HashMap<>();
        Configuration configuration = new ConfigurationByProperties(properties);

        //no JALog available

        LevelLogFactory logFactory = new LevelLogFactory(configuration, new TestLogFactory(), DefaultLog.LEVEL_INFO);

        DefaultLog log = (DefaultLog) logFactory.getLog(LogFactoryTest.class);
        assertEquals(DefaultLog.LEVEL_INFO, log.getLevel());
    }

    @Test
    public void testByClass() {

        Map<String, String> properties = new HashMap<>();
        properties.put("logging.level.de.yard.threed.core.LogFactoryTest", "debug");
        Configuration configuration = new ConfigurationByProperties(properties);

        LevelLogFactory logFactory = new LevelLogFactory(configuration, new TestLogFactory(), DefaultLog.LEVEL_INFO);

        DefaultLog log = (DefaultLog) logFactory.getLog(LogFactoryTest.class);
        assertEquals(DefaultLog.LEVEL_DEBUG, log.getLevel());
    }

    @Test
    public void testByPackage() {

        Map<String, String> properties = new HashMap<>();
        properties.put("logging.level.de.yard.threed", "debug");
        Configuration configuration = new ConfigurationByProperties(properties);

        LevelLogFactory logFactory = new LevelLogFactory(configuration, new TestLogFactory(), DefaultLog.LEVEL_INFO);

        DefaultLog log = (DefaultLog) logFactory.getLog(LogFactoryTest.class);
        assertEquals(DefaultLog.LEVEL_DEBUG, log.getLevel());
    }
}
