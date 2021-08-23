package de.yard.threed.javanative;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p/>
 * <p/>
 * Created by thomass on 08.05.20.
 */
public class FileReader {

    public static String readAsString(File file) {
        try {
            String content = IOUtils.toString(new FileInputStream(file));
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readFully(InputStream ins) {
        if (ins == null) {
            return null;
        }
        try {
            return IOUtils.toByteArray(ins);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}
