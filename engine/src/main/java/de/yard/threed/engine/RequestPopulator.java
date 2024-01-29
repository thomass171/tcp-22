package de.yard.threed.engine;

import de.yard.threed.engine.platform.common.Request;

@FunctionalInterface
public interface RequestPopulator {
    void populate(Request request);
}
