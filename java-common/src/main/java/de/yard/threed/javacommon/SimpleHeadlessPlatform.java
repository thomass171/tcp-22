package de.yard.threed.javacommon;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.yard.threed.outofbrowser.AsyncBundleLoader;
import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.platform.*;
import de.yard.threed.javanative.JsonUtil;
import de.yard.threed.core.resource.BundleResolver;
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

/**
 * Simple Platform implementation eg. for unit tests.
 * <p>
 * Just provides a logger. ResourceManager must be added later because it might need the LogFactory.#
 * 4.4.19: Aber auch ein StringHelper.
 * Nichts fuer C#.
 * <p>
 * Wie ist denn die Abgrenzung zu PlatformHomeBrew?
 * 10.9.20: Ist headless nicht einfach PlatformHomeBrew ohne Renderer? 30.6.21: Ja, so duerfte es ein.
 * 5.7.21: Nicht so ganz. PlatformHomeBrew verwendet ja die ganzen OpneGL NAtive Implemetierungen. SimpleHeadlessPlatform muss etwas dazwischen sein.
 * Mehr als core-Platform und DefaultPlatform, aber weniger als eine full platform (Abgrenzung weiter unklar).
 * Und sinnvoll ist sie nur zum Testen oder als Ersatz fuer core?
 * Aber es muss ja wirklich etwas (Java basiert) implementiert werden, z.B. Logging.
 * Darum kommt sie mal regulaer nach java-common. Das koennte auch die Basis fur JME sein (??).
 * Weils zum Testen in "engine" verwendet wird, ist ein rudimentaerer NodeTree (mit Mesh) tatsaechlich hilfreich.
 * <p>
 * <p>
 * Created on 05.12.18.
 */
public class SimpleHeadlessPlatform extends DefaultPlatform {
    public String hostdir;
    static Log logger = new JALog(/*LogFactory.getLog(*/SimpleHeadlessPlatform.class);
    public static String PROPERTY_PREFIX = "tcp22.";
    public static List<Integer> mockedKeyInput = new ArrayList<Integer>();

    /**
     * Needs access from extending classes.
     */
    public SimpleHeadlessPlatform(NativeEventBus optionalEventbus) {

        if (optionalEventbus == null) {
            eventBus = new JAEventBus();
        } else {
            eventBus = optionalEventbus;
        }
        logfactory = new JALogFactory();
        nativeScene = new DummyScene();

        hostdir = getProperty("HOSTDIR");
        if (hostdir == null) {
            throw new RuntimeException("HOSTDIR not set");
        }
    }

    public SimpleHeadlessPlatform() {
        this(null);
    }

    public static PlatformInternals init(HashMap<String, String> properties, NativeEventBus eventbus) {
        //System.out.println("PlatformOpenGL.init");

        DummySceneNode.sceneNodes.clear();

        for (String key : properties.keySet()) {
            //System.out.println("transfer of propery "+key+" to system");
            System.setProperty(PROPERTY_PREFIX + key, properties.get(key));
        }
        instance = new SimpleHeadlessPlatform(eventbus);
        SimpleHeadlessPlatform shpInstance = (SimpleHeadlessPlatform) instance;
        //MA36 ((SimpleHeadlessPlatform)instance).resetInit();

        //((PlatformHomeBrew) instance).resourcemanager = JAResourceManager.getInstance();
        PlatformInternals platformInternals = new PlatformInternals();
        DefaultResourceReader resourceReader = new DefaultResourceReader();
        instance.bundleResolver.add(new SimpleBundleResolver(shpInstance.hostdir + "/bundles", resourceReader));
        instance.bundleResolver.addAll(SyncBundleLoader.buildFromPath(SimpleHeadlessPlatform.getProperty("ADDITIONALBUNDLE"), resourceReader));
        instance.bundleLoader = new AsyncBundleLoader(resourceReader);

        logger.info("SimpleHeadlessPlatform created");
        return /*MA36 (EnginePlatform)* /instance*/platformInternals;
    }

    public static PlatformInternals init(HashMap<String, String> properties) {
        return init(properties, null);
    }

    public static String getProperty(String name) {
        String prop = System.getProperty(name);
        if (prop == null) {
            prop = System.getenv(name);
        }
        return prop;
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
    public void buildNativeModelPlain(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {
        // No access to SceneRunner/AsyncHelper here. According to the idea of headless: Just create a dummy node
        // for the model to continue the workflow.
        delegate.modelBuilt(new BuildResult(new DummySceneNode()));
    }

    public NativeMesh buildNativeMesh(NativeGeometry nativeGeometry, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        return new DummyMesh();
    }

    @Override
    public List<NativeSceneNode> findSceneNodeByName(String name) {
        List<NativeSceneNode> l = new ArrayList<>();//Platform.findNodeByName(name, ((OpenGlScene) AbstractSceneRunner.getInstance().scene).getRootTransform(), true);
        for (NativeSceneNode n : DummySceneNode.sceneNodes) {
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
    public void setSystemProperty(String key, String value) {
        System.setProperty(PROPERTY_PREFIX + key, value);
    }

    @Override
    public String getSystemProperty(String key) {
        return System.getProperty(PROPERTY_PREFIX + key);
    }

    @Override
    public Log getLog(Class clazz) {
        return logfactory.getLog(clazz);

    }

    @Override
    public NativeRay buildRay(Vector3 origin, Vector3 direction) {
        // inform caller
        throw new RuntimeException("no ray");
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
        return new JavaStringHelper();
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

    /*MA36 ueber runner @Override
    public void sendHttpRequest(String url, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate ) {
        httpClient.sendHttpRequest(url,method,header,asyncJobDelegate);
    }*/

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
    public boolean GetKeyDown(int keycode) {
        // no real input, but can be used for testing
        boolean found = mockedKeyInput.remove(new Integer(keycode));
        return found;
    }
}

class DummySceneNode implements NativeSceneNode {

    static List<NativeSceneNode> sceneNodes = new ArrayList<>();
    NativeTransform transform;
    String name;

    DummySceneNode() {
        transform = new DummyTransform(this);
        sceneNodes.add(this);
    }

    @Override
    public void setMesh(NativeMesh mesh) {

    }

    @Override
    public NativeMesh getMesh() {
        return null;
    }

    @Override
    public void setLight(NativeLight light) {

    }

    @Override
    public void destroy() {

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

    @Override
    public NativeMaterial getMaterial() {
        return null;
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
        return null;
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
        // Return a 'dummy' dimension instead of just null because some HUD builder for example use the current screen size?
        // But this ins't headless.
        //return new Dimension(300,200);
        return null;
    }


}

