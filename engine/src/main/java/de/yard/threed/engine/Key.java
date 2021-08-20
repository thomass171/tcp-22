package de.yard.threed.engine;

/**
 * Ein KEy hat einen Keycode und einen Character (der ASCII Code)
 *
 * 22.08.2014: Als Konvention gilt jetzt, dass Tasten wie Shift und Control nicht als
 * eigenstaendige Tasten betrachtet werden. D.h. erst durch das Dr�cken einer "normalen"
 * Taste entsteht ein Key(Event).
 * NeeNeeNee, das kann man so nicht vereinfachen. Es ist ja auch wichtig, ob z.B. bei einem
 * Mouseclick eine Shift oder sonstige Taste gedrueckt ist.
 * Also ist diese Klasse im wahrsten Wortsinne die Repraesentation einer Taste auf dem Keyboard.
 *
 *
 *
 * Date: 30.05.14
 */
public class Key {
    public char character;
    //TODO: Keycode genauer definieren
    public int keycode;  // der virtuelle Keycode, z.B. CURUP (nicht im Numpad) ist Dezimalwert 28 (In Java), 38 in Javascript, 0xC8 bei LWJGL

    public Key(int keycode, char character) {
        this.keycode = keycode;
        this.character = character;

    }

    @Override
    public String toString() {
        return "keycode="+keycode+",character="+character;
    }
}
