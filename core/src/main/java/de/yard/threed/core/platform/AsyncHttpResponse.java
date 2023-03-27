package de.yard.threed.core.platform;

import de.yard.threed.core.Pair;

import java.util.List;

/**
 * "response" will be null in case of network error (ie. no network connection and thus no response)
 * So the status here is the HTTP code returned.
 */
public class AsyncHttpResponse {
    public String responseText;
    int status;

    public AsyncHttpResponse(int status, List<Pair<String,String>> responseHeader, String responseText) {
        this.status = status;
        this.responseText = responseText;
    }

    public int getStatus() {
        return status;
    }
}
