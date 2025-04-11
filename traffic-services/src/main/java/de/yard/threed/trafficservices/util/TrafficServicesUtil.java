package de.yard.threed.trafficservices.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TrafficServicesUtil {

    public static <E> List<E> buildList(Iterable<E> iter) {
        List<E> list = new ArrayList<>();
        iter.forEach(list::add);
        return list;
    }

    /*public static BufferedReader getReaderFromClasspath(String fileName, Charset charset) throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        return new BufferedReader(new InputStreamReader(inputStream, charset));
    }*/

    public static BufferedReader getReaderFromPath(Path file, Charset charset) throws IOException {
        InputStream inputStream = Files.newInputStream(file);
        return new BufferedReader(new InputStreamReader(inputStream, charset));
    }
}
