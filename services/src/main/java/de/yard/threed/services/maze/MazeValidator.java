package de.yard.threed.services.maze;

import de.yard.threed.services.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component("beforeSaveMazeValidator")
public class MazeValidator implements Validator {

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Override
    public boolean supports(Class<?> clazz) {
        return Maze.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Maze maze = (Maze) target;
        log.debug("validating {}", maze);
        if (!hasValidKey(maze)) {
            throw new AccessDeniedException("");
        }
    }

    private boolean hasValidKey(Maze maze) {
        if (!maze.getLocked()) {
            return true;
        }
        String key = httpServletRequest.getHeader("Maze-Key");
        if (key != null && authorizationService.isSecretValid(maze.getId(), key)) {
            return true;
        }
        return false;
    }
}
