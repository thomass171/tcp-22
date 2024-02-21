package de.yard.threed.engine.platform;

import de.yard.threed.core.Pair;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;

import java.util.ArrayList;
import java.util.List;

public class ResourceLoaderViaHttp extends ResourceLoader {

    URL url;

    public ResourceLoaderViaHttp(URL url) {
        super(url);
        this.url = url;
    }

    @Override
    public void loadResource(AsyncJobDelegate<AsyncHttpResponse> delegate) {
        List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
        Platform.getInstance().httpGet(nativeResource.getUrl().getAsString(), parameters, headers, delegate);
    }

    @Override
    public ResourceLoader fromReference(String reference) {
        URL un = new URL(url.getBaseUrl(), url.getPath(), reference);
        return new ResourceLoaderViaHttp(un);
    }

    @Override
    public ResourceLoader fromRootReference(ResourcePath path, String reference) {
        URL un = new URL(url.getBaseUrl(), path, reference);
        return new ResourceLoaderViaHttp(un);
    }
}
