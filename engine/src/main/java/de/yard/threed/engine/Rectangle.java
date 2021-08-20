package de.yard.threed.engine;

/**
 *
 * Einfach nur ein Rechteck um nicht das aus awt zu verwenden.
 * 20.11.15: Kann sowohl für Systeme mit y unten wie auch mit y oben verwendet werden.
 *
 * Date: 15.05.14
 */
public class Rectangle {
    public int x1,x2,y1,y2;

    public Rectangle(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }
}
