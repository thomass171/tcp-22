package de.yard.threed.trafficservices.services;

import de.yard.threed.trafficservices.model.Tile;
import de.yard.threed.trafficservices.util.AirportFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
public class TileServiceTest {

    @Autowired
    private TileService tileService;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testBasicOperations() throws Exception {

        List<Tile> tiles;
        tiles = tileService.getTiles();
        assertEquals(2, tiles.size());

    }
}