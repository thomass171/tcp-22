package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.vr.VrInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.yard.threed.core.testutil.TestUtils.assertVector3;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Created by thomass on 7.10.23.
 */
public class FirstPersonMovingSystemTest {

    /**
     *
     */
    @BeforeEach
    public void setup() {
        EcsTestHelper.setup(() -> {
            SystemManager.addSystem(FirstPersonMovingSystem.buildFromConfiguration());
        });
    }

    @Test
    public void testStepForward() {

        EcsEntity player = startSimpleTest();
        FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(player);
        Vector3 initialPosition = player.getSceneNode().getTransform().getPosition();
        assertVector3(new Vector3(0, 0, 0), initialPosition);

        SystemManager.putRequest(new Request(BaseRequestRegistry.TRIGGER_REQUEST_FORWARD, player.getId()));

        int seconds = 27;
        EcsTestHelper.processSeconds(seconds);

        Vector3 position = player.getSceneNode().getTransform().getPosition();
        assertVector3(new Vector3(0, 0, -FirstPersonMovingSystem.assumedDeltaTimeWhenStepping * fpmc.getFirstPersonTransformer().getMovementSpeed()), position);
    }

    private EcsEntity startSimpleTest() {

        String testUserName = "testUserName";
        SceneNode node = new SceneNode();
        EcsEntity user = new EcsEntity(node);
        user.addComponent(new FirstPersonMovingComponent(node.getTransform()));
        user.setName(testUserName);
        return user;
    }
}
