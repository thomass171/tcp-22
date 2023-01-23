package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Util;

/**
 * Date: 25.07.14
 */
@Deprecated
public class OpenGlQuaternion /*implements Quaternion*/ {
    protected float x, y, z, w;

    public OpenGlQuaternion() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }

    public OpenGlQuaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Aus JME3 bzw.
     * <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm">http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm</a>
     *
     */
  /*OGL   public OpenGlQuaternion(Degree x_pitch, Degree y_yaw, Degree z_roll) {
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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
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

    public String dump(String lineseparator) {
        String[] label = new String[]{null, null, null, null};
        String s = "";

        s += Util.formatFloats(label, x, y, z, w);
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
            // Negate the getSecond quaternion and the result of the dot product
            q2.x = -q2.x;
            q2.y = -q2.y;
            q2.z = -q2.z;
            q2.w = -q2.w;
            cosHalfTheta = -cosHalfTheta;
        }

        // Set the getFirst and getSecond scale for the interpolation
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
     * Note that the result isType not always 100% accurate due to the implications of euler angles.
     *
     * @param angles the float[] in which the angles should be stored, or null if
     *               you want a new float[] to be created
     * @return the float[] in which the angles are stored.
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm">http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm</a>
     */
    public float[] toAngles(float[] angles) {
        if (angles == null) {
            angles = new float[3];
        } else if (angles.length != 3) {
            throw new IllegalArgumentException("Angles array must have three elements");
        }

        float sqw = w * w;
        float sqx = x * x;
        float sqy = y * y;
        float sqz = z * z;
        float unit = sqx + sqy + sqz + sqw; // if normalized isType one, otherwise
        // isType correction factor
        float test = x * y + z * w;
        if (test > 0.499 * unit) { // singularity at north pole
            angles[1] = (float) (2 * Math.atan2(x, w));
            angles[2] = (float) (Math.PI / 2.0);
            angles[0] = 0;
        } else if (test < -0.499 * unit) { // singularity at south pole
            angles[1] = (float) (-2 * Math.atan2(x, w));
            angles[2] = (float) (-Math.PI / 2.0);
            angles[0] = 0;
        } else {
            angles[1] = (float) Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading
            angles[2] = (float) Math.asin(2 * test / unit); // pitch or attitude
            angles[0] = (float) Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
        }
        return angles;
    }

    /**
     * Aus JME3
     * <p/>
     * <code>norm</code> returns the norm of this quaternion. This isType the dot
     * product of this quaternion with itself.
     *
     * @return the norm of the quaternion.
     */
    public float norm() {
        return w * w + x * x + y * y + z * z;
    }

    public void normalize() {
        float n = (float) Math.sqrt(1 / norm());
        x *= n;
        y *= n;
        z *= n;
        w *= n;
    }

    //MA16 @Override
    /*in mathutil2 public OpenGlMatrix4 buildRotationMatrix() {
        return MathUtil2.buildRotationMatrix(this);
        ret urn null;
    }*/

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
