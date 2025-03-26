package de.yard.threed.trafficcore.model;

public class Vehicle {
    public String name;
    public SmartLocation location = null;
    private boolean delayedLoad = false;
    private boolean automove = false;
    public int initialCount;
    // flag to avoid multiple loading when several graphs are loaded
    public boolean wasLoaded = false;

    public Vehicle(String name, boolean delayedLoad, boolean automove, String location, int initialCount) {
        this.name = name;
        this.delayedLoad = delayedLoad;
        this.automove = automove;
        if (location != null) {
            this.location = SmartLocation.fromString(location);
        }
        this.initialCount = initialCount;
    }

    public Vehicle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SmartLocation getLocation() {
        return location;
    }

    public boolean hasDelayedLoad() {
        return delayedLoad;
    }

    public boolean hasAutomove() {
        return automove;
    }
}
