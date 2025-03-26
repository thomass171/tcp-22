package de.yard.threed.core;


/**
 * Date: 25.07.14
 * <p/>
 * Konventionen aus Wikipedia.
 * <p/>
 * Das Element aij ist in Zeile i und  Spalte j
 * <p/>
 * Diese 3x3 Marizen sind eigentlich nur fuer manche Berechnungen der Uebersichtlichkeit wegen erforderlich bzw.
 * eine sauberer Modellierung.
 * <p/>
 * 20.5.2015: Oder als Transformationsmatrix im 2D Raum.
 */
public class Matrix3 implements Dumpable {
    //public double[] matrix = new double[9];
    // Das Array ist in column order, wie OpenGL es verwendet,d.h.
    // 0,4,8,12
    // 1,5,9,13
    // 2,6,10,14
    // 3,7,11,15
    //public double[] matrix = new double[16];
    public double e11, e12, e13;
    public double e21, e22, e23;
    public double e31, e32, e33;


    /**
     * Identity Matrix
     */
    public Matrix3() {
        init();
    }

    /**
     * Konstruktor ist in "zeilenweise lesbarer", nicht colum order Form.
     */
    public Matrix3(double a11, double a12, double a13,
                   double a21, double a22, double a23,
                   double a31, double a32, double a33) {
        e11 = a11;
        e12 = a12;
        e13 = a13;
        e21 = a21;
        e22 = a22;
        e23 = a23;
        e31 = a31;
        e32 = a32;
        e33 = a33;
    }

    public void init() {
        e11 = 1;
        e12 = 0;
        e13 = 0;
        e21 = 0;
        e22 = 1;
        e23 = 0;
        e31 = 0;
        e32 = 0;
        e33 = 1;
    }

    @Override
    public String dump(String lineseparator) {
        String[] label = new String[]{null, null, null};
        String s = "";

        s += Util.formatFloats(label, e11, e12, e13) + lineseparator;
        s += Util.formatFloats(label, e21, e22, e23) + lineseparator;
        s += Util.formatFloats(label, e31, e32, e33);
        return s;
    }

    /**
     * Multipliziert this mit m, also this * m
     *
     * @param m
     * @return
     */
    public Matrix3 multiply(Matrix3 m) {
        Matrix3 res = new Matrix3(
                e11 * m.e11 + e12 * m.e21 + e13 * m.e31,
                e11 * m.e12 + e12 * m.e22 + e13 * m.e32,
                e11 * m.e13 + e12 * m.e23 + e13 * m.e33,
                e21 * m.e11 + e22 * m.e21 + e23 * m.e31,
                e21 * m.e12 + e22 * m.e22 + e23 * m.e32,
                e21 * m.e13 + e22 * m.e23 + e23 * m.e33,
                e31 * m.e11 + e32 * m.e21 + e33 * m.e31,
                e31 * m.e12 + e32 * m.e22 + e33 * m.e32,
                e31 * m.e13 + e32 * m.e23 + e33 * m.e33);

        return res;
    }

    public Matrix3 multiply(double f) {
        Matrix3 res = new Matrix3(e11 * f, e12 * f, e13 * f,
                e21 * f, e22 * f, e23 * f,
                e31 * f, e32 * f, e33 * f);
        return res;
    }

    /**
     * TODO testen
     *
     * @return
     */
    public double getDeterminant() {
        return e11 * e22 * e33 + e12 * e23 * e31 + e13 * e21 * e32 - e31 * e22 * e13 - e32 * e23 * e11 - e33 * e21 * e12;
    }

    /**
     * scale rausrechnen
     *
     * @return
     */
    public Matrix3 removeScale() {
        double sx = new Vector3(e11, e12, e13).length();
        double sy = new Vector3(e21, e22, e23).length();
        double sz = new Vector3(e31, e32, e33).length();

        sx = 1 / sx;
        sy = 1 / sy;
        sz = 1 / sz;

        return new Matrix3(
                e11 * sx, e12 * sy, e13 * sx,
                e21 * sx, e22 * sy, e23 * sy,
                e31 * sz, e32 * sz, e33 * sz);

    }

    /**
     * Das es eine Translation im 2D Raum ist, kommt auch nue ein Vector2 rein.
     *
     * @param v
     * @return
     */
    public static Matrix3 buildTranslationMatrix(Vector2 v) {
        Matrix3 mat = new Matrix3(
                1, 0, v.getX(),
                0, 1, v.getY(),
                0, 0, 1);
        return mat;
    }

    public static Matrix3 buildRotationMatrix(double theta) {
        Matrix3 mat = new Matrix3(
                Math.cos(theta), -Math.sin(theta), 0,
                Math.sin(theta), Math.cos(theta), 0,
                0, 0, 1);
        return mat;
    }

    /**
     * Translation und Rotation in eine Matrix packen.
     */
    public static Matrix3 buildTransformationMatrix(Vector2 v, double rotation) {
        return buildTranslationMatrix(v).multiply(buildRotationMatrix(rotation));
    }

    public Vector2 transform(Vector2 v) {
        double x = e11 * v.getX() + e12 * v.getY() + e13 * 1f;
        double y = e21 * v.getX() + e22 * v.getY() + e23 * 1f;
        double z = e31 * v.getX() + e32 * v.getY() + e33 * 1f;

        v = new Vector2(x, y);
        //v.normalize();
        return v;
    }

    /**
     * Zeilen und Spalten tauschen
     *
     * @return
     */
    public Matrix3 transpose() {
        return new Matrix3(e11, e21, e31,
                e12, e22, e32,
                e13, e23, e33);
    }

    public void setTranslation(Vector2 v) {
        e13 = v.getX();
        e23= v.getY();
    }

    public Vector3 getCol0() {
        return new Vector3(e11, e21, e31);
    }

    public Vector3 getCol1() {
        return new Vector3(e12, e22, e32);
    }

    public Vector3 getCol2() {
        return new Vector3(e13, e23, e33);
    }

    public Quaternion extractQuaternion() {
        return MathUtil2.extractQuaternion(e11, e12, e13,
                e21, e22, e23,
                e31, e32, e33);
    }
}
