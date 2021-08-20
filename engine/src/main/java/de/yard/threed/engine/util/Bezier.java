package de.yard.threed.engine.util;

import de.yard.threed.core.Vector3;

public class Bezier {
    /**
     * From http://devmag.org.za/2011/04/05/bzier-curves-a-tutorial/
     */
    public static Vector3 CalculateBezierPoint(float t, Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        Vector3 p = p0.multiply(uuu);
        p = p.add(p1.multiply(3 * uu * t));
        p = p.add(p2.multiply(3 * u * tt));
        p = p.add(p3.multiply(ttt));

        return p;
    }
}
