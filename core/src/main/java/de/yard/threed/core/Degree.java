package de.yard.threed.core;

/**
 * Created by thomass on 20.04.15.
 */

public class Degree {
    private double degree;
    public static Degree ZERO = new Degree(0);

    public Degree(double degree) {
        this.degree = degree;
    }

    public static Degree buildFromRadians(double radians) {
        Degree d = new Degree(0);
        d.degree = (180.0f * radians / Math.PI);
        return d;
    }

    public double getDegree() {
        return degree;
    }

    public double toRad() {
        return MathUtil2.toRadians(degree);
    }

    public Degree add(Degree angle) {
        return new Degree(angle.degree + degree);
    }

    public Degree subtract(Degree angle) {
        return new Degree(degree - angle.degree);
    }

    public Degree multiply(double v) {
        return new Degree(degree * v);
    }

    @Override
    public String toString() {
        //return Util.formatFloats(null,new float[]{degree});
        return "" + degree;
    }

    public String toString(int total, int precision) {
        return Util.format(degree, total, precision);
    }

    /**
     * @param angle
     * @return
     */
    @Override
    public boolean equals(Object angle) {
        return isEqual((Degree) angle, MathUtil2.DBL_EPSILON);
    }

    public boolean isEqual(Degree angle, double epsilon) {
        return MathUtil2.areEqual(angle.normalize().getDegree(), this.normalize().getDegree(), epsilon);
    }

    public Degree normalize() {
        double d = degree;
        while (d < 0) {
            d += 360;
        }
        while (d >= 360) {
            d -= 360;
        }
        return new Degree(d);
    }

    public boolean isGreater(Degree degree) {
        return this.degree > degree.degree;
    }

    public boolean isLess(Degree degree) {
        return this.degree < degree.degree;
    }

    public Degree negate() {
        return new Degree(-degree);
    }

    public Degree reverse() {
        return new Degree(degree + 180);
    }

    /**
     * Gehoert eigentlich nicht hier hin weil es Koordinaten sind
     *
     * @param s
     * @return
     */
    public static Degree parseDegree(String s) {
        char f = StringUtils.charAt(s, 0);
        if (Util.isDigit(f)) {
            return new Degree(Util.parseDouble(s));
        }
        int erstesblank = StringUtils.lastIndexOf(s, " ");
        double minuten = Util.parseDouble(StringUtils.substring(s, erstesblank + 1));
        double grad = Util.parseDouble(StringUtils.substring(s, 1, erstesblank));
        double d = grad + minuten / 60f;
        if (f == 'W' || f == 'S') {
            d = -d;
        }
        return new Degree(d);
    }

    /**
     * Returns the value that needs to be added to d1 for reaching d2.
     * Range is from -180 to +180
     */
    public static Degree diff(Degree d1, Degree d2) {
        Degree diff = new Degree(d2.getDegree() - d1.getDegree());
        // using normalize() will break negative values!
        if (diff.getDegree() > 180.0) {
            return new Degree(360).subtract(diff).negate();
        }
        if (diff.getDegree() < -180.0) {
            return new Degree(360).subtract(diff.abs());
        }

        return diff;
    }

    public Degree abs() {
        return new Degree(degree >= 0 ? degree : -degree);
    }
}

