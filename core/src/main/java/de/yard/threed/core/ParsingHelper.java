package de.yard.threed.core;

public class ParsingHelper {

    public static double[] getDoubleArrayTriple(String s, int expectedLength) {

        String delimiter = " ";
        if (StringUtils.contains(s, ",")) {
            delimiter = ",";
        }
        String[] p = StringUtils.split(s, delimiter);
        double[] d = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            d[i] = Util.parseDouble(p[i]);
        }
        return d;
    }

    public static double[] getTriple(String s) {
        return getDoubleArrayTriple(s, 3);
    }

    public static Vector3 getVector3(String s) {
        double[] d = getDoubleArrayTriple(s, 3);
        return new Vector3(d[0], d[1], d[2]);
    }
}
