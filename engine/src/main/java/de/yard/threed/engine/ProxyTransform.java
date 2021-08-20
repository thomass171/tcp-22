package de.yard.threed.engine;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.NativeTransform;

/**
 * Ein Tranform, der die Rotation an einen Slave 1:1 spiegelt.
 */
public class ProxyTransform extends Transform {
    private Transform slaveTransform;

    public ProxyTransform(NativeTransform t,Transform slaveTransform) {
        super(t);
        this.slaveTransform=slaveTransform;
    }

    @Override
    public void setRotation(Quaternion rotation){
        super.setRotation(rotation);
        if (slaveTransform!=null){
            //nicht nur die lokale, sondern globale, sonst ist es ja witzlos fuer korrekte Beleuchtung
            rotation = getSceneNode().getTransform().getWorldModelMatrix().extractQuaternion();
            slaveTransform.setRotation(rotation);
        }
    }
}
