package de.yard.threed.engine.util;

import de.yard.threed.core.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Einen Vector kreis/bogenförmig um eine Achse rotieren.
 * Das ist eine 3D Verallgemeinerung diverser schon existierender Circle Rotationen.
 * <p>
 * Created on 12.11.18.
 */
public class CircleRotator {
    public static List<Vector3> buildArcByrotate(Vector3 v, Vector3 axis, int segments, float spanangle, boolean close) {
        List<Vector3> result = new ArrayList<Vector3>(segments + ((close) ? 1 : 0));
        for (int i = 0; i < segments + ((close) ? 1 : 0); i++) {
            result.add(v.rotateOnAxis(i * spanangle / segments, axis));
        }
        return result;
    }

    /**
     * Komplettkreis
     *
     * @param segments
     */
    public static List<Vector3> buildArcByrotate(Vector3 v, Vector3 axis, int segments, boolean close) {
        return buildArcByrotate(v, axis, segments, (float) (2 * Math.PI), close);
    }
}
