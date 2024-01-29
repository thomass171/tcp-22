package de.yard.threed.engine.vr;

import de.yard.threed.core.ValueWrapper;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.Observer;

public class VrOffsetWrapper implements ValueWrapper<Double> {

    public VrOffsetWrapper() {
    }

    @Override
    public Double value(Double value) {
        Vector3 v = Observer.getInstance().fineTuneOffset;
        if (value!=null) {
            Observer.getInstance().initFineTune(new Vector3(v.getX(), value, v.getZ()));
        }
        return Observer.getInstance().fineTuneOffset.getY();
    }
}
