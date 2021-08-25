using System;
using java.lang;
using UnityEngine;

namespace de.yard.threed.platform
{
    using de.yard.threed.engine;
    using de.yard.threed.core.platform;


    /**
     *
     */
    public class UnityUtil
    {
        static World world;
        private static readonly DateTime Jan1st1970 = new DateTime (1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        public static /*Unity*/de.yard.threed.core.Vector3 mirrorZ (de.yard.threed.core.Vector3 v)
        {
            if (world == null) {
                world = Scene.getWorld ();
            }

            return world.mirrorZ (v);
        }

        public static long currentTimeMillis ()
        {            
            return (long)(DateTime.UtcNow - Jan1st1970).TotalMilliseconds;
        }
    }
}