package de.yard.threed.core.resource;



/**
 * Created by thomass on 24.12.15.
 */
public class ResourceNotFoundException extends java.lang.Exception {
    public ResourceNotFoundException(String ressource, java.lang.Exception cause) {
        super(ressource + " not found",cause);
    }

    public ResourceNotFoundException(String ressource) {
        super(ressource + " not found");
    }
}
