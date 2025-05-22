package de.yard.threed.engine.ecs;

import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Payload;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.yard.threed.engine.ecs.UserSystem.USER_REQUEST_TELEPORT;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Just a draft for now.
 * More tests for TeleporterSystem are in BasicTravelSceneTest
 * <p>
 * Created by thomass on 20.05.25.
 */
public class TeleporterSystemTest {

    /**
     *
     */
    @BeforeEach
    public void setup() {
        EcsTestHelper.setup(() -> {
            SystemManager.addSystem(new AvatarSystem());
            SystemManager.addSystem(new TeleporterSystem());
        });
    }

    @Test
    public void testSimple() throws Exception {

    }

    private EcsEntity startSimpleTest() {

        String testUserName = "testUserName";
        EcsEntity user = new EcsEntity(new UserComponent("user account"));
        user.setName(testUserName);

        // TODO what??

        EcsEntity playerEntity = SystemManager.findEntities(e -> testUserName.equals(e.getName())).get(0);
        assertNotNull(playerEntity, "player");
        assertNotNull("Player", playerEntity.getName());
        assertEquals(testUserName, playerEntity.getName());

        return playerEntity;
    }

    public static void teleportTo(SceneRunnerForTesting sceneRunner, EcsEntity userEntity, TeleportComponent tc, String entity, String label, boolean teleportByDestination) throws Exception {
        if (teleportByDestination) {
            SystemManager.putRequest(UserSystem.buildTeleportRequest(userEntity.getId(), 4, entity+"."+label));
            sceneRunner.runLimitedFrames(3);
        }else {
            // teleport step by step until we reach destination entity at requested label (eg. "Driver").
            TestUtils.waitUntil(() -> {
                SystemManager.putRequest(new Request(USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
                sceneRunner.runLimitedFrames(2);
                return TeleporterSystem.getTeleportEntity(tc) != null && TeleporterSystem.getTeleportEntity(tc).getName().equals(entity)
                        && tc.getLabel().equals(label);
            }, 1000);
        }
        assertEquals(entity, tc.getTargetEntity());
        assertEquals(label, tc.getLabel());
    }
}
