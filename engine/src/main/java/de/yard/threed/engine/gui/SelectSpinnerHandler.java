package de.yard.threed.engine.gui;

import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.ValueWrapper;

public class SelectSpinnerHandler implements SpinnerHandler {

    private int current = 0;
    private String[] values;
    private ValueWrapper<String> valueWrapper;

    public SelectSpinnerHandler(String[] values, ValueWrapper<String> valueWrapper) {
        this.values = values;
        this.valueWrapper = valueWrapper;
    }

    @Override
    public void up() {
        updateValue(1);
    }

    @Override
    public String getDisplayValue() {
        return values[current];
    }

    @Override
    public void down() {
        updateValue(-1);
    }

    private void updateValue(int s) {
        current += s;
        if (current < 0) {
            current = 0;
            return;
        }
        if (current > values.length - 1) {
            current = values.length - 1;
            return;
        }
        valueWrapper.value(values[current]);
    }
}
