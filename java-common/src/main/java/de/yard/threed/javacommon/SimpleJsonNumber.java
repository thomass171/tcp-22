package de.yard.threed.javacommon;


import de.yard.threed.core.platform.NativeJsonNumber;

public class SimpleJsonNumber extends SimpleJsonValue implements NativeJsonNumber {
    double value;
    
    public SimpleJsonNumber(double value) {
        this.value = value;
        n=true;
    }


    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public int intValue() {
        return (int)value;
    }
}
