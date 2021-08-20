package de.yard.threed.engine.platform;

import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;

public interface NativeHttpClient {
    /**
     * In Anlehnung an curl?.
     * 16.5.20: Nee, lieber an andere HTTP Clients.
     *
     *
     */
    void sendHttpRequest(String url, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate );
}
