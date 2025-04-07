package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

/**
 * Now 'FG space' is the standard in module 'traffic'
 */
public class FgVehicleSpace {

    public static Vector3 DEFAULT_FORWARD = new Vector3(1, 0, 0);
    public static Vector3 DEFAULT_UP = new Vector3(0, 0, 1);
    public static Vector3 DEFAULT_RIGHT = new Vector3(0, 1, 0);

    /**
     * Returns the rotation needed to place a FG oriented model to a graph.
     * Because FG are not oriented like graph used to use.
     */
    public static Quaternion getFgVehicleForwardRotation() {
        Quaternion rotation = Quaternion.buildFromAngles(new Degree(180), new Degree(0), new Degree(-90));
        // Die Werte entstanden durch ausprobieren. :-) Vielleicht laesst sich das mal untermauern. TODO
        rotation = Quaternion.buildFromAngles(new Degree(-90), new Degree(-90), new Degree(0));
        return rotation;
    }

    /**
     * Transform vehicle/loc space ... to FG space.
     * 'Forward' is -x in both
     * 'Right' in 'loc space' is -z, in FG it is +y (with current symetric model +/- probably doesn't matter, but we should be correct.
     * 'Up' in 'loc space' is +y, in FG it is +z
     *
     * This method is not really used but for proving/testing the rotation is correct. The rotation is used in XML files.
     */
    public static Quaternion getLocSpaceToFgSpace() {
        // x rotation for transforming 'up*, should also be sufficient for fixing 'right'
        return Quaternion.buildFromAngles(new Degree(90),new Degree(0),new Degree(0));
    }
}
