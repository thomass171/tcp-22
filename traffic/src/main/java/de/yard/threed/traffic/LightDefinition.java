package de.yard.threed.traffic;

import de.yard.threed.core.Color;
import de.yard.threed.core.Vector3;

public class LightDefinition {

    /**
     * Position from where it is shining to (0,0,0)
     */
    public Vector3 position;

    public Color color;


    public LightDefinition(Color color, Vector3 position) {
        this.color = color;
        this.position = position;

    }
}
