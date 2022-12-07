package de.yard.threed.tools;

import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.AsyncBundleLoader;
import de.yard.threed.core.*;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.ResourceNotFoundException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.loader.*;
import de.yard.threed.engine.platform.common.SimpleGeometry;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.outofbrowser.NativeResourceReader;
import de.yard.threed.outofbrowser.SyncBundleLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Reads a model file and converts it to GLTF.
 * Also for dumping a GLTF and corresponding bin file
 * 17.4.19: umbenannt ModelPreProcessor->GltfProcessor
 * <p>
 * Created by thomass on 11.04.17.
 */
public class GltfProcessor {
    public static Platform platform = ToolsPlatform.init();
    static Log logger = Platform.getInstance().getLog(GltfProcessor.class);
    //22.8.21 static SGMaterialLib matlib = null;

    public static void main(String[] argv) {
        try {
            if (argv.length == 2 && argv[0].equals("-dump")) {
                dumpGltf(argv[1]);

            } else {
                if (argv.length != 4) {
                    usage();
                }
                if (!argv[1].equals("-o")) {
                    usage();
                    ;
                }
                String opt = argv[0];
                String outdir = argv[2];
                if (opt.equals("-acpp")) {
                    //22.12.18: Das ist Asbach
                    Util.nomore();


                    //preprocessAcpp(argv[3]);
                } else if (opt.equals("-gltf")) {
                    preprocessGltf(argv[3], outdir);
                } else {
                    usage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static void usage() {
        System.out.println("usage: GltfProcessor -gltf <file>");
        System.out.println("usage: GltfProcessor -dump <gltffile>");
        System.exit(1);
    }

    /**
     * 8.12.18: AcppBuilder ist doch wohl deprecated
     *
     * @param acfile
     * @throws ResourceNotFoundException
     * @throws InvalidDataException
     * @throws IOException
     */
    /*3.1.19 private static void preprocessAcpp(String acfile) throws ResourceNotFoundException, InvalidDataException, IOException {
        Util.nomore();
        /*LoadedFile ppfile = new AcppBuilder().process(acfile);
        String destination = acfile + "pp";
        NativeOutputStream outs = new JAOutputStream(destination);
        ppfile.serialize(outs);
        outs.close();* /
    }*/
    private static void preprocessGltf(String acfile, String outdir) throws ResourceNotFoundException, IOException {
        String basepath = StringUtils.substringBeforeLast(acfile, "/");
        String basename = StringUtils.substringAfterLast(acfile, "/");
        basename = StringUtils.substringBeforeLast(basename, ".");
        GltfBuilder gltfbuilder = new GltfBuilder(false);
        GltfBuilderResult gltf = null;
        try {
            gltf = gltfbuilder.process(acfile);
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
            dumpGeo(obj.geolist.get(j), obj.geolistmaterial.get(j) );
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
     * 3.1.19:TODO Methode in anderer Klasse unterbringen
     *
     * @param outdir
     * @param basename
     * @param json
     * @param bindata
     * @throws IOException
     */
    public static void writeGltfOutput(String outdir, String basename, String json, byte[] bindata) throws IOException {
        String destinationpath = outdir;//basepath + "/" + basename + "-gltf";
        String destfile = destinationpath + "/" + basename + ".gltf";
        //Files.createDirectory(Paths.get(destinationpath));
        // bewusst utf-8 verwenden
        Files.write(Paths.get(destfile), json.getBytes("utf-8"));
        destfile = destinationpath + "/" + basename + ".bin";
        Files.write(Paths.get(destfile), bindata);

    }

    /**
     * 28.12.17: Aus LoaderRegistry hierhin dupliziert.
     *
     * @throws InvalidDataException
     */
    public static PortableModelList/*21.12.17 de.yard.threed.engine.Loader/*ResourceProcessor*/ findLoaderBySuffix(/*BundleResource file, BundleData ins,*/String filename, boolean ignoreacworld, boolean usematlib) throws InvalidDataException {
        //String filename = file.getName();
        String extension;// = file.getExtension();
        extension = StringUtils.substringAfterLast(filename, ".");
        byte[] ins = new byte[0];
        try {
            ins = new DefaultResourceReader().loadBinaryFile(filename);
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
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
