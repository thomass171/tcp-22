package de.yard.threed.engine;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

/**
 * 19.5.21: Extracted interface from Transform for decoupling.
 * <p>
 * Created by thomass on 26.01.17
 */
public interface SimpleTransform {

    Quaternion getRotation();

    Vector3 getPosition();

     void setPosition(Vector3 position) ;

     void setRotation(Quaternion quaternion) ;

     // no scale, no parent, no translate
}
