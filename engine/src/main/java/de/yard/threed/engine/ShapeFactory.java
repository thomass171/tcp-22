package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.geometry.Shape;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * 30.11.16: Die Punkte des Shape muessen CW sein, wenn sie in ShapeFactory benutzt werden. Sonst passt das Culling nicht.
 * Den Versuch einer Neuorientierung CCW abgebrochen. Zu viel Änderungen (auch an Tests); und letztlich bringt es nichts
 * substantielles. Das mit dem Arc bleibt dann eben etwas gewöhnungsbedürftig.
 * 
 * Date: 18.02.2015
 */
public class ShapeFactory {
    static Log logger = Platform.getInstance().getLog(ShapeFactory.class);

    /**
     * Einen Kreisbogen erstellen. CCW vom startangle arcangle weit.
     * <p/>
     * Der Mittelpunkt liegt in (0,0)
     * 6.4.15: Begonnen wird bei startangle. 0 Grad zeigt nach rechts.
     * <p/>
     * Ein positiver arcangle fuehrt zu CCW, wie im Einheitskreis.
     * Der Shape ist nicht geschlossen. Das ist zumindest bei Zylinderbildung auch gut so, denn an der Naht gibt es verschiedene UVs.
     *
     * @param radius
     * @return
     */
    public static Shape buildArc(Degree startangle, Degree arcangle, double radius, int segments) {
        double x, y;
        Degree angle = startangle;
        Degree anglestep = new Degree(arcangle.getDegree() / segments);

        Shape shape = new Shape();
        for (int s = 0; s <= segments; s++) {
            x =  (radius * Math.cos(angle.toRad()));
            y = (radius * Math.sin(angle.toRad()));
            //logger.debug(Util.format("x=%f, y=%f", new Object[]{x, y}));
            shape.addPoint(new Vector2(x, y));
            angle = angle.add(anglestep);
        }
        return shape;
    }


    /**
     * Punktfolge im Rectangle 
     * 1-2
     * | |
     * 0-3
     * Center in 0,0
     */
    public static Shape buildRectangle(double width, double height) {
        Shape shape = new Shape(true);
        shape.addPoint(new Vector2(-width / 2, -height / 2));
        shape.addPoint(new Vector2(-width / 2, +height / 2), true);
        shape.addPoint(new Vector2(+width / 2, +height / 2), true);
        shape.addPoint(new Vector2(+width / 2, -height / 2), true);
        return shape;
    }

    /**
     * Hier sind die Ecken abgerundet. D.h., es gibt hier keine Eckpunkte.
     * Je nach Staerke der Abrundung kann dies zu einem Kreis fuehren.
     * <p/>
     * Center in 0,0
     */
    public static Shape buildRoundedRectangle(double width, double height, double radius) {
        int arcsegments = 10;
        double w2 = width / 2;
        double h2 = height / 2;
        if (radius > h2){
            // 5.12.16: Das ist ja nun nicht darstellbar
            logger.warn("invalid radius");
            radius = h2;
        }
        Shape shape = new Shape(true);
        //unten links anfangen und dann CW rum
        shape.addPoint(new Vector2(-w2 + radius, -h2));
        shape.addArc(new Vector2(-w2 + radius, -h2 + radius), new Degree(-9), arcsegments);
        //5.12.16shape.addPoint(new Vector2(-w2, -h2 + radius));
        shape.addPoint(new Vector2(-w2, h2 - radius));
        shape.addArc(new Vector2(-w2 + radius, h2 - radius), new Degree(-9), arcsegments);
        shape.addPoint(new Vector2(w2 - radius, h2));
        shape.addArc(new Vector2(w2 - radius, h2 - radius), new Degree(-9), arcsegments);
        //5.12.16 shape.addPoint(new Vector2(w2, h2 - radius));
        shape.addPoint(new Vector2(w2, -h2 + radius));
        shape.addArc(new Vector2(w2 - radius, -h2 + radius), new Degree(-9), arcsegments);
        return shape;
    }

    /**
     * von links nach rechts ueber die x-Achse.
     * Center in 0,0
     */
    public static Shape buildLine(double width, int segments) {
        Shape shape = new Shape();
        double segsize = width / segments;
        for (int s = 0; s <= segments; s++)
            shape.addPoint(new Vector2(-width / 2 + s * segsize, 0));
        return shape;
    }

    public static Shape buildU(double width, double height, double arccenteroffset) {
        Shape shape = new Shape(true);
        shape.addPoint(new Vector2(-width / 2, -height / 2));
        shape.addPoint(new Vector2(-width / 2, height / 2));
        // Bogen im Uhrzeigersinn
        shape.addArc(new Vector2(0, height / 2), 8, new Vector2(width / 2, (height / 2) - arccenteroffset), false);
        shape.addPoint(new Vector2(width / 2, -height / 2));
        return shape;
    }


}
