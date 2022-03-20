package de.yard.threed.maze;

import de.yard.threed.core.Point;

public class SimpleGridItem implements GridItem {

    int owner = -1;
    Point location = null;

    public SimpleGridItem() {

    }

    public SimpleGridItem(int owner) {
        this.owner = owner;
    }

    public SimpleGridItem(Point initialLocation) {
        this.location = initialLocation;
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
}
