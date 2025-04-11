package de.yard.threed.trafficservices.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonService {

    @Autowired
    ObjectMapper objectMapper;

    public <T> String modelToJson(T model) throws JsonProcessingException {
        return objectMapper.writeValueAsString(model);
    }

    public <T> T jsonToModel(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    public JsonNode jsonToNodeTree(String json) throws JsonProcessingException {
        return objectMapper.readTree(json);
    }

    public <T> T mapOrTreeToModel(Object mapOrTree, Class<T> clazz) {
        return objectMapper.convertValue(mapOrTree, clazz);
    }
}
