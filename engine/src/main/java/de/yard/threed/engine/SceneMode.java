package de.yard.threed.engine;

public class SceneMode {

    private int mode;

    private SceneMode(int mode) {
        this.mode = mode;
    }

    public static SceneMode forServer() {
        return new SceneMode(1);
    }

    public static SceneMode forClient() {
        return new SceneMode(2);
    }

    public static SceneMode forMonolith() {
        return new SceneMode(3);
    }

    public boolean isClient() {
        return mode == 2 || mode == 3;
    }

    public boolean isServer() {
        return mode == 1 || mode == 3;
    }
}
