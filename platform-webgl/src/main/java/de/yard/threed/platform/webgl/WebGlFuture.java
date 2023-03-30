package de.yard.threed.platform.webgl;

import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.NativeFuture;

/**
 *
 */
public class WebGlFuture<T> implements NativeFuture<T> {

    T responseText;

    WebGlFuture(T responseText) {
        this.responseText = responseText;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() {
        return responseText;
    }
}
