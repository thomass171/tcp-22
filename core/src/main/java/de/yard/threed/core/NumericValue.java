package de.yard.threed.core;

import java.util.HashMap;

/**
 * Created by thomass on 23.02.16.
 */
public class NumericValue {
    // geht nicht als final wegen C#
    public static NumericValue REPEAT = new NumericValue(1);
    public static int UNSHADED = 0;
    public static int SMOOTH = 1;
    public static int FLAT = 2;


    public int ivalue;
    float fvalue;
    boolean isfloat;

    public NumericValue(float f) {
        this.fvalue = f;
        isfloat = true;
    }

    public NumericValue(int f) {
        this.ivalue = f;
        isfloat = false;
    }

    public static boolean unshaded(HashMap<NumericType, NumericValue> parameters) {
        if (parameters == null) {
            return false;
        }
        NumericValue p = parameters.get(NumericType.SHADING);
        if (p == null) {
            return false;
        }
        return p.ivalue == UNSHADED;
    }

    public static boolean flatshading(HashMap<NumericType, NumericValue> parameters) {
        if (parameters == null) {
            return false;
        }
        NumericValue p = parameters.get(NumericType.SHADING);
        if (p == null) {
            return false;
        }
        return p.ivalue == FLAT;
    }

    public static Float transparency(HashMap<NumericType, NumericValue> parameters) {
        if (parameters == null) {
            return null;
        }
        NumericValue p = parameters.get(NumericType.TRANSPARENCY);
        if (p == null) {
            return null;
        }
        if (p.isfloat) {
            return Float.valueOf(p.fvalue);
        }
        return Float.valueOf(p.ivalue);
    }

    @Override
    public String toString() {
        if (isfloat) {
            return "" + fvalue;
        }
        return "" + ivalue;
    }
}
