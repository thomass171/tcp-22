package de.yard.threed.engine.ecs;

import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.SocketMock;
import de.yard.threed.engine.testutil.EngineTestFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class SystemManagerTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testProvider() {
        SystemManager.reset();
        SystemManager.putDataProvider("n", new SimpleDataProvider());
        try {
            SystemManager.putDataProvider("n", new SimpleDataProvider());
            fail("exception expected");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }

    /**
     *
     */
    @Test
    public void testEntityStateEventPublish() {
        SystemManager.reset();

        LoggingSystemTracker systemTracker = new LoggingSystemTracker();
        // Only a server send entity states
        DefaultBusConnector mockedServerBusConnector = new DefaultBusConnector() {
            @Override
            public void pushPacket(Packet packet, String connectionId) {
            //public List<NativeSocket> getSockets(String clientId) {
              //  return Collections.singletonList(new SocketMock());
                new SocketMock().sendPacket(packet);
            }

            @Override
            public boolean isServer() {
                return true;
            }
        };
        SystemManager.setBusConnector(mockedServerBusConnector);
        SystemManager.setSystemTracker(systemTracker);
        EcsEntity entityWithoutSceneNode = new EcsEntity();

        SystemManager.sendEvent(UserSystem.buildLoggedinEvent("a", "clientid", entityWithoutSceneNode.getId(), null));
        SystemManager.update(0);

        List<Packet> packets = systemTracker.getPacketsSentToNetwork();
        assertEquals(2, packets.size());
        // 15.5.23: entity state events are sent AFTER other events. So its the latest.
        TestUtils.assertEvent( BaseEventRegistry.EVENT_ENTITYSTATE, new Pair[]{
                new Pair("entityid", "" + entityWithoutSceneNode.getId()),
                new Pair("buildername", "*")
        }, DefaultBusConnector.decodeEvent(packets.get(1)),"");
    }
}

class SimpleDataProvider implements DataProvider {

    @Override
    public Object getData(Object[] parameter) {
        return null;
    }
}
