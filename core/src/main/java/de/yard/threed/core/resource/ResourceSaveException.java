package de.yard.threed.core.resource;



/**
 * Useful for tools
 *
 * Created by thomass 
 */
public class ResourceSaveException extends java.lang.Exception {
    public ResourceSaveException(String ressource, java.lang.Exception cause) {
        super(ressource + " save failed",cause);
    }

    public ResourceSaveException(String ressource) {
        super(ressource + " save failed");
    }
}
