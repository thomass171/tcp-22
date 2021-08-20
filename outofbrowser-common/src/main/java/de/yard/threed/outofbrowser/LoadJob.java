package de.yard.threed.outofbrowser;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;

/**
 * Aus BundleLoaderExceptGwt
 * 3.8.21: Das ist doch alles ueberdimensioniert, oder?
 */
public class LoadJob /*3.8.21 implements AsyncJob */ {
    String bundlename;
    //war eh immer null AsyncJobCallback loadlistener;
    String registername;
    boolean delayed;
    NativeResourceReader rm;
    static Log logger = Platform.getInstance().getLog(LoadJob.class);

    public LoadJob(String bundlename, String registername, boolean delayed/*, AsyncJobCallback loadlistener,*/, NativeResourceReader rm) {
        this.bundlename = bundlename;
        // this.loadlistener = loadlistener;
        this.registername = registername;
        this.delayed = delayed;
        this.rm = rm;
    }

    /**
     * Returns null on success, otherwise an error message.
     *
     * @return
     */
    //@Override
    public String execute() {
        Bundle bundle = null;

        String bundlebasedir = BundleRegistry.getBundleBasedir(bundlename, false);
        if (bundlebasedir == null) {
            //26.7.21:Besser aussteigen als nachher ewig die URsache zu suchen
            throw new RuntimeException("bundlebasedir is null");
        }
        String resource = bundlebasedir + "/" + BundleRegistry.getDirectoryName(bundlename, false);
        String dir;
        try {
            if (rm == null) {
                String msg = "no resource manager";
                logger.error(msg);
                return msg;
            }
            dir = rm.loadTextFile(resource);
        } catch (java.lang.Exception e) {
            String msg = "load resource failed:" + resource + "(" + e.getMessage() + ")";
            logger.error(msg);
            return msg;
        }
        //auch wenn es anders registirert wird, behaelt es den Originalnamen
        bundle = new Bundle(bundlename, (dir), delayed);
        for (String filename : bundle.directory) {
            if (bundle.contains(filename)) {
                logger.error("duplicate directory entry " + filename + ". Bundle load will fail.");
                continue;
            }
            resource = bundlebasedir + "/" + filename;

            LoadJob.loadBundleData(bundle, resource, filename, delayed, rm);
        }

        BundleRegistry.registerBundle((registername != null) ? registername : bundlename, bundle);
        logger.info("Bundle registered: " + bundlename);
        return null;
    }

    public static void loadBundleData(Bundle bundle, String resource, String filename, boolean delayed, NativeResourceReader rm) {
        BundleData data = null;
        char filetype = Bundle.filetype(filename);
        switch (filetype) {
            case 'T':
                // C# conform fall through
            case 't':
                if (filetype == 'T' && delayed) {
                    break;
                }
                try {
                    String s = rm.loadTextFile(resource);
                    //gltf often have up to 200000
                    if (StringUtils.length(s) > 200000) {
                        logger.warn("BundleLoader: large string. length=" + StringUtils.length(s) + " (" + resource + ")");
                    }
                    data = new BundleData(s);
                } catch (java.lang.Exception e) {
                    logger.error("load resource failed:" + resource + "(" + e.getMessage() + ")");
                    bundle.addFailure(filename, e.getMessage());
                    return;
                }
                break;
            case 'B':
                // C# conform fall through
            case 'b':
                if (filetype == 'B' && delayed) {
                    break;
                }
                try {
                    if (StringUtils.endsWith(resource, ".btg.gz")) {
                        // uncompressed lesen
                        resource = StringUtils.substringBeforeLast(resource, ".gz");
                    }
                    byte[] b = rm.loadBinaryFile(resource);
                    data = new BundleData(new SimpleByteBuffer(b), false);
                } catch (java.lang.Exception e) {
                    logger.error("load resource failed:" + resource + "(" + e.getMessage() + ")");
                    bundle.addFailure(filename, e.getMessage());
                    return;
                }
                break;
            case 'i':
                //Image/Textur wird spater "intern" geladen.
                //5.1.19: Trotzdem null value speichern. Das macht das Bundle, auch in GWT, konsistenter. Ist als Default schon gesetzt.
                break;
                /*case 'z':
                    //1.7.17: Nicht mehr genutzt wegen uncompress browser
                    //zipped binary (btg.gz). Der unzip wird schon hier statt beim getRersource gemacht, weil dies hier
                    //in der Platform ist.
                    //20.7.17: Dann raus und Loader auch fuer C# verwenden.
                    try {
                        byte[] bz = rm.loadBinaryFile(resource);
                        bz = FileReader.getInputStream(new GZIPInputStream(new ByteArrayInputStream(bz)), null).readFully();
                        data = new BundleData(new sgSimpleBuffer(bz));
                    } catch (java.lang.Exception e) {
                        logger.error("load resource failed:" + resource + "(" + e.getMessage() + ")");
                    }
                    break;*/
            case 'g':
                //gltf wird - je nach dem - spaeter "intern" geladen.
                if (true/*PlatformWebGl.customgltfloader*/) {
                    // das bin file wird erstmal on the fly mitgeladen. Besser nicht wegen ressourcenverschwendung.
                    // 7.12.17 Mach ich jetzt aber trotzzdem, weils erstmal einfacher ist.
                    //6.1.18: Das bin file steht normal mit im directory
                    try {
                        //byte[] b = rm.loadBinaryFile(StringUtils.replaceAll(resource,".gltf",".bin"));
                        //data = new BundleData(new sgSimpleBuffer(b));
                        //bundle.addResource(StringUtils.replaceAll(filename,".gltf",".bin"), data);
                        String s = rm.loadTextFile(resource);
                        data = new BundleData(s);
                    } catch (java.lang.Exception e) {
                        logger.error("load resource failed:" + resource + "(" + e.getMessage() + ")");
                        bundle.addFailure(filename, e.getMessage());
                        return;
                    }
                } else {
                    //lb.bundle.addResource(filename, null);
                }
                break;
            default:
                //unknown
                break;
        }
        bundle.addResource(filename, data);

    }

    /*3.8.21 @Override
    public AsyncJobCallback getCallback() {
        return loadlistener;
    }

    @Override
    public String getName() {
        return "loadBundle";
    }*/
}
