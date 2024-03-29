package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.ecs.EcsEntity;

public class HomeRelocationStrategy implements RelocationStrategy {
    @Override
    public Point getLocation(MazeLayout layout, EcsEntity player) {
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        return layout.getTeamByIndex(mc.getTeam()).positions.get(0).getPoint();
    }
}
