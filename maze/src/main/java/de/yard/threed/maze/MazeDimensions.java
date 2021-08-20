package de.yard.threed.maze;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.Point;

/**
 * Created by thomass on 15.05.15.
 */
public class MazeDimensions {
    // Die Groesse in 3D Darstellung
    public static final float GRIDSEGMENTSIZE = 1.5f;
    public static final int GRIDSX = 10;
    public static final int GRIDSY = 20;

    /**
     * Liefert die OpenGL World Koordinaten in der y0 Ebene des Zentrums des xy GridFeldes.
     * Das Feld 0,0 liegt genau auf 0,0,0. Das ist jetzt einfach mal Konvention, statt die
     * linke Ecke des Feldes auf 0,0,0 zu legen.
     *
     * @param x
     * @param y
     * @return
     */
    public static Vector3 getWorldElementCoordinates(int x, int y) {
        float gsz = GRIDSEGMENTSIZE;
        float gsz2 = gsz / 2;
        //float width2 = WIDTH / 2;
        return new Vector3(x * gsz, 0, -y * gsz);
    }


    /**
     * Liefert die xy Position im Grid aus den 3D Koordinaten. Hierfuer
     * wird gerundet, um das Feld zu identifizieren.
     * 3.3.17: Das ist irgendwie nicht schoen, aus 3D auf logische Daten zurückzurechnen.
     * 26.4.21: Aber fuer sowas wie Bullets doch sehr hilfreich.
     */
    /*3.3.17 @Deprecated*/
    public static Point getCoordinatesOfElement(Vector3 position) {
        double x = position.getX() / GRIDSEGMENTSIZE;
        double y = -position.getZ() / GRIDSEGMENTSIZE;
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    /**
     * Das Center des ganzen Grids.
     */
    public static Vector3 getCenterCoordinates(int width, int height) {
        float gsz = GRIDSEGMENTSIZE;
        float gsz2 = gsz / 2;
        //float width2 = WIDTH / 2;
        //TODO Die Berechnung ist noch nicht ganz passend
        return new Vector3((width - 1) * gsz / 2, 0, -(height - 1) * gsz / 2);
    }

    /**
     * Der Offset von der Mitte eines Elements nach oben/hinten (negative z-Achse).
     * Das ist dann addiert zum Center des Feldes die Centerkoordinate des TopPillar.
     *
     * @return
     */
    public static Vector3 getTopOffset() {
        return new Vector3(0, 0, -GRIDSEGMENTSIZE / 2);
    }

    /**
     * Der Offset von der Mitte eines Elements nach rechts.
     * Das ist dann addiert zum Center des Feldes die Centerkoordinate des RightPillar.
     *
     * @return
     */
    public static Vector3 getRightOffset() {
        return new Vector3(GRIDSEGMENTSIZE / 2, 0, 0);
    }


}
