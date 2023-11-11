package de.yard.threed.core;

/**
 * Created by thomass on 11.07.23.
 */
public class BooleanHolder {
    public boolean v = false;

    public BooleanHolder() {
    }

    public BooleanHolder(boolean value) {
        v = value;
    }

    public void setValue(boolean i) {
        v = i;
    }

    public boolean getValue() {
        return v;
    }
}
