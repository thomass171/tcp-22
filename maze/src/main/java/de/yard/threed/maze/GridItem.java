package de.yard.threed.maze;

import de.yard.threed.core.Point;

/**
 * Abstraction of an element that resides in a grid and can be collected (put into inventory like balls, diamonds). But it cannot move or be pushed.
 *
 * Can also be in other states like flying,rolling, aso.Known states are
 * 1) In grid, having a location
 * 2) collected, no location
 * 3) moving (no location, not collected)
 *
 *
 * Independent from ECS.
 */
public interface GridItem {

    /**
     * null for collected items.
     *
     */
    Point getLocation() ;

    /**
     * Zunaechst zum initialisieren
     * 24.4.21
     */
    void setLocation(Point point);

    /**
     * Owner is int (entity id) instead of GridMover for easier equals() and decoupling from instances in general.
     */
    int getOwner();

    /**
     * Owner is int (entity id) instead of GridMover for easier equals() and decoupling from instances in general.
     *
     */
    void collectedBy(int collector);

}
