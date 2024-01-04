package de.yard.threed.core.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockHelper {

    public static void mockHttpGet(WireMockServer wireMockServer, String endpoint, String responseBody) {

        String url = "http://localhost:" + wireMockServer.port() + endpoint;

        String responseContentType = "text/plain";

        wireMockServer.stubFor(get(urlEqualTo(endpoint))
                //.withHeader("Accept", matching("text/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", responseContentType)
                        .withBody(responseBody)));

    }

    public static void mockHttpGet(WireMockServer wireMockServer, String endpoint, byte[] responseBody) {

        // nginx apparently doesn't set a content type for bin

        wireMockServer.stubFor(get(urlEqualTo(endpoint))
                //.withHeader("Accept", matching("text/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    public static void mockHttpGet(WireMockServer wireMockServer, String endpoint, int httpError) {

        wireMockServer.stubFor(get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(httpError)));
    }
}
