package de.yard.threed.java2cs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by thomass on 29.02.16.
 */
public class SaveFileWriter {
    static Logger logger = LoggerFactory.getLogger(SaveFileWriter.class);

    public static boolean saveContentToFile(Path destination, byte[] buf,String magic) throws IOException {
        if (Files.exists(destination)) {
            //logger.warn("file exists: " + destination);
            if (!wasGenerated(destination,magic)) {
                logger.error("file exists and not marked as generated. Skipping: " + destination);
                return false;
            }
        } else {
            Files.createDirectories(destination.getParent());
            Path created = Files.createFile(destination);
        }
        Files.write(destination, buf);
        return true;
    }

    private static boolean wasGenerated(Path destination,String magic) throws IOException {
        String content = new String(Files.readAllBytes(destination));
        return content.startsWith(magic);
    }
}
