package de.yard.threed.javanative;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class JsonUtil {
    public static <T> String toJson(T model){
        GsonBuilder builder = new GsonBuilder();
        String jsonString= builder.create().toJson(model);
        return jsonString;
    }

    public static <T> T fromJson(String jsonstring, Class<T> clazz){
        GsonBuilder builder = new GsonBuilder();
        T model = builder.create().fromJson(jsonstring, clazz);
        return model;
    }
}
