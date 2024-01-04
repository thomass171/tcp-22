package de.yard.threed.engine.util;

import de.yard.threed.core.platform.NativeNode;

/**
 *
 */
@FunctionalInterface
public interface XmlFilter {
    boolean matches(NativeNode e);
}
