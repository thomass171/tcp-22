package de.yard.threed.engine.gui;

import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.Color;
import de.yard.threed.core.geometry.SimpleGeometry;

/**
 * A green/red indicator light that can be switched on/off
 */
public class Indicator extends SceneNode {

    private Material offMazerial, onMaterial;
    private boolean isOn = false;

    private Indicator(double radius, Color offColor, Color onColor) {
        offMazerial = Material.buildBasicMaterial(offColor);
        onMaterial = Material.buildBasicMaterial(onColor);

        SimpleGeometry geo = Primitives.buildSphereGeometry(radius, 32, 32);

        setMesh(new Mesh(geo, offMazerial));
    }

    public void toggle() {
        if (isOn) {
            getMesh().setMaterial(offMazerial);
        } else {
            getMesh().setMaterial(onMaterial);
        }
        isOn = !isOn;
    }

    public static Indicator buildGreen(double radius) {
        return new Indicator(radius, Color.DARKGREEN, Color.GREEN);
    }

    public static Indicator buildRed(double radius) {
        return new Indicator(radius, Color.DARKRED, Color.RED);
    }
}
