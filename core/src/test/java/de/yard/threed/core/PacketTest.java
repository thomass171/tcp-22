package de.yard.threed.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PacketTest {

    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void testEmptyParameter() {
        Packet packet = Packet.buildFromBlock(List.of(
                // Encoding of values doesn't matter here
                "p0=s:a",
                "p1=i:44",
                "p2="
        ));
        assertTrue(packet.isByIndex());
        assertEquals("s:a", packet.getValue("p0"));
        assertEquals("i:44", packet.getValue("p1"));
        // Has always been null instead of empty string
        assertNull(packet.getValue("p2"));
    }
}
