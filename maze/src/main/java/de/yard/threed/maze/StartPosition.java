package de.yard.threed.maze;

import de.yard.threed.core.Point;

public class StartPosition {
    public Point p;
    public boolean isMonster;

    public StartPosition(Point p, boolean isMonster) {
        this.p = p;
        this.isMonster = isMonster;
    }

    public Point getPoint(){
        return p;
    }
}
