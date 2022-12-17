package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.engine.util.RandomIntProvider;

/**
 * 11.4.21: y isType 3D z-Koordinate with negative in north? Aber ist das wirklich so gedacht? Ich tausch N,S mal.
 * Jetzt werden hier "logische" Maze Coordinates verwendet mit North im Layout oben (positives y).
 * <p>
 * Wegen der Berechnungen ist es hilfreich, Directions auch als Point abzubilden.
 * Keine enum wegen CS.
 * <p>
 * Created by thomass on 19.01.16.
 */
public class Direction {
    public static Direction S/*N*/ = new Direction(0, -1);
    public static Direction N/*S*/ = new Direction(0, 1);
    public static Direction W = new Direction(-1, 0);
    public static Direction E = new Direction(1, 0);
    static Direction[] ORTHODIRECTIONS = new Direction[]{N, S, W, E};
    //public int xo;
    //public int yo;
    private Point point;

    static IntProvider rand = new RandomIntProvider();

    public Direction(int xo, int yo) {
        // check auf genau 1 Step
        if (Math.abs(xo) + Math.abs(yo) != 1) {
            throw new RuntimeException("invalid direction");
        }

        point = new Point(xo, yo);
    }

    /**
     * 9.8.21: Was ist das denn, ein sich selbt veraenderndens array??
     *
     * @return
     */
    public static Direction[] getRandomOrthoDirections() {
        shuffleArray(ORTHODIRECTIONS);
        return ORTHODIRECTIONS;
    }

    private static void shuffleArray(Direction[] ar) {

        for (int i = 0; i < ar.length; i++) {
            int index = rand.nextInt() % ar.length;
            Direction a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    /*12.4.21: directions cannot be added/subtracted wird aber gemacht :-( TODO better move?*/
    @Deprecated
    public static Direction subtract(Point p1, Point p2) {
        return new Direction(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    @Deprecated
    public static Point add(Point p, Direction dir) {
        return new Point(p.getX() + dir.getPoint().getX(), p.getY() + dir.getPoint().getY());
    }

    public static Direction of(Point from, Point to) {
        if (to.getX() == from.getX()) {
            if (to.getY() < from.getY()) {
                return Direction.S;
            }
            if (to.getY() > from.getY()) {
                return Direction.N;
            }
        }
        if (to.getY() == from.getY()) {
            if (to.getX() < from.getX()) {
                return Direction.W;
            }
            if (to.getX() > from.getX()) {
                return Direction.E;
            }
        }
        return null;
    }

    public Point move(Point p) {
        return new Point(p.getX() + point.getX(), p.getY() + point.getY());
    }

    public Point multiply(int steps) {
        return point.multiply(steps);
    }

    public Direction getReverted() {
        return new Direction(point.getX() * -1, point.getY() * -1);
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public String toString() {
        if (point.getX() != 0) {
            if (point.getY() != 0) {
                return "invalid";
            }
            if (point.getX() < 0) {
                return "West";
            } else {
                return "East";
            }
        } else {
            // x==0
            if (point.getY() == 0) {
                return "invalid";
            }
            if (point.getY() < 0) {
                return "South";
            } else {
                return "North";
            }
        }
    }
}
