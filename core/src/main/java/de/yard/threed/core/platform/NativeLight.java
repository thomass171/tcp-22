package de.yard.threed.core.platform;

import de.yard.threed.core.Color;
import de.yard.threed.core.Vector3;

/**
 * Created by thomass on 05.06.15.
 * 
 * 23.3.17: Light is no longer standalone part of Scenegraph, but like mesh a component of a SceneNode (analog Unity)
 */
public interface NativeLight {

    /**
     * @return null if not ambient
     */
    Color getAmbientColor();

    /**
     * @return null if not directional
     */
    Color getDirectionalColor();

    Vector3 getDirectionalDirection();

    /**
     * Who owns me? The SceneNode to which this is a component. Should(?) not be null.
     */
    //20.2.25 not yet NativeSceneNode getSceneNode();

}
