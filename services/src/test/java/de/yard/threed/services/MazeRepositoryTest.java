package de.yard.threed.services;

import de.yard.threed.services.maze.Maze;
import de.yard.threed.services.maze.MazeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;

import static de.yard.threed.services.util.Util.buildList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class MazeRepositoryTest {

    private MockMvc mockMvc;

    @Autowired
    private MazeRepository mazeRepository;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void teardown() {
        mazeRepository.deleteAll();
    }

    /**
     * TODO add validator
     */
    @Test
    @Sql({"classpath:testGrids.sql"})
    public void test1() {

        assertEquals(2, mazeRepository.count());

        Maze maze1 = new Maze();
        maze1.setName("name");
        maze1.setGrid("aa");
        maze1.setSecret("sec");
        maze1.setDescription("bb");
        maze1.setType("P");
        maze1.setCreatedAt(ZonedDateTime.now());
        maze1.setCreatedBy("me");
        maze1.setModifiedAt(ZonedDateTime.now());
        maze1.setModifiedBy("you");

        maze1 = mazeRepository.save(maze1);

        assertEquals(3, buildList(mazeRepository.findAll()).size());

        Maze foundMaze = mazeRepository.findById(maze1.getId()).get();
        assertEquals("name", foundMaze.getName());
        assertEquals("aa", foundMaze.getGrid());
        assertEquals("sec", foundMaze.getSecret());
        assertEquals("bb", foundMaze.getDescription());
        assertEquals("P", foundMaze.getType());
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testBadRequestWhenNewlineInGrid() {

        assertEquals(2, mazeRepository.count());

        Maze maze1 = new Maze();
        maze1.setName("name");
        maze1.setGrid("aa\nbb");
        // make sure it really is newline
        assertEquals(5,maze1.getGrid().length());
        maze1.setSecret("sec");
        maze1.setDescription("bb");
        maze1.setType("P");
        maze1.setCreatedAt(ZonedDateTime.now());
        maze1.setCreatedBy("me");
        maze1.setModifiedAt(ZonedDateTime.now());
        maze1.setModifiedBy("you");

        mazeRepository.save(maze1);

    }
    }
