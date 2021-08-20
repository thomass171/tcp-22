package de.yard.threed.javacommon;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.LoadedResource;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourceNotFoundException;
import de.yard.threed.outofbrowser.NativeResourceReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Java basic file IO.
 * Replaced JAResourceManager
 * Created by thomass on 06.08.21.
 */
public class DefaultResourceReader extends NativeResourceReader {
    Log logger = Platform.getInstance().getLog(DefaultResourceReader.class);

    public DefaultResourceReader() {
    }

    public LoadedResource loadResourceSync(NativeResource resource) throws ResourceNotFoundException {
        byte[] bytebuf = null;
        try {
            return new LoadedResource(null/*16.10.18 FileReader.getFileStream(resource)*/);
        } catch (/*IO*/Exception e) {
            throw new ResourceNotFoundException(resource.getName(), e);
        }
    }

    public  String loadTextFile(String resource) throws ResourceNotFoundException {
        byte[] bytebuf = null;
        try {
            String contents = new String(Files.readAllBytes(Paths.get(resource)));
            return contents;
        } catch (IOException e) {
            throw new ResourceNotFoundException(resource, e);
        }
    }

    @Override
    public  byte[] loadBinaryFile(String resource) throws ResourceNotFoundException {
        byte[] bytebuf = null;
        try {
            /*16.10.18 das haben wir doch gar nicht mehr, oder? if (new File(resource + ".gz").exists()) {
                resource += ".gz";
                byte[] b = FileReader.getInputStream(new GZIPInputStream(new FileInputStream(resource)), null).readFully();
                return b;
            }*/
            bytebuf =Files.readAllBytes(Paths.get(resource));
            return bytebuf;
        } catch (IOException e) {
            throw new ResourceNotFoundException(resource, e);
        }
    }

    @Override
    public boolean exists(String resource) {
        if (new File(resource).exists()) {
            return true;
        }
        if (new File(resource + ".gz").exists()) {
            return true;
        }
        return false;
    }
}


