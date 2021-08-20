package de.yard.threed.engine;

import de.yard.threed.core.Color;

/**
 * Date: 29.05.14
 */
public interface ColorProvider {
    /**
     * index ist z.B. die Segmentnummer in einer ShapedGeometry
     * @param index
     * @return
     */
    Color getColor(int index);
}
