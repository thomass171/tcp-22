package de.yard.threed.core.platform;

/**
 * 13.12.23: Reactivated for having a very simple non generic general purpose callback (easy use in C# lists).
 * Unfortunatly without parameter.
 */
@FunctionalInterface
public interface AsyncDelegator {
    void run();
}
