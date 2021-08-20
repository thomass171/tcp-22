package de.yard.threed.core.platform;



/**
 *
 * 18.5.20:Nachfolger von  AsyncJobCallback mit etwas anderem Ansatz
 */
@FunctionalInterface
public interface AsyncJobDelegate<T> {
    void completed(T response);
}
