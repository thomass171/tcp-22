package de.yard.threed.platform.webgl;

import com.google.gwt.json.client.JSONNumber;
import de.yard.threed.core.platform.NativeJsonNumber;

public class WebGlJsonNumber extends WebGlJsonValue implements NativeJsonNumber {
    JSONNumber value;
    
    public WebGlJsonNumber(JSONNumber value) {
        this.value = value;
        n=true;
    }


    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public int intValue() {
        return (int)doubleValue();
    }
}
