package de.yard.threed.testutils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class TestUtils {

    public static MvcResult doPost(MockMvc mockMvc, String url, String body) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(body))
                .andDo(print()).andReturn();
        return result;
    }

    public static MvcResult doPatch(MockMvc mockMvc, String url, String body) throws Exception {
        MvcResult result = mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(body))
                .andDo(print()).andReturn();
        return result;
    }

    public static MvcResult doPatchWithKey(MockMvc mockMvc, String url, String body, String key) throws Exception {
        MvcResult result = mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("Maze-Key",key)
                        .content(body))
                .andDo(print()).andReturn();
        return result;
    }

    public static String loadFileFromClasspath(String fileName) throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

    public static void validateAlmostNow(ZonedDateTime dateTime) {

        Duration duration = Duration.between(dateTime, ZonedDateTime.now());
        assertTrue(duration.abs().getSeconds() < 10);
    }
}
