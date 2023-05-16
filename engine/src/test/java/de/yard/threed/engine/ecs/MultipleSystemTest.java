package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.AvatarComponent;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of interaction of multiple ECS systems with SystemManager. But without real Scenes.
 * <p>
 * Created by thomass on 16.02.23.
 */
public class MultipleSystemTest {

    @Test
    public void testLoginProcess() throws Exception {

        setup(new String[]{"AvatarSystem", "UserSystem", "ObserverSystem"});
        LoggingSystemTracker systemTracker = ((SceneRunnerForTesting) AbstractSceneRunner.getInstance()).getSystemTracker();

        Observer.buildForDefaultCamera();
        assertNotNull(Observer.getInstance(), "observer");
        SystemState.state = SystemState.STATE_READY_TO_JOIN;
        String testUserName = "testUserName";
        SystemManager.putRequest(UserSystem.buildLoginRequest(testUserName, "clientid"));

        assertEquals(1, SystemManager.getRequestCount(), "requests ");
        // process join
        EcsTestHelper.processSeconds(2);

        assertEquals(1, EcsTestHelper.filterEventList(systemTracker.getEventsProcessed(), (e) -> e.getType().equals(BaseEventRegistry.USER_EVENT_JOINED)).size());

        List<Event> joinEvents = EcsTestHelper.getEventsFromHistory(BaseEventRegistry.USER_EVENT_JOINED);
        assertEquals(1, joinEvents.size());
        Event joinEvent = joinEvents.get(0);
        int playerEntityId = (Integer) joinEvent.getPayload().get("userentityid");
        EcsEntity playerEntity = EcsHelper.findEntityById(playerEntityId);
        assertNotNull(playerEntity, "player");
        assertNotNull("Player", playerEntity.getName());
        assertEquals(testUserName, playerEntity.getName());

        playerEntity = SystemManager.findEntities(e -> testUserName.equals(e.getName())).get(0);
        assertNotNull(playerEntity, "player");
        assertNotNull("Player", playerEntity.getName());
        assertEquals(testUserName, playerEntity.getName());
        assertEquals(3, playerEntity.getComponentCount());
        assertNotNull(TeleportComponent.getTeleportComponent(playerEntity));
        assertNotNull(AvatarComponent.getAvatarComponent(playerEntity));
        assertNotNull(UserComponent.getUserComponent(playerEntity));
        // observercomponent is disabled by default
        assertNull(ObserverComponent.getObserverComponent(playerEntity));

        // process EVENT_USER_ASSEMBLED
        EcsTestHelper.processSeconds(2);

        assertNotNull(Observer.getInstance(), "observer");
        // Should be assigned to avatar
        Transform observerParent = Observer.getInstance().getTransform().getParent();
        assertNotNull(observerParent, "observerParent");
    }

    /**
     *
     */
    private void setup(String[] systems) {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                for (String systemName : systems) {

                    if (systemName.equals("AvatarSystem")) SystemManager.addSystem(new AvatarSystem());
                    else if (systemName.equals("UserSystem")) SystemManager.addSystem(new UserSystem());
                    else if (systemName.equals("ObserverSystem")) SystemManager.addSystem(new ObserverSystem());
                    else throw new RuntimeException("unknown system" + systemName);
                }
            }
        };
        SimpleHeadlessPlatformFactory platformFactory = new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting());
        EngineTestFactory.initPlatformForTest(new String[]{"engine"}, platformFactory, initMethod, ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));
    }

}
