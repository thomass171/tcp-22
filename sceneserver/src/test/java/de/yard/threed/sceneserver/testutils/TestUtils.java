package de.yard.threed.sceneserver.testutils;

import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;

import java.nio.charset.StandardCharsets;

public class TestUtils {

    public static String httpGet(String url) throws Exception {

        Content content = Request.get(url).execute().returnContent();
        String s = content.asString(StandardCharsets.UTF_8);
        // just assume its 200
        return s;
    }
}
