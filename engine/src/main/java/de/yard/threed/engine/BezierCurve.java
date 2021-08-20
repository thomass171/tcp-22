package de.yard.threed.engine;

import de.yard.threed.core.Vector3;

/**
 * Created by thomass on 24.01.17.
 */
public class BezierCurve {
    Vector3 p0, p1, p2, p3;

    public BezierCurve(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;

    }

    public Vector3 getPoint(float t) {
        double oneminust = 1 - t;
        double x = b3p0(t, p0.getX(), oneminust) + b3p1(t, p1.getX(), oneminust) + b3p2(t, p2.getX(), oneminust) + b3p3(t, p3.getX());
        double y = b3p0(t, p0.getY(), oneminust) + b3p1(t, p1.getY(), oneminust) + b3p2(t, p2.getY(), oneminust) + b3p3(t, p3.getY());
        double z = b3p0(t, p0.getZ(), oneminust) + b3p1(t, p1.getZ(), oneminust) + b3p2(t, p2.getZ(), oneminust) + b3p3(t, p3.getZ());
        return new Vector3(x,y,z);
    }


    private double b3p0(double t, double p, double oneminust) {
        return oneminust * oneminust * oneminust * p;
    }

    private double b3p1(double t, double p, double oneminust) {
        return 3 * oneminust * oneminust * t * p;
    }

    private double b3p2(double t, double p, double oneminust) {
        return 3 * oneminust * t * t * p;
    }

    private double b3p3(double t, double p) {
        return t * t * t * p;
    }
}
