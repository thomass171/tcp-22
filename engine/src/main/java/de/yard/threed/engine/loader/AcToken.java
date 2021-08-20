package de.yard.threed.engine.loader;

import de.yard.threed.core.StringUtils;

/**
 * Created by thomass on 31.03.16.
 */
public class AcToken {
    public float floatvalue;
    public boolean isfloat;
    public int intvalue;
    public String stringvalue;
    public boolean objTupel = false;
    public int[] ni, vi;
    public PortableMaterial material;

    public AcToken(float floatvalue) {
        this.floatvalue = floatvalue;
        isfloat = true;
    }

    public AcToken(int intvalue) {
        this.intvalue = intvalue;
    }

    public AcToken(String stringvalue) {
        this.stringvalue = stringvalue;
    }

    @Override
    public boolean equals(Object s) {
        return ((String) s).equals(stringvalue);
    }

    @Override
    public String toString() {
        return stringvalue + ";" + floatvalue + ";" + intvalue;
    }

    public boolean isObject() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("OBJECT");
    }

    public boolean isKids() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("kids");
    }

    public boolean isMaterial() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("MATERIAL");
    }

    public boolean isName() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("name");
    }

    public boolean isLoc() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("loc");
    }

    public boolean isTexture() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("texture");
    }

    public boolean isCrease() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("crease");
    }

    public boolean isNumvert() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("numvert");
    }

    public boolean isNumsurf() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("numsurf");
    }

    public boolean isSurf() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("SURF");
    }

    public boolean isMat() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("mat");
    }

    public boolean isRefs() {
        if (stringvalue == null)
            return false;
        return stringvalue.equals("refs");
    }

    public boolean isAc3D() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"AC3D");
    }

    public float getValueAsFloat() throws InvalidDataException {
        if (stringvalue != null)
            throw new InvalidDataException("token isType no numeric value but " + stringvalue);
        // die implizite Erkennung muesste so doch moeglich sein. Wenn beide 0 sind, ists egal. Und
        // beide ungleich 0 darf ja nicht. Ginge jetzt aber auch ueber isfloat.
        if (intvalue != 0)
            return intvalue;
        return floatvalue;
    }

    public boolean isMtlLib() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"mtllib");
    }

    public boolean isUsemtl() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"usemtl");
    }

    public boolean isF() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"f");
    }

    public boolean isO() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"o");
    }

    public boolean isS() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"s");
    }

    public boolean isL() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"l");
    }

    public boolean isG() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"g");
    }

    public boolean isV() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"v");
    }

    public boolean isVT() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"vt");
    }

    public boolean isVN() {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,"vn");
    }

    public boolean isIdent(String key) {
        if (stringvalue == null)
            return false;
        return StringUtils.startsWith(stringvalue,key);
    }
}
