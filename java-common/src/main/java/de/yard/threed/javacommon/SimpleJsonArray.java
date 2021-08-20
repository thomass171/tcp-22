package de.yard.threed.javacommon;



import de.yard.threed.core.platform.NativeJsonArray;
import de.yard.threed.core.platform.NativeJsonValue;

import java.util.List;

public class SimpleJsonArray extends SimpleJsonValue implements NativeJsonArray {
    List<SimpleJsonValue> value;
    
    public SimpleJsonArray(List<SimpleJsonValue> value) {
        this.value = value;
        a=true;
    }
    
    @Override
    public NativeJsonValue get(int index) {
        return SimpleJsonObject.buildJsonValue(value.get(index));
    }

    @Override
    public int size() {
        return value.size();
    }
}
