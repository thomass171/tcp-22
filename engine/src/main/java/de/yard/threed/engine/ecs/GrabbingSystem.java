package de.yard.threed.engine.ecs;


import de.yard.threed.core.EventType;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.RequestPopulator;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.vr.VRController;
import de.yard.threed.engine.vr.VrInstance;

import java.util.List;

/**
 * For grabbing entities.
 * <p>
 * Created by thomass on 26.01.24.
 */
public class GrabbingSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(GrabbingSystem.class);
    public static String TAG = "GrabbingSystem";

    /**
     *
     */
    private GrabbingSystem() {
        super(new String[]{FirstPersonMovingComponent.TAG}, new RequestType[]{
                        BaseRequestRegistry.TRIGGER_REQUEST_START_GRABBING,
                        BaseRequestRegistry.TRIGGER_REQUEST_STOP_GRABBING
                },
                new EventType[]{});
    }

    public static GrabbingSystem buildFromConfiguration() {
        return new GrabbingSystem();
    }

    /**
     *
     */
    @Override
    final public void update(EcsEntity entity, EcsGroup group, double delta) {

        GrabbingComponent gc = GrabbingComponent.getGrabbingComponent(entity);

    }

    @Override
    public boolean processRequest(Request request) {
        logger.debug("got request " + request.getType());

        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_START_GRABBING)) {
            tryGrab(request);
            return true;
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_STOP_GRABBING)) {
            unGrab(request);
            return true;
        }
        return false;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static void addDefaultKeyBindings(InputToRequestSystem inputToRequestSystem) {

        // grabposition not added to payload for now, maybe later. Receiver also needs the controller for attaching the grabbed object anyway.
        // request.getPayload().add(inputToRequestSystem.getGrabPosition());
        inputToRequestSystem.addKeyMapping(KeyCode.G, BaseRequestRegistry.TRIGGER_REQUEST_START_GRABBING,
                request -> request.getPayload().add("controllerIndex", "0"));
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.G, BaseRequestRegistry.TRIGGER_REQUEST_STOP_GRABBING,
                request -> request.getPayload().add("controllerIndex", "0"));

        inputToRequestSystem.addKeyMapping(KeyCode.J, BaseRequestRegistry.TRIGGER_REQUEST_START_GRABBING,
                request -> request.getPayload().add("controllerIndex", "1"));
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.J, BaseRequestRegistry.TRIGGER_REQUEST_STOP_GRABBING,
                request -> request.getPayload().add("controllerIndex", "1"));
    }

    public static List<EcsEntity> getTransformables() {
        return EcsHelper.findEntitiesByComponent(GrabbingComponent.TAG);
    }

    private void tryGrab(Request request) {

        int userEntityId = (int) request.getUserEntityId();
        int controllerIndex = request.getPayload().getAsInt("controllerIndex");
        //we need to find scene node anyway, so no benefit to pass position.
        //Vector3 grabPosition = request.getPayload().getPosition();
        logger.debug("tryGrab for controllerIndex " + controllerIndex);

        // The controller is needed for both knowing where to look for a grabbed object and
        // where to attach the grabbed object. So for now its easier to access the controller here instead of passing the information
        // via request.
        if (VrInstance.getInstance() != null) {
            VRController controller = VrInstance.getInstance().getController(controllerIndex);
            Vector3 wp = controller.getWorldPosition();

            for (EcsEntity entity : getTransformables()) {
                if (GrabbingComponent.getGrabbingComponent(entity).grabs(wp)) {
                    GrabbingComponent.getGrabbingComponent(entity).grabbedBy = controllerIndex;
                    entity.getSceneNode().getTransform().setPosition(new Vector3());
                    controller.attach(entity.getSceneNode());
                    logger.debug("grabbed");
                }
            }
        }
    }

    private void unGrab(Request request) {
        int userEntityId = (int) request.getUserEntityId();
        int controllerIndex = request.getPayload().getAsInt("controllerIndex");

        for (EcsEntity entity : getTransformables()) {
            GrabbingComponent gc = GrabbingComponent.getGrabbingComponent(entity);
            if (gc.grabbedBy == controllerIndex) {
                Transform grabbedTransform = entity.getSceneNode().getTransform();
                Vector3 wp = grabbedTransform.getWorldPosition();
                Quaternion wr = grabbedTransform.getWorldRotation();
                logger.debug("ungrab at " + wp);
                grabbedTransform.setParent(null);
                grabbedTransform.setPosition(wp);
                grabbedTransform.setRotation(wr);
                gc.grabbedBy = -1;
            }
        }
    }
}
