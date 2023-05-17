package de.yard.threed.engine.ecs;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

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
        EcsTestHelper.setup(() -> {
            SystemManager.addSystem(new ClientSystem(new ModelBuilderRegistry[]{}));
        });
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
