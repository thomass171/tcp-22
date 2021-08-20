package de.yard.threed.javacommon;

import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.ResourceSaveException;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.platform.Log;

import de.yard.threed.core.StringUtils;
import de.yard.threed.outofbrowser.FileSystemResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Cache für z.B. PDF images, Geladene Model.
 * Jedes Objekt im Cache hat Bezug zu genau einer Sourcedatei, deren Zeitstempel ueberwacht wird. TODO
 * Nicht mehr Singleton sondern fuer verschiedene Zwecke instanziierbar.
 * 
 * 16.10.18: Seit Bundle wird sowas doch gar nicht mehr begraucht, ausser in JME für Images. Verschoben nach desktop.
 * 4.7.21: Ist aber doch in java-common. Da liegt es eigentlich auch gut, weils primaer fur die Java png Probleme ist.
 * <p/>
 * Created by thomass on 10.06.16.
 */
public class Cache {
    private Log logger = /*4.7.21 Engine*/Platform.getInstance().getLog(Cache.class);
    private String cachedir,cachebasedir;
    private static Cache instance;
    private boolean enabled;

    public Cache(String name) {
        //4.7.21 cachebasedir = ((Platform)Platform.getInstance()).getSystemProperty("CACHEDIR");
        cachebasedir = System.getProperty("CACHEDIR");
        enabled = cachebasedir != null;
        cachedir = cachebasedir+"/"+name;
        logger.info("enabled=" + enabled);
    }
    
    public void disable(){
        enabled=false;
    }
    /**
     * Returns the
     *
     * @param res
     * @return
     */
    public InputStream getCachedObject(NativeResource res) {
        String id = getUniqueId(res);
        FileSystemResource cachedobject = new FileSystemResource(cachedir + "/" + id);
        try {
            return FileReader.getFileStream(cachedobject);
        } catch (Exception e) {
            logger.info("Object not found in cache:" + res.getFullName() + " id=" + id);
            return null;
        }
    }

    /*22.12.16 public void saveCachedObject(NativeResource res, byte[] data) throws ResourceSaveException {
        FileSystemResource cachedobject = new FileSystemResource(cachedir + "/" + getUniqueId(res));
        Platform.getInstance().saveResourceSync(cachedobject, data);

    }*/

    public NativeOutputStream saveCachedObject(NativeResource res) throws ResourceSaveException {
        FileSystemResource cachedobject = new FileSystemResource(new ResourcePath(cachedir), getUniqueId(res));
        //16.10.18 return Platform.getInstance().saveResourceSync(cachedobject);
        //21.9.19 return JmeResourceManager.getInstance().saveResourceSync(cachedobject);
        return saveResourceSync(cachedobject,logger);

    }

    /**
     * 21.9.19: Aus JAResourceManager.
     *
     * @param resource
     * @param logger
     * @return
     * @throws ResourceSaveException
     */
    //21.9.19 @Override
    public static NativeOutputStream saveResourceSync(NativeResource resource, Log logger) throws ResourceSaveException {
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
            return new JAOutputStream(fullname);
        } catch (IOException e) {

            throw new ResourceSaveException("saveResourceSync failed: " + e.getMessage(), e);
        }
    }



    public boolean isEnabled() {
        return enabled;
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
