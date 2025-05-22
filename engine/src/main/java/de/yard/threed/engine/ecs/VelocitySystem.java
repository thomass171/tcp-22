package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

import java.util.List;

/**
 * System for controlling entities VelocityComponent.
 * New natural home for SPEEDUP/DOWN request handling. Moved here from FreeFlyingSystem.
 *
 * <p>
 * Created by thomass on 16.05.25.
 */

public class VelocitySystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(VelocitySystem.class);
    public static String TAG = "VelocitySystem";
    boolean velocitysystemdebuglog = true;

    /**
     *
     */
    public VelocitySystem() {
        super(new String[]{VelocityComponent.TAG}, new RequestType[]{
                        BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDUP,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_SPEEDUP,
                        BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDDOWN,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_SPEEDDOWN,
                        BaseRequestRegistry.TRIGGER_REQUEST_SPEEDUP,
                        BaseRequestRegistry.TRIGGER_REQUEST_SPEEDDOWN,
                },
                new EventType[]{});
    }

    /**
     *
     */
    @Override
    final public void update(EcsEntity entity, EcsGroup group, double delta) {

        VelocityComponent vc = (VelocityComponent) group.cl.get(0);

        vc.updateByDelta(delta);
    }

    @Override
    public boolean processRequest(Request request) {
        if (velocitysystemdebuglog) {
            logger.debug("got request " + request);
        }
        int userEntityId = (int) request.getUserEntityId();
        EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);

        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDUP) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_SPEEDUP)) {
            processOnComponent(vc -> vc.toggleAutoSpeedUp());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDDOWN) || request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_SPEEDDOWN)) {
            processOnComponent(vc -> vc.toggleAutoSpeedDown());
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_SPEEDUP)) {
            // we only have delta in update, though just a 0.1
            processOnComponent(vc -> vc.accelerate(0.1));
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_SPEEDDOWN)) {
            // we only have delta in update, though just a -0.1
            processOnComponent(vc -> vc.accelerate(-0.1));
            return true;
        }
        return false;
    }

    @Override
    public void process(Event evt) {
        if (velocitysystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private void processOnComponent(GeneralParameterHandler<VelocityComponent> handler) {
        EcsEntity vehicleEntity = TeleporterSystem.getTeleportEntity();
        if (vehicleEntity == null) {
            logger.warn("no vehicle entity in TC");
            return;
        }
        VelocityComponent rbmc = VelocityComponent.getVelocityComponent(vehicleEntity);
        if (rbmc != null) {
            handler.handle(rbmc);
        }
    }
}
