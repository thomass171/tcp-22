package de.yard.threed.engine.ecs;

import de.yard.threed.core.EventType;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;

import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.vr.VrHelper;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2.4.21: Also for mouse input. In general for all inputs:
 * - Keyboard (desktop)
 * - Mouse (desktop)
 * - (control) menu (tablet)
 * - VR controller (in VR)
 * <p>
 * And for menus, because these also trigger requests.
 * <p>
 * Previous name was KeyToRequestSystem.
 * <p>
 *
 * <p>
 * Created by thomass on 11.10.19.
 */
public class InputToRequestSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(InputToRequestSystem.class);
    private Map<KeyEntry, RequestType> keymapping = new HashMap<KeyEntry, RequestType>();

    //2.4.21: menu. Why not in UserSystem?
    public static RequestType USER_REQUEST_MENU = new RequestType("USER_REQUEST_MENU");
    public static RequestType USER_REQUEST_CONTROLMENU = new RequestType("USER_REQUEST_CONTROLMENU");

    boolean keytorequestsystemdebuglog = true;
    private Menu menu = null;
    MenuProvider menuProvider;
    ControlMenuBuilder controlMenuProvider;
    //TODO 3.4.21: menucycler muss hier auch rein.
    public static GuiGrid controlmenu;
    private Map<Integer, RequestType> segmentRequests;
    private List<PointerHandler> pointerHandlerList = new ArrayList<PointerHandler>();
    private List<ControlPanel> controlPanelList = new ArrayList<ControlPanel>();
    static String playername;
    public static String TAG = "InputToRequestSystem";
    // 1=identity segment request (default), 2=emulate (left) VR controller trigger
    public static int MOUSE_CLICK_MODE_SEGMENT = 1;
    public static int MOUSE_CLICK_MODE_VR_LEFT = 2;
    private int mouseClickMode = MOUSE_CLICK_MODE_SEGMENT;
    public static int MOUSE_MOVE_MODE_SEGMENT = 1;
    public static int MOUSE_MOVE_MODE_VR_LEFT = 2;
    private int mouseMoveMode = MOUSE_MOVE_MODE_SEGMENT;

    public InputToRequestSystem() {
        super(new String[]{}, new RequestType[]{USER_REQUEST_MENU, USER_REQUEST_CONTROLMENU}, new EventType[]{});
        updatepergroup = false;

        // segments 0,1,2 are reserved: 1=unused due to VR toggle area, 2=control menu toggle, besser 1? 0 und 2 auch fuer movement.
        segmentRequests = new HashMap<Integer, RequestType>();
        segmentRequests.put(1, USER_REQUEST_CONTROLMENU);

        if (EngineHelper.isEnabled("argv.emulateVR")) {
            emulateLeftVrControllerByMouse();
        }
    }

    public InputToRequestSystem(MenuProvider menuProvider) {
        this();
        this.menuProvider = menuProvider;

    }

    /**
     * Auch ne Kruecke TODO
     *
     * @param playername
     */
    public static void setPayload0(String playername) {
        InputToRequestSystem.playername = playername;
    }

    public void setSegmentRequest(int segment, RequestType requestType) {
        if (segment == 1) {
            logger.warn("invalid segment " + segment);
            return;
        }
        segmentRequests.put(segment, requestType);

    }

    public void addKeyMapping(int keyCode, RequestType requestType) {
        keymapping.put(new KeyEntry(keyCode), requestType);
    }

    @Override
    public void init(EcsGroup group) {
        openCloseControlMenu();

    }

    /**
     *
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {

        // Keyboard input

        //26.10.18: 'T' statt P, damit P fuer Pause ist.
        if (Input.GetKeyDown(KeyCode.T)) {
            IntHolder option = new IntHolder(0);
            Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(option));
            if (Input.GetKey(KeyCode.Shift)) {
                //cyclePosition(tc, (false));
                option.setValue(1);
            } else {
                if (Input.GetKey(KeyCode.Ctrl)) {
                    //dann wird der naechste beim naechsten update() gesetzt.
                    //return;
                    option.setValue(2);
                } else {
                    //cyclePosition(tc, (true));
                }
            }
            SystemManager.putRequest(request);
        }

        for (KeyEntry key : keymapping.keySet()) {
            if (Input.GetKeyDown(key.keyCode)) {

                //payload playername important for firing. And more?
                SystemManager.putRequest(new Request(keymapping.get(key), new Payload(playername)));
            }
        }

        if (menu != null && menu.getNode() == null) {
            //already closed by "someone"?
            menu = null;
        }

        Camera camera = Scene.getCurrent().getDefaultCamera();

        // Mouse input

        Point mouseMovelocation = Input.getMouseMove();
        if (mouseMovelocation != null) {

            if (mouseMoveMode == MOUSE_MOVE_MODE_VR_LEFT) {
                // emulate VR controller (left by holding ctrl key)
                Ray ray = camera.buildPickingRay(camera.getCarrierTransform(), mouseMovelocation);
                // ctrl geht nicht in JME mit click, darum shift.
                processPointer(ray, Input.GetKey(KeyCode.Shift));
            }
        }
        // now check mouse clicks. Be aware of multiple possible targets, eg. segment and control menu.
        Point mouseClicklocation = Input.getMouseClick();
        if (mouseClicklocation != null) {
            logger.debug("mouseClicklocation=" + mouseClicklocation);

            if (menu != null) {
                // open menu. Only check menu item click.

                menu.checkForClickedArea(camera/*menu.getMenuCamera()*/.buildPickingRay(camera.getCarrier().getTransform(), mouseClicklocation));

                // Muss erst geschlossen werden, bevor was anderes gemacht werden kann.
                // Geht aber nicht ueber Key, der wird weiter unten erst geprueft. Naja.
                return;
            } else {
                boolean controlMenuAreaClicked = false;
                if (controlmenu != null) {
                    controlMenuAreaClicked = controlmenu.checkForClickedArea(camera.buildPickingRay(
                            camera.getCarrier().getTransform(), mouseClicklocation));
                }

                // Don't consider any further action (segment,VR emulation) when a button of control menu was clicked
                if (!controlMenuAreaClicked) {

                    if (mouseClickMode == MOUSE_CLICK_MODE_SEGMENT) {
                        // Mausclick irgendwo. Determine segment
                        int segment = Input.getClickSegment(mouseClicklocation, Scene.getCurrent().getDimension(), 3);
                        logger.debug("clicked segment:" + segment);
                        RequestType sr = segmentRequests.get(segment);
                        if (sr != null) {
                            SystemManager.putRequest(new Request(sr, null));
                        }
                    }
                    if (mouseClickMode == MOUSE_CLICK_MODE_VR_LEFT) {
                        // Mausclick irgendwo. Consider VR controller ray click  (left by holding ctrl key)

                        Ray ray = camera.buildPickingRay(camera.getCarrierTransform(), mouseClicklocation);
                        // ctrl geht nicht in JME mit click, darum shift
                        processTrigger(ray, Input.GetKey(KeyCode.Shift));
                    }
                }
            }
        }

        // VR controller input
        if (VrInstance.getInstance() != null) {

            Ray rayLeft = null, rayRight = null;
            if (VrHelper.getController(0) != null) {
                processPointer(VrHelper.getController(0).getRay(), true);
                rayLeft = VrHelper.getController(0).getRay();
            }
            if (VrHelper.getController(1) != null) {
                processPointer(VrHelper.getController(1).getRay(), false);
                rayRight = VrHelper.getController(1).getRay();
            }
            if (Input.getControllerButtonDown(0) && rayLeft != null) {
                processTrigger(rayLeft, true);
            }
            if (Input.getControllerButtonDown(10) && rayRight != null) {

                processTrigger(rayRight, false);
            }
        }
    }

    @Override
    public boolean processRequest(Request request) {
        if (keytorequestsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }

        if (request.getType().equals(USER_REQUEST_MENU)) {
            //if (menu != null) {
            openCloseMenu();
            //} else {

            //}

            return true;
        }
        if (request.getType().equals(USER_REQUEST_CONTROLMENU)) {
            openCloseControlMenu();
            return true;
        }
        return false;
    }

    /**
     * Problem? Ein menu kann sich selber schliessen, oder ein anderer. Dann ist der Toggle hier nicht zuverlaessig.
     */
    public void openCloseMenu() {
        if (menu != null && menu.getNode() != null) {
            //MainMenu.close();
            menu.remove();
            menu = null;
        } else {
            //menu = MainMenu.open(getDefaultCamera(), this);
            if (menuProvider != null) {
                menu = menuProvider.buildMenu();//MainMenu.open(getDefaultCamera(), this);
                menuProvider.getAttachNode().attach(menu.getNode());
            }
        }
    }

    public void openCloseControlMenu() {
        if (controlmenu != null && controlmenu.getNode() != null) {
            controlmenu.remove();
            controlmenu = null;
        } else {
            if (controlMenuProvider != null) {
                controlmenu = controlMenuProvider.buildControlMenu(Scene.getCurrent().getDefaultCamera());
                Scene.getCurrent().getDefaultCamera().getCarrier().attach(controlmenu);
            }
        }
    }

    public void setControlMenuBuilder(ControlMenuBuilder controlMenuBuilder) {
        this.controlMenuProvider = controlMenuBuilder;
    }

    public void addPointerHandler(PointerHandler pointerHandler) {
        pointerHandlerList.add(pointerHandler);
    }

    public void addControlPanel(ControlPanel controlPanel) {
        controlPanelList.add(controlPanel);
    }

    private void emulateLeftVrControllerByMouse() {
        this.mouseClickMode = MOUSE_CLICK_MODE_VR_LEFT;
        this.mouseMoveMode = MOUSE_MOVE_MODE_VR_LEFT;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private void processPointer(Ray ray, boolean left) {

        for (PointerHandler pointerHandler : pointerHandlerList) {
            pointerHandler.processPointer(ray, left);
        }
    }

    private void processTrigger(Ray ray, boolean left) {

        logger.debug("processTrigger, left=" + left);
        for (ControlPanel cp : controlPanelList) {
            //left/right independent?
            if (cp.checkForClickedArea(ray)) {
                return;
            }
        }

        for (PointerHandler pointerHandler : pointerHandlerList) {
            Request request = pointerHandler.getRequestByTrigger(ray, left);
            if (request != null) {
                SystemManager.putRequest(request);
            }
        }
    }
}

class KeyEntry {
    public int keyCode;

    KeyEntry(int keyCode) {
        this.keyCode = keyCode;
    }
}
