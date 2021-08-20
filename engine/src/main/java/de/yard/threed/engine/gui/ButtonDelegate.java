package de.yard.threed.engine.gui;

/**
 * 4.10.19: Deprecated, weil das doch auch ein  RequestHandler sein kann.
 * 30.12.19: Nicht mehr deprecated, um ECS und menus zu entkoppeln.
 */
//@Deprecated
@FunctionalInterface
public interface ButtonDelegate {
    void buttonpressed();
}
