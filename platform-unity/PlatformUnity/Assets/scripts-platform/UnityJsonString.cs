/*generated*/
using System;
using java.lang;
using SimpleJSON;

namespace de.yard.threed.platform.unity {
    using de.yard.threed.core.platform;
    using de.yard.threed.platform;// import de.yard.threed.platform.NativeJsonArray;
// import de.yard.threed.platform.NativeJsonNumber;
// import de.yard.threed.platform.NativeJsonObject;
// import de.yard.threed.platform.NativeJsonString;

public class UnityJsonString  :  UnityJsonValue  ,  NativeJsonString {
    public string value;
    
    public UnityJsonString(string value) {
        this.value = value;
        s=true;
    }

    virtual 
    public string stringValue() {
        return value;
    }
}
}