package de.yard.threed.engine;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Also for packets
 */
public class BusConnectorTest {
    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testLoginRequest(boolean withPacketSerialization) {
        Packet packet = DefaultBusConnector.encodeRequest(UserSystem.buildLoginRequest("carl","34"));

        if (withPacketSerialization) {
            packet= Packet.buildFromBlock(packet.getData());
        }

        Request request = DefaultBusConnector.decodeRequest(packet);
        assertEquals("carl",(String)request.getPayload().get(0));
        assertEquals("34",(String)request.getPayload().get(1));
        assertNull(request.getUserEntityId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testPayloadByName(boolean withPacketSerialization) {
        Vector3 position = new Vector3(1.0,2.0,3.0);
        Quaternion rotation = new Quaternion(4.0,5.0,6.0,7.0);
        Map<String, Object> map = new HashMap<>();
        map.put("id", "566");
        map.put("bundle","bundlename");
        map.put("model","modelfile");
        map.put("position", position.toString());
        map.put("rotation", rotation.toString());
        //11.2.23 payload no longer complies to specification, but for the test it is ok
        Event event = new Event(BaseEventRegistry.EVENT_ENTITYSTATE, new Payload(map));

        Packet packet = DefaultBusConnector.encodeEvent(event);

        if (withPacketSerialization) {
            packet= Packet.buildFromBlock(packet.getData());
        }

        event = DefaultBusConnector.decodeEvent(packet);
        assertEquals("bundlename",(String)event.getPayload().get("bundle"));

    }
}
