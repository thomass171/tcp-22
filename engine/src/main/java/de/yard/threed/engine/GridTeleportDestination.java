package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;

public class GridTeleportDestination {

    private LocalTransform transform;
    // null if there is no change in direction
    private Character direction;

    public GridTeleportDestination(LocalTransform transform, char direction) {
        this.transform = transform;
        this.direction = direction;
    }

    public GridTeleportDestination(LocalTransform transform) {
        this.transform = transform;
    }

    public LocalTransform getTransform() {
        return transform;
    }

    public Character getDirection() {
        return direction;
    }
}
