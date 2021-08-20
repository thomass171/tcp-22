package de.yard.threed.maze;

import de.yard.threed.core.Degree;

/**
 * 10.4.21: Teilweise Redundanz zu Direction merged.
 *
 * Created by thomass on 14.02.17.
 */
public class GridOrientation {
    //12.4.21 private static Point[] dirs = {new Point(0, 1), new Point(1, 0), new Point( 0,-1), new Point(-1, 0)};
    private static Direction[] dirs = {Direction.N, Direction.E, Direction.S, Direction.W};
    // yaw ist anders als heading CCW
    private static Degree[] yaw = {new Degree(0), new Degree(-90), new Degree(180), new Degree(90)};
    // Defaultausrichtung North
    private int dir = 0;
    //private static Direction[] leftdirs = {new Point(-1, 0), new Point(0, 1), new Point( 1,0), new Point(0, -1)};
    //private static Direction[] rightdirs = {new Point(1, 0), new Point(0, -1), new Point( -1,0), new Point(0, 1)};
    private static Direction[] leftdirs = {Direction.W, Direction.N, Direction.E, Direction.S};
    private static Direction[] rightdirs = {Direction.E, Direction.S, Direction.W, Direction.N};

    /**
     * Defaultausrichtung (North)
     */
    public GridOrientation() {
    }

    private GridOrientation(int dir) {
        this.dir = dir;
    }

    GridOrientation rotate(boolean left) {
        if (left) {
            if (dir > 0) {
                return new GridOrientation(dir - 1);
            } else {
                return new GridOrientation(dirs.length - 1);
            }
        }else{
            if (dir < dirs.length-1) {
                return new GridOrientation(dir + 1);
            } else {
                return new GridOrientation(0);
            }
        }
    }

    public Direction getOrientation() {
        return dirs[dir];
    }

    public Direction getDirectionForMovement(GridMovement movement) {

        // Forward als Default nehmen. Gilt auch fur ForwardMove.
        Direction p = getOrientation();

        switch (movement.movement){
            case GridMovement.BACK:
                p = p.getReverted();
                break;
            case GridMovement.LEFT:
                p = leftdirs[dir];
                break;
            case GridMovement.RIGHT:
                p = rightdirs[dir];
                break;
        }
        return p;
    }
    
    public Degree getYaw() {
        return yaw[dir];
    }

    public Direction getRevertedOrientation() {
        Direction p = getOrientation();
        return p.getReverted();
    }

    @Override
    public String toString(){
        return "dir="+dir;
    }

    public static GridOrientation fromDirection(char dir){
        switch (dir){
            case 'N':
                return new GridOrientation(0);
            case 'E':
                return new GridOrientation(1);
            case 'S':
                return new GridOrientation(2);
            case 'W':
                return new GridOrientation(3);
        }
        return null;
    }
}
