using UnityEngine;
using System;
using java.lang;

namespace de.yard.threed.platform.unity
{

    using de.yard.threed.core;
   
   
    // import de.yard.threed.platform.common.MathUtil2;

    /**
 * Date: 14.02.14
 * Time: 08:41
 */
    public class UnityVector3  /*:  NativeVector3*/
    {
        public	UnityEngine.Vector3 v;

        public UnityVector3 ()
        {
            v = new UnityEngine.Vector3 ();
        }

        public UnityVector3 (float x, float y, float z)
        {
            v = new UnityEngine.Vector3 (x, y, z);
        }

        public static UnityEngine.Vector3 toUnity(Vector3 v) {
            return  new UnityEngine.Vector3((float)v.getX(),(float)v.getY(),(float)v.getZ());
        }

        public static Vector3 fromUnity(UnityEngine.Vector3 v) {
            return  new Vector3(v.x,v.y,v.z);
        }

        public UnityVector3 (UnityEngine.Vector3 v)
        {
            this.v = v;
        }

        virtual public void setX (float x)
        {
            v.x = x;
        }

        virtual public void setY (float y)
        {
            v.y = y;
        }

        virtual public void setZ (float z)
        {
            v.z = z;
        }

        virtual public float getZ ()
        {
            return v.z;
        }

        virtual public float getY ()
        {
            return v.y;
        }

        virtual public float getX ()
        {
            return v.x;
        }


        /*8.4.15: Rotation über Vector definieren wir mal nicht wegen konzeptioneller Unsauberkeit. Wohl aber über Quaternion public void rotate(Vector3 rotation) {
         //this.rotation = rotation;
    }*/

        /**
     * Die Rotation geht vielleicht auch ohne den Umweg über Matrix.
     *
     * @return
     */
            virtual public de.yard.threed.core.Vector3 rotate (de.yard.threed.core.Quaternion rotation)
        {
            /*Native*/Matrix4 m = MathUtil2.buildRotationMatrix (rotation);
            return m.transform (UnityVector3.fromUnity(this.v));
        }

        virtual public UnityVector3 multiply (float scale)
        {
            return new UnityVector3 (v.x * scale, v.y * scale, v.z * scale);
        }

        virtual public UnityVector3 divideScalar (float scalar)
        {
            float invScalar = 1.0f / scalar;
            return new UnityVector3 (v.x * invScalar, v.y * invScalar, v.z * invScalar);
        }

   

        /**
     * TODO: Was ist hier der mathematische Unterbau, vor allem wegen des perspective divide?
     * 23.11.14: Und die ist SEHR aehnlich zu Matrix4.transform. Das muss bestimmt zusammengelegt werden.
     *
     * @return
     */
        /*OGL public OpenGlVector3 project(Matrix4 projectionmatrix) {

        float d = 1 / (projectionmatrix.e41 * x + projectionmatrix.e42 * y + projectionmatrix.e43 * z + projectionmatrix.e44); // perspective divide

        // this.x = ( e[0] * x + e[4] * y + e[8]  * z + e[12] ) * d;
        // this.y = ( e[1] * x + e[5] * y + e[9]  * z + e[13] ) * d;
        // this.z = ( e[2] * x + e[6] * y + e[10] * z + e[14] ) * d;

        float nx = (projectionmatrix.e11 * x + projectionmatrix.e12 * y + projectionmatrix.e13 * z + projectionmatrix.e14) * d;
        float ny = (projectionmatrix.e21 * x + projectionmatrix.e22 * y + projectionmatrix.e23 * z + projectionmatrix.e24) * d;
        float nz = (projectionmatrix.e31 * x + projectionmatrix.e32 * y + projectionmatrix.e33 * z + projectionmatrix.e34) * d;

        return new OpenGlVector3(nx, ny, nz);

    }*/

        public override string ToString ()
        {
            return "x=" + v.x + ",y=" + v.y + ",z=" + v.z;
        }

        public static string dump (UnityEngine.Vector3 v)
        {
            return "x=" + v.x + ",y=" + v.y + ",z=" + v.z;
        }



    }
}