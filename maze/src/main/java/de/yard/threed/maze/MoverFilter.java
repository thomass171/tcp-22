package de.yard.threed.maze;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;

public class MoverFilter implements EntityFilter {

    private boolean forBoxes;

    /**
     * Currently, every mover not being a player is a box.
     *
     * @param forBoxes
     */
    public MoverFilter(boolean forBoxes) {
        this.forBoxes = forBoxes;
    }

    @Override
    public boolean matches(EcsEntity e) {
        MoverComponent moverComponent = MoverComponent.getMoverComponent(e);
        if (moverComponent == null) {
            return false;
        }
        if (moverComponent.isPlayer() && !forBoxes) {
            return true;
        }
        if (!moverComponent.isPlayer() && forBoxes) {
            return true;
        }
        return false;
    }


}
