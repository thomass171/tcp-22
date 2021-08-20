package de.yard.threed.maze;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;

public class ItemFilter implements EntityFilter {

    int owner = -1;

    /**
     *
     */
    public ItemFilter() {

    }

    private ItemFilter(int owner) {
        this.owner = owner;
    }

    @Override
    public boolean matches(EcsEntity e) {
        ItemComponent itemComponent = ItemComponent.getItemComponent(e);
        if (itemComponent == null) {
            return false;
        }
        if (owner != -1 && owner != itemComponent.getOwner()) {
            return false;
        }
        return true;
    }

    public static ItemFilter byOwner(int owner) {
        return new ItemFilter(owner);
    }
}
