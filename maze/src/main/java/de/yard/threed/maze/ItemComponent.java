package de.yard.threed.maze;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * Created by thomass on 08.04.21.
 */
public class ItemComponent extends EcsComponent implements GridItem {
    Log logger = Platform.getInstance().getLog(ItemComponent.class);
    public static boolean debugmovement = false;
    static String TAG = "ItemComponent";
    private GridItem gridItem;

    public ItemComponent() {
        gridItem = new SimpleGridItem();
    }

    public ItemComponent(int owner) {
        gridItem = new SimpleGridItem(owner);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static ItemComponent getItemComponent(EcsEntity e) {
        ItemComponent m = (ItemComponent) e.getComponent(ItemComponent.TAG);
        return m;
    }

    @Override
    public Point getLocation() {
        return gridItem.getLocation();
    }

    @Override
    public void setLocation(Point point) {

    }

    @Override
    public int getOwner() {
        return gridItem.getOwner();
    }

    @Override
    public void collectedBy(int collector) {
        gridItem.collectedBy(collector);
    }

    public GridItem getGridItem() {
        return gridItem;
    }
}
