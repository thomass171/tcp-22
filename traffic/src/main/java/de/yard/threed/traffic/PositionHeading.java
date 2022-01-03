package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;

/**
 * Created by thomass on 02.05.17.
 */
public class PositionHeading {
 public   Vector3 position;
    public Degree heading;

    public PositionHeading(Vector3 position, Degree heading){
        this.position=position;
        this.heading=heading;
    }
}
