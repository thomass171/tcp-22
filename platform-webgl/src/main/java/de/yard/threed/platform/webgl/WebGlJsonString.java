package de.yard.threed.platform.webgl;

import com.google.gwt.json.client.JSONString;
import de.yard.threed.core.platform.NativeJsonString;

public class WebGlJsonString extends WebGlJsonValue implements NativeJsonString {
    JSONString value;
    
    public WebGlJsonString(JSONString value) {
        this.value = value;
        s=true;
    }

    @Override
    public String stringValue() {
        return value.stringValue();
    }

    
}
