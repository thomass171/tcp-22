package de.yard.threed.engine.gui;

import de.yard.threed.core.Util;

public class NumericDisplayFormatter implements DisplayFormatter {
    int precision;

    public NumericDisplayFormatter(int precision) {
        this.precision = precision;
    }

    @Override
    public String getDisplayValue(double value) {
        return Util.format(value, 10, precision);
    }
}
