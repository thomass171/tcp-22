package de.yard.threed.engine.platform.common;

/**
 * Geht wegen C# nicht als FunctionalInterface, weils es auch wirklich Interface ist.
 */
public interface RequestHandler {
    /**
     * Ein Request verarbeiten.
     * Return true if request was processed.
     *
     */
    boolean processRequest(Request request);

}
