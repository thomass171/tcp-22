package de.yard.threed.trafficservices.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemHelper {

    public static List<Path> listFiles(Path path, Predicate<Path> fileFilter) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile).filter(fileFilter).collect(Collectors.toList());
        }
        return result;

    }
}
