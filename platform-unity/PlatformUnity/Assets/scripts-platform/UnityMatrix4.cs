using System;
using java.lang;
using UnityEngine;
using de.yard.threed.core;

namespace de.yard.threed.platform.unity
{

    using de.yard.threed.engine;
    using de.yard.threed.platform;

    /**
 * Date: 25.04.14
 * <p/>
 * Konventionen aus Wikipedia.
 * <p/>
 * Das Element aij ist in Zeile i und  Spalte j
 * <p/>
 * In OpenGL werden bevorzugt column-major matrices verwendet. In Unity auch.
 */
    public class UnityMatrix4  /*:  NativeMatrix4*/
    {
        public	UnityEngine.Matrix4x4 m;

        /**
     * Identity Matrix
     */
        public UnityMatrix4 () :
            this (1, 0, 0, 0,
                  0, 1, 0, 0,
                  0, 0, 1, 0,
                  0, 0, 0, 1)
        {
        }

        /**
     * Konstruktor ist in "zeilenweise lesbarer", nicht colum order Form.
     * this[int,int]	Access element at [row, column].
     */
        public UnityMatrix4 (float a11, float a12, float a13, float a14,
                             float a21, float a22, float a23, float a24,
                             float a31, float a32, float a33, float a34,
                             float a41, float a42, float a43, float a44)
        {
            m = new Matrix4x4 ();
            m.SetRow (0, new Vector4 (a11, a12, a13, a14));
            m.SetRow (1, new Vector4 (a21, a22, a23, a24));
            m.SetRow (2, new Vector4 (a31, a32, a33, a34));
            m.SetRow (3, new Vector4 (a41, a42, a43, a44));
        }

        public UnityMatrix4 (Matrix4x4 m)
        {
            this.m = m;   
        }

        public static UnityEngine.Matrix4x4 toUnity (Matrix4 m)
        {
            Matrix4x4 um = new Matrix4x4 ();
            um.SetRow (0, new Vector4 ((float)m.a11, (float)m.a12, (float)m.a13, (float)m.a14));
            um.SetRow (1, new Vector4 ((float)m.a21, (float)m.a22, (float)m.a23, (float)m.a24));
            um.SetRow (2, new Vector4 ((float)m.a31, (float)m.a32, (float)m.a33, (float)m.a34));
            um.SetRow (3, new Vector4 ((float)m.a41, (float)m.a42, (float)m.a43, (float)m.a44));  
            return um;
        }

        public static Matrix4 fromUnity (UnityEngine.Matrix4x4 m)
        {
            return  new Matrix4 (
                (float)m.m00, (float)m.m01, (float)m.m02, (float)m.m03,
                (float)m.m10, (float)m.m11, (float)m.m12, (float)m.m13,
                (float)m.m20, (float)m.m21, (float)m.m22, (float)m.m23,
                (float)m.m30, (float)m.m31, (float)m.m32, (float)m.m33);
        }

        virtual public string toString ()
        {
            /*string s = "";
        s += e11 + "," + e12 + "," + e13 + "," + e14 + ",";
        s += e21 + "," + e22 + "," + e23 + "," + e24 + ",";
        s += e31 + "," + e32 + "," + e33 + "," + e34 + ",";
        s += e41 + "," + e42 + "," + e43 + "," + e44;*/
            return m.ToString ();
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
        public static UnityMatrix4 buildTranslationMatrix (UnityVector3 v)
        {
            UnityMatrix4 mat = new UnityMatrix4 (
                                   1, 0, 0, v.v.x,
                                   0, 1, 0, v.v.y,
                                   0, 0, 1, v.v.z,
                                   0, 0, 0, 1);
            return mat;
        }

        /*OGL public static UnityMatrix4 buildRotationXMatrix(Degree theta) {
        UnityMatrix4 mat = new Matrix4(
                1, 0, 0, 0,
                0, cos(theta), -sin(theta), 0,
                0, sin(theta), cos(theta), 0,
                0, 0, 0, 1);
        return mat;
    }

    public static UnityMatrix4 buildRotationYMatrix(Degree theta) {
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
    }*/



        /*OGL  public static float sin(Degree angle) {
        return (float) Math.sin(angle.toRad());
    }

    public static float cos(Degree angle) {
        return (float) Math.cos(angle.toRad());
    }*/

        /**
     * ohne weitere Berechnung nur den 3x3 Teil liefern.
     * @return
     */
        virtual public UnityMatrix3 extractRotationAndScale ()
        {
            UnityMatrix3 m3 = new UnityMatrix3 (getElement (0, 0), getElement (0, 1), getElement (0, 2),
                                  getElement (1, 0), getElement (1, 1), getElement (1, 2),
                                  getElement (2, 0), getElement (2, 1), getElement (2, 2));
            return m3;
        }

        public static UnityMatrix4 buildScaleMatrix (UnityVector3 v)
        {
            UnityMatrix4 mat = new UnityMatrix4 (
                                   v.v.x, 0, 0, 0,
                                   0, v.v.y, 0, 0,
                                   0, 0, v.v.z, 0,
                                   0, 0, 0, 1);
            return mat;
        }

        /**
     * Multipliziert this mit m, also this * m
     *
     */
        virtual public /*Native*/Matrix4 multiply (/*Native*/Matrix4 nm)
        {
            /*Unity*/
            Matrix4x4 m1 = UnityMatrix4.toUnity (nm);
            Matrix4x4 res = m * m1/*.m*/;
            return UnityMatrix4.fromUnity (res);
        }

        virtual public float getElement (int row, int column)
        {
            return m [row, column];
        }

        /*unityvirtual public UnityMatrix4 multiply(float f) {
	return m.
        UnityMatrix4 res = new UnityMatrix4(
                e11 * f, e12 * f, e13 * f, e14 * f,
                e21 * f, e22 * f, e23 * f, e24 * f,
                e31 * f, e32 * f, e33 * f, e34 * f,
                e41 * f, e42 * f, e43 * f, e44 * f);
        return res;
    }*/

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
     */
        virtual public de.yard.threed.core.Vector3 transform (de.yard.threed.core.Vector3 nv)
        {
            UnityEngine.Vector3 v = UnityVector3.toUnity (nv);
            //17.3.17: Trick: Unity hat verschiedene mult Funkionen, die nur Teile der MAtrix verwenden.
            //return	new UnityVector3 (m.MultiplyVector (v.v)); 
            return  UnityVector3.fromUnity (m.MultiplyPoint (v)); 
        }

   
        /*OGL   public String dump(String lineseparator) {
        String[] label = new String[]{null, null, null, null};
        String s = "";

        s += Util.formatFloats(label, new float[]{e11, e12, e13, e14}) + lineseparator;
        s += Util.formatFloats(label, new float[]{e21, e22, e23, e24}) + lineseparator;
        s += Util.formatFloats(label, new float[]{e31, e32, e33, e34}) + lineseparator;
        s += Util.formatFloats(label, new float[]{e41, e42, e43, e44});
        return s;
    }*/

        /**
     * Den scale Anteil der Matrix extrahieren.
     *
     * @return
     */
        /* unity virtual public UnityVector3 extractScale() {
        return new UnityVector3((float) Math.Sqrt(e11 * e11 + e21 * e21 + e31 * e31),
                (float) Math.Sqrt(e12 * e12 + e22 * e22 + e32 * e32),
                (float) Math.Sqrt(e13 * e13 + e23 * e23 + e33 * e33));

    }*/

        /**
     * Den Rotationsanteil der Matrix als Quaternion extrahieren.
     * Dazu das ganze auf der 3x3 Matrix machen
     *
     * @return
     */
        virtual public de.yard.threed.core.Quaternion extractQuaternion ()
        {
            return UnityQuaternion.fromUnity (new UnityMatrix3 (m [0, 0], m [0, 1], m [0, 2],
                m [1, 0], m [1, 1], m [1, 2],
                m [2, 0], m [2, 1], m [2, 2]).extractQuaternion ().q);

        }

        /*unity  virtual public UnityVector3 extractPosition() {
        return new UnityVector3(e14, e24, e34);
    }*/

        virtual public UnityMatrix4 getInverse ()
        {
            //Die folgende Operation liesse sich auch noch durch extrahieren mehrfacher Multiplikationen optimieren
            return new UnityMatrix4 (m.inverse);
        }

        /*unity virtual public float getDeterminant() {
        return e11 * (e23 * e34 * e42 - e24 * e33 * e42 + e24 * e32 * e43 - e22 * e34 * e43 - e23 * e32 * e44 + e22 * e33 * e44) +
                e21 * (e14 * e33 * e42 - e13 * e34 * e42 - e14 * e32 * e43 + e12 * e34 * e43 + e13 * e32 * e44 - e12 * e33 * e44) +
                e31 * (e13 * e24 * e42 - e14 * e23 * e42 + e14 * e22 * e43 - e12 * e24 * e43 - e13 * e22 * e44 + e12 * e23 * e44) +
                e41 * (e14 * e23 * e32 - e13 * e24 * e32 - e14 * e22 * e33 + e12 * e24 * e33 + e13 * e22 * e34 - e12 * e23 * e34);

    }*/

        /**
     * Zeilen und Spalten tauschen
     * @return
     */
        virtual public UnityMatrix4 transpose ()
        {
            return new UnityMatrix4 (m.transpose);
        }
    }
}