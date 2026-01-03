package de.yard.threed.core;

public class ParsingHelper {

    public static double[] getDoubleArrayTriple(String s, int expectedLength) throws ParseException {

        String delimiter = " ";
        if (StringUtils.contains(s, ",")) {
            delimiter = ",";
        }
        String[] p = StringUtils.splitByWholeSeparator(s, delimiter);
        double[] d = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            d[i] = Util.parseDouble(p[i]);
        }
        return d;
    }

    public static double[] getTriple(String s) throws ParseException {
        return getDoubleArrayTriple(s, 3);
    }

    public static Vector3 getVector3(String s) {
        double[] d = null;
        try {
            d = getDoubleArrayTriple(s, 3);
        } catch (ParseException e) {
            //TODO
            throw new RuntimeException(e);
        }
        return new Vector3(d[0], d[1], d[2]);
    }
}
