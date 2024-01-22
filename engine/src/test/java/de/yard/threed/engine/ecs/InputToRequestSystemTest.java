package de.yard.threed.engine.ecs;

import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.InitMethod;
import de.yard.threed.engine.gui.ControlMenuBuilder;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


public class InputToRequestSystemTest {

    SceneRunnerForTesting sceneRunner;
    InputToRequestSystem inputToRequestSystem;
    Point segment1Location = new Point(150, 30);

    @BeforeEach
    public void setup() {
        EcsTestHelper.setup(() -> {
            //should have be done in setup. SystemManager.reset();
            inputToRequestSystem = new InputToRequestSystem();
            inputToRequestSystem.addKeyMapping(KeyCode.KEY_K, RequestType.register(1016, "requestForKeyK"));
            SystemManager.addSystem(inputToRequestSystem);
        });
        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
        sceneRunner.runLimitedFrames(3);

        // Override the default sizeless scene
        ((SimpleHeadlessPlatform) Platform.getInstance()).setSceneDimension(new Dimension(300, 200));
    }

    @Test
    public void testDestinationMarker() {

        SceneNode directionMarker = new SceneNode();
        SceneNode localMarker = new SceneNode();

        //GridTeleporter gridTeleporter = new GridTeleporter(localMarker, directionMarker);
    }

    /**
     * Shouldn't send (key triggered) requests before logged in.
     */
    @Test
    public void testLogin() throws Exception {

        SimpleHeadlessPlatform.mockedKeyDownInput.add(KeyCode.KEY_K);
        EcsTestHelper.processSeconds(2);

        // key input should be ignored silently
        assertEquals(0, SimpleHeadlessPlatform.mockedKeyDownInput.size(), "mockedKeyInput ");
        assertEquals(0, SystemManager.getRequestCount(), "requests ");

        login();

        SimpleHeadlessPlatform.mockedKeyDownInput.add(KeyCode.KEY_K);
        EcsTestHelper.processSeconds(2);
        assertEquals(1, SystemManager.getRequestCount(), "requests ");

        Request request = SystemManager.getRequest(0);
        assertEquals("requestForKeyK", request.getType().getLabel());
    }

    /**
     *
     */
    @Test
    public void testControlMenu() {

        login();
        SimpleClosableGuiGrid menu = new SimpleClosableGuiGrid();
        inputToRequestSystem.setControlMenuBuilder(camera -> menu);
        assertNull(inputToRequestSystem.getControlmenu());
        // open menu
        inputToRequestSystem.openCloseControlMenu();
        assertNotNull(inputToRequestSystem.getControlmenu());

        // click position doesn't matter. control panel mock will always accept.
        SimpleHeadlessPlatform.mockedMouseDownInput.add(new Point(-100, -100));
        SimpleHeadlessPlatform.mockedMouseUpInput.add(new Point(-100, -100));
        EcsTestHelper.processSeconds(2);
        assertEquals(1, menu.checkForClickedAreaCalled);
    }

    /**
     *
     */
    @Test
    public void testControlPanel() {

        login();
        SimpleClosableControlPanel cp = new SimpleClosableControlPanel();
        inputToRequestSystem.addControlPanel(cp);

        // click position doesn't matter. control panel mock will always accept.
        SimpleHeadlessPlatform.mockedMouseDownInput.add(new Point(-100, -100));
        SimpleHeadlessPlatform.mockedMouseUpInput.add(new Point(-100, -100));
        EcsTestHelper.processSeconds(2);
        assertEquals(1, cp.checkForClickedAreaCalled);

    }

    /**
     *
     */
    @Test
    public void testSegmentControl() throws Exception {

        login();

        // nothing should happen when button does down
        SimpleHeadlessPlatform.mockedMouseDownInput.add(segment1Location);
        EcsTestHelper.processSeconds(2);
        assertEquals(0, sceneRunner.getSystemTracker().requestsPut.size(), "requests");

        // USER_REQUEST_CONTROLMENU is sent and consumed
        SimpleHeadlessPlatform.mockedMouseUpInput.add(segment1Location);
        EcsTestHelper.processSeconds(2);
        assertEquals(1, sceneRunner.getSystemTracker().requestsPut.size(), "requests");
        assertEquals(InputToRequestSystem.USER_REQUEST_CONTROLMENU, sceneRunner.getSystemTracker().requestsPut.get(0).getType());
        assertEquals(0, SystemManager.getRequestCount(), "pending requests");
    }

    private void login() {
        String clientId = "677";
        int userEntitId = 343;
        SystemManager.sendEvent(UserSystem.buildLoggedinEvent("u0", clientId, userEntitId, null));
    }
}

class SimpleClosableControlPanel extends ControlPanel {

    boolean closeOnClick = true;
    public int checkForClickedAreaCalled = 0;

    public SimpleClosableControlPanel() {
        super(new DimensionF(80, 40), Material.buildBasicMaterial(Color.BLUE), 0.1);
    }

    @Override
    public boolean checkForClickedArea(Ray ray) {

        checkForClickedAreaCalled++;
        return true;
    }
}

class SimpleClosableGuiGrid extends GuiGrid {

    public int checkForClickedAreaCalled = 0;

    public SimpleClosableGuiGrid() {
        super(new DimensionF(80, 40), 0.1,0.1,0,1,1,Color.BLUE,false);
    }

    @Override
    public boolean checkForClickedArea(Ray ray) {

        checkForClickedAreaCalled++;
        return true;
    }
}
