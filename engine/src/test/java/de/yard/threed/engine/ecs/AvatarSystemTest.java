package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Created by thomass on 7.11.21.
 */
public class AvatarSystemTest {

    /**
     *
     */
    @BeforeEach
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                //world = new SceneNode();
                SystemManager.addSystem(new AvatarSystem());
                SystemManager.addSystem(new ObserverSystem());
            }
        };
        SimpleHeadlessPlatformFactory platformFactory = new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting());
        //platformFactory.enableCamera();
        EngineTestFactory.initPlatformForTest(new String[]{"engine"}, platformFactory, initMethod, ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));
    }

    @Test
    public void testSimpleNonVR() throws Exception {

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");

        startSimpleTest(true);

        assertNotNull(Observer.getInstance(), "observer");
        // Should be assigned to avatar
        Transform observerParent = Observer.getInstance().getTransform().getParent();
        assertNotNull(observerParent, "observerParent");
    }

    @Test
    public void testSimpleVR() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put("enableVR", "true");
        Platform.getInstance().getConfiguration().addConfiguration(new ConfigurationByProperties(properties), true);
        VrInstance.buildFromArguments();
        assertNotNull(VrInstance.getInstance(), "VrInstance");

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");

        startSimpleTest(true);

        assertNotNull(Observer.getInstance(), "observer");
        // Should NOT be assigned to avatar in VR
        Transform observerParent = Observer.getInstance().getTransform().getParent();
        // 16.2.22: Now also in VR observer is attached to avatar
        assertNotNull(observerParent, "observerParent");
    }

    @Test
    public void testWithoutShortCutJoin() throws Exception {

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");
        ((AvatarSystem) SystemManager.findSystem(AvatarSystem.TAG)).disableShortCutJoin();

        EcsEntity playerEntity = startSimpleTest(false);

        assertNotNull(Observer.getInstance(), "observer");
        // Not yet assembled so not yet assigned to avatar
        Transform observerParent = Observer.getInstance().getTransform().getParent();
        assertNull(observerParent, "observerParent");

        SystemManager.sendEvent(BaseEventRegistry.buildUserJoinedEvent(playerEntity));
        EcsTestHelper.processSeconds(2);

        validateAssembledUserEntity(playerEntity);
        // Now should be assigned to avatar
        observerParent = Observer.getInstance().getTransform().getParent();
        assertNotNull(observerParent, "observerParent");
    }

    private EcsEntity startSimpleTest(boolean expectJoinedEvent) {

        String testUserName = "testUserName";
        EcsEntity user = new EcsEntity(new UserComponent("user account"));
        user.setName(testUserName);

        SystemManager.putRequest(UserSystem.buildJoinRequest(user.getId()/*, true*/));
        assertEquals(1, SystemManager.getRequestCount(), "requests ");
        // process join
        EcsTestHelper.processSeconds(2);

        EcsEntity playerEntity;
        if (expectJoinedEvent) {
            List<Event> joinEvents = EcsTestHelper.getEventsFromHistory(BaseEventRegistry.USER_EVENT_JOINED);
            assertEquals(1, joinEvents.size(), "JOIN events");
            Event joinEvent = joinEvents.get(0);
            int playerEntityId = (Integer) joinEvent.getPayload().get("userentityid");
            playerEntity = EcsHelper.findEntityById(playerEntityId);
            assertNotNull(playerEntity, "player");
            assertNotNull("Player", playerEntity.getName());
            assertEquals(testUserName, playerEntity.getName());
        }

        playerEntity = SystemManager.findEntities(e -> testUserName.equals(e.getName())).get(0);
        assertNotNull(playerEntity, "player");
        assertNotNull("Player", playerEntity.getName());
        assertEquals(testUserName, playerEntity.getName());

        return playerEntity;
    }

    private void validateAssembledUserEntity(EcsEntity userEntity){
        assertNotNull(userEntity.getSceneNode());
        // TeleportComponent is just optional
    }
}
