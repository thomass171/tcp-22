package de.yard.threed.engine;

import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

public class RequestBuilder {

    private RequestType requestType;
    private RequestPopulator requestPopulator;

    public RequestBuilder(RequestType requestType) {
        this.requestType = requestType;
    }

    public RequestBuilder(RequestType requestType, RequestPopulator requestPopulator) {
        this.requestType = requestType;
        this.requestPopulator = requestPopulator;
    }

    public Request build(Integer userEntityId) {
        Request request = new Request(requestType, userEntityId);
        if (requestPopulator != null) {
            requestPopulator.populate(request);
        }
        return request;
    }
}
