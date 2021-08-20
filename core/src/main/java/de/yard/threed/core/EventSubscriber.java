package de.yard.threed.core;

import de.yard.threed.core.EventType;

/**
 * Created by thomass on 31.08.16.
 */
public interface EventSubscriber {
    public void handle(EventType eventtype, Object evt);
}
