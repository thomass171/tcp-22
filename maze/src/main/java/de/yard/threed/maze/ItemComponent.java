package de.yard.threed.maze;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * super class for all items.
 * Created by thomass on 08.04.21.
 */
public abstract class ItemComponent extends EcsComponent implements GridItem {
    Log logger = Platform.getInstance().getLog(ItemComponent.class);

    private GridItem gridItem;

    public ItemComponent() {
        gridItem = new SimpleGridItem();
    }

    public ItemComponent(int owner) {
        gridItem = new SimpleGridItem(owner);
    }

    @Override
    public Point getLocation() {
        return gridItem.getLocation();
    }

    @Override
    public void setLocation(Point point) {
        gridItem.setLocation(point);
    }

    @Override
    public int getOwner() {
        return gridItem.getOwner();
    }

    public void setOwner(int owner) {
        gridItem.setOwner(owner);
    }

    @Override
    public void collectedBy(int collector) {
        gridItem.collectedBy(collector);
    }

    public GridItem getGridItem() {
        return gridItem;
    }
}
