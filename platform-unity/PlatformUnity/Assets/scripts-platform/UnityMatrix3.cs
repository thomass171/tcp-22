using System;
using java.lang;
using de.yard.threed.core;

namespace de.yard.threed.platform.unity
{

    using de.yard.threed.engine;

// import de.yard.threed.engine.Matrix3;
    // import de.yard.threed.engine.Util;
   

    /**
 * Date: 25.07.14
 * <p/>
 * Konventionen aus Wikipedia.
 * <p/>
 * Das Element aij ist in Zeile i und  Spalte j
 * <p/>
 * Diese 3x3 Marizen sind eigentlich nur fuer manche Berechnungen der Uebersichtlichkeit wegen erforderlich bzw.
 * eine sauberer Modellierung.
 * 17.3.16: MAtrix3 gibt es nicht als Native, weil sie in den Platformen auch kaum verwendet werden.
 * 
 * Unity hat offenbar keine Matrix3.
 */

    public class UnityMatrix3
    {
        // Das Array ist in column order, wie OpenGL und Unity es verwendet,d.h.
        // 0,4,8,12
        // 1,5,9,13
        // 2,6,10,14
        // 3,7,11,15
        public double[] matrix = new double[9];

        /**
     * Identity Matrix
     */
        public UnityMatrix3 () :
            this (1, 0, 0,
             0, 1, 0,
             0, 0, 1)
        {
        }

        /**
     * Konstruktor ist in "zeilenweise lesbarer", nicht colum order Form.
     */
        public UnityMatrix3 (double a11, double a12, double a13,
            double a21, double a22, double a23,
            double a31, double a32, double a33)
        {
            matrix [0] = a11;
            matrix [3] = a12;
            matrix [6] = a13;
            matrix [1] = a21;
            matrix [4] = a22;
            matrix [7] = a23;
            matrix [2] = a31;
            matrix [5] = a32;
            matrix [8] = a33;
        }

        /**
     * Den Rotationsanteil der Matrix als Quaternion extrahieren.
     * Der Algorithmus ist Woodo. Infos dazu finden sich bei
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
     * <p/>
     * Es gibt auch andere Quellen. Die Algorithmen sind teilweise leicht unterschiedlich.
     * Sicher ist aber, dass der Algorithmus nur mit Matrizen ohne Scale geht.
     *
     * Das hat Unity offenbar nicht als native Funktion.
     * 
     * @return
     */
        virtual public UnityQuaternion extractQuaternion ()
        {
            // als erstes scale rausrechnen
            UnityMatrix3 ohnescale = removeScale ();

            double m00 = ohnescale.matrix [0], m01 = ohnescale.matrix [3], m02 = ohnescale.matrix [6],
            m10 = ohnescale.matrix [1], m11 = ohnescale.matrix [4], m12 = ohnescale.matrix [7],
            m20 = ohnescale.matrix [2], m21 = ohnescale.matrix [5], m22 = ohnescale.matrix [8];
            double trace = m00 + m11 + m22;
            double x, y, z, w;

            // Der folgende Algorithmus ist aus jme3

            if (trace >= 0) {
                double s = Math.Sqrt (trace + 1);
                w = 0.5f * s;
                s = 0.5f / s;
                x = (m21 - m12) * s;
                y = (m02 - m20) * s;
                z = (m10 - m01) * s;
            } else if ((m00 > m11) && (m00 > m22)) {
                double s = Math.Sqrt (1.0f + m00 - m11 - m22);
                x = s * 0.5f;
                s = 0.5f / s;
                y = (m10 + m01) * s;
                z = (m02 + m20) * s;
                w = (m21 - m12) * s;
            } else if (m11 > m22) {
                double s = Math.Sqrt (1.0f + m11 - m00 - m22);
                y = s * 0.5f;
                s = 0.5f / s;
                x = (m10 + m01) * s;
                z = (m21 + m12) * s;
                w = (m02 - m20) * s;
            } else {
                double s = Math.Sqrt (1.0f + m22 - m00 - m11);
                z = s * 0.5f;
                s = 0.5f / s;
                x = (m02 + m20) * s;
                y = (m21 + m12) * s;
                w = (m10 - m01) * s;
            }
            return new UnityQuaternion (x, y, z, w);
        }

        virtual public string dump (string lineseparator)
        {
            string[] label = new String[]{ null, null, null };
            string s = "";

            s += Util.formatFloats (label, matrix [0], matrix [3], matrix [6]) + lineseparator;
            s += Util.formatFloats (label, matrix [1], matrix [4], matrix [7]) + lineseparator;
            s += Util.formatFloats (label, matrix [2], matrix [5], matrix [8]);
            return s;
        }

        /**
     * scale rausrechnen
     *
     * @return
     */
        virtual public UnityMatrix3 removeScale ()
        {
            double sx = new de.yard.threed.core.Vector3 (matrix [0], matrix [1], matrix [2]).length();
            double sy = new de.yard.threed.core.Vector3 (matrix [3], matrix [4], matrix [5]).length();
            double sz = new de.yard.threed.core.Vector3 (matrix [6], matrix [7], matrix [8]).length();

            sx = 1 / sx;
            sy = 1 / sy;
            sz = 1 / sz;

            return new UnityMatrix3 (
                matrix [0] * sx, matrix [3] * sy, matrix [6] * sx,
                matrix [1] * sx, matrix [4] * sy, matrix [7] * sy,
                matrix [2] * sz, matrix [5] * sz, matrix [8] * sz);

        }

    

    }
}