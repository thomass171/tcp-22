package de.yard.threed.javacommon;

import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.ResourceSaveException;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.platform.Log;

import de.yard.threed.core.StringUtils;
import de.yard.threed.outofbrowser.FileSystemResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A general purpose cache, currently used for images.
 * Every objekt in cache refers to a single source file. Currently there is no update (timestamp check).
 * No Singleton, allows multiple instances for many purposes.
 * <p>
 * 4.7.21: Resides in java-common because the primary use case is image loading in Java.
 * <p/>
 * Created by thomass on 10.06.16.
 */
public class Cache {
    private Log logger = /*4.7.21 Engine*/Platform.getInstance().getLog(Cache.class);
    private String cachedir;

    public Cache(String name, String cachebasedir) {
        //4.7.21 cachebasedir = ((Platform)Platform.getInstance()).getSystemProperty("CACHEDIR");
        //cachebasedir = System.getProperty("CACHEDIR");
        cachedir = cachebasedir + "/" + name;
        logger.info("Using cache " + name + " in " + cachebasedir);
    }

    /**
     * Returns the
     *
     * @param res
     * @return
     */
    public byte[] getCachedObject(NativeResource res) {
        String id = getUniqueId(res);
        FileSystemResource cachedobject = new FileSystemResource(cachedir + "/" + id);
        try {
            return FileReader.readFully(FileReader.getFileStream(cachedobject));
        } catch (Exception e) {
            logger.info("Object not found in cache:" + res.getFullName() + " id=" + id);
            return null;
        }
    }

    public BufferedImage getCachedImage(NativeResource res) {
        String id = getUniqueId(res);
        FileSystemResource cachedobject = new FileSystemResource(cachedir + "/" + id);
        try {
            //BufferedImage bi = ImageIO.read(FileReader.getFileStream(cachedobject));
            byte[] buf= FileReader.readFully(FileReader.getFileStream(cachedobject));
            if (buf != null) {
                ByteArrayInputStream b = new ByteArrayInputStream(new SimpleByteBuffer(buf));
                int width = b.readInt();
                int height = b.readInt();
                //ByteBuffer buffer = ByteBuffer.allocate(buf.length - 8);
                ByteBuffer buffer = BufferHelper.createByteBuffer(width * height * 4/*BYTES_PER_PIXEL*/); //4 for RGBA, 3 for RGB
                buffer.put(b.getBuffer(), 8, buf.length - 8);
                buffer.rewind();
                LoadedImage loadedimage = new LoadedImage(width, height, buffer);
                return BufferedImageUtils.fromBuffer(width, height, buffer);
                //das serialisierte Objekt ist schon preprocessed(??)
                //loadedimage.setFromCache();
                //return loadedimage;
            }
            return null;
        } catch (Exception e) {
            logger.info("Object not found in cache:" + res.getFullName() + " id=" + id);
            return null;
        }
    }
    /*public NativeOutputStream saveCachedObject(NativeResource res) throws ResourceSaveException {
        FileSystemResource cachedobject = new FileSystemResource(new ResourcePath(cachedir), getUniqueId(res));
        return saveResourceSync(cachedobject, logger);
    }*/

    public void saveImage(NativeResource res, BufferedImage bi) throws ResourceSaveException, IOException {
        FileSystemResource cachedobject = new FileSystemResource(new ResourcePath(cachedir), getUniqueId(res));
        NativeOutputStream outs = saveResourceSync(cachedobject, logger);

        // ImageIO.write() might fail with 'no writer' unexpectedly. And will be slow on reading again.
        /*if (!ImageIO.write(bi, "bmp", os)){
            logger.error("No writer found");
        }*/
        //int[] pxl = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
        //ByteBuffer bb = BufferHelper/*OpenGlTexture*/.buildTextureBuffer(bi.getWidth(), bi.getHeight(), pxl, 4);
        LoadedImage li = BufferedImageUtils.toLoadedImage(bi);
        outs.writeInt(bi.getWidth());
        outs.writeInt(bi.getHeight());
        for (int i = 0; i < li.width * li.height * 4; i++) {
            outs.writeByte(li.buffer.get());
        }
        outs.close();
        // flip not really needed because buffer isn't reused?
        //bb.flip();
    }

    /**
     * 21.9.19: Aus JAResourceManager.
     */
    private static NativeOutputStream saveResourceSync(NativeResource resource, Log logger) throws ResourceSaveException {
        // fname zum pruefen nehmen, aber fullname zum speichern
        String fname = resource.getName();
        // Secuirtycheck um nicht versehentlich wichtige Dateien zu ueberschreiben.
        if (!fname.contains("SL") && !fname.startsWith("GRCH") && !fname.equals("groundnet.ac")) {
            logger.error("saving resource security check failed");
            throw new RuntimeException("saving resource security check failed");
        }
        String fullname = resource.getFullName();
        if (!fullname.contains("SL") && !fullname.contains("GRCH") && !fname.endsWith("groundnet.ac")) {
            logger.error("saving resource security check failed");
            throw new RuntimeException("saving resource security check failed");
        }
        try {
            // JAOutputStream will not overwrite files
            return new JAOutputStream(fullname);
        } catch (IOException e) {

            throw new ResourceSaveException("saveResourceSync failed: " + e.getMessage(), e);
        }
    }

    /**
     * Der Name duefte eindeutig genug sein, sonst gäbe es ja grundsätzlich Probleme.
     * Nur die Länge der Uniqueid könnte ein Problem sein.
     * <p>
     * 21.12.16: Auch den ResourcePath mit einbeziehen.
     *
     * @param res
     * @return
     */
    private static String getUniqueId(NativeResource res) {
        String n = res.getFullName();
        n = StringUtils.replaceAll(n, "/", "SL");
        if (StringUtils.length(n) > 255) {
            //TODO
        }
        //n += ".ser";
        n = "GRCH" + n;
        return n;
    }

}
