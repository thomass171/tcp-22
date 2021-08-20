package de.yard.threed.engine;


import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Das leidige Thema "Gibt es einen Anfang?" und wieviele:
 * 25.8.15: Jetzt sagen wir, dass das erste Segment einen Anfang hat, die folgenden aber nicht. Das cheint schlüssig.
 * NeeNee, den Startpunkt gibt es ja schon, er liegt nur VOR dem ersten Segment.
 * <p/>
 * Created by thomass on 07.04.15.
 */
public class SegmentedPath implements Path {
    Log logger = Platform.getInstance().getLog(SegmentedPath.class);
    private Vector3 origin;
    private List<Segment> segments = new ArrayList<Segment>();
    private Vector3 destination;
   // private float length;
    public static final boolean debug = false;

    public SegmentedPath(Vector3 origin) {
        this.origin = origin;
    }

    /**
     * Die Angabe des Radius ist redumdant, weil er sich immer aus dem letzten Segemtn bzw. Origin
     * herleiten laesst.
     *
     * @param center
     * @param angle
     */
    public void addArc(Vector3 center, Degree angle, int segcnt) {
        segments.add(new Segment(getDestination(), center, angle, segcnt));
    }

    public void addLine(Vector3 destination) {
        segments.add(new Segment(getDestination(), destination));
    }

    @Override
    public Vector3 getPosition(double t) {
        if (debug) {
            logger.debug("getPosition " + t);
        }
        double t1 = t;
        for (Segment seg : segments) {
            if (t1 <= seg.len) {
                return seg.getPosition(t1);
            }
            t1 -= seg.len;
        }
        logger.warn("No path position for t=" + t + ", total length=" + getLength());
        // Das Ende kann schon mal knapp verpasst werden. Irgendwie unschoener Workaround
        if (t - getLength() < 0.00001f) {
            return getPosition(t - 0.00001f);
        }
        return null;
    }

    /**
     * Wird über den Vektor von t-0.001 bis t+0.001 ermittelt.
     * Mathematisch sehr fragwürdig.
     *
     * @param t
     * @return
     */
    @Override
    public Vector3 getTangent(double t) {
        double offset = 0.0001f;
        if (debug) {
            logger.debug("getTangent " + t);
        }
        double t1 = t - offset;
        if (t1 < 0) {
            t1 = 0;
        }
        Vector3 pos1 = getPosition(t1);
        t1 = t + offset;
        if (t1 > getLength()) {
            t1 = getLength();
        }
        Vector3 pos2;
        //TODO das ist ziemlich wirr. Ueberhaupt. Tangenten koennen exakt ueber die Segments ermittelt werden.
        //28.8.15: Aber doch nicht an den Knickpunkten. Das ist mathematisch doch überhaupt nicht definiert.
        do {
            pos2 = getPosition(t1);
            t1 -= 0.001f;
        } while (pos2 == null && t1 >= 0);
        if (pos2 == null) {
            logger.error("finally no position found for " + t);
        }
        Vector3 tangente = pos2.subtract(pos1).normalize();
        if (debug) {
            logger.debug("tangente= " + tangente.dump(""));
        }
        return tangente;
    }

    @Override
    public int getSegments() {
        return segments.size();
    }

    public Vector3 getDestination() {
        if (segments.size() == 0)
            return origin;
        return segments.get(segments.size() - 1).getDestination();
    }

    @Override
    public Vector3 getOrigin() {
        return origin;
    }


    public double getLength() {
        return getLength(segments.size());
    }

    public double getLength(int bissegment) {
        double l = 0;
        for (int i = 0; i < bissegment; i++) {
            l += segments.get(i).len;
        }
        return l;
    }

    /**
     * Die Einzelschritte in einem Segment zur Extrusion.
     * Diese Schritte sind eigentlich keine Eigenschaft des Pfads, weil sie ja speziell
     * nur zur Extrusion sind. Ist aber einfach praktisch, sie hier unterzubringen.
     *
     * @param segment
     * @return
     */
    @Override
    public double[] getExtrusionSteps(int segment) {
        double startlen = getLength(segment);
        double[] steps = segments.get(segment).getExtrusionSteps();
        for (int i = 0; i < steps.length; i++) {
            steps[i] += startlen;
        }
        return steps;
    }

    /**
     * Die Transformationsmatrix bauen, um eine Transformation abhängig von der Position auf
     * einem (Extrusions)pfad durchzufuehren.
     *
     * @return
     */
    public static Matrix4 buildTransformationMatrix(Vector3 pathposition, Vector3 extrudedirection, Vector3 pathtangent, Vector3 origin) {
        Log elogger = Platform.getInstance().getLog(SegmentedPath.class);

        if (pathposition == null) {
            elogger.warn("pathposition isType null");
        }
        //elogger.debug("origin=" + origin.dump("\n"));
        //elogger.debug("pathposition=" + pathposition.dump("\n"));
        Quaternion pathrotation = Vector3.getRotation(extrudedirection, pathtangent);
        if (debug) {
            elogger.debug("pathtangent=" + pathtangent.dump("\n") + ",pathrotation=" + pathrotation.dump("\n"));
        }
        double[] angles = new double[3];
        pathrotation.toAngles(angles);
        //elogger.debug("x-rot=" + Degree.buildFromRadians(angles[0]).degree);
        //elogger.debug("y-rot" + Degree.buildFromRadians(angles[1]).degree);
        //elogger.debug("z-rot" + Degree.buildFromRadians(angles[2]).degree);

        // Fuer die Rotation ist erst das Zuruecksetzen auf (0,0,0) erforderlich. Dann rotieren
        // und rotiert ans Ziel schieben.
        Matrix4 moveback = Matrix4.buildTranslationMatrix(origin).getInverse();
        //elogger.debug("moveback=" + moveback.dump("\n"));
        //Matrix4 transformmatrix = Matrix4.buildTransformationMatrix(pathposition, pathrotation);
        Matrix4 transformmatrix = Matrix4.buildRotationMatrix(pathrotation);
        //elogger.debug("transformmatrix reine Rotation =" + transformmatrix.dump("\n"));
        transformmatrix = transformmatrix.multiply(moveback);
        //transformmatrix = moveback.multiply(transformmatrix);
        //elogger.debug("transformmatrix moveback und Rotation =" + transformmatrix.dump("\n"));
        Matrix4 moveforward = Matrix4.buildTranslationMatrix(pathposition);
        // elogger.debug("moveforward=" + moveforward.dump("\n"));
        //transformmatrix = transformmatrix.multiply(moveforward);
        transformmatrix = moveforward.multiply(transformmatrix);

        if (debug) {
            elogger.debug("transformmatrix=" + transformmatrix.dump("\n"));
        }
        return transformmatrix;
    }

    /**
     * Ein quasi kompletter Kreis.
     * Der Kreis wird nicht ganz geschlossen, weil er für Extrusion von geschlossenen Shapes verwendet wird.
     * 02.11.16: closed geo gibts nicht mehr. Darum doch ganz rum.
     * @param segments
     * @return
     */
    public static SegmentedPath buildHorizontalArc(double radius, int segments) {
        SegmentedPath path = new SegmentedPath(new Vector3(radius, 0, 0));
        // Weil die Geometrie geschlossen wird, das letzte Segment nicht im  Path anlegen
        Degree r = new Degree(360 /** (double) /*(segments /*- 1* /) / segments*/);
        path.addArc(new Vector3(0, 0, 0), r, segments /*- 1*/);
        return path;
    }

    /**
     * Ein nicht geschlossenes Bogenstueck mit Center in 0,0,0.
     *
     * @param segments
     * @return
     */
    public static SegmentedPath buildHorizontalArc(Degree len, double radius, int segments) {
        SegmentedPath path = new SegmentedPath(new Vector3(radius, 0, 0));
        path.addArc(new Vector3(0, 0, 0), len, segments);
        return path;
    }
}

class Segment {
    public double len;
    private Vector3 origin;
    boolean isline;
    Vector3 destination;
    // Nur fuer Arc
    Vector3 center;
    Degree angle;
    int steps;
    Log logger = Platform.getInstance().getLog(Segment.class);

    /**
     * Line Constructor
     *
     * @param origin
     * @param destination
     */
    public Segment(Vector3 origin, Vector3 destination) {
        this.origin = origin;
        this.destination = destination;
        this.len = Vector3.getDistance(origin, destination);
        if (Math.abs(len) < 0.00001) {
            //TODO anderes Handling
            throw new RuntimeException("length isType 0");
        }

        isline = true;
    }

    /**
     * Arc Constructor
     * Der Winkel ist der Gesamtwinkel, der sich dann in die Anzahl Steps teilt. Die einzelnen Winkelstuecke sind dann angle/steps
     */
    public Segment(Vector3 origin, Vector3 center, Degree angle, int steps) {
        if (SegmentedPath.debug) {
            logger.debug("Segment at " + origin + ", center=" + center + ", angle=" + angle.getDegree() + ", steps=" + steps);
        }
        this.origin = origin;
        this.center = center;
        this.angle = angle;
        this.steps = steps;
        len =  (angle.toRad() * Vector3.getDistance(origin, center));
        if (Math.abs(len) < 0.00001) {
            //TODO anderes Handling
            throw new RuntimeException("length isType 0");
        }

        isline = false;
        this.destination = getDestinationOfArc(angle);
        // System.out.println("arclen=" + length);
    }

    public Vector3 getPosition(double t) {
        if (SegmentedPath.debug) {
            logger.debug("getPosition " + t + " len=" + len);
        }
        if (isline) {
            // logger.debug("getPosition origin=" + origin);
            // logger.debug("getPosition destination=" + destination);

            return origin.add(destination.subtract(origin).multiply(t / len));
        }
        Degree anglestep = new Degree(angle.getDegree() * t / len);
        Vector3 result = getDestinationOfArc(anglestep);
        if (SegmentedPath.debug) {
            logger.debug("getPosition returns " + result);
        }
        return result;

    }


    public Vector3 getDestination() {
        return destination;
    }

    private Vector3 getDestinationOfArc(Degree angle) {
        // mit Matrizen dürfte das  eleganter sein.

        Matrix4 rotmatrix = Matrix4.buildRotationYMatrix(angle);
        Vector3 v = origin.subtract(center);
        //System.out.println(rotmatrix.dump("\n"));
        return center.add(rotmatrix.transform(v));
    }

    /**
     * Immer den Start mitliefern ist zwar fuer den Anfang praktisch, danach aber nicht mehr
     * weil damit einfach nur doppelte Extrusionen an der selben Position entstehen.
     * 16.4.15: Das lass ich erstmal. Für den Anfang wird erstmal 0 als Default verwendet.
     *
     * @return
     */
    public double[] getExtrusionSteps() {
        if (isline) {
            return new double[]{/*0,*/ len};
        }
        double[] esteps = new double[steps /*+ 1/*(int) (angle.degree / 10)*/];
        // esteps[0] = 0;
        for (int i = 0; i < steps; i++) {
            esteps[i/* + 1*/] = len * (i + 1) / steps;
        }
        return esteps;
    }
}
