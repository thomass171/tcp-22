package de.yard.threed.engine.ecs;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

import java.util.ArrayList;
import java.util.List;

/**
 * First person movement of an entity. This typically should have the observer attached.
 * Start position is unset, so on (0,0,0).
 * <p>
 * Created by thomass on 26.08.23.
 */
public class FirstPersonMovingSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(FirstPersonMovingSystem.class);
    public static String TAG = "FirstPersonMovingSystem";
    public boolean firstpersonmovingsystemdebuglog = true;
    // Mouse movement is different between platforms and hard to unify. So stay with key control
    // and focus on VR
    boolean useMouseControl = false;
    // update by delta time to honor defined speeds
    static public double assumedDeltaTimeWhenStepping = 0.1;
    public List<ViewPoint> viewPoints = new ArrayList<ViewPoint>();

    /**
     *
     */
    private FirstPersonMovingSystem() {
        super(new String[]{FirstPersonMovingComponent.TAG}, new RequestType[]{
                        BaseRequestRegistry.TRIGGER_REQUEST_FORWARD,
                        BaseRequestRegistry.TRIGGER_REQUEST_BACK,
                        BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT,
                        BaseRequestRegistry.TRIGGER_REQUEST_TURNRIGHT,
                        BaseRequestRegistry.TRIGGER_REQUEST_TURNUP,
                        BaseRequestRegistry.TRIGGER_REQUEST_TURNDOWN,
                        BaseRequestRegistry.TRIGGER_REQUEST_ROLLLEFT,
                        BaseRequestRegistry.TRIGGER_REQUEST_ROLLRIGHT,
                        //
                        BaseRequestRegistry.TRIGGER_REQUEST_START_FORWARD,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_FORWARD,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_BACK,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_BACK,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_TURNLEFT,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNLEFT,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_TURNRIGHT,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNRIGHT,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_TURNUP,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNUP,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_TURNDOWN,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNDOWN,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLLEFT,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLLEFT,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLRIGHT,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLRIGHT
                },
                new EventType[]{BaseEventRegistry.EVENT_USER_ASSEMBLED,
                        BaseEventRegistry.USER_EVENT_VIEWPOINT});
    }

    public static FirstPersonMovingSystem buildFromConfiguration() {
        return new FirstPersonMovingSystem();
    }

    /**
     * @param group
     */
    @Override
    public void init(EcsGroup group) {
        if (group != null) {
            FirstPersonMovingComponent gmc = (FirstPersonMovingComponent) group.cl.get(0);
        }
    }

    static Point lastpoint;

    /**
     *
     */
    @Override
    final public void update(EcsEntity entity, EcsGroup group, double delta) {

        FirstPersonMovingComponent fpmc = (FirstPersonMovingComponent) group.cl.get(0);

        if (!fpmc.initialLocated && viewPoints.size() > 0) {
            // Set initial position to first viewpoint
            fpmc.getFirstPersonTransformer().getTransform().setPosition(viewPoints.get(0).transform.position);
            fpmc.getFirstPersonTransformer().getTransform().setRotation(viewPoints.get(0).transform.rotation);
            fpmc.initialLocated = true;
        }
        fpmc.autoMoveByDelta(delta);

        if (useMouseControl) {
            Point point = Input.getMouseMove();
            if (point != null) {
                if (lastpoint != null) {

                    logger.debug("mouse move " + point);
                    int dx = point.getX() - lastpoint.getX();
                    int dy = point.getY() - lastpoint.getY();
                    fpmc.getFirstPersonTransformer().mouseMove(dx, dy);
                }
                lastpoint = point;
            }
        }
    }

    @Override
    public boolean processRequest(Request request) {
        if (firstpersonmovingsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }

        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_FORWARD) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_BACK)) {
            int userEntityId = (int) request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
            //FirstPersonController.moveForward(userEntity.getSceneNode().getTransform(), 0.2);

            Matrix4 m4 = userEntity.getSceneNode().getTransform().getLocalModelMatrix();

            Vector3 refVector = new Vector3(0, 0, -1);
            m4 = m4.multiply(Matrix4.buildTransformationMatrix(refVector, m4.extractQuaternion()));
            //userEntity.getSceneNode().getTransform().setPosition(m4.extractPosition());
            //userEntity.getSceneNode().getTransform().setRotation(m4.extractQuaternion());
            fpmc.moveForwardByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_FORWARD) ? assumedDeltaTimeWhenStepping : -assumedDeltaTimeWhenStepping);
            //  movedirection = orientation.forward +
            if (firstpersonmovingsystemdebuglog) {
                logger.debug("new position:" + fpmc.getFirstPersonTransformer().getTransform().getPosition());
            }
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNRIGHT)) {
            int userEntityId = (int) request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
            fpmc.getFirstPersonTransformer().incHeadingByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT) ? assumedDeltaTimeWhenStepping : -assumedDeltaTimeWhenStepping);
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNUP) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNDOWN)) {
            int userEntityId = (int) request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
            fpmc.getFirstPersonTransformer().incPitchByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNUP) ? assumedDeltaTimeWhenStepping : -assumedDeltaTimeWhenStepping);
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_ROLLLEFT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_ROLLRIGHT)) {
            int userEntityId = (int) request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
            fpmc.getFirstPersonTransformer().incRollByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_ROLLLEFT) ? assumedDeltaTimeWhenStepping : -assumedDeltaTimeWhenStepping);
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_FORWARD) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_FORWARD)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoForward());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_BACK) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_BACK)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoBack());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNLEFT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNLEFT)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoTurnleft());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNRIGHT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNRIGHT)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoTurnright());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNUP) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNUP)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoTurnup());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNDOWN) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNDOWN)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoTurndown());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLLEFT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLLEFT)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoRollleft());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLRIGHT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLRIGHT)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoRollright());
            return true;
        }
        return false;
    }

    @Override
    public void process(Event evt) {
        if (firstpersonmovingsystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }
        if (evt.getType().equals(BaseEventRegistry.EVENT_USER_ASSEMBLED)) {
            int userEntityId = (int) ((Integer) evt.getPayload().get("userentityid"));
            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);

            userEntity.addComponent(new FirstPersonMovingComponent(userEntity.getSceneNode().getTransform()));
        }
        if (evt.getType().equals(BaseEventRegistry.USER_EVENT_VIEWPOINT)) {
            Payload payload = evt.getPayload();
            LocalTransform localTransform = new LocalTransform(payload.getPosition(), payload.getRotation(), payload.getScale());
            ViewPoint viewPoint = new ViewPoint(payload.getName(), localTransform);
            viewPoints.add(viewPoint);
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static void addDefaultKeyBindings(InputToRequestSystem inputToRequestSystem) {
        // use continuous movement
        inputToRequestSystem.addKeyMapping(KeyCode.W, BaseRequestRegistry.TRIGGER_REQUEST_START_FORWARD);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.W, BaseRequestRegistry.TRIGGER_REQUEST_STOP_FORWARD);

        inputToRequestSystem.addKeyMapping(KeyCode.S, BaseRequestRegistry.TRIGGER_REQUEST_START_BACK);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.S, BaseRequestRegistry.TRIGGER_REQUEST_STOP_BACK);

        inputToRequestSystem.addKeyMapping(KeyCode.LeftArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNLEFT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.LeftArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNLEFT);
        inputToRequestSystem.addKeyMapping(KeyCode.RightArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNRIGHT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.RightArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNRIGHT);

        inputToRequestSystem.addKeyMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNUP);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNUP);
        inputToRequestSystem.addKeyMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNDOWN);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNDOWN);

        // use a/d for rolling, which will also be available in VR by default
        inputToRequestSystem.addKeyMapping(KeyCode.A, BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLLEFT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.A, BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLLEFT);
        inputToRequestSystem.addKeyMapping(KeyCode.D, BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLRIGHT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.D, BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLRIGHT);
    }

    private void firstPersonMovingComponent(Request request, GeneralParameterHandler<FirstPersonMovingComponent> handler) {
        int userEntityId = (int) request.getUserEntityId();

        EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
        FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
        if (fpmc != null) {
            handler.handle(fpmc);
        }
    }
}
