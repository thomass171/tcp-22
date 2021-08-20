package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;

/**
 * Einmalige Aktion zu einem Event. Darum der Name Callback statt Listener. Notify waere auch gut.
 * 
 * Oder gehoert das zum Eventbus? Nee, dahin, wo Events zugestellt werden.
 * 
 * Created by thomass on 29.12.16.
 */
public interface EventNotify {
    void eventPublished(Event evt);
}
