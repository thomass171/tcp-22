package de.yard.threed.core;

/**
 * Created by thomass on 11.08.16.
 */
public class IntHolder {
    public int v;

    public IntHolder() {
        v = 0;
    }

    public IntHolder(int i) {
        v = i;
    }

    public void setValue(int i) {
        v = i;
    }

    public void inc() {
        v++;
    }

    public int getValue() {
        return v;
    }
}
