package de.yard.threed.tools;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * Base unit isType meter.
 */
public class DecorationFactory {
    /**
     * Ein (Abbiege)Pfeil.
     * Per default Richtung +y.
     * In CCW, obwohl das egal sein könnte.
     * positiver angle dreht nach links
     */
    public static Decoration buildRoadArrow(double len, double basewidth, double angle, double shapescale) {
        GeneralPath p = new GeneralPath();
        len *= shapescale;
        basewidth *= shapescale;

        double len2 = len / 2, len4 = len / 4;
        double basewidth2 = basewidth / 2;

        p.moveTo(-basewidth2, -len2);
        p.lineTo(basewidth2, -len2);
        p.lineTo(basewidth2, len4);
        p.lineTo(basewidth2 + len4, len4);
        p.lineTo(0, len2);
        p.lineTo(-basewidth2 - len4, len4);
        p.lineTo(-basewidth2, len4);
        p.closePath();
        //shape = shape.rotate(angle);
        // return buildPolygonFromShape(shape);
        Decoration decoration = new Decoration(Color.white);
        decoration.add(p);
        return decoration;

    }

    /**
     * Eine Fahrbahmlinie quasi entlang y.
     */
    public static Decoration buildRoadMarker(double len, double basewidth, double yoffset, Double dashlen, double shapescale) {
        len *= shapescale;
        basewidth *= shapescale;
        yoffset *= shapescale;

        Decoration decoration = new Decoration(Color.white);

        double len2 = len / 2;
        double basewidth2 = basewidth / 2;
        if (dashlen == null) {
            decoration.add(buildRectangle(-len2, -basewidth2 + yoffset, len2, basewidth2 + yoffset));
        } else {
            dashlen *= shapescale;
            double xpos = -len2 + dashlen / 2;
            //avoid reaching beyond total len
            while (xpos + dashlen < len2) {
                decoration.add(buildRectangle(xpos, -basewidth2 + yoffset, xpos + dashlen, basewidth2 + yoffset));
                xpos += 2 * dashlen;
            }
        }

        return decoration;
    }

    public static Decoration buildRoadMarker(double len, double basewidth, double yoffset, double shapescale) {
        return buildRoadMarker(len, basewidth, yoffset, null, shapescale);
    }

    /**
     * @return
     */
    public static Decoration buildParkPos( double shapescale) {
        double baselen = 3.0 * shapescale;
        double width = 1.0 * shapescale;
        double linewidth = 0.15 * shapescale;

        Decoration decoration = new Decoration(Color.yellow);

        double baselen2 = baselen / 2;
        double width2 = width / 2;
        double linewidth2 = linewidth / 2;
        decoration.add(buildRectangle(-linewidth2, -baselen2, linewidth2, baselen2));
        decoration.add(buildRectangle(-width2, -linewidth2, width2, linewidth2));
        //label above line
        decoration.addText(new Decoration.Text(linewidth,-linewidth,"737"));
        return decoration;
    }

    private static GeneralPath buildRectangle(double xl, double yu, double xr, double yl) {
        GeneralPath p = new GeneralPath();
        p.moveTo(xl, yu);
        p.lineTo(xl, yl);
        p.lineTo(xr, yl);
        p.lineTo(xr, yu);
        p.closePath();
        return p;
    }
}


