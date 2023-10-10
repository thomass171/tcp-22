package de.yard.threed.engine.ecs;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
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
                        BaseRequestRegistry.TRIGGER_REQUEST_STARTFORWARD,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOPFORWARD
                },
                new EventType[]{BaseEventRegistry.EVENT_USER_ASSEMBLED});
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

        if (fpmc.hasAutoForward()) {
            fpmc.moveForwardByDelta(delta);
        }

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
        // update by delta time to honor defined speeds
        double assumedDeltaTime = 0.1;

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
            fpmc.moveForwardByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_FORWARD) ? assumedDeltaTime : -assumedDeltaTime);
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
            fpmc.getFirstPersonTransformer().incHeadingByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT) ? assumedDeltaTime : -assumedDeltaTime);
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNUP) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNDOWN)) {
            int userEntityId = (int) request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
            fpmc.getFirstPersonTransformer().incPitchByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_TURNUP) ? assumedDeltaTime : -assumedDeltaTime);
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_ROLLLEFT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_ROLLRIGHT)) {
            int userEntityId = (int) request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
            fpmc.getFirstPersonTransformer().incRollByDelta(request.isType(BaseRequestRegistry.TRIGGER_REQUEST_ROLLLEFT) ? assumedDeltaTime : -assumedDeltaTime);
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STARTFORWARD) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOPFORWARD)) {
            firstPersonMovingComponent(request, fpmc -> fpmc.toggleAutoForward());
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
