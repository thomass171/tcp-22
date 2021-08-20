package de.yard.threed.engine.ecs;



public class NameFilter implements EntityFilter {
    private String name;

    public NameFilter(String name){
        this.name=name;

    }
    @Override
    public boolean matches(EcsEntity e) {
        if (name.equals(e.getName())) {
            return true;
        }
        return false;
    }
}
