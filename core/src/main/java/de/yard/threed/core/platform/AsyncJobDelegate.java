package de.yard.threed.core.platform;



/**
 * A very generic async completion delegate.
 *
 * 18.5.20:Nachfolger von  AsyncJobCallback mit etwas anderem Ansatz
 * 7.11.23: The phrase 'Job' might be confusing, because its not only for 'jobs'. BTW: What is a job?
 */
@FunctionalInterface
public interface AsyncJobDelegate<T> {
    /**
     * 'completed' is independent from success, so it should always be called?
     */
    void completed(T response);
}
