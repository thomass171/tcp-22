package de.yard.threed.trafficservices.services;

import de.yard.threed.trafficservices.model.FgTile;
import de.yard.threed.trafficservices.model.Tile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.yard.threed.trafficservices.util.FileSystemHelper.listFiles;

@Service
@Slf4j
public class TileService {

    @Value("${terrasync.basedir:.}")
    private String terrasyncbasedir;

    public List<Tile> getTiles() throws IOException {

        Path path = getTerraSyncPath();
        path = path.resolve("Terrain");
        log.debug("Using 'Terrain' from '{}'", path);

        log.info("Tiles from {}", path);
        // getFileName() returns a (Unix)Path, so toString() is needed.
        List<Path> paths = listFiles(path, p -> p.getFileName().toString().endsWith(".stg"));
        paths.forEach(x -> log.debug("path={}", x));
        return paths.stream().map(p -> FgTile.buildFromPath(p)).collect(Collectors.toUnmodifiableList());
    }

    private Path getTerraSyncPath() {

        Path workingDir;

        if (terrasyncbasedir.startsWith("/")) {
            workingDir = Paths.get(terrasyncbasedir);
        } else {
            Path userDir = Paths.get(System.getProperty("user.dir"));
            workingDir = userDir.resolve(terrasyncbasedir);
        }
        if (!Files.exists(workingDir)){
            log.error("Does not exist: {}", workingDir);
        }
        return workingDir;
    }
}

