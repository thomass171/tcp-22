package de.yard.threed.platform.webgl;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import de.yard.threed.core.platform.NativeJsonArray;
import de.yard.threed.core.platform.NativeJsonNumber;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonString;
import de.yard.threed.core.platform.NativeJsonValue;

public abstract class WebGlJsonValue implements NativeJsonValue {
    JSONValue value;
    boolean a,o,s,n;
    
    public WebGlJsonValue(JSONValue value) {
        this.value = value;
    }

    public WebGlJsonValue() {
    }

    @Override
    public NativeJsonArray isArray() {
        return (a)?((NativeJsonArray)this):null;
    }

    @Override
    public NativeJsonObject isObject() {
        return (o)?((NativeJsonObject)this):null;
    }

    @Override
    public NativeJsonString isString() {
        return (s)?((NativeJsonString)this):null;
    }

    @Override
    public NativeJsonNumber isNumber() {
        return (n)?((NativeJsonNumber)this):null;
    }

    public static NativeJsonValue buildJsonValue(JSONValue value){
        if (value==null) {
            return null;
        }

        JSONString s = value.isString();
        if (s!=null){
            return (new WebGlJsonString(s));
        }
        JSONArray a = value.isArray();
        if (a!=null){
            return (new WebGlJsonArray(a));
        }
        JSONNumber n = value.isNumber();
        if (n!=null){
            return (new WebGlJsonNumber(n));
        }
        JSONObject o = value.isObject();
        if (o!=null){
            return (new WebGlJsonObject(o));
        }
        throw new RuntimeException("invalid json");
    }
}
