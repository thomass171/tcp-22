package de.yard.threed.traffic;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Created by thomass on 7.11.21.
 */
public class FlatTerrainSystemTest {

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
                SystemManager.addSystem(new FlatTerrainSystem());

            }
        };

        TestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod);
        //16.12.21 AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
    }

    @Test
    public void testFlatWaylandWithoutTrafficWorldXml() throws Exception {
        //DefaultTrafficWorld.instance = null;

        startSimpleTest(/*"Desdorf"*/"tiles/Wayland.xml");

        /*// 0 because of no TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals("requests ", 0, SystemManager.getRequestCount());
        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals("completeEvents.size", 1, completeEvents.size());
        assertNull("", DefaultTrafficWorld.getInstance());
        ;*/


    }


    private void startSimpleTest(String tilename) {

        SystemManager.sendEvent(TrafficEventRegistry.buildLOCATIONCHANGED(null, /*27.3.20 projection,*/  null,
                 BundleResource.buildFromFullQualifiedString("traffic:"+tilename)));
        //too many assertEquals("events ", 1, SystemManager.getEventCount());
        EcsTestHelper.processSeconds(2);
    }
}
