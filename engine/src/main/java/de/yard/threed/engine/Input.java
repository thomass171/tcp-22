package de.yard.threed.engine;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

/**
 * Created by thomass on 19.01.16.
 * <p/>
 * Inspired by Unity. And provides option for adding convenience methods.
 * static is good.
 * <p>
 * Die Unity Methode getAxis() mit -1..1 Wertebreich ignoriere ich erstmal, bis der Nutzen geklärt ist.
 */
public class Input {
    static Platform platform = Platform.getInstance();

    public static boolean getKeyDown(int keycode) {
        return platform.getKeyDown(keycode);
    }

    public static boolean getKeyUp(int keycode) {
        return platform.getKeyUp(keycode);
    }

    /**
     * Kann solange abgefragt werden, wie die Taste gedrückt ist.
     *
     * @return
     */
    public static boolean getKey(int keycode) {
        return platform.getKey(keycode);
    }

    public static Point getMouseMove() {
        return platform.getMouseMove();
    }

    public static Point getMouseClick() {
        return platform.getMouseClick();
    }

    public static Point getMousePress() {
        return platform.getMousePress();
    }

    /**
     * Range -1 bis 1 ausser bei Mousedelta
     *
     * @return
     */
    public float GetAxis(String axis) {
        if (axis.equals("Mouse Y")) {
            // sr.getMouseDelta();
        }
        return 0;
    }

    /*MA36public static boolean getControllerButtonDown(int button) {
        return platform.getControllerButtonDown(button);
    }*/

    /*MA36public static boolean getButtonUp(int button) {
        return platform.getButtonUp(button);
    }*/

    /**
     * Liefert true wenn eine Controller(nicht Maus!) Taste pressed/released wird.
     * down statt press, um evtl. auch Drag verwenden zu können.
     * Kann nur in einem einzigen Frame (update()) abgefragt werden.
     * Die Buttonmnumber sind hier jetzt einfach mal als Konvention festgelegt:
     * 0 -9 linker Controller, 10-19 rechter.
     * 0,10 Trigger (Oculus primary trigger), Fire
     *
     * @return
     */
    public static boolean getControllerButtonDown(int button) {
        return AbstractSceneRunner.getInstance().isButtonDown(button);
    }

    public static boolean getButtonUp(int button) {
        return AbstractSceneRunner.getInstance().isButtonUp(button);
    }

    /**
     * Liefert das Segemnt in einem 3x3 Grid, in das geclickt wurde, beginnend links unten mit 0:
     * 678
     * 345
     * 012
     * Es mag leichte Unschaerfen an den Kanten geben, aber das duerfte egal sein.
     * <p>
     * 14.5.19: Anzahl Segmente nicht immer nur 3.
     * <p>
     * Returns -1 if no segment was hit.
     *
     * @return
     */
    public static int getClickSegment(Point mouselocation, Dimension dimension, int segments) {
        if (segments != 3 && segments != 5) {
            return -1;
        }
        int x = mouselocation.getX();
        int y = mouselocation.getY();
        //x-=dimension.width/2;
        //y-=dimension.height/2;
        int seg = y / (dimension.height / segments) * segments + x / (dimension.width / segments);
        /*14,5,19if (seg > 8) {
            seg = 8;
        }*/
        return seg;
    }
}
