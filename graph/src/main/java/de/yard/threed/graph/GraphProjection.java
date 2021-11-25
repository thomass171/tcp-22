package de.yard.threed.graph;

import de.yard.threed.core.LocalTransform;

/**
 * Eine Projection zwischen zwei GraphKoordinatensystemen. Vermeidet Abhaengigkeit zu einer MapProjection, die
 * vielleicht doch zu speziell ist.
 * 
 * Created on 10.01.19.
 */
public interface GraphProjection {
    LocalTransform project( GraphPosition cp);
}
