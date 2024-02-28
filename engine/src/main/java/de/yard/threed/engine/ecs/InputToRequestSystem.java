package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;

import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.vr.VRController;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert user input to either events/requests immediately or preprocess input for some defined standard actions (eg. menus) and derive requests from that.
 * In general for all inputs:
 * - Keyboard (desktop)
 * - Mouse (desktop)
 * - Touch (touchpad,tablet)
 * - (control) menu (tablet)
 * - VR controller (in VR)
 * <p>
 * standard actions include:
 * - menus, because these also trigger requests.
 * - segment control, important for touchpads
 * <p>
 * But requests are not sent before the user logs in.
 * Previous name was KeyToRequestSystem.
 * 22.1.24: No longer decide mouseClickMode between segment and control panel (VR) mode. Control panel should also be avialable for mouse clicks
 * in general.
 * <p>
 * Dragging for now has no real use case. At least its no non-VR supplement for grabbing.
 * <p>
 * Created by thomass on 11.10.19.
 */
public class InputToRequestSystem extends DefaultEcsSystem {
    static Log logger = Platform.getInstance().getLog(InputToRequestSystem.class);
    private Map<KeyEntry, RequestBuilder> keymapping = new HashMap<KeyEntry, RequestBuilder>();

    //2.4.21: menu. Why not in UserSystem?
    //4.2.22: What is the difference? Apparently the only difference is a control menu is attached to a camera while
    //a menu is attached to some node. Well, in principle both should be ready for nearView/cameraRelated and scene located.
    //Even though its quite the same, a control menu is intended to be displayed all the time, while a menu is toggled for
    // a short time and quickly closed again. And a menu can be shown in addition to a control menu.
    public static RequestType USER_REQUEST_MENU = RequestType.register(1006, "USER_REQUEST_MENU");
    public static RequestType USER_REQUEST_CONTROLMENU = RequestType.register(1007, "USER_REQUEST_CONTROLMENU");

    private Menu menu = null;
    MenuProvider menuProvider;
    ControlMenuBuilder controlMenuProvider;
    //TODO 3.4.21: menucycler muss hier auch rein.
    public static GuiGrid controlmenu;
    private Map<Integer, RequestType> segmentRequests;
    private List<PointerHandler> pointerHandlerList = new ArrayList<PointerHandler>();
    private List<ControlPanel> controlPanelList = new ArrayList<ControlPanel>();
    @Deprecated
    static String playername;
    public static String TAG = "InputToRequestSystem";
    private List<MockedInput> mockedInputs = new ArrayList<MockedInput>();
    // Entity id of the current player (the one controlling the game). null unless not logged in
    private Integer userEntityId = null;
    private Point possiblestartdrag;
    private Integer draggedEntity = null;
    // in some large scale setups (FG) the small z offsets of the default camera and guigrid just do not fit
    // for those cases a dedicated deferred camera for menus can be established. For menu and control menu.
    private Camera cameraForMenu = null;

    public InputToRequestSystem() {
        super(new String[]{}, new RequestType[]{USER_REQUEST_MENU, USER_REQUEST_CONTROLMENU}, new EventType[]{UserSystem.USER_EVENT_LOGGEDIN});
        updatepergroup = false;

        // segments 0,1,2 are reserved: 1=unused due to VR toggle area, 2=control menu toggle, besser 1? 0 und 2 auch fuer movement.
        segmentRequests = new HashMap<Integer, RequestType>();
        segmentRequests.put(1, USER_REQUEST_CONTROLMENU);
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
    @Deprecated
    public static void setPayload0(String playername) {
        logger.warn("remove this");
        InputToRequestSystem.playername = playername;
    }

    public void setSegmentRequest(int segment, RequestType requestType) {
        if (segment == 1) {
            logger.warn("invalid segment " + segment);
            return;
        }
        segmentRequests.put(segment, requestType);

    }

    /**
     * Mapping for 'key went down'.
     */
    public void addKeyMapping(int keyCode, RequestType requestType) {
        keymapping.put(new KeyEntry(keyCode), new RequestBuilder(requestType));
    }

    public void addKeyMapping(int keyCode, RequestType requestType, RequestPopulator requestPopulator) {
        keymapping.put(new KeyEntry(keyCode), new RequestBuilder(requestType, requestPopulator));
    }

    /**
     * Mapping for 'key went down' with shift modifier pressed.
     */
    public void addShiftKeyMapping(int keyCode, RequestType requestType) {
        keymapping.put(new KeyEntry(keyCode, true), new RequestBuilder(requestType));
    }

    /**
     * Mapping for 'key went up'.
     */
    public void addKeyReleaseMapping(int keyCode, RequestType requestType) {
        keymapping.put(new KeyEntry(keyCode, false, true), new RequestBuilder(requestType));
    }

    public void addKeyReleaseMapping(int keyCode, RequestType requestType, RequestPopulator requestPopulator) {
        keymapping.put(new KeyEntry(keyCode, false, true), new RequestBuilder(requestType, requestPopulator));
    }

    public void addShiftKeyReleaseMapping(int keyCode, RequestType requestType) {
        keymapping.put(new KeyEntry(keyCode, true, true), new RequestBuilder(requestType));
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

        //26.10.18: 'T' instead P for teleport for having 'P' for Pause. TODO:should be configured by keymappings
        if (Input.getKeyDown(KeyCode.T)) {
            IntHolder option = new IntHolder(0);
            Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{option}));
            if (Input.getKey(KeyCode.Shift)) {
                //cyclePosition(tc, (false));
                option.setValue(1);
            } else {
                if (Input.getKey(KeyCode.Ctrl)) {
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
            if (!key.release && Input.getKeyDown(key.keyCode)) {
                logger.debug("key down detected ");
                if (key.shift == Input.getKey(KeyCode.Shift)) {
                    if (userEntityId != null) {
                        // only create request if client/user is logged in yet. userEntityId is not a payload but a request property.
                        SystemManager.putRequest(buildRequestFromKeyMapping(key, userEntityId));
                    }
                }
            }
            if (key.release && Input.getKeyUp(key.keyCode)) {
                logger.debug("key up detected ");

                if (key.shift == Input.getKey(KeyCode.Shift)) {
                    if (userEntityId != null) {
                        // only create request if client/user is logged in yet. userEntityId is not a payload but a request property.
                        SystemManager.putRequest(buildRequestFromKeyMapping(key, userEntityId));
                    }
                }
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

            // No longer just emulate VR controller but general feature. But still have a left and right pointer even with mouse (left by holding ctrl key)
            Ray ray = camera.buildPickingRay(camera.getCarrierTransform(), mouseMovelocation);
            // ctrl geht nicht in JME mit click, darum shift.
            processPointer(ray, Input.getKey(KeyCode.Shift));
        }
        // now check mouse clicks. Be aware of multiple possible targets, eg. segment and control menu.
        // VR has no mouse (clicks)
        Point mouseDownLocation = Input.getMouseDown();
        if (mouseDownLocation != null) {
            possiblestartdrag = mouseDownLocation;
            //logger.debug("possible drag from " + startdrag);
        }
        if (possiblestartdrag != null) {
            Point mouseMoveLocation = Input.getMouseMove();
            if (mouseMoveLocation != null) {
                Point offset = mouseMoveLocation.subtract(possiblestartdrag);
                logger.debug("dragging offset " + offset);
                if (draggedEntity == null) {
                    draggedEntity = findDraggedEntity(camera);
                    if (draggedEntity == null) {
                        // no entity hit. Abort drag.
                        possiblestartdrag = null;
                    } else {
                        logger.debug("Dragging entity with id " + draggedEntity);
                    }
                }
                // This calc is highly q&d. Dragged object should stay in its plane.
                double dragfactor = 0.003;
                double dragX = (double) offset.getX() * dragfactor;
                double dragY = -(double) offset.getY() * dragfactor;
                if (draggedEntity != null) {
                    requestDragTransform(dragX, dragY);
                }

            }
        }
        Point mouseUpLocation = Input.getMouseUp();
        // Only handle mouseUp as click when it is at the same location as mouseDown.
        if (mouseUpLocation != null) {
            if (possiblestartdrag == null || possiblestartdrag.equals(mouseUpLocation)) {
                logger.debug("mouseClicklocation=" + mouseUpLocation);

                if (menu != null) {
                    // menu is open. 1.2.24: Continue if no menu item was clicked. Otherwise a control panel menu button cannot close menu.
                    if (menu.checkForClickedArea(camera.buildPickingRay(camera.getCarrier().getTransform(), mouseUpLocation))) {
                        return;
                    }
                }

                boolean controlMenuAreaClicked = false;
                if (controlmenu != null) {
                    controlMenuAreaClicked = controlmenu.checkForClickedArea(camera.buildPickingRay(
                            camera.getCarrier().getTransform(), mouseUpLocation));
                }

                // Don't consider any further action (segment,VR emulation) when a button of control menu was clicked
                if (!controlMenuAreaClicked) {

                    // Mouse click somewhere. Consider VR controller or mouse ray click (left by holding ctrl key)
                    Ray ray = camera.buildPickingRay(camera.getCarrierTransform(), mouseUpLocation);
                    // ctrl geht nicht in JME mit click, darum shift
                    boolean consumed = processTrigger(ray, Input.getKey(KeyCode.Shift));
                    if (!consumed) {
                        // Fall through to segment control. Determine segment. 20.3.21: But send it only with user id.
                        int segment = Input.getClickSegment(mouseUpLocation, Scene.getCurrent().getDimension(), 3);
                        logger.debug("clicked segment:" + segment);
                        RequestType sr = segmentRequests.get(segment);
                        if (sr != null && userEntityId != null) {
                            SystemManager.putRequest(new Request(sr, userEntityId));
                        }
                    }
                }

            }
            // mouse up ends drag in any case
            possiblestartdrag = null;
        }

        // VR controller input
        if (VrInstance.getInstance() != null) {

            VrInstance vrInstance = VrInstance.getInstance();

            Ray rayLeft = null, rayRight = null;
            if (vrInstance.getController(0) != null) {
                processPointer(vrInstance.getController(0).getRay(), true);
                rayLeft = vrInstance.getController(0).getRay();
            }
            if (vrInstance.getController(1) != null) {
                processPointer(vrInstance.getController(1).getRay(), false);
                rayRight = vrInstance.getController(1).getRay();
            }
            if (Input.getControllerButtonDown(0) && rayLeft != null) {
                processTrigger(rayLeft, true);
            }
            if (Input.getControllerButtonDown(10) && rayRight != null) {

                processTrigger(rayRight, false);
            }
        }

        if (mockedInputs.size() > 0) {
            MockedInput mi = mockedInputs.remove(0);
            if (mi.clicked) {
                processTrigger(mi.ray, mi.left);
            } else {
                processPointer(mi.ray, mi.left);
            }
        }
    }

    private Request buildRequestFromKeyMapping(KeyEntry key, Integer userEntityId) {
        Request request = keymapping.get(key).build(userEntityId);
        return request;
    }

    @Override
    public boolean processRequest(Request request) {
        logger.debug("got request " + request.getType());

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

    @Override
    public void process(Event evt) {
        logger.debug("got event " + evt.getType());

        if (evt.getType().equals(UserSystem.USER_EVENT_LOGGEDIN)) {
            String username = (String) evt.getPayload().get("username");
            String clientid = (String) evt.getPayload().get("clientid");
            Integer userEntityId = (Integer) evt.getPayload().get("userentityid");

            //TODO check clientid that login was from here. For now only consider first login to be the player
            if (this.userEntityId == null) {
                this.userEntityId = userEntityId;
            }
        }
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
                Camera c = cameraForMenu == null ? Scene.getCurrent().getDefaultCamera() : cameraForMenu;
                menu = menuProvider.buildMenu(c);
                //27.1.22 menuProvider.getAttachNode().attach(menu.getNode());
                menu.getNode().getTransform().setParent(menuProvider.getAttachNode());
            }
        }
    }

    public void openCloseControlMenu() {
        if (controlmenu != null && controlmenu.getNode() != null) {
            controlmenu.remove();
            controlmenu = null;
        } else {
            if (controlMenuProvider != null) {
                Camera c = cameraForMenu == null ? Scene.getCurrent().getDefaultCamera() : cameraForMenu;
                controlmenu = controlMenuProvider.buildControlMenu(c);
                c.getCarrier().attach(controlmenu);
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

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Intended for testing
     */
    public void mockInput(Ray ray, boolean clicked, boolean left) {
        mockedInputs.add(new MockedInput(ray, clicked, left));
    }

    /**
     * Helper for sending a request with a userEntityId
     */
    public static void sendRequestWithId(Request request) {
        InputToRequestSystem inputToRequestSystem = (InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG);
        if (inputToRequestSystem == null) {
            return;
        }
        request.setUserEntityId(inputToRequestSystem.getUserEntityId());
        SystemManager.putRequest(request);
    }

    private Integer getUserEntityId() {
        return userEntityId;
    }

    /**
     * Typically userEntityId is set by receiving a login event.
     * This is an option for setting userEntityId if no login(System) is used.
     */
    public void setUserEntityId(int userEntityId) {
        this.userEntityId = userEntityId;
    }

    /**
     * Only for testing.
     */
    public GuiGrid getControlmenu() {
        return controlmenu;
    }

    public void setMenuProvider(MenuProvider menuProvider) {
        this.menuProvider = menuProvider;
    }

    public void setCameraForMenu(Camera cameraForMenu) {
        this.cameraForMenu = cameraForMenu;
    }

    private void processPointer(Ray ray, boolean left) {

        for (PointerHandler pointerHandler : pointerHandlerList) {
            pointerHandler.processPointer(ray, left);
        }
    }

    /**
     * For both VR pointer and traditional mouse click rays.
     */
    private boolean processTrigger(Ray ray, boolean left) {

        logger.debug("processTrigger, left=" + left + ",userEntityId=" + (int) userEntityId);

        if (userEntityId != null) {
            for (ControlPanel cp : controlPanelList) {
                //left/right independent?
                if (cp.checkForClickedArea(ray)) {
                    return true;
                }
            }

            boolean consumed = false;
            for (PointerHandler pointerHandler : pointerHandlerList) {
                Request request = pointerHandler.getRequestByTrigger((int) userEntityId, ray, left);
                if (request != null) {
                    SystemManager.putRequest(request);
                    consumed = true;
                }
            }
            return consumed;
        }
        return false;
    }

    private Integer findDraggedEntity(Camera camera) {
        Ray ray = camera.buildPickingRay(camera.getCarrier().getTransform(), possiblestartdrag);

        if (ray == null) {
            return null;
        }
        // Using intersections might be quite inefficient, but there is no other way currently
        // until we have a collider concept.
        List<NativeCollision> intersections = ray.getIntersections();
        logger.debug("intersections: " + intersections.size());
        for (int i = 0; i < intersections.size(); i++) {
            //logger.debug("intersection: " + intersections.get(i).getSceneNode().getName());
            NativeSceneNode intersectingNode = intersections.get(i).getSceneNode();
            /*if (intersections.get(i).getSceneNode().getName().equals("red box")) {
                SceneNode pickerobject = new SceneNode(intersections.get(i).getSceneNode());
            }*/
            for (EcsEntity entity : GrabbingSystem.getTransformables()) {
                if (entity.getSceneNode() != null) {
                    //TODO comparing by name is poor. Needs Id or similar.
                    if (entity.getSceneNode().getName().equals(intersectingNode.getName())) {
                        logger.debug("intersected entity found: " + entity.getName());
                        return entity.getId();
                    }
                }
            }
        }
        return null;
    }

    private void requestDragTransform(double dragX, double dragY) {
        // for now directly, but in future for client/server via request and additional TransformService
        EcsEntity entity = EcsHelper.findEntityById(draggedEntity);
        Transform transform = entity.getSceneNode().getTransform();
        // TODO this is highly q&d. Dragged object should stay in its plane.
        transform.setPosition(transform.getPosition().add(new Vector3(dragX, 0, dragY)));
    }


}

class KeyEntry {
    public int keyCode;
    public boolean shift = false;
    public boolean release = false;

    KeyEntry(int keyCode) {
        this.keyCode = keyCode;
    }

    KeyEntry(int keyCode, boolean shift) {
        this.keyCode = keyCode;
        this.shift = shift;
    }

    KeyEntry(int keyCode, boolean shift, boolean release) {
        this.keyCode = keyCode;
        this.shift = shift;
        this.release = release;
    }
}

/**
 * Not real input intended for testing.
 */
class MockedInput {
    Ray ray;
    boolean clicked;
    boolean left;

    public MockedInput(Ray ray, boolean clicked, boolean left) {
        this.ray = ray;
        this.left = left;
        this.clicked = clicked;
    }
}
