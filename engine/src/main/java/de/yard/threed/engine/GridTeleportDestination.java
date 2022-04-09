package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;

public class GridTeleportDestination {

    private LocalTransform transform;
    // null if there is no change in direction. String instead of Character for easier CS operating.
    private String direction;

    public GridTeleportDestination(LocalTransform transform, String direction) {
        this.transform = transform;
        this.direction = direction;
    }

    public GridTeleportDestination(LocalTransform transform) {
        this.transform = transform;
    }

    public LocalTransform getTransform() {
        return transform;
    }

    /**
     * String instead of Character for easier CS operating
     * @return
     */
    public String getDirection() {
        return direction;
    }
}
