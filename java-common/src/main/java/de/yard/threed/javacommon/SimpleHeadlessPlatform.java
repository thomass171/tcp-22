package de.yard.threed.javacommon;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.URL;
import de.yard.threed.outofbrowser.FileSystemResource;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Simple Platform implementation eg. for unit tests. This is less than a full platform and PlatformHomeBrew without renderer but more
 * than DefaultPlatform. However clear difference is not yet defined, but there are some charateristics:
 * - has no dependency to a 3D library/engine, so has no renderer (headless!)
 * - uses typical Java implementations (eg. log4j for logging, GsonBuilder for json)
 * Therefore it resides in java-common. Not for C#.
 * <p>
 * The main difference to homebrew is, that homebrew is not suited for (unit) testing because that leads to module cycles.
 * <p>
 * Provides a logger and StringHelper. ResourceManager must be added later because it might need the LogFactory.
 * <p>
 * The name "headless" is confusing because its the super class for all(some?) Java based platforms.
 * Or the class hierarchy is confusing? Jme extends it and replaces may things. Maybe a component based approavh is better
 * for sharing coommon Java elements like {@link JavaSocket}.
 * Because its used for testing in "engine", a simple node tree (incl mesh) is useful indeed.
 * 13.9.23: Also material and texture.
 * 23.9.23: Also geometry and ray (intersections).
 * 29.9.23: Also used for replacing ToolsPlatform.
 * <p>
 * Created on 05.12.18.
 */
public class SimpleHeadlessPlatform extends DefaultPlatform {
    public String hostdir;
    public static String PROPERTY_PREFIX = "tcp22.";
    public static List<Integer> mockedKeyDownInput = new ArrayList<Integer>();
    public static List<Integer> mockedKeyUpInput = new ArrayList<Integer>();
    public static List<Point> mockedMouseMoveInput = new ArrayList<>();
    public static List<Point> mockedMouseDownInput = new ArrayList<>();
    public static List<Point> mockedMouseUpInput = new ArrayList<>();
    protected Configuration configuration;
    public List<NativeSceneNode> sceneNodes = new ArrayList<>();

    /**
     * Needs access from extending classes.
     */
    public SimpleHeadlessPlatform(NativeEventBus optionalEventbus, Configuration configuration) {

        StringUtils.init(buildStringHelper());
        if (optionalEventbus == null) {
            eventBus = new JAEventBus();
        } else {
            eventBus = optionalEventbus;
        }
        logfactory = new LevelLogFactory(configuration, new JALogFactory(), getDefaultLogLevel());
        logger = logfactory.getLog(SimpleHeadlessPlatform.class);
        nativeScene = new DummyScene();
        this.configuration = configuration;

        hostdir = configuration.getString("HOSTDIR");
        if (hostdir == null) {
            throw new RuntimeException("HOSTDIR not set");
        }
    }

    /**
     * For overriding. DEBUG because common use case is testing.
     */
    public int getDefaultLogLevel() {
        return DefaultLog.LEVEL_DEBUG;
    }

    public SimpleHeadlessPlatform(Configuration configuration) {
        this(null, configuration);
    }

    public static PlatformInternals init(Configuration configuration, NativeEventBus eventbus) {
        //System.out.println("PlatformOpenGL.init");

        //local now DummySceneNode.sceneNodes.clear();

        /*for (String key : properties.keySet()) {
            //System.out.println("transfer of propery "+key+" to system");
            System.setProperty(PROPERTY_PREFIX + key, properties.get(key));
        }*/
        instance = new SimpleHeadlessPlatform(eventbus, configuration);
        SimpleHeadlessPlatform shpInstance = (SimpleHeadlessPlatform) instance;
        //MA36 ((SimpleHeadlessPlatform)instance).resetInit();

        //((PlatformHomeBrew) instance).resourcemanager = JAResourceManager.getInstance();
        PlatformInternals platformInternals = new PlatformInternals();
        DefaultResourceReader resourceReader = new DefaultResourceReader();
        instance.bundleResolver.add(new SimpleBundleResolver(shpInstance.hostdir + "/bundles", resourceReader));
        instance.bundleResolver.addAll(SimpleBundleResolver.buildFromPath(configuration.getString("ADDITIONALBUNDLE"), resourceReader));
        //13.12.23 instance.bundleLoader = new AsyncBundleLoader(resourceReader);

        instance.logger.info("SimpleHeadlessPlatform created");
        return /*MA36 (EnginePlatform)* /instance*/platformInternals;
    }

    public static PlatformInternals init(Configuration configuration) {
        return init(configuration, null);
    }

    @Override
    public NativeSceneNode buildModel(String name) {
        NativeSceneNode n = new DummySceneNode();
        n.setName(name);
        return n;
    }

    @Override
    public NativeSceneNode buildModel() {
        return new DummySceneNode();
    }

    @Override
    public void buildNativeModelPlain(ResourceLoader resourceLoader, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {
        // No access to SceneRunner/AsyncHelper here from java-common. According to the idea of headless: Just create a dummy node
        // for the model to continue the workflow. See AdvancedHeadlessPlatform if more is needed.
        delegate.modelBuilt(new BuildResult(new DummySceneNode()));
    }

    @Override
    public NativeGeometry buildNativeGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        return new DummyGeometry(vertices, indices, uvs, normals);
    }

    @Override
    public NativeMesh buildNativeMesh(NativeGeometry nativeGeometry, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        return new DummyMesh((DummyGeometry) nativeGeometry, material);
    }

    @Override
    public List<NativeSceneNode> findSceneNodeByName(String name) {
        List<NativeSceneNode> l = new ArrayList<>();//Platform.findNodeByName(name, ((OpenGlScene) AbstractSceneRunner.getInstance().scene).getRootTransform(), true);
        for (NativeSceneNode n : sceneNodes) {
            if (n.getName() != null && n.getName().equals(name)) {
                l.add(n);
            }
        }
        return l;
    }

    @Override
    public NativeSceneNode buildLine(Vector3 from, Vector3 to, Color color) {
        return new DummySceneNode();
    }

    @Override
    public NativeCamera buildPerspectiveCamera(double fov, double aspect, double near, double far) {
        return new DummyCamera(fov, aspect, near, far);
    }

    @Override
    public NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap<String, NativeTexture> texture, HashMap<NumericType, NumericValue> parameters, Object effect) {
        return new DummyMaterial(name);
    }

    @Override
    public NativeTexture buildNativeTexture(/*2.1.24BundleResource*/URL filename, HashMap<NumericType, NumericValue> parameters) {
        // Even its not used check whether it can be load. Important in tests.
        if (filename.isHttp()) {
            BufferedImage li = JavaBundleHelper.loadBundleTexture(filename);
            if (li == null) {
                return null;
            }
        } else {
            // But don't use image loader, which is quite slow.
            FileSystemResource resource = FileSystemResource.buildFromFullString(filename.getAsString());
            try {
                FileReader.readFully(FileReader.getFileStream(resource));
            } catch (IOException e) {
                logger.error("failed to load texture: " + e.getMessage());
                return null;
            }
        }
        return new DummyTexture(filename.getUrl());

    }

    @Override
    public NativeDocument parseXml(String xmltext) throws XmlException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmltext)));
            return new JavaXmlDocument(doc);
        } catch (Exception e) {
            throw new XmlException(e);
        }

    }

    @Override
    public NativeJsonValue parseJson(String jsonstring) {
        GsonBuilder builder = new GsonBuilder();
        LinkedTreeMap map = (LinkedTreeMap) builder.create().fromJson(jsonstring, Object.class);
        return new SimpleJsonObject(map);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Log getLog(Class clazz) {
        return logfactory.getLog(clazz);
    }

    @Override
    public NativeRay buildRay(Vector3 origin, Vector3 direction) {
        return new DummyRay(origin, direction);
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * @return
     */
    @Override
    public NativeStringHelper buildStringHelper() {
        return new DefaultJavaStringHelper();
    }

    @Override
    public NativeEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public float getFloat(byte[] buf, int offset) {
        return Util.getFloat(buf, offset);
    }

    @Override
    public void setFloat(byte[] buf, int offset, float f) {
        Util.setFloat(buf, offset, f);
    }

    @Override
    public double getDouble(byte[] buf, int offset) {
        return Util.getDouble(buf, offset);
    }

    @Override
    public NativeByteBuffer buildByteBuffer(int size) {
        SimpleByteBuffer buf = new SimpleByteBuffer(new byte[size]);
        return buf;
    }

    @Override
    public void httpGet(String url, List<Pair<String, String>> params, List<Pair<String, String>> header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {

        NativeFuture<AsyncHttpResponse> future = new JavaWebClient().httpGet(url, params, header);
        sceneRunner.addFuture(future, asyncJobDelegate);
    }

    @Override
    public NativeScene getScene() {
        return nativeScene;
    }

    /*@Override
    public void loadBundle(String bundlename, BundleLoadDelegate bundleLoadDelegate, boolean delayed) {
        AsyncHelper.asyncBundleLoad(bundlename, AbstractSceneRunner.getInstance().invokeLater(bundleLoadDelegate), delayed);
    }*/

    @Override
    public String getName() {
        return "SimpleHeadless";
    }

    @Override
    public boolean getKeyDown(int keycode) {
        // no real input, but can be used for testing
        boolean found = mockedKeyDownInput.remove(Integer.valueOf(keycode));
        return found;
    }

    @Override
    public boolean getKeyUp(int keycode) {
        // no real input, but can be used for testing
        boolean found = mockedKeyUpInput.remove(Integer.valueOf(keycode));
        return found;
    }

    @Override
    public Point getMouseDown() {
        // no real input, but can be used for testing
        if (mockedMouseDownInput.size() == 0) {
            return null;
        }
        return mockedMouseDownInput.remove(0);
    }

    @Override
    public Point getMouseMove() {
        // no real input, but can be used for testing
        if (mockedMouseMoveInput.size() == 0) {
            return null;
        }
        return mockedMouseMoveInput.remove(0);
    }

    @Override
    public Point getMouseUp() {
        // no real input, but can be used for testing
        if (mockedMouseUpInput.size() == 0) {
            return null;
        }
        return mockedMouseUpInput.remove(0);
    }

    @Override
    public NativeSocket connectToServer(Server server) {
        return JavaSocket.build(server.getHost(), server.getPort());
    }

    @Override
    public NativeBundleResourceLoader buildResourceLoader(String bundlename, String location) {

        return JavaBundleResolverFactory.buildResourceLoader(bundlename, location, bundleResolver);
    }

    /**
     * 22.1.24:  Some tests really need a size, so they might set it.
     */
    public void setSceneDimension(Dimension dimension) {
        ((DummyScene) nativeScene).dimension = dimension;
    }
}

class DummySceneNode implements NativeSceneNode {

    NativeTransform transform;
    String name;
    private static int uniqueId = 1000;
    private int id = uniqueId++;
    private NativeMesh mesh;

    DummySceneNode() {
        transform = new DummyTransform(this);
        ((SimpleHeadlessPlatform) Platform.getInstance()).sceneNodes.add(this);
    }

    @Override
    public void setMesh(NativeMesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public NativeMesh getMesh() {
        return mesh;
    }

    @Override
    public void setLight(NativeLight light) {

    }

    @Override
    public int getUniqueId() {
        return id;
    }

    @Override
    public void destroy() {
        ((SimpleHeadlessPlatform) Platform.getInstance()).sceneNodes.remove(this);
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NativeTransform getTransform() {
        return transform;
    }

    @Override
    public NativeCamera getCamera() {
        return null;
    }
}

class DummyTransform implements NativeTransform {

    DummyTransform parent;
    List<NativeTransform> children = new ArrayList<NativeTransform>();
    Vector3 position = new Vector3();
    Vector3 scale = new Vector3(1, 1, 1);
    Quaternion rotation = new Quaternion();
    NativeSceneNode parentscenenode;

    public DummyTransform(DummySceneNode parentscenenode) {
        this.parentscenenode = parentscenenode;
    }

    @Override
    public Quaternion getRotation() {
        return rotation;
    }

    @Override
    public Vector3 getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector3 vector3) {
        position = vector3;
    }

    @Override
    public void setRotation(Quaternion quaternion) {
        rotation = quaternion;
    }

    @Override
    public Vector3 getScale() {
        return scale;
    }

    @Override
    public void setScale(Vector3 scale) {
        this.scale = scale;
    }

    @Override
    public void translateOnAxis(Vector3 axis, double distance) {
        axis = MathUtil2.multiply(rotation, axis);
        Vector3 v = axis.multiply(distance);
        position = MathUtil2.add(position, v);

    }

    @Override
    public void rotateOnAxis(Vector3 axis, double angle) {
        Quaternion q = Quaternion.buildQuaternionFromAngleAxis(angle, axis);
        rotation = MathUtil2.multiply(rotation, q);
    }

    @Override
    public void setParent(NativeTransform parent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = (DummyTransform) parent;
        if (this.parent != null) {
            this.parent.children.add(this);
        }
    }

    @Override
    public NativeTransform getParent() {
        if (parent == null /*TODO || parent.isRoot*/) {
            // den nicht liefern
            return null;
        }
        return parent;
    }

    @Override
    public Matrix4 getWorldModelMatrix() {
        Matrix4 local = getLocalModelMatrix();
        if (parent != null) {
            //7.7.21: Order correct?
            return parent.getWorldModelMatrix().multiply(local);
        } else
            return local;
    }

    @Override
    public Matrix4 getLocalModelMatrix() {
        return MathUtil2.buildMatrix(position, rotation, scale);
    }

    @Override
    public NativeTransform getChild(int index) {
        return children.get(index);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public List<NativeTransform> getChildren() {
        List<NativeTransform> l = new ArrayList<NativeTransform>();
        for (NativeTransform t : children) {
            l.add(t);
        }
        return l;
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return parentscenenode;
    }

    @Override
    public void setLayer(int layer) {

    }

    @Override
    public int getLayer() {
        return 0;
    }
}

class DummyMesh implements NativeMesh {

    NativeMaterial material;
    DummyGeometry geometry;

    DummyMesh(DummyGeometry geometry, NativeMaterial material) {
        this.geometry = geometry;
        this.material = material;
    }

    @Override
    public NativeMaterial getMaterial() {
        return material;
    }

    @Override
    public void setBoxColliderSizeHint(Vector3 size) {

    }

    @Override
    public NativeSceneNode getSceneNode() {
        return null;
    }
}

class DummyCamera implements NativeCamera {
    double fov;
    double aspect;
    double near;
    double far;
    DummySceneNode carrier = new DummySceneNode();

    public DummyCamera(/*int width, int height,*/ double fov, double aspect, double near, double far) {
        super(/*width, height*/);
        this.fov = fov;
        this.aspect = aspect;//((double) width) / (double) height;
        this.near = near;
        this.far = far;

    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return null;
    }

    @Override
    public Matrix4 getViewMatrix() {
        // taken from HomebrewCamera
        Matrix4 worldmatrix = carrier.getTransform().getWorldModelMatrix();
        Matrix4 viewMatrix = MathUtil2.getInverse(worldmatrix);
        return viewMatrix;
    }

    @Override
    public NativeRay buildPickingRay(NativeTransform realViewPosition, Point mouselocation) {
        return null;
    }

    @Override
    public double getNear() {
        return near;
    }

    @Override
    public double getFar() {
        return far;
    }

    @Override
    public double getAspect() {
        return aspect;
    }

    @Override
    public double getFov() {
        return fov;
    }

    @Override
    public Vector3 getVrPosition(boolean dumpInfo) {
        return null;
    }

    @Override
    public void setLayer(int layer) {

    }

    @Override
    public int getLayer() {
        return 0;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public NativeSceneNode getCarrier() {
        return carrier;
    }

    @Override
    public void setClearDepth(boolean clearDepth) {

    }

    @Override
    public void setClearBackground(boolean clearBackground) {

    }

    @Override
    public void setEnabled(boolean b) {

    }

    @Override
    public void setFar(double far) {

    }
}

class DummyScene implements NativeScene {

    // Use a 'dummy' dimension instead of just null because some HUD builder for example use the current screen size?
    // But this ins't headless. new Dimension(300,200); 22.1.24: But can be set.
    Dimension dimension = null;

    public DummyScene() {
        int h = 9;
    }

    @Override
    public void add(NativeSceneNode object3d) {

    }

    @Override
    public void add(NativeLight light) {

    }

    @Override
    public Dimension getDimension() {
        return dimension;
    }
}

class DummyMaterial implements NativeMaterial {

    String name;

    DummyMaterial(String name) {
        this.name = name;
    }

    @Override
    public void setTransparency(boolean enabled) {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public NativeTexture[] getMaps() {
        return new NativeTexture[0];
    }
}

class DummyTexture implements NativeTexture {

    URL name;

    DummyTexture(URL name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name.getName();
    }
}

class DummyGeometry implements NativeGeometry {
    Vector3Array vertices;
    int[] indices;

    DummyGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        this.vertices = vertices;
        this.indices = indices;
    }

    @Override
    public String getId() {
        throw new RuntimeException("not yet");
        // return "";
    }
}

class DummyCollision implements NativeCollision {
    private final DummySceneNode node;
    Vector3 point;

    DummyCollision(DummySceneNode node, Vector3 point) {
        this.node = node;
        this.point = point;
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return node;
    }

    @Override
    public Vector3 getPoint() {
        return point;
    }
}

/**
 * Copied from OpenGlRay. Not nice copyPaste, but the attempt to have a common IntersectionHelper in core
 * leads to nasty dependencies.
 */
class DummyRay implements NativeRay {
    Vector3 origin, direction;

    public DummyRay(Vector3 origin, Vector3 direction) {
        this.origin = origin;
        this.direction = direction;
    }

    @Override
    public Vector3 getDirection() {
        return direction;
    }

    @Override
    public Vector3 getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "origin=" + origin + ",direction=" + direction;
    }

    @Override
    public List<NativeCollision> getIntersections() {
        // Scene and world are not available here. Assume first node to be world.
        // But especially in tests there might be multiple 'worlds' causing difficult to track failed tests.
        // So look for world and also check to have only one world.
        NativeSceneNode world = null;
        for (NativeSceneNode n : ((SimpleHeadlessPlatform) Platform.getInstance()).sceneNodes) {
            if ("World".equals(n.getName())) {
                if (world != null) {
                    throw new RuntimeException("multiple 'world' found");
                }
                world = n;
            }
        }
        if (world == null) {
            throw new RuntimeException("no 'world' found");
        }
        return intersects(world);
    }

    public List<NativeCollision> intersects(NativeSceneNode model) {
        //DummySceneNode n = (DummySceneNode) model;
        List<NativeCollision> na = new ArrayList<NativeCollision>();
        TransformNodeVisitor nodeVisitor = new TransformNodeVisitor() {

            @Override
            public void handleNode(NativeTransform node) {
                DummySceneNode n = (DummySceneNode) node.getSceneNode();
                DummyMesh mesh = (DummyMesh) n.getMesh();
                if (mesh != null) {
                    List<Vector3> results = getIntersection(mesh.geometry, n.getTransform().getWorldModelMatrix());
                    for (Vector3 p : results) {
                        na.add(new DummyCollision(n, p));
                    }
                }
            }
        };
        //intersects(n, na);
        PlatformHelper.traverseTransform(model.getTransform(), nodeVisitor);
        return na;
    }

    /**
     * Order of intersection points is non deterministic.
     * <p/>
     * 26.8.2016: Requires vertex data, which in this platform is retained. Commonly these reside in the GPU.
     * <p>
     * Vertex data is in local space. Ray is transformed to that space.
     * 22.3.18: Is that all correct?
     *
     * @return
     */
    private List<Vector3> getIntersection(DummyGeometry geo, Matrix4 worldModelMatrix) {
        Matrix4 worldModelMatrixInverse = MathUtil2.getInverse(worldModelMatrix);
        Vector3 lorigin = worldModelMatrixInverse.transform(origin);
        //Why is direction not transformed?
        //Vector3 ldirection = worldModelMatrixInverse.transform(direction);
        List<Vector3> intersections = GeometryHelper.getRayIntersections(geo.vertices, geo.indices, lorigin, direction);
        // transform intersections back to world space of ray.
        List<Vector3> transformeedIntersections = new ArrayList<Vector3>();
        for (Vector3 intersection : intersections) {
            transformeedIntersections.add(worldModelMatrix.transform(intersection));
        }
        return transformeedIntersections;
    }
}

