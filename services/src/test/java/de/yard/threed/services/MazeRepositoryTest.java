package de.yard.threed.services;

import static de.yard.threed.testutils.TestUtils.loadFileFromClasspath;
import static de.yard.threed.testutils.TestUtils.validateAlmostNow;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import de.yard.threed.services.maze.Maze;
import de.yard.threed.services.maze.MazeRepository;
import de.yard.threed.testutils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;

import static de.yard.threed.services.util.Util.buildList;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class MazeRepositoryTest {

    final String SB_NAME = "Sokoban Wikipedia";
    final String S10x10_NAME = "Sokoban 10x10";
    public static final String ENDPOINT_MAZES = "/mazes/mazes";

    private MockMvc mockMvc;

    @Autowired
    private MazeRepository mazeRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JsonService jsonService;

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
        assertEquals(5, maze1.getGrid().length());
        maze1.setSecret("sec");
        maze1.setDescription("bb");
        maze1.setType("P");
        maze1.setCreatedAt(ZonedDateTime.now());
        maze1.setCreatedBy("me");
        maze1.setModifiedAt(ZonedDateTime.now());
        maze1.setModifiedBy("you");

        mazeRepository.save(maze1);

    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testFindByName() throws Exception {
        this.mockMvc.perform(get("/mazes/mazes/search/findByName?name=Sokoban Wikipedia")).andDo(print())
                .andExpect(content().string(containsString("##")));
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testHidingSecrets() throws Exception {
        this.mockMvc.perform(get("/mazes/mazes")).andDo(print())
                .andExpect(content().string(not(containsString("secret"))));

        this.mockMvc.perform(get("/mazes/mazes")).andDo(print())
                .andExpect(content().string(containsString("\"locked\" : false")));

        this.mockMvc.perform(get("/mazes/mazes/search/findByName?name=Sokoban Wikipedia")).andDo(print())
                .andExpect(content().string(containsString("\"locked\" : true")));
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void postNewShouldCreate() throws Exception {

        String json = loadFileFromClasspath("MazeJsonTemplate.json");
        json = json.replace("Sokoban Wikipedia", "Playground");
        MvcResult result = TestUtils.doPost(mockMvc, ENDPOINT_MAZES, json);
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        assertEquals(3, mazeRepository.count());
        assertNotNull(mazeRepository.findByName("Playground"));
        assertEquals("Playground", mazeRepository.findByName("Playground").getName());
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void postWithDuplicateNameShouldFail() throws Exception {

        String json = loadFileFromClasspath("MazeJsonTemplate.json");

        MvcResult result = TestUtils.doPost(mockMvc, ENDPOINT_MAZES, json);
        // unique constraint violation causing 409 (conflict)
        assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus());
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testPatch() throws Exception {

        Maze mazeSB = getMazeByName(SB_NAME);
        //String json = loadFileFromClasspath("MazeJsonTemplate.json");
        String json = "{\"description\": \"New description\"}";

        // Locked without key Should fail
        String requestBody = jsonService.modelToJson(mazeSB);
        assertFalse(requestBody.contains("secret"));
        MvcResult result = TestUtils.doPatch(mockMvc, ENDPOINT_MAZES + "/" + mazeSB.getId(), requestBody);
        assertEquals(403, result.getResponse().getStatus());

        // Locked with wrong key should fail
        requestBody = jsonService.modelToJson(mazeSB);
        result = TestUtils.doPatchWithKey(mockMvc, ENDPOINT_MAZES + "/" + mazeSB.getId(), requestBody, "invalid");
        assertEquals(403, result.getResponse().getStatus());

        // Locked with valid key should succeed
        requestBody = jsonService.modelToJson(mazeSB);
        result = TestUtils.doPatchWithKey(mockMvc, ENDPOINT_MAZES + "/" + mazeSB.getId(), requestBody, "Baskerville");
        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());

        // unlocked without key should succeed
        Maze mazeS10x10 = getMazeByName(S10x10_NAME);
        requestBody = jsonService.modelToJson(mazeS10x10).replace(S10x10_NAME, "play");

        result = TestUtils.doPatch(mockMvc, ENDPOINT_MAZES + "/" + mazeS10x10.getId(), requestBody);
        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
        assertNotNull(getMazeByName("play"));
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testExcludedFieldsFromDeserialization() throws Exception {
        Maze maze = getMazeByName(SB_NAME);
        assertNotNull(maze.getId());
        assertNotNull(maze.getCreatedAt());
        maze.setCreatedAt(ZonedDateTime.now().minusYears(2));

        maze = jsonService.jsonToModel(jsonService.modelToJson(maze), Maze.class);

        assertNull(maze.getId());
        // filled with default in constructor
        assertNotNull(maze.getCreatedAt());
        validateAlmostNow(maze.getCreatedAt());

    }

    private Maze getMazeByName(String name) {
        Maze maze = mazeRepository.findByName(name);
        assertEquals(name, maze.getName());
        if (name.equals(SB_NAME)) {
            assertTrue(maze.getLocked());
        }
        if (name.equals(S10x10_NAME)) {
            assertFalse(maze.getLocked());
        }
        return maze;
    }
}
