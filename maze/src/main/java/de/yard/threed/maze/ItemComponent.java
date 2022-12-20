package de.yard.threed.maze;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;

/**
 * super class for all items.
 * An item might be in the inventory of a player (collected), where it should not be visible at all. It is
 * hidden by scaling the size to 0.
 *
 * Created by thomass on 08.04.21.
 */
public abstract class ItemComponent extends EcsComponent implements GridItem {
    Log logger = Platform.getInstance().getLog(ItemComponent.class);

    private GridItem gridItem;
    // only set when hidden
    private Vector3 savedScale = null;

    public ItemComponent(int owner) {
        gridItem = new SimpleGridItem(owner, this);
    }

    public ItemComponent(Point location) {
        gridItem = new SimpleGridItem(location, this);
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

    /**
     * collected items should not be visible.
     */
    @Override
    public void collectedBy(int collector) {
        gridItem.collectedBy(collector);
        if (collector == -1) {
            unhide();
        } else {
            hide();
        }
    }

    public GridItem getGridItem() {
        return gridItem;
    }

    @Override
    public boolean isNeededForSolving() {
        return gridItem.isNeededForSolving();
    }

    @Override
    public void setNeededForSolving() {
        gridItem.setNeededForSolving();
    }

    @Override
    public int getId() {
        return getEntityId();
    }

    public boolean isHidden() {
        return savedScale != null;
    }

    /**
     * make it invisble.
     */
    private void hide() {
        SceneNode n = EcsHelper.findEntityById(getEntityId()).getSceneNode();
        savedScale = n.getTransform().getScale();
        n.getTransform().setScale(new Vector3());
    }

    /**
     * make it visble again.
     */
    private void unhide() {
        if (savedScale != null) {
            SceneNode n = EcsHelper.findEntityById(getEntityId()).getSceneNode();
            n.getTransform().setScale(savedScale);
            savedScale = null;
        }
    }
}
