package de.yard.threed.services.maze;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Not used currently.
 */
public class MazeDeserializer extends JsonDeserializer<Maze> {
        @Override
        public Maze deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Maze maze = new Maze();
            //maze.setName();
            return maze;
        }

}
