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
 * Independent from ECS.
 *
 * 18.3.22: Yes, balls are also GridItems, even though they are not created initially by the maze grid but dynamically when player join.
 * 'location' and 'owner' are mutually excluding.
 */
public interface GridItem {

    /**
     * null for collected (or moving/flying) items.
     *
     */
    Point getLocation() ;

    /**
     * Zunaechst zum initialisieren
     * 24.4.21
     */
    void setLocation(Point point);

    /**
     * Owner is int (eg. entity id) instead of GridMover for easier equals() and decoupling from instances in general.
     * Returns -1 for no owner.
     */
    int getOwner();

    void setOwner(int owner);

    /**
     * Set item state to 'collected'. Must clear location.
     * Owner is int (eg. entity id) instead of GridMover for easier equals() and decoupling from instances in general.
     *
     */
    void collectedBy(int collector);

    boolean isNeededForSolving();

    void setNeededForSolving();

    /**
     * Some id that make the item unique. Might be the entity id in ECS.
     */
    int getId();

}
