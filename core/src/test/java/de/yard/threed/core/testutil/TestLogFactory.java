package de.yard.threed.core.testutil;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeLogFactory;

/**
 * Created by thomass on 21.03.16.
 */
public class TestLogFactory implements NativeLogFactory {
    @Override
    public Log getLog(Class clazz) {
        return new TestLogger(clazz.getName());
    }
}
