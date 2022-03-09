package de.yard.threed.services;

import de.yard.threed.services.maze.Maze;
import de.yard.threed.services.maze.MazeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    public void test1() {
        Maze maze1 = new Maze();
        maze1.setGrid("aa");
        maze1.setDescription("bb");
        maze1.setCreatedAt(ZonedDateTime.now());
        maze1.setCreatedBy("me");
        maze1.setModifiedAt(ZonedDateTime.now());
        maze1.setModifiedBy("you");

        mazeRepository.save(maze1);

        assertEquals(1,buildList(mazeRepository.findAll()).size());
    }
}
