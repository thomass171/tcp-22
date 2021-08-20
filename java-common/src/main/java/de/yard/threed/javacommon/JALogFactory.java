package de.yard.threed.javacommon;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeLogFactory;

/**
 * Created by thomass on 21.03.20.
 */
public class JALogFactory implements NativeLogFactory {
    @Override
    public Log getLog(Class clazz) {
        return new JALog(clazz);
    }
}
