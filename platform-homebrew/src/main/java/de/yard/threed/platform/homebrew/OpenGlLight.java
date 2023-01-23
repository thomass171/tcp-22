package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeLight;

import de.yard.threed.core.Color;

/**
 * Fuer Point, ambient und directional.
 * <p/>
 * Date: 04.07.14
 */
public class OpenGlLight implements NativeLight {
    private Vector3 position = new Vector3(0, 0, 0);
    private Vector3 direction = null;

    protected Color color;

    public OpenGlLight(Color col) {
        color = col;
    }

    public OpenGlLight(Color col, Vector3 direction) {
        color = col;
        this.direction = direction;
    }

    public Vector3 getPosition() {
        return position;
    }

    /*@Override
    public void setPosition(Vector3 pos) {
        position = pos;
    }*/

    public Color getColor() {
        return color;
    }

    public static NativeLight buildPointLight(Color col) {
        return new OpenGlLight(col);
    }

    public static NativeLight buildDirectionalLight(Color color, Vector3 direction) {
        return new OpenGlLight(color, direction);
    }

    public Vector3 getDirection() {
        return direction;
    }

    public static NativeLight buildAmbientLight(Color color) {
        return  new OpenGlLight(color);
    }
}
