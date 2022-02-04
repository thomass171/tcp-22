package de.yard.threed.engine.gui;

import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.Observer;

public class VrOffsetHandler implements SpinnerHandler {

    public VrOffsetHandler() {
    }

    @Override
    public void up() {
        updateValue(0.1);
    }

    @Override
    public String getValue() {
        return "" + Observer.getInstance().getFinetune().getY();
    }

    @Override
    public void down() {
        updateValue(-0.1);
    }

    private void updateValue(double offset) {

        Vector3 v = Observer.getInstance().fineTuneOffset;
        Observer.getInstance().initFineTune(new Vector3(v.getX(), v.getY() + offset, v.getZ()));
    }
}
