package de.yard.threed.maze;

import de.yard.threed.core.Point;

/**
 * A box with a player (child)
 */
public class CombinedGridMover extends SimpleGridMover {

    CombinedGridMover(Point location, GridOrientation orientation, GridMover child){
        super(location,orientation);

    }

}
