/*generated*/
using System;
using java.lang;
using SimpleJSON;
using de.yard.threed.platform;
using java.util;
using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{

    public class UnityJsonArray  :  UnityJsonValue  ,  NativeJsonArray
    {
        public  JSONArray value;

        public UnityJsonArray (JSONArray value)
        {
            this.value = value;
            a = true;
        }

        virtual     public NativeJsonValue get (int index)
        {
            return UnityJsonObject.buildJsonValue (value [index]);
        }

        virtual     public int size ()
        {
            return value.Count;
        }
    }
}