package de.yard.threed.engine.gui;

import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.ValueWrapper;

public class NumericSpinnerHandler implements SpinnerHandler {

    private double step;
    private ValueWrapper<Double> valueWrapper;
    // Have clear limit for overflow, so int.
    private Integer overflowThreshold = null;
    private DisplayFormatter displayFormatter = null;

    public NumericSpinnerHandler(double step, ValueWrapper<Double> valueWrapper, Integer overflowThreshold, DisplayFormatter displayFormatter) {
        this.step = step;
        this.valueWrapper = valueWrapper;
        this.overflowThreshold = overflowThreshold;
        this.displayFormatter = displayFormatter;
    }

    public NumericSpinnerHandler(double step, ValueWrapper<Double> valueWrapper) {
        this.step = step;
        this.valueWrapper = valueWrapper;
    }

    @Override
    public void up() {
        updateValue(step);
    }

    @Override
    public String getDisplayValue() {
        if (displayFormatter != null) {
            return displayFormatter.getDisplayValue(valueWrapper.value(null));
        }
        return "" + valueWrapper.value(null);
    }

    @Override
    public void down() {
        updateValue(-step);
    }

    private void updateValue(double s) {

        double currentValue = valueWrapper.value(null);
        double newValue = currentValue + s;
        if (overflowThreshold != null) {
            if (Math.round(newValue) >= overflowThreshold) {
                newValue = newValue - overflowThreshold;
            }
            if (newValue < 0) {
                newValue = overflowThreshold + newValue;
            }
        }
        valueWrapper.value(newValue);

    }
}
