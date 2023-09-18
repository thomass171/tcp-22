package de.yard.threed.tools;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.ModelCreateException;
import de.yard.threed.core.geometry.ProceduralModelCreator;

import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Log;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.lang.reflect.InvocationTargetException;

/**
 * Procedureally create a model file (GLTF+bin file). These are stored to the specified outdir.
 * <p>
 * Created by thomass on 03.01.19.
 */
public class ModelCreator {
    public static Platform platform = ToolsPlatform.init();
    static Log logger = Platform.getInstance().getLog(ModelCreator.class);

    // intended not the final destination directory. Result file should be validated before moving.
    public static String outputdir = ".";


    private ModelCreator() {

    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("c", "class", true, "class name of builder");
        options.addOption("n", "name", true, "model and file name");

        //options.addOption("c", "config", true, "configuration file suffix, eg. poc");
        options.addOption("o", "outdir", true, "output directory");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            usage();
        }

        String classname = cmd.getOptionValue("c");
        String name = cmd.getOptionValue("n");

        if (classname == null || name == null) {
            usage();
        }
        if (cmd.getOptionValue("o") != null) {
            outputdir = cmd.getOptionValue("o");
        }

        String remaingArgs[] = cmd.getArgs();
        try {
            GltfBuilderResult gltf = createModel(name, classname, remaingArgs);
            new GltfProcessor().writeGltfOutput(outputdir, name, gltf.gltfstring, gltf.bin);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static void usage() {
        System.err.println("usage:TODO");
        System.exit(1);
    }

    /**
     * separated for testing.
     *
     * @param name
     * @param classname
     * @return
     */
    public static GltfBuilderResult createModel(String name, String classname, String[] remainingArgs) throws ModelCreateException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("createModel: classname='" + classname + "',remainingArgs=" + remainingArgs);
        PortableModelList pml;
        try {
            Class clazz = Class.forName(classname);
            ProceduralModelCreator pmc;
            if (remainingArgs == null || remainingArgs.length == 0) {
                pmc = (ProceduralModelCreator) clazz.newInstance();
            } else {
                //TODO auch andere
                pmc = (ProceduralModelCreator) clazz.getDeclaredConstructor(remainingArgs.getClass()/*String.class, String.class*/)
                        .newInstance(new Object[] { remainingArgs});
            }
            pml = pmc.createModel();
        } catch (NoSuchMethodException | InvocationTargetException e) {
            logger.error("",e);
            throw new ModelCreateException(e.getMessage());
        }
        GltfBuilder gltfBuilder = new GltfBuilder();
        return gltfBuilder.process(pml);
    }
}