package de.yard.threed.traffic;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.traffic.apps.BasicTravelScene;

/**
 * Generic flying of a non graph bound plane entity with FreeFlyingComponent.
 * <p>
 * Characteristics are:
 * - vehicle can speed up/down
 * - vehicle will not ... terrain
 * Alternative to GraphMovingSystem
 * Start position is unset, so on (0,0,0).
 * For simplification and avoiding confusion with controls/requests 'aircraft flying' split from FirstPersonMovingSystem.
 * No viewpoints like in FirstPersonMovingSystem.
 * <p>
 */
public class FreeFlyingSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(FreeFlyingSystem.class);
    public static String TAG = "FreeFlyingSystem";
    public boolean freeflyingsystemdebuglog = true;

    /**
     *
     */
    private FreeFlyingSystem() {
        // Add VelocityComponent for getting movement speed like we do in GraphMovingSystem
        super(new String[]{FreeFlyingComponent.TAG, VelocityComponent.TAG},
                new RequestType[]{
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
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLRIGHT,

                },
                //not needed?
                new EventType[]{BaseEventRegistry.EVENT_USER_ASSEMBLED
                });
    }

    public static FreeFlyingSystem buildFromConfiguration() {
        return new FreeFlyingSystem();
    }

    /**
     * @param group
     */
    @Override
    public void init(EcsGroup group) {
        if (group != null) {

        }
    }

    /**
     *
     */
    @Override
    final public void update(EcsEntity entity, EcsGroup group, double delta) {

        FreeFlyingComponent ffc = (FreeFlyingComponent) group.cl.get(0);
        VelocityComponent vc = (VelocityComponent) group.cl.get(1);

        ffc.updateByDelta(delta, vc.getMovementSpeed());

        ffc.checkForPositionUpdate();
    }

    @Override
    public boolean processRequest(Request request) {
        if (freeflyingsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }

        int userEntityId = (int) request.getUserEntityId();
        EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);


        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNLEFT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNLEFT)) {
            processOnComponent(request, rbmc -> rbmc.toggleAutoTurnleft());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNRIGHT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNRIGHT)) {
            processOnComponent(request, rbmc -> rbmc.toggleAutoTurnright());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNUP) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNUP)) {
            processOnComponent(request, rbmc -> rbmc.toggleAutoTurnup());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_TURNDOWN) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNDOWN)) {
            processOnComponent(request, rbmc -> rbmc.toggleAutoTurndown());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLLEFT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLLEFT)) {
            processOnComponent(request, rbmc -> rbmc.toggleAutoRollleft());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLRIGHT) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLRIGHT)) {
            processOnComponent(request, rbmc -> rbmc.toggleAutoRollright());
            return true;
        }
        // SPEEDUP/DOWN moved to VelocitySystem
        return false;
    }

    @Override
    public void process(Event evt) {
        if (freeflyingsystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }
        /*probably not needed if (evt.getType().equals(BaseEventRegistry.EVENT_USER_ASSEMBLED)) {
            int userEntityId = (int) ((Integer) evt.getPayload().get("userentityid"));
            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);

            userEntity.addComponent(new FirstPersonMovingComponent(userEntity.getSceneNode().getTransform()));
        }*/

    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Binds keys for movement control, which is more intuitive. So there are requests for start and stop of roll/turn
     * by cursor keys. PGUP/DOWN is for speed up/down.
     * Also mouse drag for moving (useful for touchscreen).
     */
    public static void addDefaultKeyBindingsforContinuousMovement(InputToRequestSystem inputToRequestSystem) {
        // forward/back is part of auto move of the rigid body
        inputToRequestSystem.addKeyMapping(KeyCode.LeftArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNLEFT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.LeftArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNLEFT);
        inputToRequestSystem.addKeyMapping(KeyCode.RightArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNRIGHT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.RightArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNRIGHT);

        if (VrInstance.getInstance() != null) {
            // Moving the VR controller stick forward triggers UpArrow, thus pitch up. But that
            // isn't intuitive for aircraft controlling, where moving the yoke forward pitches down. So revert.
            inputToRequestSystem.addKeyMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNUP);
            inputToRequestSystem.addKeyReleaseMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNUP);
            inputToRequestSystem.addKeyMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNDOWN);
            inputToRequestSystem.addKeyReleaseMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNDOWN);
        } else {
            inputToRequestSystem.addKeyMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNUP);
            inputToRequestSystem.addKeyReleaseMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNUP);
            inputToRequestSystem.addKeyMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNDOWN);
            inputToRequestSystem.addKeyReleaseMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNDOWN);
        }
        inputToRequestSystem.addKeyMapping(KeyCode.PageUp, BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDUP);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.PageUp, BaseRequestRegistry.TRIGGER_REQUEST_STOP_SPEEDUP);
        inputToRequestSystem.addKeyMapping(KeyCode.PageDown, BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDDOWN);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.PageDown, BaseRequestRegistry.TRIGGER_REQUEST_STOP_SPEEDDOWN);

        // As long as we don't have auto-roll use a/d for rolling, which will also be available in VR by default
        inputToRequestSystem.addKeyMapping(KeyCode.A, BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLLEFT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.A, BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLLEFT);
        inputToRequestSystem.addKeyMapping(KeyCode.D, BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLRIGHT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.D, BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLRIGHT);

        inputToRequestSystem.setDragMapping(BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT, BaseRequestRegistry.TRIGGER_REQUEST_TURNRIGHT,
                BaseRequestRegistry.TRIGGER_REQUEST_TURNDOWN, BaseRequestRegistry.TRIGGER_REQUEST_TURNUP,
                null, null);

    }

    private void processOnComponent(Request request, GeneralParameterHandler<FreeFlyingComponent> handler) {
        /*int userEntityId = (int) request.getUserEntityId();

        EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);*/
        EcsEntity vehicleEntity = TeleporterSystem.getTeleportEntity();
        if (vehicleEntity == null) {
            logger.warn("no vehicle entity in TC");
            return;
        }
        FreeFlyingComponent rbmc = FreeFlyingComponent.getFreeFlyingComponent(vehicleEntity);
        if (rbmc != null) {
            handler.handle(rbmc);
        }
    }
}
