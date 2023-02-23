package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.engine.BaseEventRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventTest {

    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void testPayloadNull() {
        Event evt = new Event(BaseEventRegistry.EVENT_ENTITYSTATE, null);
        Assertions.assertNotNull(evt.toString(), "toString");
    }
}
