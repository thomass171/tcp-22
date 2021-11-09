package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;

public class ViewPoint {
    public String name;
    public LocalTransform transform;

    public ViewPoint(String name, LocalTransform transform) {
        this.name=name;
        this.transform=transform;
    }

}