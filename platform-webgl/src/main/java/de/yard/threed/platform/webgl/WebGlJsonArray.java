package de.yard.threed.platform.webgl;

import com.google.gwt.json.client.JSONArray;
import de.yard.threed.core.platform.NativeJsonArray;
import de.yard.threed.core.platform.NativeJsonValue;

public class WebGlJsonArray extends WebGlJsonValue implements NativeJsonArray {
    JSONArray value;
    
    public WebGlJsonArray(JSONArray value) {
        this.value = value;
        a=true;
    }
    
    @Override
    public NativeJsonValue get(int index) {
        return WebGlJsonObject.buildJsonValue(value.get(index));
    }

    @Override
    public int size() {
        return value.size();
    }
}
