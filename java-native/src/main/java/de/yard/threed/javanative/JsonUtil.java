package de.yard.threed.javanative;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.LinkedTreeMap;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * There is also a JsonHelper class in core.
 */
public class JsonUtil {
    public static <T> String toJson(T model) {
        GsonBuilder builder = createGsonBuilder();
        String jsonString = builder.create().toJson(model);
        return jsonString;
    }

    public static <T> T fromJson(String jsonstring, Class<T> clazz) {
        GsonBuilder builder = createGsonBuilder();
        T model = builder.create().fromJson(jsonstring, clazz);
        return model;
    }

    public static GsonBuilder createGsonBuilder() {

        GsonBuilder builder = new GsonBuilder();
        // use ISO timestamps instead of single fields per element
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        builder.registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>)
                (src, typeOfSrc, context) -> new JsonPrimitive(formatter.format(src)));
        builder.registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>)
                (json, type, context) -> OffsetDateTime.parse(json.getAsString(), formatter));

        return builder;
    }
}
