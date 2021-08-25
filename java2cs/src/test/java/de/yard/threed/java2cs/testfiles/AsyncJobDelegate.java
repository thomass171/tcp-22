package de.yard.threed.java2cs.testfiles;

/**
 * Created by thomass on 21.06.20.
 */
@FunctionalInterface
public interface AsyncJobDelegate<T> {
    void completed(T response);
}

