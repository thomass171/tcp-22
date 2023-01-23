package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Matrix4;
import de.yard.threed.javacommon.BufferHelper;


import java.nio.FloatBuffer;

/**
 * Date: 25.04.14
 * <p/>
 * Konventionen aus Wikipedia.
 * <p/>
 * Das Element aij ist in Zeile i und  Spalte j
 * <p/>
 * In OpenGL werden bevorzugt column-major matrices verwendet.
 * 
 * 15.11.18: Nicht deprecated wegen floatbuffer
 */
/* MA36 brauchts doch nicht?? doch, tofloatbuffer. Und die merkwuerdigen converter */
public class OpenGlMatrix4 {/*implements Matrix4* / {
    // Das Array ist in column order, wie OpenGL es verwendet,d.h.
    //26.1.16: Die diskrete Speicherung statt Array ist DV technisch Unsinn, vielleicht etwas schneller.
    //Aber da es nunmal so ist, lass ich es erstmal auch so.
    // 0,4,8,12
    // 1,5,9,13
    // 2,6,10,14
    // 3,7,11,15
    //public float[] matrix = new float[16];*/
    public float e11, e12, e13, e14;
    public float e21, e22, e23, e24;
    public float e31, e32, e33, e34;
    public float e41, e42, e43, e44;

    /**
     * Identity Matrix
     */
    public OpenGlMatrix4() {
        this(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
    }

    /**
     * Konstruktor ist in "zeilenweise lesbarer", nicht colum order Form.
     * 8.7.14: Hmm, ist es doch.
     */
    public OpenGlMatrix4(float a11, float a12, float a13, float a14,
                         float a21, float a22, float a23, float a24,
                         float a31, float a32, float a33, float a34,
                         float a41, float a42, float a43, float a44) {
        e11 = a11;
        e12 = a12;
        e13 = a13;
        e14 = a14;
        e21 = a21;
        e22 = a22;
        e23 = a23;
        e24 = a24;
        e31 = a31;
        e32 = a32;
        e33 = a33;
        e34 = a34;
        e41 = a41;
        e42 = a42;
        e43 = a43;
        e44 = a44;

    }

    public static OpenGlMatrix4 toOpenGl(Matrix4 m) {
        return  new OpenGlMatrix4(
                (float)m.a11,(float)m.a12,(float)m.a13,(float)m.a14,
                (float)m.a21,(float)m.a22,(float)m.a23,(float)m.a24,
                (float)m.a31,(float)m.a32,(float)m.a33,(float)m.a34,
                (float)m.a41,(float)m.a42,(float)m.a43,(float)m.a44           );
    }

    public static Matrix4 fromOpenGl(OpenGlMatrix4 m) {
        return  new Matrix4(
                (float)m.e11,(float)m.e12,(float)m.e13,(float)m.e14,
                (float)m.e21,(float)m.e22,(float)m.e23,(float)m.e24,
                (float)m.e31,(float)m.e32,(float)m.e33,(float)m.e34,
                (float)m.e41,(float)m.e42,(float)m.e43,(float)m.e44           );
    }
    
    /*28.7.14 public Matrix4(float[] f) {
        System.arraycopy(f, 0, matrix, 0, 16);
    }* /

    /**
     * Angelegt 16.5.14: Nicht sicher, dass der geht (scheint aber richtig)
     * /
   /*OGL  public OpenGlMatrix4(Matrix4f viewer) {
        /*this(viewer.m00, viewer.m01, viewer.m02, viewer.m03,
                viewer.m10, viewer.m11, viewer.m12, viewer.m13,
                viewer.m20, viewer.m21, viewer.m22, viewer.m23,
                viewer.m30, viewer.m31, viewer.m32, viewer.m33); * /
        this(viewer.m00, viewer.m10, viewer.m20, viewer.m30,
                viewer.m01, viewer.m11, viewer.m21, viewer.m31,
                viewer.m02, viewer.m12, viewer.m22, viewer.m32,
                viewer.m03, viewer.m13, viewer.m22, viewer.m33);
    }* /

    public String toString() {
        String s = "";
        s += e11 + "," + e12 + "," + e13 + "," + e14 + ",";
        s += e21 + "," + e22 + "," + e23 + "," + e24 + ",";
        s += e31 + "," + e32 + "," + e33 + "," + e34 + ",";
        s += e41 + "," + e42 + "," + e43 + "," + e44;
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

    }* /
    public static OpenGlMatrix4 buildTranslationMatrix(OpenGlVector3 v) {
        OpenGlMatrix4 mat = new OpenGlMatrix4(
                1, 0, 0, v.x,
                0, 1, 0, v.y,
                0, 0, 1, v.z,
                0, 0, 0, 1);
        return mat;
    }

    /*OGL public static OpenGlMatrix4 buildRotationXMatrix(Degree theta) {
        OpenGlMatrix4 mat = new Matrix4(
                1, 0, 0, 0,
                0, cos(theta), -sin(theta), 0,
                0, sin(theta), cos(theta), 0,
                0, 0, 0, 1);
        return mat;
    }

    public static OpenGlMatrix4 buildRotationYMatrix(Degree theta) {
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
    }*/

    /**
     * Translation und Rotation in eine Matrix packen.
     * 16.4.15: Nach zwei Unittest stimmt die Reihenfolge wohl.
     * @param v
     * @param rotation
     * @return
     */
    /*OGL public static Matrix4 buildTransformationMatrix(Vector3 v, Quaternion rotation) {
        return buildTranslationMatrix(v).multiply(buildRotationMatrix(rotation));
    }* /



   /*OGL  public static float sin(Degree angle) {
        return (float) Math.sin(angle.toRad());
    }

    public static float cos(Degree angle) {
        return (float) Math.cos(angle.toRad());
    }* /

    /**
     * ohne weitere Berechnung nur den 3x3 Teil liefern.
     * @return
     * /
    public OpenGlMatrix3 extractRotationAndScale(){
        OpenGlMatrix3 m3 = new OpenGlMatrix3(getElement(0,0), getElement(0,1), getElement(0,2),
                getElement(1,0), getElement(1,1), getElement(1,2),
                getElement(2,0), getElement(2,1), getElement(2,2));
        return m3;
    }

    public static OpenGlMatrix4 buildScaleMatrix(OpenGlVector3 v) {
        OpenGlMatrix4 mat = new OpenGlMatrix4(
                v.x, 0, 0, 0,
                0, v.y, 0, 0,
                0, 0, v.z, 0,
                0, 0, 0, 1);
        return mat;
    }

    /**
     * Multipliziert this mit m, also this * m
     *
     * /
    public OpenGlMatrix4 multiply(OpenGlMatrix4 nm) {
        OpenGlMatrix4 m = (OpenGlMatrix4) nm;
        OpenGlMatrix4 res = new OpenGlMatrix4(
                e11 * m.e11 + e12 * m.e21 + e13 * m.e31 + e14 * m.e41,
                e11 * m.e12 + e12 * m.e22 + e13 * m.e32 + e14 * m.e42,
                e11 * m.e13 + e12 * m.e23 + e13 * m.e33 + e14 * m.e43,
                e11 * m.e14 + e12 * m.e24 + e13 * m.e34 + e14 * m.e44,
                e21 * m.e11 + e22 * m.e21 + e23 * m.e31 + e24 * m.e41,
                e21 * m.e12 + e22 * m.e22 + e23 * m.e32 + e24 * m.e42,
                e21 * m.e13 + e22 * m.e23 + e23 * m.e33 + e24 * m.e43,
                e21 * m.e14 + e22 * m.e24 + e23 * m.e34 + e24 * m.e44,
                e31 * m.e11 + e32 * m.e21 + e33 * m.e31 + e34 * m.e41,
                e31 * m.e12 + e32 * m.e22 + e33 * m.e32 + e34 * m.e42,
                e31 * m.e13 + e32 * m.e23 + e33 * m.e33 + e34 * m.e43,
                e31 * m.e14 + e32 * m.e24 + e33 * m.e34 + e34 * m.e44,
                e41 * m.e11 + e42 * m.e21 + e43 * m.e31 + e44 * m.e41,
                e41 * m.e12 + e42 * m.e22 + e43 * m.e32 + e44 * m.e42,
                e41 * m.e13 + e42 * m.e23 + e43 * m.e33 + e44 * m.e43,
                e41 * m.e14 + e42 * m.e24 + e43 * m.e34 + e44 * m.e44
        );

        return res;
    }

    //MA16 @Override
    public float getElement(int row, int column) {
        switch(row*4+column) {
            case 0:
                return e11;
            case 1:
                return e12;
            case 2:
                return e13;
            case 3:
                return e14;
            case 4:
                return e21;
            case 5:
                return e22;
            case 6:
                return e23;
            case 7:
                return e24;
            case 8:
                return e31;
            case 9:
                return e32;
            case 10:
                return e33;
            case 11:
                return e34;
            case 12:
                return e41;
            case 13:
                return e42;
            case 14:
                return e43;
            case 15:
                return e44;
        }
        //TODO Fehlerbehandlung
        return 0;
    }

    public OpenGlMatrix4 multiply(float f) {
        OpenGlMatrix4 res = new OpenGlMatrix4(
                e11 * f, e12 * f, e13 * f, e14 * f,
                e21 * f, e22 * f, e23 * f, e24 * f,
                e31 * f, e32 * f, e33 * f, e34 * f,
                e41 * f, e42 * f, e43 * f, e44 * f);
        return res;
    }

    /*private float multiply(float[] m1, int row, float[] m2, int col) {
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
     * 07.04.2015: Unabhaenig von Vertexbuffern (die spielen hier doch eh keine Rolle, hier
     * wird transformiert!) wird diese Methode zur Transformation eines Vector3 genutzt. Und
     * das sollte zuverlaessig funktionieren. Und das Resultat wird nicht normiert. Wenn der
     * Aufrufer das möchte, soll er es selber machen.
     *
     * @return
     * /
    public OpenGlVector3 transform(OpenGlVector3 nv) {
        OpenGlVector3 v = (OpenGlVector3) nv;
        float x = e11 * v.x + e12 * v.y + e13 * v.z + e14 * 1f;
        float y = e21 * v.x + e22 * v.y + e23 * v.z + e24 * 1f;
        float z = e31 * v.x + e32 * v.y + e33 * v.z + e34 * 1f;
        float w = e41 * v.x + e42 * v.y + e43 * v.z + e44 * 1f;

        v = new OpenGlVector3(x, y, z);
        //v.normalize();
        return v;
    }*/

    public FloatBuffer toFloatBuffer() {
        FloatBuffer buffer = BufferHelper.createFloatBuffer(16);
        buffer.put(e11);
        buffer.put(e21);
        buffer.put(e31);
        buffer.put(e41);
        buffer.put(e12);
        buffer.put(e22);
        buffer.put(e32);
        buffer.put(e42);
        buffer.put(e13);
        buffer.put(e23);
        buffer.put(e33);
        buffer.put(e43);
        buffer.put(e14);
        buffer.put(e24);
        buffer.put(e34);
        buffer.put(e44);
        buffer.flip();
        return buffer;
    }

    public static FloatBuffer toFloatBuffer(Matrix4 m) {
        FloatBuffer buffer = BufferHelper.createFloatBuffer(16);
        buffer.put((float)m.a11);
        buffer.put((float)m.a21);
        buffer.put((float)m.a31);
        buffer.put((float)m.a41);
        buffer.put((float)m.a12);
        buffer.put((float)m.a22);
        buffer.put((float)m.a32);
        buffer.put((float)m.a42);
        buffer.put((float)m.a13);
        buffer.put((float)m.a23);
        buffer.put((float)m.a33);
        buffer.put((float)m.a43);
        buffer.put((float)m.a14);
        buffer.put((float)m.a24);
        buffer.put((float)m.a34);
        buffer.put((float)m.a44);
        buffer.flip();
        return buffer;
    }

  /*OGL   public String dump(String lineseparator) {
        String[] label = new String[]{null, null, null, null};
        String s = "";

        s += Util.formatFloats(label, new float[]{e11, e12, e13, e14}) + lineseparator;
        s += Util.formatFloats(label, new float[]{e21, e22, e23, e24}) + lineseparator;
        s += Util.formatFloats(label, new float[]{e31, e32, e33, e34}) + lineseparator;
        s += Util.formatFloats(label, new float[]{e41, e42, e43, e44});
        return s;
    }* /

    /**
     * Den scale Anteil der Matrix extrahieren.
     *20.4.16: deprecated wegen Mathutil2
     *
     * @return
     * /
    @Deprecated
    public OpenGlVector3 extractScale() {
        return new OpenGlVector3((float) Math.sqrt(e11 * e11 + e21 * e21 + e31 * e31),
                (float) Math.sqrt(e12 * e12 + e22 * e22 + e32 * e32),
                (float) Math.sqrt(e13 * e13 + e23 * e23 + e33 * e33));

    }

    /**
     * Den Rotationsanteil der Matrix als Quaternion extrahieren.
     * Dazu das ganze auf der 3x3 Matrix machen
     *20.4.16: deprecated wegen Mathutil2
     * @return
     */
    /*MA16 @Deprecated
    public OpenGlQuaternion extractQuaternion() {
        return new OpenGlMatrix3(e11, e12, e13,
                e21, e22, e23,
                e31, e32, e33).extractQuaternion();

    }* /

    /**
     * 
     * 20.4.16: deprecated wegen Mathutil2
     * @return
     * /
    @Deprecated
    public OpenGlVector3 extractPosition() {
        return new OpenGlVector3(e14, e24, e34);
    }

    public OpenGlMatrix4 getInverse() {
        //Die folgende Operation liesse sich auch noch durch extrahieren mehrfacher Multiplikationen optimieren
        OpenGlMatrix4 inverse = new OpenGlMatrix4(
                e23 * e34 * e42 - e24 * e33 * e42 + e24 * e32 * e43 - e22 * e34 * e43 - e23 * e32 * e44 + e22 * e33 * e44,
                e14 * e33 * e42 - e13 * e34 * e42 - e14 * e32 * e43 + e12 * e34 * e43 + e13 * e32 * e44 - e12 * e33 * e44,
                e13 * e24 * e42 - e14 * e23 * e42 + e14 * e22 * e43 - e12 * e24 * e43 - e13 * e22 * e44 + e12 * e23 * e44,
                e14 * e23 * e32 - e13 * e24 * e32 - e14 * e22 * e33 + e12 * e24 * e33 + e13 * e22 * e34 - e12 * e23 * e34,
                e24 * e33 * e41 - e23 * e34 * e41 - e24 * e31 * e43 + e21 * e34 * e43 + e23 * e31 * e44 - e21 * e33 * e44,
                e13 * e34 * e41 - e14 * e33 * e41 + e14 * e31 * e43 - e11 * e34 * e43 - e13 * e31 * e44 + e11 * e33 * e44,
                e14 * e23 * e41 - e13 * e24 * e41 - e14 * e21 * e43 + e11 * e24 * e43 + e13 * e21 * e44 - e11 * e23 * e44,
                e13 * e24 * e31 - e14 * e23 * e31 + e14 * e21 * e33 - e11 * e24 * e33 - e13 * e21 * e34 + e11 * e23 * e34,
                e22 * e34 * e41 - e24 * e32 * e41 + e24 * e31 * e42 - e21 * e34 * e42 - e22 * e31 * e44 + e21 * e32 * e44,
                e14 * e32 * e41 - e12 * e34 * e41 - e14 * e31 * e42 + e11 * e34 * e42 + e12 * e31 * e44 - e11 * e32 * e44,
                e12 * e24 * e41 - e14 * e22 * e41 + e14 * e21 * e42 - e11 * e24 * e42 - e12 * e21 * e44 + e11 * e22 * e44,
                e14 * e22 * e31 - e12 * e24 * e31 - e14 * e21 * e32 + e11 * e24 * e32 + e12 * e21 * e34 - e11 * e22 * e34,
                e23 * e32 * e41 - e22 * e33 * e41 - e23 * e31 * e42 + e21 * e33 * e42 + e22 * e31 * e43 - e21 * e32 * e43,
                e12 * e33 * e41 - e13 * e32 * e41 + e13 * e31 * e42 - e11 * e33 * e42 - e12 * e31 * e43 + e11 * e32 * e43,
                e13 * e22 * e41 - e12 * e23 * e41 - e13 * e21 * e42 + e11 * e23 * e42 + e12 * e21 * e43 - e11 * e22 * e43,
                e12 * e23 * e31 - e13 * e22 * e31 + e13 * e21 * e32 - e11 * e23 * e32 - e12 * e21 * e33 + e11 * e22 * e33

        );
        float determinant = getDeterminant();

        if (determinant == 0.0f) {

            throw new RuntimeException("determinant isType 0");
        }
        return inverse.multiply(1f / determinant);

    }

    public float getDeterminant() {
        return e11 * (e23 * e34 * e42 - e24 * e33 * e42 + e24 * e32 * e43 - e22 * e34 * e43 - e23 * e32 * e44 + e22 * e33 * e44) +
                e21 * (e14 * e33 * e42 - e13 * e34 * e42 - e14 * e32 * e43 + e12 * e34 * e43 + e13 * e32 * e44 - e12 * e33 * e44) +
                e31 * (e13 * e24 * e42 - e14 * e23 * e42 + e14 * e22 * e43 - e12 * e24 * e43 - e13 * e22 * e44 + e12 * e23 * e44) +
                e41 * (e14 * e23 * e32 - e13 * e24 * e32 - e14 * e22 * e33 + e12 * e24 * e33 + e13 * e22 * e34 - e12 * e23 * e34);

    }

    /**
     * Zeilen und Spalten tauschen
     * @return
     * /
    public OpenGlMatrix4 transpose() {
        return new OpenGlMatrix4(e11, e21, e31, e41,
                e12, e22, e32, e42,
                e13, e23, e33, e43,
                e14, e24, e34, e44);
    }*/
}
