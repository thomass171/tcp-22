package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Util;
import de.yard.threed.javacommon.BufferHelper;

import java.nio.FloatBuffer;

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
 *  * 15.11.18: Nicht deprecated wegen floatbuffer
 */

/* MA36 brauchts doch nicht?? doch, tofloatbuffer*/
public class OpenGlMatrix3  {

/*
    // Das Array ist in column order, wie OpenGL es verwendet,d.h.
    // 0,4,8,12
    // 1,5,9,13
    // 2,6,10,14
    // 3,7,11,15
    public float[] matrix = new float[9];

    /**
     * Identity Matrix
     * /
    public OpenGlMatrix3() {
        this(1, 0, 0,
                0, 1, 0,
                0, 0, 1);
    }

    /**
     * Konstruktor ist in "zeilenweise lesbarer", nicht colum order Form.
     * /
    public OpenGlMatrix3(float a11, float a12, float a13,
                         float a21, float a22, float a23,
                         float a31, float a32, float a33) {
        matrix[0] = a11;
        matrix[3] = a12;
        matrix[6] = a13;
        matrix[1] = a21;
        matrix[4] = a22;
        matrix[7] = a23;
        matrix[2] = a31;
        matrix[5] = a32;
        matrix[8] = a33;
    }

   
    public String dump(String lineseparator) {
        String[] label = new String[]{null, null, null};
        String s = "";

        s += Util.formatFloats(label, matrix[0], matrix[3], matrix[6]) + lineseparator;
        s += Util.formatFloats(label, matrix[1], matrix[4], matrix[7]) + lineseparator;
        s += Util.formatFloats(label, matrix[2], matrix[5], matrix[8]) ;
        return s;
    }

    /**
     * scale rausrechnen
     * Jetzt in mathutil2
     * @return
     * /
    @Deprecated
    public Matrix3 removeScale() {
        Util.nomore();
        /*MA16double sx =new Vector3(matrix[0], matrix[1], matrix[2]).length();
        double sy = (float) MathUtil2.length(new Vector3(matrix[3], matrix[4], matrix[5]));
        double sz = (float) MathUtil2.length(new Vector3(matrix[6], matrix[7], matrix[8]));

        sx = 1 / sx;
        sy = 1 / sy;
        sz = 1 / sz;

        return new Matrix3(
                matrix[0] * sx, matrix[3] * sy, matrix[6] * sx,
                matrix[1] * sx, matrix[4] * sy, matrix[7] * sy,
                matrix[2] * sz, matrix[5] * sz, matrix[8] * sz);
* /
        return null;
    }

    public FloatBuffer toFloatBuffer() {
        FloatBuffer buffer = BufferHelper.createFloatBuffer(9);
        buffer.put(matrix[0]);
        buffer.put(matrix[1]);
        buffer.put(matrix[2]);
        buffer.put(matrix[3]);
        buffer.put(matrix[4]);
        buffer.put(matrix[5]);
        buffer.put(matrix[6]);
        buffer.put(matrix[7]);
        buffer.put(matrix[8]);
        buffer.flip();
        return buffer;
    }*/

    public static FloatBuffer toFloatBuffer(Matrix3 m) {
        FloatBuffer buffer = BufferHelper.createFloatBuffer(9);
        buffer.put((float)m.e11);
        buffer.put((float)m.e21);
        buffer.put((float)m.e31);
        buffer.put((float)m.e12);
        buffer.put((float)m.e22);
        buffer.put((float)m.e32);
        buffer.put((float)m.e13);
        buffer.put((float)m.e23);
        buffer.put((float)m.e33);
        buffer.flip();
        return buffer;
    }

}
