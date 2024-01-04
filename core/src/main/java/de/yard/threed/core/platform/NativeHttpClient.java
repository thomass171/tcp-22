package de.yard.threed.core.platform;

import de.yard.threed.core.Pair;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeFuture;

import java.util.List;

/**
 * 23.3.23: Platform also provides httpGet without returned future but delegate for easier usage and runtime integration from app.
 * But still we have this interface for easier decoupling.
 */
public interface NativeHttpClient {
    /**
     * "response" will be null in case of network error (ie. no network connection and thus no response)
     */
    NativeFuture<AsyncHttpResponse>  httpGet(String url, List<Pair<String, String>> parameter, List<Pair<String, String>> header);
}
