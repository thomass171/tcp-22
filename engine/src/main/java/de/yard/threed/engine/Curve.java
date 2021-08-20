package de.yard.threed.engine;

import de.yard.threed.core.Vector3;

/**
 *
 *
 * Created by thomass on 01.05.15.
 */
public abstract class Curve {
    
    public abstract Vector3 getPointAt(double t);

    //type.getTangentAt
}
