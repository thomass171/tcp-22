package de.yard.threed.platform.webgl;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import de.yard.threed.core.platform.NativeJsonNumber;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonString;
import de.yard.threed.core.platform.NativeJsonValue;

public class WebGlJsonObject extends WebGlJsonValue implements NativeJsonObject {
    JSONObject value;
    
    public WebGlJsonObject(JSONObject value) {
        this.value = value;
        o = true;
    }

    @Override
    public NativeJsonValue get(String key) {
        JSONValue o = value.get(key);
        return buildJsonValue(o);
    }

    @Override
    public int getInt(String key) {
        NativeJsonValue v = get(key);
        if (v==null){
            return -1;
        }
        NativeJsonNumber vi = v.isNumber();
        if (vi==null){
            return -1;
        }
        return vi.intValue();
    }

    @Override
    public String getString(String key) {
        NativeJsonValue v = get(key);
        if (v==null){
            return null;
        }
        NativeJsonString vi = v.isString();
        if (vi==null){
            return null;
        }
        return vi.stringValue();
    }


}
