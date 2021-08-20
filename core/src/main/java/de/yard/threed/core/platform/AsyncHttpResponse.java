package de.yard.threed.core.platform;

public class AsyncHttpResponse {
    public String responseText;
    int status;

    public AsyncHttpResponse(int status, String responseText) {
        this.status = status;
        this.responseText = responseText;
    }

    public int getStatus() {
        return status;
    }
}
