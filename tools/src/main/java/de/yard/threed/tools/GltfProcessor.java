package de.yard.threed.tools;

import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.loader.AbstractLoaderBuilder;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.core.*;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.ResourceNotFoundException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.loader.StringReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Reads a model file and converts it to GLTF.
 * Also for dumping a GLTF and corresponding bin file.
 * Usages:
 * - GltfProcessor -gltf -o destinationdirectory inputfile [-l loaderclass ]
 * - GltfProcessor -dump gltffile
 * <p>
 * 22.12.18: Option "-acpp" and preprocessAcpp(), AcppBuilder removed
 * 17.4.19: renamed ModelPreProcessor->GltfProcessor
 * <p>
 * Created by thomass on 11.04.17.
 */
public class GltfProcessor {
    public static Platform platform = ToolsPlatform.init();
    static Log logger = Platform.getInstance().getLog(GltfProcessor.class);

    public static void main(String[] argv) {
        try {
            runMain(argv);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Extracted to be testable without calling exit()
     */
    public static void runMain(String[] argv) throws Exception {

        if (argv.length == 2 && argv[0].equals("-dump")) {
            dumpGltf(argv[1]);

        } else {
            if (argv.length != 4 && argv.length != 6) {
                usage();
            }
            if (!argv[1].equals("-o")) {
                usage();
            }
            String opt = argv[0];
            String outdir = argv[2];
            if (opt.equals("-gltf")) {
                Optional<String> loaderClass = Optional.empty();
                if (argv.length == 6) {
                    if (!argv[4].equals("-l")) {
                        usage();
                    }
                    loaderClass = Optional.ofNullable(argv[5]);
                }
                convertToGltf(argv[3], outdir, loaderClass);
            } else {
                usage();
            }
        }
    }

    private static void usage() {
        System.out.println("usage: GltfProcessor -gltf -o <destinationdirectory> <file> [-l loaderclass ]");
        System.out.println("usage: GltfProcessor -dump <gltffile>");
        System.exit(1);
    }

    private static void convertToGltf(String inputfile, String outdir, Optional<String> loaderClass) throws
            IOException {
        String basepath = StringUtils.substringBeforeLast(inputfile, "/");
        String basename = StringUtils.substringAfterLast(inputfile, "/");
        basename = StringUtils.substringBeforeLast(basename, ".");
        GltfBuilder gltfbuilder = new GltfBuilder();
        GltfBuilderResult gltf = null;
        try {
            AbstractLoaderBuilder customLoader = null;
            if (loaderClass.isPresent()) {
                customLoader = buildDynamicLoader(loaderClass.get());
            }
            // AC world will be ignored.
            gltf = gltfbuilder.process(loadBySuffix(inputfile, true, customLoader));
        } catch (InvalidDataException e) {
            System.out.println("InvalidDataException:" + e.getMessage());
            return;
        }
        writeGltfOutput(outdir, basename, gltf.gltfstring, gltf.bin);
    }

    private static void dumpGltf(String gltffilename) throws ResourceNotFoundException, IOException {
        String basepath = StringUtils.substringBeforeLast(gltffilename, "/");
        String basename = StringUtils.substringAfterLast(gltffilename, "/");
        basename = StringUtils.substringBeforeLast(basename, ".");
        File bininput = new File(basepath + "/" + basename + ".bin");
        String gltfinput = FileUtils.readFileToString(new File(gltffilename));
        try {
            SimpleByteBuffer binbuffer = new SimpleByteBuffer(FileUtils.readFileToByteArray(bininput));
            LoaderGLTF loaderGLTF = new LoaderGLTF(gltfinput, binbuffer, null, "");
            PortableModelList pml = loaderGLTF.ploadedfile;
            for (int i = 0; i < pml.getObjectCount(); i++) {
                PortableModelDefinition obj = pml.getObject(i);
                dumpObject(obj);

            }
        } catch (InvalidDataException e) {
            System.out.println("InvalidDataException:" + e.getMessage());
            return;
        }
    }

    private static void dumpObject(PortableModelDefinition obj) {
        System.out.println("Node " + obj.name);
        for (int j = 0; j < obj.geolist.size(); j++) {
            dumpGeo(obj.geolist.get(j), obj.geolistmaterial.get(j));
        }
        System.out.println("Kids " + obj.kids.size());
        for (int i = 0; i < obj.kids.size(); i++) {
            dumpObject(obj.kids.get(i));
        }
    }

    private static void dumpGeo(SimpleGeometry geo, String material) {
        //no name in geo
        System.out.println("Mesh ");
        System.out.println("Material " + material);
        Vector3Array vertices = geo.getVertices();
        Vector2Array uvs = geo.getUvs();
        System.out.println("Vertices");
        for (int i = 0; i < vertices.size(); i++) {
            Vector3 v = vertices.getElement(i);
            Vector2 uv = uvs.getElement(i);
            System.out.print(String.format("%10.4f %10.4f %10.4f %8.4f %8.4f", v.getX(), v.getY(), v.getZ(), uv.x, uv.y).replaceAll(",", "."));
            if (geo.getNormals() != null) {
                v = geo.getNormals().getElement(i);
                System.out.print(String.format("   %6.4f %6.4f %6.4f", v.getX(), v.getY(), v.getZ(), uv.x, uv.y).replaceAll(",", "."));
            }
            System.out.println("");
        }
        System.out.println("Indices");
        for (int i = 0; i < geo.getIndices().length; i++) {
            System.out.println(geo.getIndices()[i]);
        }
    }

    /**
     * @param outdir
     * @param basename
     * @param json
     * @param bindata
     * @throws IOException
     */
    public static void writeGltfOutput(String outdir, String basename, String json, byte[] bindata) throws
            IOException {
        String destinationpath = outdir;//basepath + "/" + basename + "-gltf";
        String destfile = destinationpath + "/" + basename + ".gltf";
        //Files.createDirectory(Paths.get(destinationpath));
        // bewusst utf-8 verwenden
        Files.write(Paths.get(destfile), json.getBytes("utf-8"));
        destfile = destinationpath + "/" + basename + ".bin";
        Files.write(Paths.get(destfile), bindata);

    }

    private static AbstractLoaderBuilder buildDynamicLoader(String loaderClass) {
        try {
            logger.debug("Building loader from " + loaderClass);
            Class clazz = Class.forName(loaderClass);
            Constructor constructor = clazz.getConstructor(new Class[]{});
            Object instance = constructor.newInstance(new Object[]{});
            return (AbstractLoaderBuilder) instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 28.12.17: Duplicated from LoaderRegistry.
     * 8.9.23: Using a LoaderRegistry only sounds good. Model loader might have complex setup
     * (eg. ac texturepath, btg matlib) and the use case often needs to know what type of model
     * it is loading for providing all needed information.
     *
     * @throws InvalidDataException
     */
    private static PortableModelList loadBySuffix(String filename, boolean ignoreacworld, AbstractLoaderBuilder customLoader) throws
            InvalidDataException {
        //String filename = file.getName();
        String extension;// = file.getExtension();
        extension = StringUtils.substringAfterLast(filename, ".");
        byte[] ins = new byte[0];
        try {
            ins = new DefaultResourceReader().loadBinaryFile(filename);
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (customLoader != null && customLoader.supports(extension)) {
            AbstractLoader loader = customLoader.buildAbstractLoader(ins, filename);
            return loader.preProcess();
        }
        if (extension.equals("3ds")) {
            //22.8.21 TODO 3DS plugin
            Util.notyet();
            /*AbstractLoader loader = new Loader3DS(new ByteArrayInputStream(new SimpleByteBuffer(ins)));
            // Bei einem Fehler ist er schon ausgestiegen
            PortableModelList ppfile = loader.preProcess();
            return ppfile;*/
        }
        if (extension.equals("ac")) {

            AbstractLoader loader = new LoaderAC(new StringReader(new String(ins)), ignoreacworld);
            // Bei einem Fehler ist er schon ausgestiegen
            // TO DO Wenn das ac in einem jar lag, muss der texturepath auch noch den bundlkepfad enthalten!
            // 6.12.17: Von wann ist das denn? brauchts das wirklich? mal ohne versuchen. Nein, das geht nicht bei readerwriterstg.
            // 28.12.17: ob das jetzt so aber geht?
            loader.loadedfile.texturebasepath = new ResourcePath(StringUtils.substringBeforeLast(filename, "/"));//.file.getPath();
            PortableModelList ppfile = loader.preProcess();
            return ppfile;
        }

        if (extension.equals("btg")) {
            Util.notyet();
            //return loadBTG(ins, usematlib);
        }
        //6.12.17: wegen extension einfach mal annegebn, dass jedes gz ein btg ist
        if (extension.equals("gz")) {
            //TODO ueber loadBTG
            Util.notyet();
            //AbstractLoader loader = new LoaderBTG((ins.b), null, boptions);
            return null;//loader.preProcess();
        }
        // GLTF nicht laden, denn das soll ja gebaut werden.
        logger.warn("unknown suffix " + extension);
        return null;
    }

    /*TODO create BTG plugin
    private static PortableModelList loadBTG(byte[] ins, boolean usematlib) throws InvalidDataException {
        AbstractLoader loader;
        if (usematlib) {
            if (matlib == null) {
                NativeResourceReader rm = new DefaultResourceReader();
                SyncBundleLoader.loadBundleSyncInternal(BundleRegistry.FGROOTCOREBUNDLE, null, false,  rm);
                //BundleLoaderExceptGwt.loadBundleSyncInternal(BundleRegistry.FGHOMECOREBUNDLE, null, false, null);
                SyncBundleLoader.loadBundleSyncInternal(SGMaterialLib.BUNDLENAME, null, false,  rm);
                matlib = FlightGear.loadMatlib();
            }
            if (matlib == null) {
                throw new RuntimeException("matlib.load failed");
            }

            loader = new LoaderBTG(((new ByteArrayInputStream(new SimpleByteBuffer(ins)))), null, new LoaderOptions(matlib), "source");
        } else {
            loader = new LoaderBTG(((new ByteArrayInputStream(new SimpleByteBuffer(ins)))), null, null, "source");
        }
        return loader.preProcess();
    }*/


}
