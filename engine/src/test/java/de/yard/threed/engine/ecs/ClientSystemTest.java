package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <p>
 * Created by thomass on 7.11.21.
 */
public class ClientSystemTest {

    /**
     *
     */
    @BeforeEach
    public void setup() {
        InitMethod initMethod = () -> {
            SystemManager.addSystem(new ClientSystem(new ModelBuilderRegistry[]{}));
        };
        SimpleHeadlessPlatformFactory platformFactory = new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting());
        TestFactory.initPlatformForTest(new String[]{"engine"}, platformFactory, initMethod, Configuration.buildDefaultConfigurationWithEnv(new HashMap<>()));
    }

    @Test
    public void testEntitiyStateEvent() throws Exception {

        String testUserName = "testUserName";
        EcsEntity user = new EcsEntity(new UserComponent("user account"));
        user.setName(testUserName);

        SystemManager.sendEvent(DefaultBusConnector.buildEntitiyStateEvent(user));
        SystemManager.update(1);

        ClientSystem clientSystem = (ClientSystem) SystemManager.findSystem(ClientSystem.TAG);
        assertNotNull(clientSystem);

        //TODO further assertions
    }

}
