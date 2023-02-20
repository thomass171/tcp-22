package de.yard.threed.platform.homebrew;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.yard.threed.core.*;
import de.yard.threed.core.Util;
import de.yard.threed.core.buffer.NativeByteBuffer;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.resource.*;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.*;


import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.javacommon.*;

import de.yard.threed.outofbrowser.AsyncBundleLoader;
import de.yard.threed.outofbrowser.FileSystemResource;
import de.yard.threed.outofbrowser.NativeResourceReader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import de.yard.threed.outofbrowser.SyncBundleLoader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 24.4.20: A self implemented platform for example based on native OpenGL. Also for testing and scene serving.
 * <p>
 * The renderer is just a plugable option. It could be a OpenGL based renderer or even no renderer.
 * <p>
 * Without renderer this is a headleass platform like {@link SimpleHeadlessPlatform}, but more complete.
 * This platform is only for Java desktop environments, no C# nor wegbgl. So no limitation to Java.
 * For consistency and simplicity everything is in the same thread, but might be async.
 *
 * Ausnahme evtl. im ResourceManager? Das will gut durchdacht sein.
 * Besser wäre, wenn es das nicht gibt. Synchrone Aktionen sollten komplett ausserhalb der Platform inkl. ResourceManager liegen.
 * <p>
 * Die ganzen OpenGl Klassen muessten auch umbenannt werden? Zumindest zum Teil; muss halt etwas getrennt werden vom Rendering.
 * <p>
 * 18.2.23: The idea of a defaulttexture was been removed a long time ago (replaced by wirefram?)
 * <p/>
 * Created by thomass on 20.07.15.
 */
public class PlatformHomeBrew extends DefaultPlatform {
    // Set in constructor. Cannot be used before factory is set.
    private Log logger;
    public NativeResourceReader resourcemanager;
    // 31.10.17: Texturelist for really found/loaded textures.
    public Map<String, NativeTexture> texturemap = new HashMap<>();
    public List<String> texturelist = new ArrayList<String>();
    public String hostdir;
    // Render needs to be here instead of runner because it cannot be looked up in runner due to possibly various runner.
    public HomeBrewRenderer renderer;
    // 6.2.23 switch to use configuration
    private Configuration configuration;

    private PlatformHomeBrew(Configuration configuration) {
        //21.7.21 jetzt hier
        eventBus = new JAEventBus();
        logfactory = new JALogFactory();
        this.configuration = configuration;
        logger = logfactory.getLog(PlatformHomeBrew.class);
    }

    /**
     * Siehe platformjme.
     * 11.12.17: Die instance nur einmalig anzulegen kann dazu fuehren, dass andere Tests, die auch einen init() aufrufen, ihre Properties nicht haben.
     * Darum immer neu anlegen. Komisch, dass das bisher noch nie ein Problem war.
     *
     * @return
     */
    public static PlatformInternals init(Configuration configuration/*HashMap<String, String> properties*/) {
        // 6.2.23 Since configuration system properties is deprecated
        /*for (String key : properties.keySet()) {
            //System.out.println("transfer of propery "+key+" to system");
            System.setProperty(key, properties.get(key));
        }*/
        instance = new PlatformHomeBrew(configuration);

        ((PlatformHomeBrew) instance).resourcemanager = new DefaultResourceReader();
        PlatformInternals platformInternals = new PlatformInternals();

        ((PlatformHomeBrew) instance).hostdir = configuration.getString("HOSTDIR");
        if (((PlatformHomeBrew) instance).hostdir == null) {
            throw new RuntimeException("HOSTDIR not set");
        }
        ((PlatformHomeBrew) instance).resourcemanager = new DefaultResourceReader();
        instance.bundleResolver.add(new SimpleBundleResolver(((PlatformHomeBrew) instance).hostdir + "/bundles", ((PlatformHomeBrew) instance).resourcemanager));
        instance.bundleResolver.addAll(SyncBundleLoader.buildFromPath(configuration.getString("ADDITIONALBUNDLE"), ((PlatformHomeBrew) instance).resourcemanager));
        instance.bundleLoader = new AsyncBundleLoader(new DefaultResourceReader());

        instance.nativeScene = new HomeBrewScene();
        ((PlatformHomeBrew) instance).logger.info("PlatformHomeBrew created. Working Directory = " + System.getProperty("user.dir"));

        return platformInternals;
    }

    public static PlatformInternals init(Configuration configuration, NativeEventBus eventBus) {
        PlatformInternals pl = init(configuration);
        ((PlatformHomeBrew) instance).eventBus = eventBus;
        return pl;
    }

    @Override
    public NativeSceneNode buildModel() {
        return buildModel(null);
    }

    @Override
    public NativeSceneNode buildModel(String name) {
        HomeBrewSceneNode n = new HomeBrewSceneNode(name);
        //native2nativewrapper.put(n.getUniqueId(), n);
        return n;
    }

    /**
     * Not multithreaded, but async by AsyncHelper.
     *
     * @param filename
     */
    @Override
    public void buildNativeModelPlain(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {

        AsyncHelper.asyncModelBuild(filename, opttexturepath, options, AbstractSceneRunner.getInstance().invokeLater(delegate));
    }

    /*4.8.21 public void /*Bundle* / loadBundle(String bundlename, /*AsyncJobCallback* /BundleLoadDelegate delegate, boolean delayed) {
        AsyncHelper.asyncBundleLoad(bundlename, AbstractSceneRunner.getInstance().invokeLater(delegate), delayed);
    }*/

    /*@Override
    public void sendHttpRequest(String url, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate ) {
        httpClient.sendHttpRequest(url,method,header,asyncJobDelegate);
    }*/

    @Override
    public List<NativeSceneNode> findSceneNodeByName(String name) {
        List<NativeSceneNode> l = findNodeByName(name, ((HomeBrewScene) nativeScene/*AbstractSceneRunner.getInstance().scene*/).getRootTransform(), true);
        return l;
    }

    /*29.12.17 private void buildNativeModelInternal(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate delegate) {
        ModelBuildResult r = ModelLoader.buildModelFromBundle(filename,opttexturepath);
        delegate.modelBuilt(r);
    }*/

    @Override
    public NativeMesh buildNativeMesh(NativeGeometry geometry, NativeMaterial material, boolean castShadow, boolean receiveShadow/*, boolean isLine*/) {
        HomeBrewGeometry geo = (HomeBrewGeometry) geometry;
        HomeBrewMesh mesh = HomeBrewMesh.buildMesh(geo, material, castShadow, receiveShadow);

        return mesh;
    }

    /**
     * Tja. //TODO Fix this mess (Kokolores).
     */
    @Override
    public NativeSceneNode buildLine(Vector3 from, Vector3 to, Color color) {
        Vector3Array vertices = buildVector3Array(2);
        vertices.setElement(0, from);
        vertices.setElement(1, to);
        int[] indices = new int[3];
        indices[0] = 0;
        indices[1] = 1;
        indices[2] = 0;
        Vector2Array uvs = buildVector2Array(2);
        uvs.setElement(0, new Vector2());
        uvs.setElement(1, new Vector2());
        Vector3Array normals = buildVector3Array(2);
        normals.setElement(0, new Vector3());
        normals.setElement(1, new Vector3());
        HomeBrewGeometry geo = HomeBrewGeometry.buildGeometry(vertices, indices, uvs, normals);
        HashMap<ColorType, Color> colors = new HashMap<>();
        colors.put(ColorType.MAIN, color);
        MaterialDefinition materialDefinition = new MaterialDefinition("name", colors, null, null);

        HomeBrewMaterial material = null;
        material = HomeBrewMaterial.buildMaterial(renderer.getGlContext(), materialDefinition, null);
        HomeBrewMesh linemesh = new HomeBrewMesh(geo, material, false, false);
        HomeBrewSceneNode n = new HomeBrewSceneNode((String) null);
        n.setMesh(linemesh);
        return n;
    }

    @Override
    public void updateMesh(NativeMesh mesh, NativeGeometry geometry, NativeMaterial material) {
        ((HomeBrewMesh) mesh).updateMesh(geometry, material);
    }

    @Override
    public NativeByteBuffer buildByteBuffer(int size) {
        SimpleByteBuffer buf = new SimpleByteBuffer(new byte[size]);
        return buf;
    }

    public NativeCamera buildPerspectiveCamera(double fov, double aspect, double near, double far) {
        HomeBrewPerspectiveCamera camera = new HomeBrewPerspectiveCamera(fov, aspect, near, far);
        return camera;
    }

    @Override
    public NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap<String, NativeTexture> texture, HashMap<NumericType, NumericValue> parameter, Object/*Effect*/ effect) {
        MaterialDefinition materialDefinition = new MaterialDefinition(name, color, texture, parameter);
        return HomeBrewMaterial.buildMaterial(renderer.getGlContext(), materialDefinition, (Effect) effect);
    }

    /**
     * @return
     */
    private NativeTexture buildNativeTextureOpenGL(NativeResource filename, HashMap<NumericType, NumericValue> params) {
        //default kein Repeat
        int mode = 1;
        NumericValue wraps = params.get(NumericType.TEXTURE_WRAP_S);
        if (wraps != null) {
            if (wraps.equals(NumericValue.REPEAT))
                mode = 0;
        }
        NumericValue wrapt = params.get(NumericType.TEXTURE_WRAP_T);
        if (wrapt != null) {
            if (wrapt.equals(NumericValue.REPEAT))
                mode = 0;
        }
        if (filename.getFullName().contains("yoke")) {
            int h = 9;
        }
        OpenGlTexture tex = OpenGlTexture.loadFromFile(renderer.getGlContext(), filename, mode);
        if (tex == null) {
            logger.warn("Loading texture " + filename.getFullName() + " failed. Using default");
            return null;
        }
        texturelist.add(filename.getFullName());
        texturemap.put(filename.getFullName(), tex);
        return tex;
    }

    @Override
    public NativeTexture buildNativeTexture(BundleResource filename, HashMap<NumericType, NumericValue> parameters) {
        if (filename.bundle == null) {
            logger.error("buildNativeTexture:bundle not set for file " + filename.getFullName());
            return null;
        }
        //String bundlebasedir = BundleRegistry.getBundleBasedir(filename.bundle.name, false);
        String bundlebasedir = BundleResolver.resolveBundle(filename.bundle.name, Platform.instance.bundleResolver).getPath();
        //4.7.21: TODO fix FileSystemResource dependency.
        // 23.7.21: Wird der Zwischenschritt ueberhaupt gebraucht? 2
        // 6.7.21: Eigentlich wohl nicht, z.Z. aber schon. Zieht sich total durch.

        FileSystemResource resource = FileSystemResource.buildFromFullString(bundlebasedir + "/" + filename.getFullName());
        NativeTexture t = buildNativeTextureOpenGL(resource, parameters);
        return t;
    }

    @Override
    public NativeTexture buildNativeTexture(ImageData imagedata, boolean fornormalmap) {
        return OpenGlTexture.buildFromImage(renderer.getGlContext(), imagedata);
    }

    @Override
    public NativeTexture buildNativeTexture(NativeCanvas imagedata) {
        return null;
    }

  /*  @Override
    public NativeMaterial buildBasicMaterial(/*Native* /Color colorargb) {
        return OpenGlMaterial.buildBasicMaterial(colorargb, null);
    }

    @Override
    public NativeMaterial buildBasicMaterial(NativeTexture texture) {
        return OpenGlMaterial.buildBasicMaterial(null, texture);
    }*/

    @Override
    public NativeLight buildPointLight(Color argb, double range) {
        return OpenGlLight.buildPointLight(argb);
    }

    @Override
    public NativeLight buildAmbientLight(Color argb) {
        return OpenGlLight.buildAmbientLight(argb);
    }

    @Override
    public NativeLight buildDirectionalLight(Color argb, Vector3 direction) {
        return OpenGlLight.buildDirectionalLight(argb, direction);
    }


    @Override
    public NativeGeometry buildNativeGeometry(Vector3Array vertices, /*List<* /Face3List*/int[] indices, Vector2Array uvs, Vector3Array normals) {
        return HomeBrewGeometry.buildGeometry(vertices, indices, uvs, normals);
    }

    @Override
    public Log getLog(Class clazz) {
        //JALog gibt es hier nicht. Über Modul desktop muesste es aber die Logfactory geben. Fuer Tests halt nicht.
        if (logfactory != null) {
            return logfactory.getLog(clazz);
        }
        return logger;//logfactory.getLog(clazz);
    }

    /*@Override
    public ResourceManager getRessourceManager() {
        return resourcemanager;
    }*/

    /**
     * Ob hier vielleicht ein Inputstream als Input guenstiger ist? Kennt der GWT Ressourceloader
     * aber nicht.
     *
     * @param xmltext
     * @return
     * @throws
     */
    @Override
    public NativeDocument parseXml(String xmltext) throws XmlException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader((xmltext))));
            return new JavaXmlDocument(doc);
        } catch (Exception e) {
            throw new XmlException(e);
        }

    }

    @Override
    public NativeJsonValue parseJson(String jsonstring) {
        //Gson gson = new Gson();
        //ParsedGltf gltf = gson.fromJson(jsonstring, ParsedGltf.class);
        GsonBuilder builder = new GsonBuilder();
        LinkedTreeMap map = (LinkedTreeMap) builder.create().fromJson(jsonstring, Object.class);
        return new SimpleJsonObject(map);
    }

    /*@Override
    public <T> Object parseJsonToModel(String jsonstring, Class clazz) {
        GsonBuilder builder = new GsonBuilder();
        T model = (T) builder.create().fromJson(jsonstring, clazz);
        return model;
    }

    @Override
    public String modelToJson(Object model) {
        GsonBuilder builder = new GsonBuilder();
        return builder.create().toJson(model);
    }*/

    @Override
    public boolean isDevmode() {
        return true;
    }

    @Override
    protected Log getLog() {
        return logger;
    }

    @Override
    public NativeVRController getVRController(int index) {
        return null;
    }

    @Override
    public NativeRenderProcessor buildSSAORenderProcessor() {
        return null;
    }

    @Override
    public void addRenderProcessor(NativeRenderProcessor renderProcessor) {

    }

    /*MA36@Override
    public NativeWebClient getWebClient(String baseUrl) {
        return null;
    }*/

    @Override
    public NativeCanvas buildNativeCanvas(int width, int height) {
        return null;
    }

    @Override
    public NativeContentProvider getContentProvider(char type, String location, TestPdfDoc docid) {
        // Das mit dem Sample ist erstmal nur ne Krücke
        return new SampleContentProvider(11);
    }

    /*@Override
    public NativePdfDocument buildNativePdfDocument(String name) {
        return new  JmePdfDocument(name);
    }*/

    /*1.3.17 @Override
    public ImageData buildTextImage(String text, Color textcolor, String font, int fontsize) {
        //return OpenGlContext.getGlContext().drawString(image, text, x, y,  textcolor,  font, fontsize);
        //BufferedImage img = ImageUtil.loadImageFromFile(new BundleResource("FontMap.png"));
        //ImageData fontmap = ImageUtil.buildImageData(img);
        ImageData image = null;//ImageFactory.buildLabelImage(text, textcolor, fontmap);
        image = ImageFactory.buildSingleColor(20, 10, textcolor);
        //TODO 24.5.16 
        return image;
    }*/

    @Override
    public NativeSplineInterpolationFunction buildNativeSplineInterpolationFunction(double[] x, double[] y) {
//        SplineInterpolator si = new SplineInterpolator();
        //      PolynomialSplineFunction fct = si.interpolate(x, y);
        //    return new JmeSplineInterpolationFunction(fct);
        return (NativeSplineInterpolationFunction) Util.notyet();


    }

    @Override
    public NativeRay buildRay(Vector3 origin, Vector3 direction) {
        return new OpenGlRay(origin, direction);
    }

    @Override
    public float getFloat(byte[] buf, int offset) {
        return de.yard.threed.javacommon.Util.getFloat(buf, offset);
    }

    @Override
    public void setFloat(byte[] buf, int offset, float f) {
        de.yard.threed.javacommon.Util.setFloat(buf, offset, f);
    }

    @Override
    public double getDouble(byte[] buf, int offset) {
        return de.yard.threed.javacommon.Util.getDouble(buf, offset);
    }

    @Override
    public NativeStringHelper buildStringHelper() {
        return new JavaStringHelper();
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean GetKeyDown(int keycode) {
        return AbstractSceneRunner.getInstance().keyPressed(keycode);
    }

    @Override
    public Point getMouseClick() {
        return AbstractSceneRunner.getInstance().mouseclick;
    }

    @Override
    public Point getMousePress() {
        return AbstractSceneRunner.getInstance().mousepress;
    }

    /**
     * Kann solange abgefragt werden, wie die Taste gedrückt ist.
     *
     * @return
     */
    @Override
    public boolean GetKey(int keycode) {
        return AbstractSceneRunner.getInstance().keyStillPressed(keycode);
    }

    @Override
    public Point getMouseMove() {
        return AbstractSceneRunner.getInstance().getMouseMove();
    }

    @Override
    public String getName() {
        return "OpenGL";
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public NativeEventBus getEventBus() {
        // hinten rum wegen Abhaengigkeiten.25.4.20: Häh? Warumaus OpenGL? Weil der JAEventBus nicht in engine liegt.
        // Es gibt jetzt aber einen in der Platform.
        // OpenGL soll aber JAEventBus verwenden? TODO das muss wieder hintenrum rein.
        if (eventBus == null) {
            eventBus = new SimpleEventBus();
        }
        //28.4.20 return OpenGlContext.getGlContext().getEventBus();
        return eventBus;
    }

    @Override
    public void abort() {
        Util.notyet();
    }

    @Override
    public NativeSocket connectToServer(String server, int port) {
        return JavaSocket.build(server, port);
    }

    @Override
    public NativeScene getScene() {
        return nativeScene;
    }
}

