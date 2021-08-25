using System;
using UnityEngine;
using java.lang;

namespace de.yard.threed.platform.unity
{

    using de.yard.threed.platform;

    // import de.yard.threed.platform.NativeLight;
    // import de.yard.threed.platform.NativeVector3;
    using de.yard.threed.core.platform;
    using de.yard.threed.core;
    // import de.yard.threed.platform.common.Color;

    /**
     * Fuer Point, ambient (direction==null) und directional.
     * Das Light ist auch in der mirrorworld, weil es Komponente einer SceneNode ist.
     * 
     * <p/>
     * Date: 04.07.14
     */
    public class UnityLight : NativeLight
    {
        //private NativeVector3 position = new UnityVector3 (0, 0, 0);
        public /*Unity*/de.yard.threed.core.Vector3 direction = null;

        public Color color;

        public UnityLight (Color col)
        {
            color = col;
        }

        /**
         * Directional light
         */
        public UnityLight (Color col, de.yard.threed.core.Vector3 dir)
        {
            color = col;
            this.direction = dir;

        }

        /* virtual public NativeVector3 getPosition ()
         {
             return position;
         }

         virtual     public void setPosition (NativeVector3 pos)
         {
             position = pos;
         }*/

        virtual public Color getColor ()
        {
            return color;
        }

        public static NativeLight buildPointLight (de.yard.threed.core.Color col)
        {
            return new UnityLight (col);
        }

        public static NativeLight buildDirectionalLight (de.yard.threed.core.Color color, de.yard.threed.core.Vector3 direction)
        {
            return new UnityLight (color, direction);
        }

        virtual public de.yard.threed.core.Vector3 getDirection ()
        {
            return (direction);
        }

        public static NativeLight buildAmbientLight (de.yard.threed.core.Color color)
        {
            return new UnityLight (color);
        }
    }
}