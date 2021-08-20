package de.yard.threed.engine;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 01.03.14
 * <p/>
 * Die Tasten die gedr�ckt wurden (auch wenn wieder released) und gedrueckt gehalten wurden.
 * Nee, das muss schon unterschieden werden, sonst hat man bei schaltenden Keys immer einen
 * Prelleffekt.
 *
 * Siehe auch die Doku in {@link Key Key}
 */
public class ActiveKeys {
    // Die im letzten Zyklus gepressten
    List<Key> pressedkeys = new ArrayList<Key>();
    // Die immer noch gepressten. Ein Key kann nur in eine der beiden Listen vorkommen
    List<Key> stillpressedkeys = new ArrayList<Key>();
    Log logger = Platform.getInstance().getLog(ActiveKeys.class);

    public ActiveKeys() {

    }

    /*public void addKey(int key) {
        keys.add(key);
    } */

    /*28.6.21public List<Key> getPressedKeys() {
        return pressedkeys;
    }

    public List<Key> getStillpressedKeys() {
        return stillpressedkeys;
    }*/

    public boolean pressed(char k) {
        //logger.debug("pressedkeys="+getDebugString()+",k="+k);
        for (Key key : pressedkeys)
            if (k == key.character)
                return true;
        return false;
    }

    /*28.6.21public void setStillpressedKeys(List<Key> pk) {
        this.stillpressedkeys = pk;
    }

    public void setPressedKeys(List<Key> pk) {
        this.pressedkeys = pk;
    }

    public List<Key> getPressedAndStillpressed() {
        List<Key> l = new ArrayList<Key>();
        l.addAll(pressedkeys);
        l.addAll(stillpressedkeys);
        return l;
    }*/

    private String getDebugString() {
        String s_pressed = "";
        for (Key key : pressedkeys) {
            s_pressed += "" + key +",";
        }
        return s_pressed;
    }

    @Override
    public String toString() {
        return getDebugString();
    }

    /**
     * Das mit dem Shift ist doch 'ne Kruecke
     * @return
     */
    /*28.6.21 public boolean hasShift() {
        /*THREED TODO for (Key k : pressedkeys)
            if (k.keycode == Keyboard.KEY_LSHIFT || k.keycode == Keyboard.KEY_RSHIFT)
                return true;* /
        return false;
    }*/
}
