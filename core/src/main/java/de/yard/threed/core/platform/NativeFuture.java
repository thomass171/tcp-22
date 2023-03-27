package de.yard.threed.core.platform;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

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
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     *
     * try without exception.
     */
    V get() /*throws FutureException, ExecutionException*/;
}
