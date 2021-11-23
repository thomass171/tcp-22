package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static de.yard.threed.core.testutil.TestUtil.assertNotNull;
import static org.junit.Assert.*;

/**
 * <p>
 * Created by thomass on 7.11.21.
 */
public class AvatarSystemTest {

    /**
     *
     */
    @Before
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                //world = new SceneNode();
                SystemManager.addSystem(new AvatarSystem());
            }
        };
        SimpleHeadlessPlatformFactory platformFactory = new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting());
        //platformFactory.enableCamera();
        TestFactory.initPlatformForTest(new String[]{"engine"}, platformFactory, initMethod);
    }

    @Test
    public void testSimpleNonVR() throws Exception {

        Observer.buildForDefaultCamera();
        assertNotNull("observer", Observer.getInstance());

        startSimpleTest();

        assertNotNull("observer", Observer.getInstance());
        // Should be assigned to avatar
        Transform observerParent = Observer.getInstance().getTransform().getParent();
        assertNotNull("observerParent", observerParent);
    }

    @Test
    public void testSimpleVR() throws Exception {

        Platform.getInstance().setSystemProperty("argv.enableVR", "true");
        VrInstance.buildFromArguments();
        assertNotNull("VrInstance", VrInstance.getInstance());

        Observer.buildForDefaultCamera();
        assertNotNull("observer", Observer.getInstance());

        startSimpleTest();

        assertNotNull("observer", Observer.getInstance());
        // Should NOT be assigned to avatar in VR
        Transform observerParent = Observer.getInstance().getTransform().getParent();
        assertNull("observerParent", observerParent);
    }

    private void startSimpleTest() {

        SystemManager.putRequest(UserSystem.buildJOIN("", true));
        assertEquals("requests ", 1, SystemManager.getRequestCount());
        // process join
        EcsTestHelper.processSeconds(2);

        List<Event> joinEvents = EcsTestHelper.getEventsFromHistory(UserSystem.USER_EVENT_JOINED);
        assertEquals(1, joinEvents.size());
        Event joinEvent = joinEvents.get(0);
        EcsEntity playerEntity = (EcsEntity) joinEvent.getPayloadByIndex(0);
        assertNotNull("player", playerEntity);
        assertNotNull("Player", playerEntity.getName());

        playerEntity = SystemManager.findEntities(new NameFilter("Player")).get(0);
        assertNotNull("player", playerEntity);
        assertNotNull("Player", playerEntity.getName());
    }
}
