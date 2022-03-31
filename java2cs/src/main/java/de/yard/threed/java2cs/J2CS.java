package de.yard.threed.java2cs;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Very basic Java to CS syntax converter.
 * Based on Pat Niemeyers Java to Swift converter (https://github.com/patniemeyer/j2swift)
 *
 */
public class J2CS {
    Logger logger = Logger.getLogger(J2CS.class);
    public static final String CSMAGIC = "/*generated*/\n";
    public static final String NASALMAGIC = "#generated\n";
    int errorcnt = 0;

    J2CS(Path src, String[] files2convert, Path target, String[] excludes, KnownInterfaces knownInterfaces, KnownGenericMethods knownGenericMethods, boolean force, boolean nasal) {
        logger.info("started");
        for (String file2convert : files2convert) {
            // This boilerplate largely from the ANTLR example
            InputStream is = null;
            try {
                Path sourcefile = src.resolve(file2convert);
                if (Files.isDirectory(sourcefile)) {
                    List<String> javafiles = listJavaFiles(sourcefile);
                    for (String javafile : javafiles) {
                        if (nasal) {
                            String targetfile = javafile.replace(".java", ".nas");
                            //Der Sourcepath als Destination wird ignoriert, da alles ins selbe target kommt.
                            convertSingleFile(sourcefile.resolve(javafile), target.resolve(targetfile), excludes, knownInterfaces, force, knownGenericMethods, nasal);
                        } else {
                            String targetfile = javafile.replace(".java", ".cs");
                            convertSingleFile(sourcefile.resolve(javafile), target.resolve(file2convert).resolve(targetfile), excludes, knownInterfaces, force, knownGenericMethods, nasal);
                        }
                    }
                } else {
                    if (nasal) {
                        String targetfile = StringUtils.substringAfterLast(file2convert.replace(".java", ".nas"), "/");
                        convertSingleFile(sourcefile, target.resolve(targetfile), excludes, knownInterfaces, force, knownGenericMethods, nasal);
                    } else {
                        String targetfile = file2convert.replace(".java", ".cs");
                        convertSingleFile(sourcefile, target.resolve(targetfile), excludes, knownInterfaces, force, knownGenericMethods, nasal);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (errorcnt > 0) {
            logger.error(errorcnt + " errors detected");
        } else {
            logger.info("completed");
        }
    }

    private void convertSingleFile(Path src, Path target, String[] excludes, KnownInterfaces knownInterfaces, boolean force, KnownGenericMethods knownGenericMethods, boolean nasal) throws IOException {
        for (String exclude : excludes) {
            if (src.toFile().getCanonicalPath().endsWith(exclude)) {
                logger.info("Skipping excluded file " + exclude);
                return;
            }
        }
        if (src.toFile().exists() && target.toFile().exists()) {
            if (modtime(target) > modtime(src) && !force) {
                //logger.debug("File " + src.toString() + " unchanged. Skipping.");
                return;
            }
        }
        String filename = src.toString();
        logger.info("Converting " + filename + " to " + target);
        if (!filename.endsWith(".java")) {
            throw new RuntimeException("no java file:" + filename);
        }
        InputStream is = Files.newInputStream(src);
        ANTLRInputStream input = new ANTLRInputStream(is);
        Java8Lexer lexer = new Java8Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        //soll es schneller machen, fuehrt aber nur zu Problemen
        // parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        ParseTree tree = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        J2CSListener swiftListener = new J2CSListener(tokens, knownInterfaces, knownGenericMethods, nasal);

        walker.walk(swiftListener, tree);
        String result = ((nasal) ? NASALMAGIC : CSMAGIC) + swiftListener.rewriter.getText();
        // Namespace schliessen
        result += "}";
        swiftListener.checkforReservedword(tree);
        result = handleUnityComments(result);
        if (nasal) {
            result = handleNasalComments(result);
            result = handleEmptyLines(result);
        }
        result = handleMisc(filename, result);
        //System.out.println(result);
        if (swiftListener.errors.size() > 0) {
            errorcnt += swiftListener.errors.size();
        } else {
            boolean success = SaveFileWriter.saveContentToFile(target, result.getBytes("UTF-8"), ((nasal) ? NASALMAGIC : CSMAGIC));
            if (!success) {
                System.err.println("Converted file not saved. Exiting");
                System.exit(1);
            }
        }
    }

    /**
     * Wofuer ist das?
     * <p>
     * Erstmal als Q&D Ansatz.
     *
     * @param result
     * @return
     */
    private String handleUnityComments(String result) {
        result = result.replaceAll("/\\*Unity", "");
        result = result.replaceAll("Unity\\*/", "");
        return result;
    }

    /**
     * Erstmal alle raus.
     */
    private String handleNasalComments(String result) {
        // removes also comment in literals? TODO check
        result = result.replaceAll("/\\*.*\\*/", "");
        //result = result.replaceAll("/\\*\\*.*\\*/", "");
        result = result.replaceAll("//.*?\n", "\n");
        result = result.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
        return result;
    }

    private String handleEmptyLines(String result) {
        //result = result.replaceAll ("^[ |\t]*\n${2,}", "");
        result = result.replaceAll("(?m)^[ \\t]*\\r?\\n{2,}", "");
        return result;
    }

    /**
     * Es gibt Konstruktionen, die sich bisher einer allgemeingültigen Konvertierung entziehen. Die sind hier fest
     * verdrahtet.
     * <p>
     * Erstmal als Q&D Ansatz.
     *
     * @param result
     * @return
     */
    private String handleMisc(String file, String result) {
        if (file.endsWith("de/yard/threed/fg/CppHashMap.java")) {
            result = result.replace("public T2 get(Object key)", "public T2 get(T1 key)");
        }
        if (file.endsWith("de/yard/threed/core/Util.java")) {
            //TODO 6.7.20: geht jetzt generisch?
            result = result.replace("public static <T> List<T> buildList(", "public static List<T> buildList<T>(");
        }
        /*if (file.endsWith("de/yard/threed/platform/DefaultPlatform.java")) {
            result = result.replace("public <T> T parseJsonToModel(", "public T parseJsonToModel(");
            result = result.replace("return null;//C# likes 'return default(T);'", "return default(T);");
        }
        if (file.endsWith("de/yard/threed/platform/EnginePlatform.java")) {
            result = result.replace("public abstract <T> T parseJsonToModel(", "public abstract T parseJsonToModel(");
        }*/
        /*if (file.endsWith("de/yard/threed/platform/commonexceptgwt/AirportDataProviderMock.java")) {
            result = result.replace("<Airport>modelToJson(ap)", "modelToJson<Airport>(ap)");
        }
        if (file.endsWith("de/yard/threed/traffic/GroundServicesSystem.java")) {
            result = result.replace("<Airport>parseJsonToModel(", "parseJsonToModel<Airport>(");
        }*/
        if (file.endsWith("de/yard/threed/traffic/model/Airport.java")) {
            result = result.replace("public class Airport {", "[Serializable]public class Airport {");
            result = result.replaceAll("private", "public");
        }
        if (file.endsWith("de/yard/threed/traffic/model/Runway.java")) {
            result = result.replace("public class Runway {", "[Serializable]public class Runway {");
            result = result.replaceAll("private", "public");
        }
       /* if (file.endsWith("de/yard/threed/fg/simgear.scene.model/ModelRegistryCallbackProxy.java")) {
            result = result.replace("T extends ReadFileCallback", "T");
        }
        if (file.endsWith("de/yard/threed/fg/osgdb/RegisterReaderWriterProxy.java")) {
            result = result.replace("T extends ReaderWriter", "T");
        }*/

        // 22.6.20: Manchmal entfernt Idea(?) einzelne Blanks aus den CS reffiles. Dann scheitert der Test. Darum sowas rausnehmen.
        // from https://stackoverflow.com/questions/4123385/remove-all-empty-lines
        result = result.replaceAll("(?m)^ $\n", "\n");

        return result;
    }

    private long modtime(Path file) throws IOException {
        FileTime modtime = Files.getLastModifiedTime(file);
        return modtime.toMillis();
    }

    public static void main(String[] args) throws Exception {
        J2CS j2s = main1(args, buildKnownInterfaces(), buildKnownGenericMethods());

        if (j2s.errorcnt > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }

    }

    /**
     * Entkoppelt um sie aus Tests aufrufen zu koennen.
     *
     * @param args
     * @param knownGenericMethods
     * @return
     * @throws Exception
     */
    public static J2CS main1(String[] args, KnownInterfaces knownInterfaces, KnownGenericMethods knownGenericMethods) throws Exception {

        Path src = null, target = null;

        Options options = new Options();
        Option srcoption = Option.builder("src")
                .required(true)
                .hasArg(true)
                .build();
        options.addOption(srcoption);
        Option targetoption = Option.builder("target")
                .required(true)
                .hasArg(true)
                .build();
        options.addOption(targetoption);
        Option excludeoption = Option.builder("exclude")
                .required(false)
                .hasArg(true)
                .build();
        options.addOption(excludeoption);
        Option forceoption = Option.builder("f")
                .required(false)
                .hasArg(false)
                .build();
        options.addOption(forceoption);
        Option nasaloption = Option.builder("nasal")
                .required(false)
                .hasArg(false)
                .build();
        options.addOption(nasaloption);

        CommandLineParser cliparser = new DefaultParser();
        CommandLine cmd = cliparser.parse(options, args);
        if (cmd.hasOption("src")) {
            src = Paths.get(cmd.getOptionValue("src"));
        }
        if (cmd.hasOption("target")) {
            target = Paths.get(cmd.getOptionValue("target"));
        }
        String[] excludes = new String[0];
        if (cmd.hasOption("exclude")) {
            String excludestring = cmd.getOptionValue("exclude");
            if (excludestring.length() > 0) {
                excludes = excludestring.split(",");
            }
        }
        boolean force = cmd.hasOption('f');
        boolean nasal = cmd.hasOption("nasal");
        String[] files2convert = cmd.getArgs();

        J2CS j2s = new J2CS(src, files2convert, target, excludes, knownInterfaces, knownGenericMethods, force, nasal);
        return j2s;
    }

    public static List<String> listJavaFiles(Path directory) throws IOException {
        List<String> fileNames = new ArrayList<String>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);

        for (Path path : directoryStream) {
            String name = path.getFileName().toString();
            if (name.endsWith(".java")) {
                fileNames.add(name);
            }
        }
        return fileNames;
    }

    private static KnownInterfaces buildKnownInterfaces() {
        KnownInterfaces knownInterfaces = new KnownInterfaces();
        knownInterfaces.add("SceneUpdater", new String[]{"update"});
        knownInterfaces.add("Entity", new String[]{"getMover"});
        knownInterfaces.add("ResourceLoadingListener", new String[]{"onLoad", "onProgress", "onError"});
        knownInterfaces.add("AnimatedModel", new String[]{"getAnimations", "processAnimationStep"});
        knownInterfaces.add("AnimationListener", new String[]{"animationCompleted"});
        knownInterfaces.add("SGSubsystem", new String[]{"init", "postinit", "reinit", "shutdown", "bind", "unbind", "update", "suspend", "resume", "is_suspended", "reportTiming", "stamp"});
        knownInterfaces.add("GraphVisualizer", new String[]{"visualizePath", "visualizeEdge", "visualizeGraph", "getPositionOffset", "visualize"});
        knownInterfaces.add("Transform", new String[]{"setParent", "getParent", "getPosition", "getScale", "setScale"});
        knownInterfaces.add("EventNotify", new String[]{"eventPublished"});
        knownInterfaces.add("ModelBuilder", new String[]{"build"});
        knownInterfaces.add("MapProjection", new String[]{"project", "unproject", "unprojectToFlightLocation"});
        knownInterfaces.add("NativeJsonString", new String[]{"stringValue"});
        knownInterfaces.add("NativeJsonNumber", new String[]{"doubleValue", "intValue"});
        //NativeJsonObject erkennt er wohl nicht TODO
        knownInterfaces.add("NativeJsonObject", new String[]{"getInt", "getString"});
        knownInterfaces.add("NativeJsonArray", new String[]{"get", "size"});
        //ist eigentlich eine abstrakte Klasse
        //geht nicht knownInterfaces.add("OriginMapProjection", new String[]{"calcPos"});
        knownInterfaces.add("GenericControlPanel", new String[]{"checkForClickedArea"});
        knownInterfaces.add("Menu", new String[]{"checkForClickedArea","getNode","checkForSelectionByKey","remove"});

        //Animation ist abstrakte Klasse knownInterfaces.add("Animation", new String[]{"processAnimationStep"});
        //10.1.19: Manche sind jetzt abstract.
        knownInterfaces.add("GraphWorld", new String[]{"XXgetStartPosition", "XXgetGraph", "XXgetEngine", "XXupdate", "XXaddOutsidePositions", "XXgetVisualizer", "XXinit",
                "XXgetRunway", "XXgetGroundNet", "XXgetConfiguration"});
        knownInterfaces.add("RequestHandler", new String[]{"processRequest"});
        knownInterfaces.add("MenuBuilder", new String[]{"buildMenu", "getAttachNode", "getCamera"});
        knownInterfaces.add("DataProvider", new String[]{"getData"});
        //nutzt nicht knownInterfaces.add("AvatarMenuProvider", new String[]{"buildMenu"});

        //FunctionalInterfaces
        knownInterfaces.add("RotateDelegate", "point");
        knownInterfaces.add("AsyncJobDelegate", "completed");
        knownInterfaces.add("EcsGroupHandler", "processGroups");
        knownInterfaces.add("TransformNodeVisitor", "handleNode");
        knownInterfaces.add("ButtonDelegate", "buttonpressed");
        knownInterfaces.add("ModelBuildDelegate", "modelBuilt");
        knownInterfaces.add("BundleLoadDelegate", "bundleLoad");
        knownInterfaces.add("VehicleBuiltDelegate", "vehicleBuilt");
        knownInterfaces.add("VehicleLoadedDelegate", "vehicleLoaded");

        //Maze
        knownInterfaces.add("GridItem", new String[]{"getLocation", "setLocation", "getOwner", "collectedBy","isNeededForSolving","setNeededForSolving"});
        knownInterfaces.add("PointerHandler", new String[]{"processPointer", "getRequestByTrigger"});
        knownInterfaces.add("GridMover", new String[]{"getLocation", "setLocation", "getOrientation", "rotate", "setOrientation", "walk", "getParent", "setOrientation","getMoveOptions","getId", "getTeam"});

        return knownInterfaces;
    }

    private static KnownGenericMethods buildKnownGenericMethods() {
        KnownGenericMethods knowngenericmethods = new KnownGenericMethods();
        //23.6.20: asyncContentLoad ist doch gar nicht generic??
        knowngenericmethods.add("asyncContentLoad", new String[]{"PlatformAsyncCallback"});
        knowngenericmethods.add("parseJsonToModel", new String[]{"??"});
        knowngenericmethods.add("modelToJson", new String[]{"??"});
        knowngenericmethods.add("completed", new String[]{"AsyncJobDelegate"});
        return knowngenericmethods;
    }
}


class FileFinder extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private List<Path> matchedPaths = new ArrayList<Path>();

    FileFinder(String pattern) {
        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
    }

    // Compares the glob pattern against
    // the file or directory name.
    void match(Path file) {
        Path name = file.getFileName();

        if (name != null && matcher.matches(name)) {
            matchedPaths.add(name);
        }
    }

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        match(file);
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
                                             BasicFileAttributes attrs) {
        match(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }

    public int getTotalMatches() {
        return matchedPaths.size();
    }

    public Collection<Path> getMatchedPaths() {
        return matchedPaths;
    }

} // class FileFinder ends
