package de.yard.threed.javacommon;

import de.yard.threed.core.Pair;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JavaWebClient {

    /**
     * From https://hc.apache.org/httpcomponents-client-5.2.x/quickstart.html
     * Its unclear how to close a connection when using HttpAsyncClients. All the examples just wait for the future,
     * which is not what async intends.
     * One more reason to use our own async (AsyncHelper) is to avoid MT effects when the http client callback suddenly executes. Many platforms
     * really don't like MT.
     */
    public static NativeFuture<AsyncHttpResponse> httpGet(String url, List<Pair<String, String>> params, List<Pair<String, String>> header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<AsyncHttpResponse> future = executor.submit(() -> {
            CloseableHttpClient client = null;
            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = null;
            PoolingHttpClientConnectionManager connectionManager = null;
            try {
                // BTW: Apache httpclient 5 is annoying.
                // The default connection pooling is a pain in unit tests causing socket errors on wiremock restart. So reinit a client for each
                // request for now. The code is extracted from Request.get(url).execute().
                // The Java 11 httpclient is even more pain. It might return a 400 with body '<h1>Bad Message 400</h1><pre>reason: Bad Request</pre>' from nowhere(??).
                // The Java 11 httpclient is really a 'no go'. Debugging is hard and logging (by '-Djdk.httpclient.HttpClient.log=requests') is useless.
                connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder
                        .create()
                        .useSystemProperties()
                        .setMaxConnPerRoute(1)
                        .setMaxConnTotal(1)
                        .setDefaultConnectionConfig(ConnectionConfig.custom().setValidateAfterInactivity(TimeValue.ofSeconds(10L)).build());
                connectionManager = connectionManagerBuilder.build();
                client = HttpClientBuilder.create().setConnectionManager(connectionManager).useSystemProperties().evictExpiredConnections().evictIdleConnections(TimeValue.ofMinutes(1L)).build();

                CloseableHttpResponse r = client.execute(new HttpGet(url));

                int statusCode = r.getCode();
                if (statusCode >= 300) {
                    getLogger().warn("url=" + url + ",statusCode=" + statusCode + ",header=" + r.getHeaders());
                }
                byte[] buffer = FileReader.readFully(r.getEntity().getContent());
                r.close();
                return new AsyncHttpResponse(statusCode, null, new SimpleByteBuffer(buffer));
            } catch (HttpResponseException e) {
                getLogger().error("Got HttpResponseException:" + e.getMessage());
                return new AsyncHttpResponse(e.getStatusCode(), e.getMessage());
            } catch (Exception e) {
                getLogger().error("Got " + e.getClass().getName() + ":" + e.getMessage() + " for url " + url);
                return new AsyncHttpResponse(-1, e.getMessage());
            } finally {
                IOUtils.closeQuietly(connectionManager);
                IOUtils.closeQuietly(client);
                if (executor != null) {
                    executor.shutdown();
                }
            }
        });

        return new JavaFuture(future);
    }

    private static Log getLogger() {
        Log logger = Platform.getInstance().getLog(JavaWebClient.class);
        return logger;
    }
}
