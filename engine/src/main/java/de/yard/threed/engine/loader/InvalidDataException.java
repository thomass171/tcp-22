package de.yard.threed.engine.loader;

/**
 * Created by thomass on 16.02.16.
 */
public class InvalidDataException extends java.lang.Exception {
    public InvalidDataException(String msg) {
        super(msg);
    }

    public InvalidDataException(String msg, java.lang.Exception e) {
        super(msg, e);
    }
}
