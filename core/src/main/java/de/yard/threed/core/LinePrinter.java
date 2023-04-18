package de.yard.threed.core;

@FunctionalInterface
public interface LinePrinter {
    void println(String text) throws WriteException;
}
