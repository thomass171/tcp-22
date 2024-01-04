package de.yard.threed.traffic;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.VehicleDefinition;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Date: 30.10.23
 */
public class ConfigHelperTest {

    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    //moved to TrafficConfigTest

}
