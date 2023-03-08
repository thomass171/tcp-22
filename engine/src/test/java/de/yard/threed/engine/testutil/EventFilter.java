package de.yard.threed.engine.testutil;

import de.yard.threed.core.Event;

/**
 *
 */
@FunctionalInterface
public interface EventFilter {
    boolean matches(Event e);
}
