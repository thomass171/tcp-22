/*generated*/
using System;
using java.lang;
using SimpleJSON;
using de.yard.threed.platform;
using java.util;
using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{
    public abstract class UnityJsonValue  :  NativeJsonValue
    {
        //UnityJsonArray array;
        public bool a, o, s, n;

        public UnityJsonValue (Map<Object,Object> map)
        {
        
        }

        public UnityJsonValue ()
        {
        }

        virtual     public NativeJsonArray isArray ()
        {
            return (a) ? ((NativeJsonArray)this) : null;
        }

        virtual     public NativeJsonObject isObject ()
        {
            return (o) ? ((NativeJsonObject)this) : null;
        }

        virtual     public NativeJsonString isString ()
        {
            return (s) ? ((NativeJsonString)this) : null;
        }

        virtual     public NativeJsonNumber isNumber ()
        {
            return (n) ? ((NativeJsonNumber)this) : null;
        }

        public static NativeJsonValue buildJsonValue (Object o)
        {
            if (o == null) {
                return null;
            }
            if (o is JSONLazyCreator || o is JSONNull) {
                //??
                return null;
            }
            // order matters: its important to check array before object
            if (o is JSONArray) {
                return new UnityJsonArray ((JSONArray)o);
            }
            if (o is JSONString) {
                return new UnityJsonString (((JSONString)o).Value);
            }


            if (o is JSONObject) {
                return new UnityJsonObject ((JSONObject)o);
            }
            if (o is JSONNumber) {
                return new UnityJsonNumber ((JSONNumber)o);
            }
            //if (o is JSONLazyCreator) {
             //   return new UnityJsonNumber ((JSONNumber)o);
           // }
            throw new RuntimeException ("invalid json object " + o);
        }
    }
}