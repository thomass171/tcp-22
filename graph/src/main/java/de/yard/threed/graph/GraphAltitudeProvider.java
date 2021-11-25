package de.yard.threed.graph;

import de.yard.threed.core.Vector3;

/**
 * Entkopplung speziell fuer "getHyperSpeedAltitude" und orbit travel.
 *
 * MA31
 */
public interface GraphAltitudeProvider {
    double getAltitude(Vector3 vector3);
}
