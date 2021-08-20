package de.yard.threed.engine;

import de.yard.threed.core.Vector3;

/**
 * Ein Pfad durch den 3D Raum.
 *
 * 28.8.2015: Es ist mathematisch ziemlich unsauber, stetige differenzierbare
 * Pfade und segmentierte geichzusatzen. Bei segmentierten, dürfte die Tangente an
 * Knickpunkte überhaupt nicht ermittelbar sein.
 * Created by thomass on 07.04.15.
 *
 */
public interface Path {
    public Vector3 getPosition(double t);

    public Vector3 getTangent(double t);

    public int getSegments();

    /**
     * Die Einzelschritte in einem Segment zur Extrusion.
     * Diese Schritte sind eigentlich keine Eigenschaft des Pfads, weil sie ja speziell
     * nur zur Extrusion sind. Ist aber einfach praktisch, sie hier unterzubringen.
     * Enthaelt auch die Startposition; die Groesse ist damit dann immmer
     * Anzahl Tapes in dem Segment + 1.
     *
     * @param segment
     * @return
     */
    public double[] getExtrusionSteps(int segment);

    public Vector3 getOrigin();
}
