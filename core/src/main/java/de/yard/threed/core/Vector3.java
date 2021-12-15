package de.yard.threed.core;


/**
 * 30.4.15 die setter fuer x,y,z  lass ich mal weg
 * <p>
 * Date: 14.02.14
 * Time: 08:41
 */
public class Vector3 implements Dumpable {
    private double x, y, z;

    public Vector3() {
        this(0, 0, 0);
    }


    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getZ() {
        return z;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(getX() + v.getX(), getY() + v.getY(), getZ() + v.getZ());
    }

    public Vector3 subtract(Vector3 v) {
        return new Vector3(getX() - v.getX(), getY() - v.getY(), getZ() - v.getZ());
    }

    /*8.4.15: Rotation über Vector definieren wir mal nicht wegen konzeptioneller Unsauberkeit. Wohl aber über Quaternion public void setRotateStatus(Vector3 rotation) {
         //this.rotation = rotation;
    }*/

    /**
     * Die Rotation geht vielleicht auch ohne den Umweg über Matrix.
     * In https://gamedev.stackexchange.com/questions/28395/rotating-vector3-by-a-quaternion
     * findet sich ein feiner Algorithmus.
     * 8.5.18: Den versuch ich mal. Laut Tests gehts.
     *
     * @return
     */
    public Vector3 rotate(Quaternion q) {
        //Matrix4 m = Matrix4.buildRotationMatrix(rotation);
        //return m.transform(this);
        Vector3 u = new Vector3(q.getX(), q.getY(), q.getZ());
        // Extract the scalar part of the quaternion
        double s = q.getW();

        // Do the math
        Vector3 result = u.multiply(2.0f * getDotProduct(u, this)).add(
                this.multiply(s * s - getDotProduct(u, u))).add(
                getCrossProduct(u, this).multiply(2.0f * s));
        return result;
    }

    public Vector3 rotateOnAxis(double angle, Vector3 axis) {
        Quaternion q = Quaternion.buildQuaternionFromAngleAxis(angle, axis);
        return rotate(q);
    }

    public Vector3 rotateOnAxis(Degree angle, Vector3 axis) {
        return rotateOnAxis(angle.toRad(), axis);
    }

    public Vector3 multiply(double scale) {
        return new Vector3(x * scale, y * scale, z * scale);
    }

    public Vector3 multiply(Quaternion q) {
        return MathUtil2.multiply(q, this);
    }

    public Vector3 multiplyScalar(double scale) {
        return multiply(scale);
    }

    public Vector3 divideScalar(double scalar) {
        double invScalar = 1.0f / scalar;
        return new Vector3(getX() * invScalar, getY() * invScalar, getZ() * invScalar);
    }

    public Vector3 negate() {
        return new Vector3(-getX(), -getY(), -getZ());
    }

    public Vector3 clone() {
        return new Vector3(x, y, z);
    }


    /**
     * Aus http://stackoverflow.com/questions/15777757/drawing-normals-in-lwjgl-messes-with-lighting
     * Feel free to use this for whatever you want, no licenses applied or anything.
     */
    /*public static Vector3 getNormal(Vector3 p1, Vector3 p2, Vector3 p3) {

        //Create normal vector we are going to output.
        Vector3 output = new Vector3();

        //Calculate vectors used for creating normal (these are the edges of the triangle).
        Vector3f calU = new Vector3f(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
        Vector3f calV = new Vector3f(p3.x - p1.x, p3.y - p1.y, p3.z - p1.z);

        //The output vector is equal to the cross products of the two edges of the triangle
        output.x = calU.y * calV.z - calU.z * calV.y;
        output.y = calU.z * calV.x - calU.x * calV.z;
        output.z = calU.x * calV.y - calU.y * calV.x;

        //Return the resulting vector.
        return output.normalize();
    }*/
    public static Vector3 getCrossProduct(Vector3 p1, Vector3 p2) {
        return (MathUtil2.getCrossProduct(p1, p2));
    }

    public Vector3 cross(Vector3 v) {
        return getCrossProduct(this, v);
    }

    public static double getDotProduct(Vector3 p1, Vector3 p2) {
        double p = MathUtil2.getDotProduct(p1, p2);
        if (MathUtil.mathvalidate) {
            if (java.lang.Double.isNaN(p)) {
                throw new RuntimeException("NaN getDotProduct");
            }
            /*stimmt nicht if (p < -1 || p > 1){
                throw new RuntimeException("invalid value in getDotProduct: "+p);
            }*/
        }
        return p;
    }

    public double dot(Vector3 v) {
        return getDotProduct(this, v);
    }

    public static double getAngleBetween(Vector3 p1, Vector3 p2) {
        return MathUtil2.getAngleBetween(p1, p2);
    }

    public static double getDistance(Vector3 p1, Vector3 p2) {
        return p1.subtract(p2).length();
    }

    /**
     * Liefert die Rotation, die erforderlich ist um p1 in die selbe Orientierung wie p2 zu rotieren.
     */
    public static Quaternion getRotation(Vector3 p1, Vector3 p2) {
        return Quaternion.buildQuaternion(p1, p2);
    }

    public Vector3 normalize() {

        double len = length();
        if (Math.abs(len) < 0.0000000001) {
            return clone();
        }
        return divideScalar(len);
    }

    public double length() {
        return Math.sqrt(lengthSqr());
    }

    public double lengthSqr() {
        return x * x + y * y + z * z;
    }

    @Override
    public String dump(String lineseparator) {
        //28.8.16:Lesbarkeit verbessert
        String[] label = new String[]{"", "", ""};//new String[]{"x", "y", "z"};
        String s = "";

        s += "(" + Util.formatFloats(label, x, y, z) + ")";
        return s;
    }


    @Override
    public String toString() {
        //28.8.16:Lesbarkeit verbessert
        //return "x=" + getX() + ",y=" + getY() + ",z=" + getZ();
        return "(" + getX() + "," + getY() + "," + getZ() + ")";
    }

    public static Vector3 parseString(String data) {
        String[] s;
        if (StringUtils.contains(data, ",")) {
            s = StringUtils.split(data, ",");
        } else {
            s = StringUtils.split(data, " ");
        }
        if (s.length != 3) {
            //logger.error("parseString: invalid vector3 data");
        }
        return new Vector3(Util.parseFloat(s[0]), Util.parseFloat(s[1]), Util.parseFloat(s[2]));
    }

    /**
     * Liefert den Winkel in Radian zwischen zwei Vektoren.
     *
     * @param p1
     * @return
     */
    public double getAngleBetween(Vector3 p1) {
        return MathUtil2.getAngleBetween(this, p1);
    }

    public boolean isValid() {
        if (java.lang.Double.isNaN(getX())) {
            return false;
        }
        if (java.lang.Double.isNaN(getY())) {
            return false;
        }
        if (java.lang.Double.isNaN(getZ())) {
            return false;
        }
        return true;
    }

    public static Vector3 getNearestPointOnVector(Vector3 p, Vector3 start, Vector3 v) {
        return (MathUtil2.getNearestPointOnVector(p, start, v));
    }

    public static Vector3 buildFromVector2(Vector2 v2) {
        return new Vector3(v2.getX(), v2.getY(), 0);
    }

    /**
     * 31.5.21: Jetzt mal so als Standard: translate ist selbstaendernd.
     */
    public void translateX(int t) {
        x += t;
    }

    public void translateY(int t) {
        y += t;
    }

    public void translateZ(int t) {
        z += t;
    }

    public Vector3 addX(double t) {
        return new Vector3(x + t, y, z);
    }

    public Vector3 addY(double t) {
        return new Vector3(x, y + t, z);
    }

    public Vector3 addZ(double t) {
        return new Vector3(x, y, z + t);
    }
}