package de.yard.threed.core.geometry;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.MathUtil2;

import java.util.ArrayList;
import java.util.List;

/**
 * Im Prinzip einfach ein Polygonzug, offen oder geschlossen.
 * 
 * 25.05.2015: Siehe Kommentar ShapeGeometry und ShapeFactory.
 * 28.11.16: Ein Shape sollte CW sein, weil die ShapeFactory davon ausgeht.
 * 28.03.2017: Vielleicht asbeune und dann Polygon nennen?
 * Date: 25.04.14
 */
public class Shape {
    List<Vector2> points = new ArrayList<Vector2>();
    Log logger = Platform.getInstance().getLog(Shape.class);
    private boolean closed = false;
    private List<Integer> edges = new ArrayList<Integer>();
    //  4.7.18: MIN_VALUE ist positiv!
    private double maxX = -java.lang.Double.MAX_VALUE;
    private double minY = java.lang.Double.MAX_VALUE;
    private double maxY = -java.lang.Double.MAX_VALUE;
    private double minX = java.lang.Double.MAX_VALUE;

    public Shape() {

    }

    public Shape(boolean closed) {
        this.closed = closed;
    }

    public void addPoint(Vector2 p, boolean isedge) {
        points.add(p);
        if (isedge)
            edges.add(points.size() - 1);
        if (p.getX() < minX)
            minX = p.getX();
        if (p.getY() < minY)
            minY = p.getY();
        if (p.getX() > maxX)
            maxX = p.getX();
        if (p.getY() > maxY)
            maxY = p.getY();
    }

    public void addPoint(Vector2 p) {
        addPoint(p, false);
    }

    public void addPoint(double x, double y) {
        addPoint(new Vector2(x, y));
    }

    public void addPoint(double x, double y, boolean isedge) {
        addPoint(new Vector2(x, y), isedge);
    }

    /**
     * Einen (Kreis)bogen hinzuf�gen mit Mittelpunkt in center, was nicht zum Shape gehoert
     * Begonnen wird beim letzten Punkt.
     * Ein Zielpunkt muss angegeben werden, denn sonst kann die Bestimmung des Winkels kompliziert sein.
     * TODO: Bestimmen ob rechts oder links. Auslagern in Util Methode anglebetween
     * 02.04.2015: Das mit links/rechts kann nicht ermittelt werden. Das muss uebergeben werden.
     */
    public void addArc(Vector2 center, int segments, Vector2 destination, boolean ccw) {
        Vector2 lastpoint = points.get(points.size() - 1);
        Degree anglespan; // Der Winkel, wie lang der Bogen ist.
        anglespan = Vector2.angle(center, lastpoint, destination, ccw);
        logger.debug("anglespan=" + anglespan.getDegree());
        Degree anglestep = new Degree(anglespan.getDegree() / segments);
        addArc(center, anglestep, segments);
    }

    /**
     * Einen (Kreis)bogen hinzuf�gen mit Mittelpunkt in center, was nicht zum Shape gehoert.
     * Es werden <segments> Segmente a <anglestep> Grad geadded.
     * Begonnen wird beim letzten Punkt.
     */
    public void addArc(Vector2 center, Degree anglestep, int segments) {
        addArc(center, anglestep, segments, 1, 1);
    }

    public void addArc(Vector2 center, Degree anglestep, int segments, float xscale, float yscale) {
        if (points.size() == 0)
            throw new RuntimeException("no point"); //TODO anders?
        Vector2 lastpoint = points.get(points.size() - 1);
        double x, y;
        double radius = center.distance(lastpoint);
        // Grundorientierung
        Degree basis = center.angle(lastpoint);
        //System.out.println("base=" + base.degree);
        Degree angle = anglestep.add(basis);
        for (int s = 0; s < segments; s++) {
            x = (center.x /*- lastpoint.x*/) + (float) (radius * xscale * Math.cos(MathUtil2.toRadians(angle.getDegree())));
            y = (center.y /*- lastpoint.y*/) + (float) (radius * yscale * Math.sin(MathUtil2.toRadians(angle.getDegree())));
            logger.debug(Util.format("addArc: x=%f, y=%f", new Object[]{x, y}));
            addPoint(new Vector2(x, y));
            angle = angle.add(anglestep);
        }
    }

    public List<Vector2> getPoints() {
        return points;
    }

    public boolean isClosed() {
        return closed;
    }


    public boolean isEdge(int index) {
        for (int edge : edges)
            if (edge == index)
                return true;
        return false;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    /**
     * add ist eigentlich falsch, besser setStartline oder sowas.
     *
     * @param width
     * @param yoffset
     */
    public void addTopLine(double width, double yoffset) {
        addPoint(new Vector2(-width / 2f, yoffset));
        addPoint(new Vector2(width / 2f, yoffset));
    }

    /**
     * hier stimmt das add
     *
     * @param width
     */
    public void addBottomLine(double width) {
        Vector2 lastpoint = points.get(points.size() - 1);

        addPoint(new Vector2(lastpoint.x - width, lastpoint.y));
    }

    /**
     * Einen Bogen bis herunter auf die gegenueberliegende negative y Position bilden.
     * Degree ist der "Anbindungswinkel", 0 fuer eine glatte (differenziebare) Anbindung.
     *
     * @param degree
     */
    public void addSemiSymetricArc(Degree degree) {
        Vector2 lastpoint = points.get(points.size() - 1);

        //Ob ein Winkel >= 90 Grad Sinn macht, sei mal dahingestellt. TODO Eher verhindern, wegen tan Ueberlauf
        //<0 ist auch fraglich
        Vector2 centerofarc = new Vector2(lastpoint.x - (double) Math.tan(degree.toRad()), 0);
        Vector2 destinationpointofarc = new Vector2(lastpoint.x, -lastpoint.y);
        // Bogen soll im Uhrzeigersinn gehen
        addArc(centerofarc, 6, destinationpointofarc,false);

    }

    public Shape mirror() {
        Shape shape = new Shape();
        for (int i = 0; i < points.size(); i++) {
            shape.addPoint(-points.get(i).getX(), points.get(i).getY());
        }
        return shape;
    }

    /**
     * 28.11.16: Ware ja auch was fuer ne MAtrix
     * 18.4.19: Rotiert mathematisch (CCW)
     * @param angleRad
     * @return
     */
    public Shape rotate(double angleRad) {
        Shape shape = new Shape();
        shape.closed= closed;
        for (int i = 0; i < points.size(); i++) {
            double x = points.get(i).getX();
            double y = points.get(i).getY();
            double nx = (double) (x * Math.cos(angleRad) - y * Math.sin(angleRad));
            double ny = (double) (y * Math.cos(angleRad) + x * Math.sin(angleRad));
            shape.addPoint(nx, ny);
        }
        return shape;
    }

    /**
     * * 28.11.16: Ware ja auch was fuer ne MAtrix
     * @param 
     * @return
     */
    public Shape translateX(double offset) {
        return translate(new Vector2(offset,0));
    }

    public Shape translate(Vector2 offset) {
        Shape shape = new Shape(isClosed());
        for (int i = 0; i < points.size(); i++) {
            double x = points.get(i).getX();
            double y = points.get(i).getY();
            double nx = x+offset.getX();
            double ny = y+offset.getY();
            shape.addPoint(nx, ny, isEdge(i));
        }
        return shape;
    }

    /**
     * Reihenfolge der Points inverten.
     * @return
     */
    public Shape revert() {
        Shape shape = new Shape();
        for (int i = points.size() - 1; i >= 0; i--) {
            shape.addPoint(points.get(i).getX(), points.get(i).getY());
        }
        return shape;
    }
}
