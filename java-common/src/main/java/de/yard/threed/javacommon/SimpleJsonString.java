package de.yard.threed.javacommon;


import de.yard.threed.core.platform.NativeJsonString;

public class SimpleJsonString extends SimpleJsonValue implements NativeJsonString {
    String value;
    
    public SimpleJsonString(String value) {
        this.value = value;
        s=true;
    }

    @Override
    public String stringValue() {
        return value;
    }
}
