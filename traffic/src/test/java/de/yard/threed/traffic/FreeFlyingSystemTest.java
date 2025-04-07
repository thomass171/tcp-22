package de.yard.threed.traffic;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.yard.threed.core.testutil.TestUtils.assertVector3;

/**
 * Also for FreeFlyingComponent.
 * <p>
 * Created by thomass on 7.03.25.
 */
public class FreeFlyingSystemTest {

    EcsEntity player;
    EcsEntity movingEntity;

    /**
     *
     */
    @BeforeEach
    public void setup() {
        EcsTestHelper.setup(() -> {
            SystemManager.addSystem(FreeFlyingSystem.buildFromConfiguration());
        });
    }

    /**
     * rigid body should just forward with speed set. No request needed.
     * with all defaults moves in direction of +z axis.
     */
    @Test
    public void testStepForwardWithDefaults() {

        initSimpleTest();
        FreeFlyingComponent rbmc = FreeFlyingComponent.getFreeFlyingComponent(movingEntity);
        double movementSpeed = 3.4;
        rbmc.setMovementSpeed(movementSpeed);
        Vector3 initialPosition = movingEntity.getSceneNode().getTransform().getPosition();
        assertVector3(new Vector3(0, 0, 0), initialPosition);

        //SystemManager.putRequest(new Request(BaseRequestRegistry.TRIGGER_REQUEST_FORWARD, player.getId()));

        int seconds = 2;
        EcsTestHelper.processSeconds(seconds);

        Vector3 position = movingEntity.getSceneNode().getTransform().getPosition();
        // moves in 'vehicle move space', so forward is along -x
        Vector3 expectedNewPosition = new Vector3(-seconds * movementSpeed,0,0);
        assertVector3(expectedNewPosition, position);
    }

    /**
     * We also set up a user entity that will be the sender of requests.
     */
    private void initSimpleTest() {

        String testUserName = "testUserName";
        player = new EcsEntity(new SceneNode());
        player.setName(testUserName);

        SceneNode node = new SceneNode();
        movingEntity = new EcsEntity(node);
        Quaternion baseRotation = new Quaternion();
        node.getTransform().setRotation(baseRotation);
        movingEntity.addComponent(new FreeFlyingComponent(node.getTransform()));
        movingEntity.setName("movingEntity");
    }
}
