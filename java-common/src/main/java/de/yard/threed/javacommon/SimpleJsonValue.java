package de.yard.threed.javacommon;



import de.yard.threed.core.platform.*;

import java.util.List;
import java.util.Map;

public abstract class SimpleJsonValue implements NativeJsonValue {
    //SimpleJsonArray array;
    boolean a,o,s,n;
    
    public SimpleJsonValue(Map<Object,Object> map) {
        
    }

    public SimpleJsonValue() {
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

    public static NativeJsonValue buildJsonValue(Object o){
        if (o==null) {
            return null;
        }
        if (o instanceof Map/*illegal <Object,Object>C# manuel*/){
            return new SimpleJsonObject((Map<Object,Object>)o);
        }
        if (o instanceof String){
            return new SimpleJsonString((String)o);
        }
        if (o instanceof List/*illegal <Object,Object>C# manuel*/){
            return new SimpleJsonArray((List<SimpleJsonValue>)o);
        }
        if (o instanceof Double){
            return new SimpleJsonNumber((Double)o);
        }
        throw new RuntimeException("invalid json object "/*+o.getClass().getName()*/);
    }
}
