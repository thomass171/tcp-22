package de.yard.threed.core;

/**
 * 24.12.25: It is no good idea to use either a RuntimeException or default value when parsing fails.
 */
public class ParseException extends Exception {

    public ParseException(String s) {
        super(s);
    }
}
