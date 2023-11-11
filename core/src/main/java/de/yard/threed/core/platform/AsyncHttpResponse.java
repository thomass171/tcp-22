package de.yard.threed.core.platform;

import de.yard.threed.core.Pair;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.NativeByteBuffer;

import java.util.List;

/**
 * The basic response data in HTTP like in all network connections is a byte stream. It might be converted to
 * a string if the encoding is known.
 * "response" will be null in case of network error (ie. no network connection and thus no response)
 * So the status here is the HTTP code returned or -1 if no response was received (any kind of exception).
 */
public class AsyncHttpResponse {
    int status;
    private NativeByteBuffer responseData;

    public AsyncHttpResponse(int status, List<Pair<String, String>> responseHeader, NativeByteBuffer responseData) {
        this.status = status;
        this.responseData = responseData;
    }

    /**
     * Constructor for errors without content.
     */
    public AsyncHttpResponse(int status, List<Pair<String, String>> responseHeader) {
        this.status = status;
    }

    /**
     * Constructor for errors without header but with error message.
     */
    public AsyncHttpResponse(int status, String message) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public NativeByteBuffer getContent() {
        return responseData;
    }

    /**
     * Its quite useless to pass an encoding here as the requester cannot know the encoding reliably. He just might request it via HTTP header.
     */
    public String getContentAsString() {
        if (responseData == null) {
            return null;
        }
        // TODO use response header for knowing how to do decoding
        return StringUtils.buildString(responseData.getBuffer());
    }

    @Override
    public String toString() {
        return "status=" + status + ",body size=" + ((responseData == null) ? 0 : responseData.getSize());
    }
}
