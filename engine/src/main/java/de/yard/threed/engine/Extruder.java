package de.yard.threed.engine;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

/**
 * Created by thomass on 04.09.15.
 */
public interface Extruder {
    Vector3 transformPoint(Vector2 p, double t);

    /**
     * Enthaelt auch die Startposition. Das ist bei aneinandergestückten wohl unguenstig, aber jetzt im
     * Moment das einzig sinnvolle. Sonst fehlt ja die Startposition. Wo soll die sonst herkommen?
     * 25.8.15: Naja, die Startpositon liegt ja wie beim SegmentedPath explizit vor. Sagen wir doch mal,
     * dass die hier nicht enthalten ist.
     *
     * @param segment
     * @return
     */
    double[] getTapeSteps(int segment);

    int getSegments();

    double getStart();
}
/*class Tape {
    List<Face> faces = new ArrayList<Face>();
    int index;

    Tape(int index) {
        this.index = index;
    }
} */

