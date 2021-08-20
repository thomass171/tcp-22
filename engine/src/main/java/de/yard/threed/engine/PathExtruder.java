package de.yard.threed.engine;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.platform.Log;

/**
 * Created by thomass on 04.09.15.
 */
public class PathExtruder implements Extruder {
    private  Path path;
    // die Richtung der Extrusion (negatives x, weil nach hinten in den Raum)
    Vector3 extrudedirection = new Vector3(0, 0, -1);
    Log elogger = Platform.getInstance().getLog(Extruder.class);

    public PathExtruder(Path path) {
        this.path = path;
    }
    /**
     * Die Position ist zum verschieben, die Tangente zum Rotieren
     * Und natürlich erst rotieren dann schieben.
     * 5.12.16: Je nach Dichte der Segmente und Größe der Rotation können Nachfolgesegmente "vor" den Vorgängern liegen. Das führt zu entarteten Oberflächen mit falschen
     * Normalen (z.B. beim wheel). Eine alternative Extrusion ist per CircleExtruder.
     */
    @Override
    public Vector3 transformPoint(Vector2 p, double t) {
        if (ShapeGeometry.debug) {
            elogger.debug("transformPoint: point=" + p + ", t=" + t);
        }
        Vector3 pathposition = path.getPosition(t);
        Vector3 v = new Vector3(p.x, p.y, 0);
        Matrix4 transformmatrix = SegmentedPath.buildTransformationMatrix(pathposition, extrudedirection, path.getTangent(t), /*v*/path.getOrigin());
        return transformmatrix.transform(v);
    }

    @Override
    public double[] getTapeSteps(int segment) {
        // Der Path zaehlt den Beginn nicht als Step. Darum wird 1 addiert, weil der
        // getTapeSteps da... Nee, ist jetzt doch nicht mit drin.
        return path.getExtrusionSteps(segment);
    }

    @Override
    public int getSegments() {
        return path.getSegments();
    }

    @Override
    public double getStart() {
        return 0;
    }
}
