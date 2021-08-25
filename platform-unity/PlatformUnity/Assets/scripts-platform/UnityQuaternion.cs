using System;
using java.lang;

namespace de.yard.threed.platform.unity
{

    using de.yard.threed.core;



    /**
 * Date: 25.07.14
 */
    public class UnityQuaternion  /*:  NativeQuaternion*/
    {
        public  UnityEngine.Quaternion q;

        public UnityQuaternion ()
        {
            q = UnityEngine.Quaternion.identity;
        }

        public UnityQuaternion (double x, double y, double z, double w)
        {
            q = new UnityEngine.Quaternion ((float)x, (float)y, (float)z, (float)w);
        }

        public static UnityEngine.Quaternion toUnity (Quaternion v)
        {
            return  new UnityEngine.Quaternion ((float)v.getX (), (float)v.getY (), (float)v.getZ (), (float)v.getW ());
        }

        public static Quaternion fromUnity (UnityEngine.Quaternion v)
        {
            return  new Quaternion (v.x, v.y, v.z, v.w);
        }

        public UnityQuaternion (UnityEngine.Quaternion q)
        {
            this.q = q;
        }

        /**
     * Aus JME3 bzw.
     * <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm">http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm</a>
     *
     */
        /*OGL   public UnityQuaternion(Degree x_pitch, Degree y_yaw, Degree z_roll) {
        float sinY, sinZ, sinX, cosY, cosZ, cosX;
        sinX = Matrix4.sin(x_pitch.multiply(0.5f));
        cosX = Matrix4.cos(x_pitch.multiply(0.5f));
        sinY = Matrix4.sin(y_yaw.multiply(0.5f));
        cosY = Matrix4.cos(y_yaw.multiply(0.5f));
        sinZ = Matrix4.sin(z_roll.multiply(0.5f));
        cosZ = Matrix4.cos(z_roll.multiply(0.5f));

        float cosYXcosZ = cosY * cosZ;
        float sinYXsinZ = sinY * sinZ;
        float cosYXsinZ = cosY * sinZ;
        float sinYXcosZ = sinY * cosZ;

        w = (cosYXcosZ * cosX - sinYXsinZ * sinX);
        x = (cosYXcosZ * sinX + sinYXsinZ * cosX);
        y = (sinYXcosZ * cosX + cosYXsinZ * sinX);
        z = (cosYXsinZ * cosX - sinYXcosZ * sinX);

        normalize();
    }*/

        virtual public float getX ()
        {
            return q.x;
        }

        virtual public float getY ()
        {
            return q.y;
        }

        virtual public float getZ ()
        {
            return q.z;
        }

        virtual public float getW ()
        {
            return q.w;
        }

        /*OGL  public Degree getYaw() {
        float[] angles = new float[3];
        toAngles(angles);
        return Degree.buildFromRadians(angles[0]);
    }

    public Degree getPitch() {
        float[] angles = new float[3];
        toAngles(angles);
        return Degree.buildFromRadians(angles[2]);
    }*/

        virtual public string dump (string lineseparator)
        {
            string[] label = new String[]{ null, null, null, null };
            string s = "";

            s += Util.formatFloats (label, q.x, q.y, q.z, q.w);
            return s;
        }

        /**
     * Aus jme3. Ist aber quais identisch zu ThreeJS. Beide d�rften auf dem Algorithmus aus
     * http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/index.htm
     * basieren.
     * <p/>
     * Hier wird "this" ver�ndert und nicht ein ge�nderter Quaternion zur�ckgegeben. Ob das so
     * bleiben sollte, m�ssen wir nochmal sehen. Nee, machen wir nie. Auch "q" wird hier
     * nicht veraendert.
     *
     * @param q          Final interpolation value
     * @param changeAmnt The amount diffrence
     */
        /*OGLpublic Quaternion slerp(Quaternion q, float changeAmnt) {
        /*if (this.x == q2.x && this.y == q2.y && this.z == q2.z
                && this.w == q2.w) {
            return;
        }* /
        Quaternion q2 = new Quaternion(q.x, q.y, q.z, q.w);

        float cosHalfTheta = (x * q2.x) + (y * q2.y) + (z * q2.z)
                + (w * q2.w);

        if (cosHalfTheta < 0.0f) {
            // Negate the second quaternion and the result of the dot product
            q2.x = -q2.x;
            q2.y = -q2.y;
            q2.z = -q2.z;
            q2.w = -q2.w;
            cosHalfTheta = -cosHalfTheta;
        }

        // Set the first and second scale for the interpolation
        float scale0 = 1 - changeAmnt;
        float scale1 = changeAmnt;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - cosHalfTheta) > 0.1f) {
            // Get the angle between the 2 quaternions, and then store the sin()
            // of that angle
            float theta = (float) Math.acos(cosHalfTheta);
            float invSinTheta = (float) (1f / Math.sin(theta));

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = (float) (Math.sin((1 - changeAmnt) * theta) * invSinTheta);
            scale1 = (float) (Math.sin((changeAmnt * theta)) * invSinTheta);
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special
        // form of linear interpolation for quaternions.
        return new Quaternion((scale0 * this.x) + (scale1 * q2.x),
                (scale0 * this.y) + (scale1 * q2.y),
                (scale0 * this.z) + (scale1 * q2.z),
                (scale0 * this.w) + (scale1 * q2.w));
    }*/

        /**
     * Liefert die Winkel in Radian.
     * <p/>
     * <code>toAngles</code> returns this quaternion converted to Euler
     * rotation angles (yaw,roll,pitch).<br/>
     * Note that the result is not always 100% accurate due to the implications of euler angles.
     *
     * @param angles the float[] in which the angles should be stored, or null if
     *               you want a new float[] to be created
     * @return the float[] in which the angles are stored.
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm">http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm</a>
     */
        /*Unity  virtual public float[] toAngles (float[] angles)
        {
            if (angles == null) {
                angles = new float[3];
            } else if (angles.Length != 3) {
                throw new IllegalArgumentException ("Angles array must have three elements");
            }

            float sqw = w * w;
            float sqx = x * x;
            float sqy = y * y;
            float sqz = z * z;
            float unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
            // is correction factor
            float test = x * y + z * w;
            if (test > 0.499 * unit) { // singularity at north pole
                angles [1] = (float)(2 * Math.Atan2 (x, w));
                angles [2] = (float)(Math.PI / 2.0);
                angles [0] = 0;
            } else if (test < -0.499 * unit) { // singularity at south pole
                angles [1] = (float)(-2 * Math.Atan2 (x, w));
                angles [2] = (float)(-Math.PI / 2.0);
                angles [0] = 0;
            } else {
                angles [1] = (float)Math.Atan2 (2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading
                angles [2] = (float)Math.Asin (2 * test / unit); // pitch or attitude
                angles [0] = (float)Math.Atan2 (2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
            }
            return angles;
        }*/

        /**
     * Aus JME3
     * <p/>
     * <code>norm</code> returns the norm of this quaternion. This is the dot
     * product of this quaternion with itself.
     *
     * @return the norm of the quaternion.
     */
        virtual public float norm ()
        {
            return q.w * q.w + q.x * q.x + q.y * q.y + q.z * q.z;
        }

        /**
         * gibt es Unity offenbar nicht
         */
        virtual public void normalize ()
        {               
            float n = (float)Math.Sqrt (1 / norm ());
            q.x *= n;
            q.y *= n;
            q.z *= n;
            q.w *= n;
        }

        virtual public /*Native*/Matrix4 buildRotationMatrix ()
        {
            return MathUtil2.buildRotationMatrix (UnityQuaternion.fromUnity (this.q));
        }

        /*OGL public static Quaternion extractFromMatrix(Matrix4 m) {
        return m.extractQuaternion();
    }*/

        /**
     * Liefert die Rotation, die erforderlich ist, um v1 in die selbe Orientierung wie v2 zu rotieren.
     * Aus der ThreeJs Funktion setFromUnitVectors(), die wiederum auf
     * http://lolengine.net/blog/2014/02/24/quaternion-from-two-vectors-final
     * basiert. Beide Vectors muessen mormalisiert sein. Warum eigentlich?
     */
        /*OGL public static Quaternion buildQuaternion(Vector3 from, Vector3 to) {
        float r;
        Vector3 v1;
        float EPS = 0.000001f;

        r = Vector3.getDotProduct(from, to) + 1;
        if (r < EPS) {
            r = 0;
            if (Math.abs(from.x) > Math.abs(from.z)) {
                v1 = new Vector3(-from.y, from.x, 0);
            } else {
                v1 = new Vector3(0, -from.z, from.y);
            }
        } else {
            v1 = Vector3.getCrossProduct(from, to);
        }
        Quaternion q = new Quaternion(v1.x, v1.y, v1.z, r);
        q.normalize();
        return q;
    }*/

    }
}