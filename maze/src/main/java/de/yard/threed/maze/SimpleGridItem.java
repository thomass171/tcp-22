package de.yard.threed.maze;

import de.yard.threed.core.Point;

/**
 *
 */
public class SimpleGridItem implements GridItem {

    int owner = -1;
    Point location = null;
    boolean neededForSolving = false;
    private ItemComponent parent;
    // id to be used outside ECS.
    private int nonEcsId;
    private static int id = 1;

    /**
     * Constructor for non ECS usage (testing, dry run).
     */
    public SimpleGridItem(Point location) {
        this.location = location;
        this.nonEcsId = id++;
       // this.team = team;
    }

    /**
     * Constructor for ECS usage.
     */
    public SimpleGridItem(Point location, ItemComponent parent) {
        this.location = location;
        this.parent = parent;
        nonEcsId = -1;
        this.owner = -1;
    }

    public SimpleGridItem(int owner, ItemComponent parent) {
        this.location = null;
        this.parent = parent;
        nonEcsId = -1;
        this.owner = owner;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(Point point) {
        location = point;
    }

    @Override
    public int getOwner() {
        return owner;
    }

    @Override
    public void setOwner(int owner) {
        this.owner = owner;
    }

    @Override
    public void collectedBy(int collector) {
        owner = collector;
        location = null;
    }

    @Override
    public boolean isNeededForSolving() {
        return neededForSolving;
    }

    @Override
    public void setNeededForSolving() {
        neededForSolving = true;
    }

    @Override
    public int getId() {
        if (parent != null) {
            return parent.getId();
        } else {
            return nonEcsId;
        }
    }

    @Override
    public String toString() {
        return "SimpleGridItem(owner=" + owner + ",location=" + location + ")";
    }
}
