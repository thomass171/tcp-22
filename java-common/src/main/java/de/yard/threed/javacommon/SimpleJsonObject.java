package de.yard.threed.javacommon;



import de.yard.threed.core.platform.NativeJsonNumber;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonString;
import de.yard.threed.core.platform.NativeJsonValue;

import java.util.Map;

public class SimpleJsonObject extends SimpleJsonValue implements NativeJsonObject {
    Map<Object,Object> map;

    public SimpleJsonObject(Map<Object,Object> map) {
        this.map = map;
        o = true;
    }

    @Override
    public NativeJsonValue get(String key) {
        Object o = map.get(key);
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
