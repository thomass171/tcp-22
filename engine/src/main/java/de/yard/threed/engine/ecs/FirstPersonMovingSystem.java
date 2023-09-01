package de.yard.threed.engine.ecs;


import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
 *
 * <p>
 * Created by thomass on 26.08.23.
 */
public class FirstPersonMovingSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(FirstPersonMovingSystem.class);
    public static String TAG = "FirstPersonMovingSystem";
    boolean terrainsystemdebuglog = false;

    /**
     *
     */
    public FirstPersonMovingSystem() {
        super(new String[]{FirstPersonMovingComponent.TAG}, new RequestType[]{BaseRequestRegistry.TRIGGER_REQUEST_FORWARD}, new EventType[]{});
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

    /**
     *
     */
    @Override
    final public void update(EcsEntity entity, EcsGroup group, double tpf) {

        FirstPersonMovingComponent tmc = (FirstPersonMovingComponent) group.cl.get(0);

        if (tmc.hasAutomove()) {
            //moveForward(entity, gmc, vc, tpf * vc.movementSpeed);
            //logger.debug("new position of "+entity.getName()+entity.getId()+" isType "+gmc.getPosition());
        }


    }


    @Override
    public boolean processRequest(Request request) {
        if (terrainsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }
       /* if (request.isType(MazeRequestRegistry.TRIGGER_REQUEST_FIRE)) {
        }*/
        return false;
    }


}
