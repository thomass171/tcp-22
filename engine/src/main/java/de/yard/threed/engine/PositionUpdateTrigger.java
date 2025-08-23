package de.yard.threed.engine;

import de.yard.threed.core.Vector3;
import de.yard.threed.engine.ecs.SystemManager;

import static de.yard.threed.engine.ecs.TeleporterSystem.buildPositionChanged;

public class PositionUpdateTrigger {
    Vector3 lastWorldPositionForPositionUpdate = null;

    private boolean farAwayForPositionUpdate(Transform transform) {
        Vector3 currentPosition = transform.getWorldPosition();
        // try with 800 meter
        if (lastWorldPositionForPositionUpdate == null || Vector3.getDistance(currentPosition, lastWorldPositionForPositionUpdate) > 800) {
            lastWorldPositionForPositionUpdate = currentPosition;
            return true;
        }
        return false;
    }

    /**
     * 21.8.25: Check movement of 'user'(?) for possible terrain load requests (via positionchange)
     */
    public void checkForPositionUpdate(Transform transform) {

        // should not run every frame
        if (farAwayForPositionUpdate(transform)) {
            SystemManager.sendEvent(buildPositionChanged(lastWorldPositionForPositionUpdate));
        }
    }
}
