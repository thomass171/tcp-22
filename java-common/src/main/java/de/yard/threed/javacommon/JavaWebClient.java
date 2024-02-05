package de.yard.threed.javacommon;

import de.yard.threed.core.Pair;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.NativeHttpClient;
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
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.util.TimeValue;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The default connection pooling is a risk in unit tests causing socket errors on wiremock restart.
 * So close() should be used to close all pooled connection during test startup.
 */
public class JavaWebClient implements NativeHttpClient {

    static ExecutorService executor = Executors.newFixedThreadPool(16);

    static PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = null;
    static PoolingHttpClientConnectionManager connectionManager = null;
    static private CloseableHttpClient client;

    /**
     * From https://hc.apache.org/httpcomponents-client-5.2.x/quickstart.html
     * Its unclear how to close a connection when using HttpAsyncClients. All the examples just wait for the future,
     * which is not what async intends.
     * One more reason to use our own async (AsyncHelper) is to avoid MT effects when the http client callback suddenly executes. Many platforms
     * really don't like MT.
     * 28.12.23 no longer static
     */
    synchronized public NativeFuture<AsyncHttpResponse> httpGet(String url, List<Pair<String, String>> params, List<Pair<String, String>> header) {

        if (connectionManager == null) {
            connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder
                    .create()
                    .useSystemProperties()
                    .setMaxConnPerRoute(16)
                    .setMaxConnTotal(16)
                    // The ValidateAfterInactivity value seems to effect the time for ending a process, eg. after a single test or after
                    // pressing ESC to end a scene.
                    .setDefaultConnectionConfig(ConnectionConfig.custom().setValidateAfterInactivity(TimeValue.ofSeconds(1L)).build());
            connectionManager = connectionManagerBuilder.build();
            client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();//.useSystemProperties().evictExpiredConnections().evictIdleConnections(TimeValue.ofMinutes(1L)).build();

        }

        Future<AsyncHttpResponse> future = executor.submit(() -> {

            try {
                // BTW: Apache httpclient 5 is annoying.
                // The code is extracted from Request.get(url).execute().
                // The Java 11 httpclient is even more pain. It might return a 400 with body '<h1>Bad Message 400</h1><pre>reason: Bad Request</pre>' from nowhere(??).
                // The Java 11 httpclient is really a 'no go'. Debugging is hard and logging (by '-Djdk.httpclient.HttpClient.log=requests') is useless.
                long startMillis = System.currentTimeMillis();

                CloseableHttpResponse r = client.execute(new HttpGet(url));

                int statusCode = r.getCode();
                if (statusCode >= 300) {
                    String h = "";
                    for (Header s : r.getHeaders()) {
                        h += s.getName() + "=" + s.getValue() + ",";
                    }
                    getLogger().warn("url=" + url + ",statusCode=" + statusCode + ",header=" + h);
                }
                byte[] buffer = FileReader.readFully(r.getEntity().getContent());
                r.close();
                return new AsyncHttpResponse(statusCode, null, new SimpleByteBuffer(buffer), System.currentTimeMillis() - startMillis);
            } catch (HttpResponseException e) {
                getLogger().error("Got HttpResponseException:" + e.getMessage());
                return new AsyncHttpResponse(e.getStatusCode(), e.getMessage());
            } catch (Exception e) {
                getLogger().error("Got " + e.getClass().getName() + ":" + e.getMessage() + " for url " + url);
                return new AsyncHttpResponse(-1, e.getMessage());
            } finally {
            }
        });

        return new JavaFuture(future);
    }

    synchronized public static void close() {
        //not reliable getLogger().debug("Closing");
        if (connectionManager != null) {
            connectionManager.close();
        }
        if (client != null) {
            IOUtils.closeQuietly(client);
        }

        client = null;
        connectionManager = null;
        connectionManagerBuilder = null;
    }

    private static Log getLogger() {
        Log logger = Platform.getInstance().getLog(JavaWebClient.class);
        return logger;
    }
}
