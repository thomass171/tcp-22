package de.yard.threed.core;

/**
 * Universal class for 2D int coordinates
 * <p/>
 * Created by thomass on 19.06.15.
 */
public class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this(p.x, p.y);
    }

    /**
     * The orthogonal distance between two fields. That is the number of steps needed for walking from
     * one field to the other.
     */
    public static int getDistance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "x=" + x + ",y=" + y;
    }

    /**
     * If two objects are equal according to the equals(Object) method, then calling the hashCode
     * method on each of the two objects must produce the same integer result.
     *
     * @return
     */
    @Override
    public boolean equals(Object po) {
        // Should throw exception on wrong usage
        Point p = (Point) po;
        return this.x == p.x && this.y == p.y;
    }

    /**
     * If two objects are equal according to the equals(Object) method, then calling the hashCode
     * method on each of the two objects must produce the same integer result.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return x * 1000000000 + y;
    }

    public Point add(Point p) {
        return new Point(x + p.x, y + p.y);
    }

    public Point add(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public Point subtract(Point p) {
        return new Point(x - p.x, y - p.y);
    }

    public Point multiply(int steps) {
        return new Point(x * steps, y * steps);
    }


    public Point negate() {
        return new Point(-x, -y);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Annotation @Override not possible? Breaks GWT?
     */
    public Point clone() {
        return new Point(x, y);
    }

    /**
     * 31.5.21: als Standard: translate ist selbstaendernd.
     *
     * @param t
     */
    public void translateX(int t) {
        x += t;
    }

    public void translateY(int t) {
        y += t;
    }

    public Point addX(int t) {
        return new Point(x + t, y);
    }

    public Point addY(int t) {
        return new Point(x, y + t);
    }

    public boolean onSameAxis(Point p) {
        return (p.getX() == x || p.getY() == y);
    }

    /**
     * Inclusive!
     */
    public boolean insideXRange(int lowerX, int upperX) {
        return x >= lowerX && x <= upperX;
    }

    /**
     * Inclusive!
     */
    public boolean insideYRange(int lowerY, int upperY) {
        return y >= lowerY && y <= upperY;
    }

    public void visitNeighbor(PointVisitor visitor) {
        for (int i = -1; i < 1; i++) {
            visitor.visit(add(i, -1));
            visitor.visit(add(i, 1));
        }
        visitor.visit(add(-1, 0));
        visitor.visit(add(1, 0));
    }
}
