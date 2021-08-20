package de.yard.threed.engine.ecs;


public class ComponentFilter implements EntityFilter {
    private String tag;

    public ComponentFilter(String tag) {
        this.tag = tag;

    }

    @Override
    public boolean matches(EcsEntity e) {
        if (e.getComponent(tag) != null) {
            return true;
        }
        return false;
    }
}
