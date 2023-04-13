package de.yard.threed.services.maze;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@CrossOrigin
@RepositoryRestResource(collectionResourceRel = "mazes", path = "mazes")
public interface MazeRepository extends PagingAndSortingRepository<Maze, Long> {

    List<Maze> findByCreatedBy(@Param("createdBy") String name);

    @RestResource(exported = true)
    Maze findByName(@Param("name") String name);
}
