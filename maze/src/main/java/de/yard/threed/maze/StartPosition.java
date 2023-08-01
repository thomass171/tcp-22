package de.yard.threed.maze;

import de.yard.threed.core.Point;

public class StartPosition {
    public Point p;
    //public boolean isMonster;
    public GridOrientation initialOrientation;

    public StartPosition(Point p/*, boolean isMonster*/) {
        this.p = p;
        //this.isMonster = isMonster;
    }

    public StartPosition(int x, int y, GridOrientation initialOrientation) {
        this.p = new Point(x,y);
        this.initialOrientation = initialOrientation;
    }

    public StartPosition(Point p, GridOrientation initialOrientation) {
        this.p = p;
        this.initialOrientation = initialOrientation;
    }

    public Point getPoint(){
        return p;
    }

    public void setInitialOrientation(GridOrientation initialOrientation) {
        this.initialOrientation=initialOrientation;
    }
}
