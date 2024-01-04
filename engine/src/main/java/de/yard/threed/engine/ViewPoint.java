package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.util.XmlHelper;

/**
 * Even though we don't want to define model classes for XML structures. This is also used outside XML.
 */
public class ViewPoint {
    public String name;
    public LocalTransform transform;

    public ViewPoint(String name, LocalTransform transform) {
        this.name=name;
        this.transform=transform;
    }
}