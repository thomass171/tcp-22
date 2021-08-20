package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
 * Created by thomass on 27.12.16.
 */
public class DefaultEcsSystem extends EcsSystem {

    public DefaultEcsSystem() {
        super();
    }

    public DefaultEcsSystem(String[] tags) {
        super(tags);
    }

    public DefaultEcsSystem(EventType[] t) {
        super(t);
    }

    public DefaultEcsSystem(String[] tags, EventType[] t) {
        super(tags, t);
    }

    public DefaultEcsSystem(String[] tags, RequestType[] r, EventType[] t) {
        super(tags, r, t);
    }

    public DefaultEcsSystem(RequestType[] r, EventType[] t) {
        super(r, t);
    }

    @Override
    public void init() {
    }

    @Override
    public void init(EcsGroup group) {
    }

    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {

    }

    @Override
    public void process(Event evt) {

    }

    @Override
    public boolean processRequest(Request request) {
        return false;
    }

    @Override
    public void frameinit() {

    }
}
