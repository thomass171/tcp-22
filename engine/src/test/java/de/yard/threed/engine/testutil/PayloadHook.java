package de.yard.threed.engine.testutil;

import de.yard.threed.core.Payload;

/**
 *
 */
@FunctionalInterface
public interface PayloadHook {
    void handle(Payload e);
}
