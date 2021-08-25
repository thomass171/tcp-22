/*generated*/
using System;
using java.lang;
using SimpleJSON;

namespace de.yard.threed.platform.unity
{
    using de.yard.threed.core.platform;
    using de.yard.threed.platform;
    using java.util;

    public class UnityJsonObject  :  UnityJsonValue  ,  NativeJsonObject
    {
        JSONObject node;

        public UnityJsonObject (JSONObject node)
        {
            this.node = node;
            o = true;
        }

        virtual     public NativeJsonValue get (string key)
        {
            Object o = node [key];
            return buildJsonValue (o);
        }

        public int getInt (string key)
        {
            NativeJsonValue v = get (key);
            if (v == null) {
                return -1;
            }
            NativeJsonNumber vi = v.isNumber ();
            if (vi == null) {
                return -1;
            }
            return vi.intValue ();
        }

        public string getString (string key)
        {
            NativeJsonValue v = get (key);
            if (v == null) {
                return null;
            }
            NativeJsonString vi = v.isString ();
            if (vi == null) {
                return null;
            }
            return vi.stringValue ();
        }
    }
}