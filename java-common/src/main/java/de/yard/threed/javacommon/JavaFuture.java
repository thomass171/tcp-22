package de.yard.threed.javacommon;

import de.yard.threed.core.platform.NativeFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JavaFuture<T> implements NativeFuture<T> {

    Future<T> future;

    public JavaFuture(Future<T> future){
    this.future=future;
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * Try without exception. Might need improvement.
     */
    @Override
    public T get() {
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
