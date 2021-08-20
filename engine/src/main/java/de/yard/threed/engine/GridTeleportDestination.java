package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;

public class GridTeleportDestination {

    public LocalTransform transform;
    public Character direction;

    public GridTeleportDestination(LocalTransform transform, char direction) {
        this.transform = transform;
        this.direction = direction;
    }

    public GridTeleportDestination(LocalTransform transform) {
        this.transform = transform;
    }

}
