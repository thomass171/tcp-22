package de.yard.threed.engine;


import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

/**
 * Als Umfang 1 annehmen.
 * Es gibt nur ein Segment im Sinne des Extruders, nämlich den Kreisbogen. Der hat wiederum verschiedene Steps.
 * Kreisförmige Extrusion per SegmentedPath kann bei engen Kurven zu inkonsistenten Faces führen (innere Vertices überlappend).
 * <p>
 * Created by thomass on 05.12.16.
 */
public class CircleExtruder implements Extruder {
    int steps;
    private double[] tapesteps;
    double spanangle;
    //float radius ;

    public CircleExtruder(int segments, double spanangle) {
        this.steps = segments;
        this.spanangle = spanangle;
        tapesteps = new double[segments];
        for (int i = 1; i <= segments; i++) {
            tapesteps[i - 1] =  (double)i / segments;
        }
    }

    /**
     * Komplettkreis
     *
     * @param segments
     */
    public CircleExtruder(int segments) {
        this(segments,  (2 * Math.PI));
    }

    @Override
    public Vector3 transformPoint(Vector2 p, double t) {
        double angle =  (2 *  Math.PI * t * (spanangle / (2 * Math.PI)));
        Vector3 v = new Vector3(p.x *  Math.cos(angle), p.y, -p.x *  Math.sin(angle));
        return v;
    }

    /**
     * @param segment
     * @return
     */
    @Override
    public double[] getTapeSteps(int segment) {
        return tapesteps;
    }

    @Override
    public int getSegments() {
        return 1;
    }

    @Override
    public double getStart() {
        return 0;
    }
}
