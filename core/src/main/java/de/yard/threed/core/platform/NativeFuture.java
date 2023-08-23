package de.yard.threed.core.platform;

/**
 * Extracted from java.util.concurrent.Future.
 */
public interface NativeFuture<V> {
    /**
     * Returns {@code true} if this task completed.
     *
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * {@code true}.
     *
     * @return {@code true} if this task completed
     */
    boolean isDone();

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     *
     * try without exception.
     */
    V get() /*throws FutureException, ExecutionException*/;
}
