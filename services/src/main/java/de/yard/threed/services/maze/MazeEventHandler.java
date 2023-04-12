package de.yard.threed.services.maze;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Component
@RepositoryEventHandler
public class MazeEventHandler {

    // Triggered for PUT / PATCH
    /*@HandleBeforeSave
    public void onBeforeSave(Maze maze) {
    }*/
}
