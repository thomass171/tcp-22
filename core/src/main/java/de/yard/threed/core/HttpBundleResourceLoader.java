package de.yard.threed.core;

import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * For using from outside platform (needs future processing by scene runner).
 * Alternative is NativeHttpClient.
 */
public class HttpBundleResourceLoader implements NativeBundleResourceLoader {

    String url;

    public HttpBundleResourceLoader(String url) {
        this.url = url;
    }

    @Override
    public void loadFile(String resource, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {

        List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
        Platform.getInstance().httpGet(url + "/" + resource,parameters, headers, asyncJobDelegate);
    }

    @Override
    public String getBasePath() {
        return url;
    }
}
