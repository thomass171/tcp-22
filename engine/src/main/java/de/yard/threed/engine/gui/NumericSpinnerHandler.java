package de.yard.threed.engine.gui;

import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.ValueWrapper;

public class NumericSpinnerHandler implements SpinnerHandler {

    private double step;
    private ValueWrapper<Double> valueWrapper;

    public NumericSpinnerHandler(double step, ValueWrapper<Double> valueWrapper) {
        this.step = step;
        this.valueWrapper = valueWrapper;
    }

    @Override
    public void up() {
        updateValue(step);
    }

    @Override
    public String getValue() {
        return "" + valueWrapper.value(null);
    }

    @Override
    public void down() {
        updateValue(-step);
    }

    private void updateValue(double s) {

        double d = valueWrapper.value(null);
        valueWrapper.value(d+s);
    }
}
