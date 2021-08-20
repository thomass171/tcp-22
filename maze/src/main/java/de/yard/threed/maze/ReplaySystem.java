package de.yard.threed.maze;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.platform.common.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Replay a set of movements.
 *
 * Sends from a list of requests, when no mover isType currently moving.
 *
 * <p>
 * Created by thomass on 14.04.21.
 */

public class ReplaySystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(ReplaySystem.class);

    boolean replaysystemdebuglog = true;

    List<Request> replayList = new ArrayList<Request>();

    /**
     *
     */
    public ReplaySystem() {
        // no update per ground
    }


    /**
     * Only called once per frame.
     *
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        if (replaysystemdebuglog) {
            //logger.debug("got event " + evt.getType());
        }

        boolean someoneMoving = false;

        for (EcsEntity e : SystemManager.findEntities((EntityFilter) null)) {
            MoverComponent mc = MoverComponent.getMoverComponent(e);
            if (mc != null && mc.isMoving()){
                someoneMoving=true;
                break;
            }
        }

        if (!someoneMoving && replayList.size()>0) {
            Request request = replayList.remove(0);
            logger.debug("Replaying "+request.getType());
            SystemManager.putRequest(request);
        }

    }

    public void addRequests(Request request) {
        replayList.add(request);
    }
}
