package de.yard.threed.core;

/**
 * threshold value
 */
public class Threshold {

    long value = 0;
    long threadholdValue;

    public Threshold(long threadholdValue) {
        this.threadholdValue = threadholdValue;
    }

    public boolean reached(long increment) {
        value+=increment;
        if (value>=threadholdValue){
            value=0;
            return true;
        }
        return false;
    }

}
