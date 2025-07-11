package de.yard.threed.testutils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class TestUtils {

    public static MvcResult doGet(MockMvc mockMvc, String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andDo(print()).andReturn();
        return result;
    }

    public static MvcResult doGet(MockMvc mockMvc, String url, MultiValueMap<String, String> params) throws Exception {
        MvcResult result = mockMvc.perform(get(url).params(params))
                .andDo(print()).andReturn();
        return result;
    }

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

    /*public static String loadFileFromClasspath(String fileName) throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = getReaderFromClasspath(fileName, StandardCharsets.UTF_8)) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }*/
}
