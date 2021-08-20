package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Color;

/**
 * 23.3.17 Die Verwendung ist noch weitgehend unklar. Besser erstmal Directional.
 * Evtl. nicht in alklen Platformen und performancehungrig?
 * Darum Deprecated.
 * 
 * Date: 09.07.14
 */
@Deprecated
public class PointLight extends Light {
    public PointLight(Color color) {
        //this.color = color;
        nativelight = Platform.getInstance().buildPointLight(color);
    }

    /*public Vector3 getPosition() {
        return new Vector3(nativelight.getPosition());
    }

    public void setPosition(Vector3 pos) {
        nativelight.setPosition(pos.vector3);
    }*/

}
