package de.yard.threed.engine.ecs;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Properties;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.yard.threed.core.testutil.TestUtils.assertVector3;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Created by thomass on 27.1.24.
 */
public class GrabbingSystemTest {

    EcsEntity entity;
    InputToRequestSystem inputToRequestSystem;
    int userEntitId = 343;

    /**
     *
     */
    @BeforeEach
    public void setup() {
        EcsTestHelper.setup(() -> {
            inputToRequestSystem = new InputToRequestSystem();
            SystemManager.addSystem(inputToRequestSystem);
            SystemManager.addSystem(GrabbingSystem.buildFromConfiguration());
            GrabbingSystem.addDefaultKeyBindings(inputToRequestSystem);
        }, new Properties().add("emulateVR", "true"));

        VrInstance.buildFromArguments();
        VrInstance.getInstance().setEmulatedControllerPosition(new Vector3(2, 3, 4));

    }

    @Test
    public void testGrabInVR() {

        startSimpleTest();
        assertNotNull(VrInstance.getInstance());
        assertTrue(VrInstance.getInstance().isEmulated());
        assertVector3(new Vector3(2, 3, 4), VrInstance.getInstance().getController(0).getWorldPosition());
        assertVector3(new Vector3(2, 3, 4), VrInstance.getInstance().getController(1).getWorldPosition());

        GrabbingComponent gc = GrabbingComponent.getGrabbingComponent(entity);
        assertEquals(-1, gc.grabbedBy);

        entity.getSceneNode().getTransform().setPosition(new Vector3(2.1, 3.1, 4.1));
        SimpleHeadlessPlatform.mockedKeyDownInput.add(KeyCode.KEY_G);
        int seconds = 2;
        EcsTestHelper.processSeconds(seconds);
        assertEquals(0, gc.grabbedBy);
        SimpleHeadlessPlatform.mockedKeyUpInput.add(KeyCode.KEY_G);
        EcsTestHelper.processSeconds(seconds);
        assertEquals(-1, gc.grabbedBy);

        entity.getSceneNode().getTransform().setPosition(new Vector3(2.1, 3.1, 4.1));
        SimpleHeadlessPlatform.mockedKeyDownInput.add(KeyCode.KEY_J);
        EcsTestHelper.processSeconds(seconds);
        assertEquals(1, gc.grabbedBy);
        SimpleHeadlessPlatform.mockedKeyUpInput.add(KeyCode.KEY_J);
        EcsTestHelper.processSeconds(seconds);
        assertEquals(-1, gc.grabbedBy);
    }

    private void startSimpleTest() {

        login();

        entity = new EcsEntity(new SceneNode());
        entity.addComponent(new GrabbingComponent());
        entity.getSceneNode().getTransform().setPosition(new Vector3(2.1, 3.1, 4.1));
    }

    /**
     * Just a login but no user entity.
     */
    private void login() {
        String clientId = "677";

        SystemManager.sendEvent(UserSystem.buildLoggedinEvent("u0", clientId, userEntitId, null));
        EcsTestHelper.processSeconds(2);
    }
}
