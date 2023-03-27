package de.yard.threed.engine.platform;

import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeFuture;

/**
 * 23.3.23: Back in Platform as httpGet
 */
@Deprecated
public interface NativeHttpClient {
    /**
     * In Anlehnung an curl?.
     * 16.5.20: Nee, lieber an andere HTTP Clients.
     *
     *
     */
    @Deprecated
    void sendHttpRequest(String url, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate );


    }
