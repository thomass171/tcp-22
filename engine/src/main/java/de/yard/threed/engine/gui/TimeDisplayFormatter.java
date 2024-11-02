package de.yard.threed.engine.gui;

public class TimeDisplayFormatter implements DisplayFormatter {
    @Override
    public String getDisplayValue(double value) {
        int minutesFromMidnight = (int) value;
        int hours = minutesFromMidnight / 60;
        int minutes = minutesFromMidnight % 60;
        return ((hours > 9) ? "" : "0") + hours + ((minutes > 9) ? ":" : ":0") + minutes;
    }
}
