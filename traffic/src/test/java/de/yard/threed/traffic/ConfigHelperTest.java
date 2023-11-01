package de.yard.threed.traffic;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.VehicleConfig;
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

    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testLocomotive() throws Exception {

        String xmlfile = "traffic/src/main/resources/locomotive.xml";

        NativeDocument xmlDoc = Platform.getInstance().parseXml(new String(IOUtils.toByteArray(new FileInputStream(TestUtils.locatedTestFile(xmlfile)))));

        VehicleConfig vc = ConfigHelper.getVehicleDefinition(xmlDoc, "Locomotive");
        assertNotNull(vc);
        Assertions.assertEquals("loc-lowres.xml", vc.getLowresFile());

        LocalTransform localTransform = ConfigHelper.getBaseTransformForVehicleOnGraph(xmlDoc);
        assertNotNull(localTransform);
        assertEquals(0.16666667,localTransform.position.getY());
    }

}
