package de.yard.threed.traffic;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * <p>
 * Created by thomass on 30.11.21.
 */
public class TrafficSystemTest {

    SceneNode world;

    /**
     *
     */
    @BeforeEach
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                world = new SceneNode();
                SystemManager.addSystem(new TrafficSystem());
            }
        };

        EngineTestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));
        //16.12.21 AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
    }

    @Test
    public void testWaylandWithoutTrafficWorldXml() throws Exception {
        //DefaultTrafficWorld.instance = null;

        startSimpleTest("traffic:tiles/Wayland.xml");

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull(railwayGraph, "railwayGraph");

    }

    @Test
    public void testDemoWithConfigXml() throws Exception {
        //DefaultTrafficWorld.instance = null;
        //assertNull("", DefaultTrafficWorld.getInstance());

        startSimpleTest("traffic:tiles/Demo.xml");

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull(railwayGraph, "railwayGraph");


    }

    @Test
    public void testLocSpaceToFgSpace() {
        Vector3 v = new Vector3(1, 2, -3);

        TestUtils.assertVector3(new Vector3(1, 3, 2), v.multiply(FgVehicleSpace.getLocSpaceToFgSpace()));
    }

    private void startSimpleTest(String tilename) {

        SystemManager.sendEvent(TrafficEventRegistry.buildSPHERELOADED(BundleResource.buildFromFullQualifiedString(tilename), null));
        //too many assertEquals("events ", 1, SystemManager.getEventCount());
        EcsTestHelper.processSeconds(2);
    }
}
