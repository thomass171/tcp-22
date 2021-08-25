/*generated*/
using System;
using java.lang;
using SimpleJSON;

namespace de.yard.threed.platform.unity {
    using de.yard.threed.core.platform;
    using de.yard.threed.platform;// import de.yard.threed.platform.NativeJsonNumber;

public class UnityJsonNumber  :  UnityJsonValue  ,  NativeJsonNumber {
        public JSONNumber value;
    
        public UnityJsonNumber(JSONNumber value) {
        this.value = value;
        n=true;
    }


    virtual 
    public double doubleValue() {
            return value.AsDouble;
    }

    virtual 
    public int intValue() {
            return value.AsInt;
    }
}
}