package de.yard.threed.engine;

import de.yard.threed.core.platform.NativeLight;

/**
 * Light is like a mesh a component of a scene node, though its standalone in some platforms (JME, ThreeJS)
 * A light can be removed by removing the node.
 * 20.2.25 This sounds inconsistent for ambient/directional, which have no position. But it's consistent in terms of components
 * added to nodes (the Unity way).
 * <p/>
 * Date: 04.07.14
 */
public abstract class Light {
    public NativeLight nativelight;
}
