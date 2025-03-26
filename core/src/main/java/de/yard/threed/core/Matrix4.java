package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Date: 25.04.14
 * <p/>
 * Konventionen aus Wikipedia.
 * <p/>
 * Das Element aij ist in Zeile i und  Spalte j
 * <p/>
 * In OpenGL werden bevorzugt column-major matrices verwendet.
 */
public class Matrix4 /*30.5.implements Dumpable*/ {
    //Log logger = Platform.getInstance().getLog(Matrix4.class);
    // Das Array ist in column order, wie OpenGL es verwendet,d.h.
    // 0,4,8,12
    // 1,5,9,13
    // 2,6,10,14
    // 3,7,11,15
    //public float[] matrix = new float[16];
    public double a11, a12, a13, a14;
    public double a21, a22, a23, a24;
    public double a31, a32, a33, a34;
    public double a41, a42, a43, a44;

    /**
     * Identity Matrix
     */
    public Matrix4() {
        this(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
    }

    /*public Matrix4(Matrix4 m) {
        matrix4 = m;
    }*/

    /**
     * Konstruktor ist in "zeilenweise lesbarer", nicht colum order Form.
     * 8.7.14: Hmm, ist es doch.
     */
    public Matrix4(double a11, double a12, double a13, double a14,
                   double a21, double a22, double a23, double a24,
                   double a31, double a32, double a33, double a34,
                   double a41, double a42, double a43, double a44) {
        this.a11 = a11;
        this.a12 = a12;
        this.a13 = a13;
        this.a14 = a14;
        this.a21 = a21;
        this.a22 = a22;
        this.a23 = a23;
        this.a24 = a24;
        this.a31 = a31;
        this.a32 = a32;
        this.a33 = a33;
        this.a34 = a34;
        this.a41 = a41;
        this.a42 = a42;
        this.a43 = a43;
        this.a44 = a44;
    }

    public String toString() {
        String s = "";
        s += getElement(0, 0) + "," + getElement(0, 1) + "," + getElement(0, 2) + "," + getElement(0, 3) + ",";
        s += getElement(1, 0) + "," + getElement(1, 1) + "," + getElement(1, 2) + "," + getElement(1, 3) + ",";
        s += getElement(2, 0) + "," + getElement(2, 1) + "," + getElement(2, 2) + "," + getElement(2, 3) + ",";
        s += getElement(3, 0) + "," + getElement(3, 1) + "," + getElement(3, 2) + "," + getElement(3, 3);
        return s;
    }

    /**
     * Einen Translation Vektor in die Matrix addieren.
     *
     * @param v
     * @return
     */
   /* public void addTranslation(Vector3 v) {
        matrix[12] += v.x;
        matrix[13] += v.y;
        matrix[14] += v.z;

    }*/
    public static Matrix4 buildTranslationMatrix(Vector3 v) {
        return MathUtil2.buildTranslationMatrix(v);
    }

    public static Matrix4 buildRotationXMatrix(Degree theta) {
        Matrix4 mat = new Matrix4(
                1, 0, 0, 0,
                0, cos(theta), -sin(theta), 0,
                0, sin(theta), cos(theta), 0,
                0, 0, 0, 1);
        return mat;
    }

    public static Matrix4 buildRotationYMatrix(Degree theta) {
        Matrix4 mat = new Matrix4(
                cos(theta), 0, sin(theta), 0,
                0, 1, 0, 0,
                -sin(theta), 0, cos(theta), 0,
                0, 0, 0, 1);
        return mat;
    }

    public static Matrix4 buildRotationZMatrix(Degree theta) {
        Matrix4 mat = new Matrix4(
                cos(theta), -sin(theta), 0, 0,
                sin(theta), cos(theta), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        return mat;
    }

    public static Matrix4 buildTransformationMatrix(Vector3 pos, Quaternion rotation, Vector3 scale) {
        return MathUtil2.buildMatrix(pos, rotation, scale);
    }

    /**
     * Translation und Rotation in eine Matrix packen.
     * 16.4.15: Nach zwei Unittest stimmt die Reihenfolge wohl.
     *
     * @param v
     * @param rotation
     * @return
     */
    public static Matrix4 buildTransformationMatrix(Vector3 v, Quaternion rotation) {
        return buildTranslationMatrix(v).multiply(buildRotationMatrix(rotation));
    }

    /**
     * TODO: Muesste wegen konzeptioneller Reinheit - wie einige obige Methoden auch - eine Matrix3 liefern
     * <p/>
     *
     * @param rotation
     * @return
     */
    public static Matrix4 buildRotationMatrix(Quaternion rotation) {
        // 7.9.2015: Ich mache hier die Normalisierung des Quaternion, weil die
        // wohl in beiden Plattformen erforderlich ist. Nicht unbedingt mathematisch,
        // aber visuell, sonst stimmen die Ergebnisse tatsächlich nicht.
        //MA16 return new Matrix4(rotation.normalize().buildRotationMatrix());
        return MathUtil2.buildRotationMatrix(rotation);
    }

    public static double sin(Degree angle) {
        return Math.sin(angle.toRad());
    }

    public static double cos(Degree angle) {
        return Math.cos(angle.toRad());
    }

    public static Matrix4 buildScaleMatrix(Vector3 v) {
        Matrix4 mat = new Matrix4(
                v.getX(), 0, 0, 0,
                0, v.getY(), 0, 0,
                0, 0, v.getZ(), 0,
                0, 0, 0, 1);
        return mat;
    }

    /**
     * Multipliziert this mit m, also this * m
     * <p/>
     *
     * @param m
     * @return
     */
    public Matrix4 multiply(Matrix4 m) {
        //logger.debug("multiply this=" + dump("\n"));
        //logger.debug("multiply m=" + m.dump("\n"));
        Matrix4 res = new Matrix4(
                a11 * m.a11 + a12 * m.a21 + a13 * m.a31 + a14 * m.a41,
                a11 * m.a12 + a12 * m.a22 + a13 * m.a32 + a14 * m.a42,
                a11 * m.a13 + a12 * m.a23 + a13 * m.a33 + a14 * m.a43,
                a11 * m.a14 + a12 * m.a24 + a13 * m.a34 + a14 * m.a44,
                a21 * m.a11 + a22 * m.a21 + a23 * m.a31 + a24 * m.a41,
                a21 * m.a12 + a22 * m.a22 + a23 * m.a32 + a24 * m.a42,
                a21 * m.a13 + a22 * m.a23 + a23 * m.a33 + a24 * m.a43,
                a21 * m.a14 + a22 * m.a24 + a23 * m.a34 + a24 * m.a44,
                a31 * m.a11 + a32 * m.a21 + a33 * m.a31 + a34 * m.a41,
                a31 * m.a12 + a32 * m.a22 + a33 * m.a32 + a34 * m.a42,
                a31 * m.a13 + a32 * m.a23 + a33 * m.a33 + a34 * m.a43,
                a31 * m.a14 + a32 * m.a24 + a33 * m.a34 + a34 * m.a44,
                a41 * m.a11 + a42 * m.a21 + a43 * m.a31 + a44 * m.a41,
                a41 * m.a12 + a42 * m.a22 + a43 * m.a32 + a44 * m.a42,
                a41 * m.a13 + a42 * m.a23 + a43 * m.a33 + a44 * m.a43,
                a41 * m.a14 + a42 * m.a24 + a43 * m.a34 + a44 * m.a44
        );
        return res;
        //logger.debug("multiply result=" + result.dump("\n"));
    }

    Matrix4 multiply(double f) {
        Matrix4 res = new Matrix4(
                getElement(0, 0) * f, getElement(0, 1) * f, getElement(0, 2) * f, getElement(0, 3) * f,
                getElement(1, 0) * f, getElement(1, 1) * f, getElement(1, 2) * f, getElement(1, 3) * f,
                getElement(2, 0) * f, getElement(2, 1) * f, getElement(2, 2) * f, getElement(2, 3) * f,
                getElement(3, 0) * f, getElement(3, 1) * f, getElement(3, 2) * f, getElement(3, 3) * f);
        return res;
    }

    /*private double multiply(double[] m1, int row, double[] m2, int col) {
        return m1[row + 4 * 0] * m2[col * 4 + 0] +
                m1[row + 4 * 1] * m2[col * 4 + 1] +
                m1[row + 4 * 2] * m2[col * 4 + 2] +
                m1[row + 4 * 3] * m2[col * 4 + 3];
    }*/

    /**
     * Das geht nur mit Translations und evtl. Scaling, aber nicht mit Rotations. Dann ist
     * die vierte Kompoente nicht mehr 1 und der resultierende Vektov muss normiert werden???
     * Nebul�s.  Aber im Vertexbuffer w�re Platz daf�r. Also doch keine Normalisierung
     * erforderlich?
     * 07.04.2015: das Resultat wird nicht normiert. Wenn der
     * Aufrufer das möchte, soll er es selber machen.
     * <p/>
     *
     * @param
     * @return
     */
    public Vector3 transform(Vector3 v) {
        //MA16 war Platform  //return new Vector3(matrix4.transform(v.vector3));

        double x = a11 * v.getX() + a12 * v.getY() + a13 * v.getZ() + a14 * 1f;
        double y = a21 * v.getX() + a22 * v.getY() + a23 * v.getZ() + a24 * 1f;
        double z = a31 * v.getX() + a32 * v.getY() + a33 * v.getZ() + a34 * 1f;
        double w = a41 * v.getX() + a42 * v.getY() + a43 * v.getZ() + a44 * 1f;

        v = new Vector3(x, y, z);
        // normalizing is nonsens
        return v;

    }


    //@Override
    public String dump(String lineseparator) {
        String[] label = new String[]{null, null, null, null};
        String s = "";

        s += Util.formatFloats(label, getElement(0, 0), getElement(0, 1), getElement(0, 2), getElement(0, 3)) + lineseparator;
        s += Util.formatFloats(label, getElement(1, 0), getElement(1, 1), getElement(1, 2), getElement(1, 3)) + lineseparator;
        s += Util.formatFloats(label, getElement(2, 0), getElement(2, 1), getElement(2, 2), getElement(2, 3)) + lineseparator;
        s += Util.formatFloats(label, getElement(3, 0), getElement(3, 1), getElement(3, 2), getElement(3, 3));
        return s;
    }

    /**
     * Das ist jetzt auch so eine Ungereimtheit. Muesste igentlich Matrix3 sein.
     *
     * @param column
     * @return
     */
    public Vector3 getColumn(int column) {
        return (MathUtil2.getColumn(this, column));
    }

    /**
     * row und column sind ab 0.
     *
     * @param row
     * @param column
     * @return
     */
    public double getElement(int row, int column) {
        if (row == 0) {
            switch (column) {
                case 0:
                    return a11;
                case 1:
                    return a12;
                case 2:
                    return a13;
                case 3:
                    return a14;
            }
        }
        if (row == 1) {
            switch (column) {
                case 0:
                    return a21;
                case 1:
                    return a22;
                case 2:
                    return a23;
                case 3:
                    return a24;
            }
        }
        if (row == 2) {
            switch (column) {
                case 0:
                    return a31;
                case 1:
                    return a32;
                case 2:
                    return a33;
                case 3:
                    return a34;
            }
        }
        if (row == 3) {
            switch (column) {
                case 0:
                    return a41;
                case 1:
                    return a42;
                case 2:
                    return a43;
                case 3:
                    return a44;
            }
        }
        return 0;
    }

    /**
     * Den scale Anteil der Matrix extrahieren.
     *
     * @return
     */
    public Vector3 extractScale() {
        return new Vector3(Math.sqrt(getElement(0, 0) * getElement(0, 0) + getElement(1, 0) * getElement(1, 0) + getElement(2, 0) * getElement(2, 0)),
                Math.sqrt(getElement(0, 1) * getElement(0, 1) + getElement(1, 1) * getElement(1, 1) + getElement(2, 1) * getElement(2, 1)),
                Math.sqrt(getElement(0, 2) * getElement(0, 2) + getElement(1, 2) * getElement(1, 2) + getElement(2, 2) * getElement(2, 2)));

    }

    /**
     * Den Rotationsanteil der Matrix als Quaternion extrahieren.
     * Dazu das ganze auf der 3x3 Matrix machen. Da das Woodo ist (siehe Kommentar Matrix3)
     * ueber die Platform.
     * <p/>
     * Das geht nicht unbewdingt mit jeder Matrix, wenn sie z.B. non uniform scale enthält.
     * <p/>
     * Das ist ganz gut in http://stackoverflow.com/questions/27655885/get-position-rotation-and-scale-from-matrix-in-opengl
     * erklärt.
     *
     * @return
     */
    public Quaternion extractQuaternion() {
        return MathUtil2.extractQuaternion(this);
    }

    /**
     * Den Rotationsanteil der Matrix extrahieren.
     * Dazu das ganze auf der 3x3 Matrix machen.
     * <p/>
     * Das geht nicht unbedingt mit jeder Matrix, wenn sie z.B. non uniform scale enthält.
     * <p/>
     * Das ist ganz gut in http://stackoverflow.com/questions/27655885/get-position-rotation-and-scale-from-matrix-in-opengl
     * erklärt.
     * <p>
     * 21.4.16: Deprecated weil 3x3 Matrix nur für konzeptionelle Reinheit ist, aber nicht praktikabel. Oder?
     * Vielleicht für Tracks? Aber da brauchts kein scale.
     *
     * @return
     */
    @Deprecated
    public Matrix3 extractRotation() {
        Matrix3 m3 = extractRotationAndScale();
        // Jetzt muss der Scale rausgerechnet werden.
        // "If there is no scaling, each row vector of this 3x3 matrix has length 1.0. "
        return m3.removeScale();
    }

    /**
     * ohne weitere Berechnung nur den 3x3 Teil liefern.
     * 21.4.16: Deprecated weil 3x3 Matrix nur für konzeptionelle Reinheit ist, aber nicht praktikabel. Oder?
     * Vielleicht für Tracks? Aber da brauchts kein scale.
     *
     * @return
     */
    @Deprecated
    public Matrix3 extractRotationAndScale() {
        Matrix3 m3 = new Matrix3(getElement(0, 0), getElement(0, 1), getElement(0, 2),
                getElement(1, 0), getElement(1, 1), getElement(1, 2),
                getElement(2, 0), getElement(2, 1), getElement(2, 2));
        return m3;
    }

    public Vector3 extractPosition() {
        //return new Vector3(getElement(0, 3), getElement(1, 3), getElement(2, 3));
        return (MathUtil2.extractPosition(this));
    }

    public Matrix4 getInverse() {
        return (MathUtil2.getInverse(this));
    }

    /**
     * http://www.songho.ca/opengl/gl_lookattoaxes.html
     * Confusing: Why left? This must be right in OpenGL(??)
     *
     * @return
     */
    public Vector3 getRight/*Left*/() {
        return getColumn(0).normalize();
    }

    /**
     * http://www.songho.ca/opengl/gl_lookattoaxes.html
     *
     * @return
     */
    public Vector3 getUp() {
        return getColumn(1).normalize();
    }

    /**
     * calculated by normalizing the lookat vector.
     * http://www.songho.ca/opengl/gl_lookattoaxes.html
     *
     * @return
     */
    public Vector3 getForward() {
        return getColumn(2).normalize();
    }

    public void setColumn0(Vector3 v) {
        a11 = v.getX();
        a21 = v.getY();
        a31 = v.getZ();
    }

    public void setColumn1(Vector3 v) {
        a12 = v.getX();
        a22 = v.getY();
        a32 = v.getZ();
    }

    public void setColumn2(Vector3 v) {
        a13 = v.getX();
        a23 = v.getY();
        a33 = v.getZ();
    }

    /**
     * Aka setColumn3()
     */
    public void setTranslation(Vector3 v) {
        a14 = v.getX();
        a24 = v.getY();
        a34 = v.getZ();
    }

    public static Matrix4 buildFromRightUpForward(Vector3 right, Vector3 up, Vector3 forward) {
        Matrix4 m4 = new Matrix4();
        m4.setColumn0(right);
        m4.setColumn1(up);
        m4.setColumn2(forward);
        return m4;
    }
}
