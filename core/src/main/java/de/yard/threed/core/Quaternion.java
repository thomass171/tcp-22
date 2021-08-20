package de.yard.threed.core;


/**
 * Date: 25.07.14
 */
public class Quaternion implements Dumpable {
    //Log logger = Platform.getInstance().getLog(Quaternion.class);
    private double x, y, z, w;
    /*public Quaternion quaternion;

    public Quaternion(Quaternion q) {
        quaternion = q;
    }*/

    /**
     * Identity Quaternion
     */
    public Quaternion() {
        this(0, 0, 0, 1);
    }

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Der Axisvector muss normalisiert sein (oder auch nicht? Who knows). Verkehrt ist es sicher nicht.
     * 26.2.2016: Es dürfte Quatsch sein, die Eingabe zu normalisieren. Das ist docvh überhaupt kein Vektor.
     * <p/>
     * Es wird die UnityOrder verwendet. Nee lieber nicht, das ist left handed.
     * * Aus JME3 bzw.
     * <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm">http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm</a>
     *
     * @param
     */
    public static Quaternion buildFromAngles(Degree x_pitch, Degree y_yaw, Degree z_roll) {
        //quaternion = MathUtil2.buildQuaternionFromAngles((double), (double), (double));
        return buildFromAngles(x_pitch.toRad(), y_yaw.toRad(), z_roll.toRad());
    }

    public static Quaternion buildFromAngles(double xa, double ya, double za) {

        // 13.4.16: Obv das mit dem mirror angle das wahre ist, ist voellig offen
        // 18.4.16: Nicht, solange sich alles in der Mirrorworld befindet
        //ya = Platform.getInstance().getWorld().mirrorAngle(ya);
        double sinY, sinZ, sinX, cosY, cosZ, cosX;
        sinX = Math.sin(xa * 0.5f);
        cosX = Math.cos(xa * 0.5f);
        sinY = Math.sin(ya * 0.5f);
        cosY = Math.cos(ya * 0.5f);
        sinZ = Math.sin(za * 0.5f);
        cosZ = Math.cos(za * 0.5f);

        //logger.debug("sinX="+sinX);

        double cosYXcosZ = cosY * cosZ;
        double sinYXsinZ = sinY * sinZ;
        double cosYXsinZ = cosY * sinZ;
        double sinYXcosZ = sinY * cosZ;

        double    w = (cosYXcosZ * cosX - sinYXsinZ * sinX);
        double     x = (cosYXcosZ * sinX + sinYXsinZ * cosX);
        double     y = (sinYXcosZ * cosX + cosYXsinZ * sinX);
        double    z = (cosYXsinZ * cosX - sinYXcosZ * sinX);

        if (MathUtil.mathvalidate) {
            if (java.lang.Double.isNaN(x)) {
                throw new RuntimeException("NaN");
            }
            if (java.lang.Double.isNaN(y)) {
                throw new RuntimeException("NaN");
            }
            if (java.lang.Double.isNaN(z)) {
                throw new RuntimeException("NaN");
            }
            if (java.lang.Double.isNaN(w)) {
                throw new RuntimeException("NaN");
            }
        }
        return new Quaternion(x,y,z,w).normalize();
        //logger.debug("nach norm x="+getX())
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getW() {
        return w;
    }

    @Override
    public String dump(String lineseparator) {
        //28.8.16:Lesbarkeit verbessert
        String[] label = new String[]{"", "", "", ""};//new String[]{"x", "y", "z"};
        String s = "";

        s += "(" + Util.formatFloats(label, getX(), getY(), getZ(), getW()) + ")";

        /*String[] label = new String[]{null, null, null, null};
        s += Util.formatFloats(label, getX(), getY(), getZ(), getW());*/
        return s;
    }

    @Override
    public String toString() {
        return "(" + getX() + "," + getY() + "," + getZ() + "," + getW() + ")";
    }

    /**
     * Aus jme3. Ist aber quais identisch zu ThreeJS. Beide d�rften auf dem Algorithmus aus
     * http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/index.htm
     * basieren.
     * <p/>
     * Hier wird "this" ver�ndert und nicht ein ge�nderter Quaternion zur�ckgegeben. Ob das so
     * bleiben sollte, m�ssen wir nochmal sehen. Nee, machen wir nie. Auch "q" wird hier
     * nicht veraendert.
     * 29.3.17: changeAmnt zwischen 0 und 1?
     *
     * @param q          Final interpolation value
     * @param changeAmnt The amount diffrence
     */
    public Quaternion slerp(Quaternion q, double changeAmnt) {

        Quaternion q2 = new Quaternion(q.getX(), q.getY(), q.getZ(), q.getW());

        double cosHalfTheta = (getX() * q2.getX()) + (getY() * q2.getY()) + (getZ() * q2.getZ())
                + (getW() * q2.getW());

        if (cosHalfTheta < 0.0f) {
            // Negate the getSecond quaternion and the result of the dot product
            q2 = new Quaternion(-q2.getX(), -q2.getY(), -q2.getZ(), -q2.getW());
            cosHalfTheta = -cosHalfTheta;
        }

        // Set the getFirst and getSecond scale for the interpolation
        double scale0 = 1 - changeAmnt;
        double scale1 = changeAmnt;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - cosHalfTheta) > 0.1f) {
            // Get the angle between the 2 quaternions, and then store the sin()
            // of that angle
            double theta = (double) Math.acos(cosHalfTheta);
            double invSinTheta = (double) (1f / Math.sin(theta));

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = (double) (Math.sin((1 - changeAmnt) * theta) * invSinTheta);
            scale1 = (double) (Math.sin((changeAmnt * theta)) * invSinTheta);
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special
        // form of linear interpolation for quaternions.
        return new Quaternion((scale0 * this.getX()) + (scale1 * q2.getX()),
                (scale0 * this.getY()) + (scale1 * q2.getY()),
                (scale0 * this.getZ()) + (scale1 * q2.getZ()),
                (scale0 * this.getW()) + (scale1 * q2.getW()));

    }

    /**
     * Liefert die Winkel in Radian.
     * <p>
     * 30.3.2017: Die Nutzung ist brandgefährlich. Vor allem, wenn man nur einzelne Winkel weiterverwendet. Richtig brauchbar ist das Ergebnis nur,
     * wenn man alle Winkel zusammen als eine Eulertransformation sieht (Skizze29b). Darum erstmal deprecated. Aber das Ding muss raus.
     * Fuer Analysezwecke ist es aber hilfreich. Darum nur deprecated lassen.
     *
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm">http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm</a>
     */
    @Deprecated
    public void toAngles(double[] angles) {
        double x = getX();
        double y = getY();
        double z = getZ();
        double w = getW();

        double sqw = w * w;
        double sqx = x * x;
        double sqy = y * y;
        double sqz = z * z;
        double unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
        // is correction factor
        double test = x * y + z * w;
        if (test > 0.499 * unit) { // singularity at north pole
            angles[1] = (double) (2 * Math.atan2(x, w));
            angles[2] = (double) (Math.PI / 2.0);
            angles[0] = 0;
        } else if (test < -0.499 * unit) { // singularity at south pole
            angles[1] = (double) (-2 * Math.atan2(x, w));
            angles[2] = (double) (-Math.PI / 2.0);
            angles[0] = 0;
        } else {
            angles[1] = (double) Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading
            angles[2] = (double) Math.asin(2 * test / unit); // pitch or attitude
            angles[0] = (double) Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
        }
    }

    /**
     * 8.8.15 Not self modifying. Wie in Vector3, der manipuliert auch nicht intern.
     */
    public Quaternion normalize() {
        double n = w * w + x * x + y * y + z * z;
        n = Math.sqrt(1 / n);
        return new Quaternion(x * n, y * n, z * n, w * n);
    }

    /**
     * Liefert die Rotation, die erforderlich ist, um from in die selbe Orientierung wie to zu rotieren.
     */
    public static Quaternion buildQuaternion(Vector3 from, Vector3 to) {
        return MathUtil2.buildQuaternion(from, to);
    }

    public Quaternion clone() {
        return new Quaternion(getX(), getY(), getZ(), getW());
    }

    public Quaternion multiply(Quaternion q) {
        return MathUtil2.multiply(this, q);
    }

    /**
     * siehe MathUtil2.
     *
     * @param forward
     * @param up
     * @return
     */
    public static Quaternion buildLookRotation(Vector3 forward, Vector3 up) {
        return (MathUtil2.buildLookRotation(forward, up));
    }

    public static Quaternion buildRotationX(Degree degree) {
        return Quaternion.buildFromAngles(degree, new Degree(0), new Degree(0));
    }

    public static Quaternion buildRotationY(Degree degree) {
        return  Quaternion.buildFromAngles(new Degree(0), degree, new Degree(0));
    }

    public static Quaternion buildRotationZ(Degree degree) {
        return  Quaternion.buildFromAngles(new Degree(0), new Degree(0), degree);
    }

    /**
     * Aus JME.
     */
    public static Quaternion buildQuaternionFromAngleAxis(double angle, Vector3 axis) {
        axis = axis.normalize();
        double halfAngle = 0.5f * angle;
        double sin =  Math.sin(halfAngle);

        double w =  Math.cos(halfAngle);
        double x = sin * axis.getX();
        double y = sin * axis.getY();
        double z = sin * axis.getZ();
        Quaternion quaternion = new Quaternion(x, y, z, w).normalize();

        return quaternion;
    }

    public static Quaternion buildQuaternionFromAngleAxis(Degree angle, Vector3 axis) {
        return buildQuaternionFromAngleAxis(angle.toRad(), axis);
    }
    
    public static double getDotProduct(Quaternion p1, Quaternion p2) {
        double p = MathUtil2.getDotProduct(p1, p2);
        if (MathUtil.mathvalidate) {
            if (java.lang.Double.isNaN(p)) {
                throw new RuntimeException("NaN getDotProduct");
            }
        }
        return p;
    }

    public double dot(Quaternion v) {
        return getDotProduct(this, v);
    }
}
