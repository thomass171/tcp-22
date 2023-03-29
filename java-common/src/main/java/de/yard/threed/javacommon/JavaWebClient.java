package de.yard.threed.javacommon;

import de.yard.threed.core.Pair;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeFuture;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JavaWebClient {

    /**
     * From https://hc.apache.org/httpcomponents-client-5.2.x/quickstart.html
     * Its unclear how to close a connection when using HttpAsyncClients. All the examples just wait for the future,
     * which is not what async intends.
     * So build our own async.
     */
    public static NativeFuture<AsyncHttpResponse> httpGet(String url, List<Pair<String, String>> params, List<Pair<String, String>> header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<AsyncHttpResponse> future = executor.submit(() -> {
            try {
                Content content = Request.get(url).execute().returnContent();
                String s = content.asString(StandardCharsets.UTF_8);
                return new AsyncHttpResponse(0, null, s);
            } catch (HttpResponseException e) {
                return new AsyncHttpResponse(e.getStatusCode(), null, null);
            } catch (Exception e) {
                return new AsyncHttpResponse(-1, null, null);
            }
        });

        return new JavaFuture(future);
    }
}
