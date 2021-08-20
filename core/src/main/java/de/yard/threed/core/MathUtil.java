package de.yard.threed.core;

import de.yard.threed.core.Degree;

/**
 * Created by thomass on 13.11.14.
 */
public class MathUtil {
    public static final float PI = (float) Math.PI;
    public static final float PI_2 = (float) Math.PI / 2;
    public static final float PI_4 = (float) Math.PI / 4;
    public static float floatequalstolerance = 0.0001f;
    //TODO in config wie geodebug etc.
    public static boolean mathvalidate = true;
    
    /**
     * Die Laenge der Basis eines gleichschenkeligen Dreiecks berechnen.
     *
     * @return
     */
    public static float getBaseLen(float leglen, Degree angle){
        float len = (float) Math.sqrt(2 * leglen * leglen * (1 - Math.cos(angle.toRad())));
        return len;
    }

    /**
     * TODO: Das muss optimiert werden, damit die Toleranz von der Dimension abhängt (bzw nur die Mantisse verglichen wird (oder irgendwie so)
     * 20.10.19: Gibts besser in MathUtil2. Weg damit.
     * @param f1
     * @param f2
     * @return
     */
    @Deprecated
    public static boolean floatEquals(float f1, float f2) {
        return Math.abs(f1 - f2) < floatequalstolerance;
    }

    


}
