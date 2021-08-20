package de.yard.threed.engine.graph;

import de.yard.threed.core.Quaternion;

/**
 * 22.2.2020: Abstraktion für die 3D Rotation an einer GraphPosition. Die ist für Vehicle anders als für Planeten.
 * Und hat nur bedingt mit dem Graph(Orientation) zu tun.
 * 23.6.20 Nicht als @FunctionalInterface, weil C# keine Delegates ableiten kann.
 */

public interface RotationProvider {
    Quaternion get3DRotation();
}
