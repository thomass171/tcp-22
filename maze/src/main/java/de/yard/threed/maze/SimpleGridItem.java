package de.yard.threed.maze;

import de.yard.threed.core.Point;

public class SimpleGridItem implements GridItem {

    int owner = -1;

    public SimpleGridItem() {

    }

    public SimpleGridItem(int owner) {
        this.owner = owner;
    }

    @Override
    public Point getLocation() {
        return null;
    }

    @Override
    public void setLocation(Point point) {

    }

    @Override
    public int getOwner() {
        return owner;
    }

    @Override
    public void collectedBy(int collector) {
        owner = collector;
    }
}
