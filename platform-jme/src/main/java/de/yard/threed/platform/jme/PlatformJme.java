package de.yard.threed.platform.jme;

import com.google.gson.GsonBuilder;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import de.yard.threed.outofbrowser.AsyncBundleLoader;
import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.outofbrowser.FileSystemResource;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.ShapeGeometry;

import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.javacommon.JALog;
import de.yard.threed.javanative.JsonUtil;
import de.yard.threed.javanative.SocketClient;


import de.yard.threed.engine.platform.common.*;
import de.yard.threed.javacommon.*;
import de.yard.threed.javacommon.JavaXmlDocument;
import de.yard.threed.javacommon.Util;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 5.7.21: Jetzt mal als Erweiterung der SimpleHeadlessPlatform
 * <p>
 * Created by thomass on 20.04.15.
 */
public class PlatformJme extends SimpleHeadlessPlatform/*EngineHelper*/ {
    // kann nicht ueber die Factory gebaut werden, weil die gerade noch initialisiert wird
    Log logger = new JALog(/*LogFactory.getLog(*/PlatformJme.class);
    JmeResourceManager jmeResourceManager;

    /**
     * 16.11.16: Umbenannt von getInstance zu init, um die Bedeutung zu verdeutlichen. Braucht Properties wie z.B. CACHEDIR, die schon in der Platform
     * z.B. für defaulttexture gebraucht werden.
     * <p>
     * 23.7.21: JmeScene kann hier noch nicht angelegt werden. Kommt spaeter.
     * 2.8.21: Returns PlatformInternals now instead of just Platform
     *
     * @return
     */
    public static PlatformInternals init(HashMap<String, String> properties) {
        //if (EngineHelper.instance == null || !(EngineHelper.instance instanceof PlatformJme)) {
        if (properties != null) {
            for (String key : properties.keySet()) {
                System.setProperty(key, properties.get(key));
            }
        }
        instance = new PlatformJme();
        //  Als default texture sowas wie void.png o.ae. nehmen
        //((EngineHelper)EngineHelper.instance).defaulttexture = JmeTexture.loadFromFile(new BundleResource("FontMap.png"));
        //}


        PlatformInternals platformInternals = new PlatformInternals();
        return platformInternals/*instance*/;
    }

    public void postInit(JmeResourceManager rm) {
        ((PlatformJme) instance).jmeResourceManager = rm;
        instance.bundleLoader = new AsyncBundleLoader(rm);
    }

    @Override
    public NativeSceneNode buildModel() {
        return buildModel(null);
    }

    @Override
    public NativeSceneNode buildModel(String name) {
        JmeSceneNode n = new JmeSceneNode(name);
        //native2nativewrapper.put(n.getUniqueId(), n);
        return n;
    }

    //int threadcnt = 0;

    @Override
    public void buildNativeModelPlain(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {
        // verwendet nicht asyncjob, weil es ja intern ist. JME mag kein modelload in MT. Das gibt sonst ruckzuck ConcurrentModificationException. Darum einfach async um das
        // delayed testen zu koennen.
        int delegateid = AbstractSceneRunner.getInstance().invokeLater(delegate);

        //logger.debug("buildNativeModel "+filename+", delegateid="+delegateid);
        AsyncHelper.asyncModelBuild(filename, opttexturepath, options, delegateid);
    }



    /*MA36 ueber runner @Override
    public void sendHttpRequest(String url, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate ) {
        httpClient.sendHttpRequest(url,method,header,asyncJobDelegate);
    }*/

    @Override
    public List<NativeSceneNode> findSceneNodeByName(String name) {
        List<NativeSceneNode> l = new ArrayList<NativeSceneNode>();
        SceneGraphVisitor visitor = new SceneGraphVisitor() {

            @Override
            public void visit(Spatial spat) {
                // search criterion can be control class:
                if (name.equals(spat.getName())) {
                    l.add(new JmeSceneNode(spat));
                }
            }

        };

        // Now scan the tree either depth getFirst...
        JmeScene.getInstance().getRootNode().depthFirstTraversal(visitor);
        return l;
    }


    @Override
    public NativeMesh buildNativeMesh(NativeGeometry geometry, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        if (material == null) {
            // Wireframe Darstellung
            material = JmeMaterial.buildWireframeMaterial();
        }
        return JmeMesh.buildMesh((JmeGeometry) geometry, (JmeMaterial) material, castShadow, receiveShadow, false);
    }

    @Override
    public NativeSceneNode buildLine(Vector3 from, Vector3 to, Color color) {
        Vector3f[] vertices = new Vector3f[2];
        vertices[0] = JmeVector3.toJme(from);
        vertices[1] = JmeVector3.toJme(to);
        Vector2f[] uvs = new Vector2f[2];
        uvs[0] = (new Vector2f());
        uvs[1] = (new Vector2f());
        JmeGeometry geometry = JmeGeometry.buildMesh(vertices, new int[]{0, 1}, uvs, vertices);
        MaterialDefinition materialDefinition = new MaterialDefinition("mat", Material.buildColorMap(color), null, null);

        NativeMaterial material = JmeMaterial.buildMaterial(materialDefinition, null);
        JmeMesh mesh = JmeMesh.buildMesh((JmeGeometry) geometry, (JmeMaterial) material, false, false, true);
        JmeSceneNode n = new JmeSceneNode((String) null);
        n.setMesh(mesh);
        return n;
    }

   /* @Override
    public NativeMesh buildMesh(List<Vector3> vertices, List<Face3List> faces, /*List<NativeMaterial>* /NativeMaterial material, List<Vector3> normals, boolean castShadow, boolean receiveShadow) {
        /*02.05.16 das muesste aber gehen if (normals != null && faces.size() > 1) {
            throw new RuntimeException("nt submeshes with explicit normal list");
        }*/
        /*2.5.16: jetzt in Model if (faces.size() > 1 && material.size() > 1) {
            // Bei multiple material in einzelne aufteilen, weil JME kein multiple material kann.
            List<SimpleGeometry> geolist = GeometryHelper.extractSubmeshes(vertices, faces, normals);
            JmeModel container = new JmeModel();
            int i = 0;
            for (SimpleGeometry geo : geolist) {
                JmeGeometry jg = JmeGeometry.buildGeometry(geo.vertices, new SmartArrayList<Face3List>(geo.faces),normals);
                NativeMesh mesh = JmeMesh.buildGeometry(jg, (JmeMaterial) material.get(i), castShadow, receiveShadow);
                container.add(mesh);
                i++;
            }
            return JmeMesh.buildContainerMesh(container);
        }* /
        JmeGeometry jg = JmeGeometry.buildGeometry(vertices, faces, normals);

        return JmeMesh.buildMesh(jg/*vertices, faces* /, (JmeMaterial) material, castShadow, receiveShadow);
    }*/

    @Override
    public void updateMesh(NativeMesh mesh, NativeGeometry nativeGeometry, NativeMaterial material) {
        ((JmeMesh) mesh).updateMesh(nativeGeometry, material);
    }


    @Override
    public NativeByteBuffer buildByteBuffer(int size) {
        //return new JmeVector3Array(size);
        SimpleByteBuffer buf = new SimpleByteBuffer(new byte[size]);
        return buf;
        //return new Vector3Array(buf,0,size);
    }

    /*@Override
    public NativeColor buildColor(float r, float g, float b, float a) {
        return new JmeColor(r, g, b, a);
    }*/

    /*public JmeScene buildScene() {
        return new JmeScene();
    }*/

    public NativeCamera buildPerspectiveCamera(double fov, double aspect, double near, double far) {
        JmeCamera cam = new JmeCamera(null, JmeSceneRunner.getInstance().rootnode, fov, aspect, near, far, Settings.backgroundColor, null);
        return cam;
    }


    /*public Matrix4 buildMatrix4(float a11, float a12, float a13, float a14,
                                      float a21, float a22, float a23, float a24,
                                      float a31, float a32, float a33, float a34,
                                      float a41, float a42, float a43, float a44) {
        return new JmeMatrix4(a11, a12, a13, a14,
                a21, a22, a23, a24,
                a31, a32, a33, a34,
                a41, a42, a43, a44);
    }*/

    @Override
    public List<NativeCamera> getCameras() {
        return AbstractSceneRunner.instance.getCameras();
    }

    @Override
    public NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap</*TextureType*/String, NativeTexture> texture,
                                        HashMap<NumericType, NumericValue> parameters, /*MA36 TODO Effect*/ Object effect) {
        return JmeMaterial.buildMaterial(new MaterialDefinition(name, color, texture, parameters), (Effect) effect);
    }
    //return buildMaterial(new MaterialDefinition(name,color,texture,parameters),effect);
    
    /*@Override
    public NativeMaterial buildLambertMaterialWithNormalMap(Color colorargb) {
        return JmeMaterial.buildLambertMaterialWithNormalMap(colorargb, null);
    }

    @Override
    public NativeMaterial buildLambertMaterialWithNormalMap(NativeTexture texture) {
        return JmeMaterial.buildLambertMaterialWithNormalMap(null, texture);
    }*/

    private NativeTexture buildNativeTextureJme(NativeResource textureresource, HashMap<NumericType, NumericValue> params) {
        //logger.debug("buildNativeTexture " + textureresource.getName());
        JmeTexture tex = JmeTexture.loadFromFile(textureresource);
        if (tex == null) {
            logger.warn("Loading texture " + textureresource.getName() + " failed. Using default");
            return null;//defaulttexture;
        }
        NumericValue wraps = params.get(NumericType.TEXTURE_WRAP_S);
        if (wraps != null) {
            if (wraps.equals(NumericValue.REPEAT))
                tex.texture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
        }
        NumericValue wrapt = params.get(NumericType.TEXTURE_WRAP_T);
        if (wrapt != null) {
            if (wrapt.equals(NumericValue.REPEAT))
                tex.texture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
        }
        return tex;
    }

    @Override
    public NativeTexture buildNativeTexture(/*Bundle dummywegensigbundle, */BundleResource filename, HashMap<NumericType, NumericValue> parameters) {
        if (filename.bundle == null) {
            logger.error("bundle not set for file " + filename.getFullName());
            return null;//defaulttexture;
        }
        String bundlebasedir;
        FileSystemResource resource;
        /*if (filename.bundle instanceof BtgBundle) {
            //oh oh, das wird jetzt aber immer wüster.
            bundlebasedir = ((BtgBundle) filename.bundle).fname;
            // Und wenn der Pfad absolut ist, das Bundle ignorieren.
            if (filename.getFullName().startsWith("/")) {
                resource = FileSystemResource.buildFromFullString(filename.getFullName());
            } else {
                resource = FileSystemResource.buildFromFullString(bundlebasedir + "/" + filename.getFullName());
            }
        } else {*/
        //bundlebasedir = Platform.getInstance().getSystemProperty("BUNDLEDIR") + "/" + filename.bundle.name;
        bundlebasedir = BundleRegistry.getBundleBasedir(filename.bundle.name, false);
        resource = FileSystemResource.buildFromFullString(bundlebasedir + "/" + filename.getFullName());
        //}
        return buildNativeTextureJme(resource, parameters);
    }

    /*@Override
    public void executeAsyncJobNurFuerRunnerhelper(final AsyncJob job) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String msg = job.execute();
                    if (Config.isAsyncdebuglog()) {
                        logger.debug("job completed");
                    }
                    RunnerHelper.getInstance().addCompletedJob(new CompletedJob(job, msg));
                } catch (Exception e) {
                    RunnerHelper.getInstance().addCompletedJob(new CompletedJob(job, e.getMessage()));
                }
            }
        }.start();
    }*/

    @Override
    public NativeTexture buildNativeTexture(ImageData imagedata, boolean fornormalmap) {
        return JmeTexture.buildFromImage((imagedata));
    }

    @Override
    public NativeTexture buildNativeTexture(NativeCanvas canvas) {
        JmeTexture texture = JmeTexture.buildFromImage(((JmeCanvas) canvas).image);
        return texture;
    }

    @Override
    public NativeLight buildPointLight(Color argb) {
        return JmeLight.buildPointLight(argb);
    }

    @Override
    public NativeLight buildAmbientLight(Color argb) {
        return JmeLight.buildAmbientLight(argb);
    }

    @Override
    public NativeLight buildDirectionalLight(Color argb, Vector3 direction) {
        return JmeLight.buildDirectionalLight(argb, direction);
    }

    @Override
    public NativeGeometry buildNativeGeometry(Vector3Array vertices,int[] indices, Vector2Array uvs, Vector3Array normals) {
        if (ShapeGeometry.debug) {
            logger.debug("buildGeometry with " + vertices.size() + " vertices and  " + indices.length + " indices");
        }
        return JmeGeometry.buildGeometry(vertices, indices, uvs, normals);
    }

    /*10.7.21 @Override
    public NativeSceneRunner getSceneRunner() {
        return JmeSceneRunner.getInstance();
    }*/

    @Override
    public Log getLog(Class clazz) {
        return new JALog(clazz);
    }

    /*@Override
    public ResourceManager getRessourceManager() {
        return JmeResourceManager.getInstance();
    }*/

    /**
     * Ob hier vielleicht ein Inputstream als Input guenstiger ist? Kennt der GWT Ressourceloader
     * aber nicht.
     *
     * @param xmltext
     * @return
     * @throws XmlException
     */
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
    public NativeCanvas buildNativeCanvas(int width, int height) {
        JmeCanvas canvas = new JmeCanvas(width, height);

        return canvas;
    }

    @Override
    public NativeContentProvider getContentProvider(char type, String location, TestPdfDoc docid) {
        if (type == 'I') {
            return new JmePhotoAlbumContentProvider(new File(location));
        }
        if (type == 'D') {
            return new JmePdfContentProvider(docid);
        }
        return (NativeContentProvider) de.yard.threed.core.Util.notyet();
    }

    @Override
    public NativeSplineInterpolationFunction buildNativeSplineInterpolationFunction(double[] x, double[] y) {
        SplineInterpolator si = new SplineInterpolator();
        PolynomialSplineFunction fct = si.interpolate(x, y);
        return new JmeSplineInterpolationFunction(fct);
    }

    @Override
    public NativeRay buildRay(Vector3 origin, Vector3 direction) {
        return JmeRay.buildRay(origin, direction);
    }

    /*@Override
    public boolean exists(NativeResource file) {
        return new JAFile(file).exists();
    }*/

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

    public static ColorRGBA buildColor(Color col) {
        return new ColorRGBA(col.getR(), col.getG(), col.getB(), col.getAlpha());
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
        return AbstractSceneRunner.getInstance().getMouseClick();
    }

    @Override
    public Point getMousePress() {
        return AbstractSceneRunner.getInstance().getMousePress();
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
        return "Jme";
    }

    /*@Override
    public byte[] serialize(Object obj) {        
        return Misc.serialize(obj);
    }

    @Override
    public Object deserialize(byte[] b) {
        return Misc.deserialize(b);
    }*/

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    @Override
    public NativeEventBus getEventBus() {
        return JAEventBus.getInstance();
    }

    @Override
    public void abort() {
        de.yard.threed.core.Util.notyet();
    }


    @Override
    public <T> Object parseJsonToModel(String jsonstring, Class clazz) {
        GsonBuilder builder = new GsonBuilder();
        Object model = builder.create().fromJson(jsonstring, clazz);
        return model;
    }

    @Override
    public String modelToJson(Object model) {
        return JsonUtil.toJson(model);
    }

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
        FilterPostProcessor fpp = new FilterPostProcessor(jmeResourceManager.am);
        SSAOFilter ssaoFilter = new SSAOFilter(2.9299974f, 32.920483f, 5.8100376f, 0.091000035f);
        ;
        //ssaoFilter.setApproximateNormals(true);
        fpp.addFilter(ssaoFilter);
        //SSAOUI ui = new SSAOUI(inputManager, ssaoFilter);
        JmeRenderProcessor jmeRenderProcessor = new JmeRenderProcessor();
        jmeRenderProcessor.fpp = fpp;
        return jmeRenderProcessor;
    }

    @Override
    public void addRenderProcessor(NativeRenderProcessor renderProcessor) {
        JmeSceneRunner.getInstance().jmecamera.getViewPort().addProcessor(((JmeRenderProcessor) renderProcessor).fpp);
    }

    /*MA36 @Override
    public NativeWebClient getWebClient(String baseUrl) {
        return new JavaWebClient(baseUrl);
    }*/

    @Override
    public NativeSocket connectToServer() {

        if (nativeSocket != null) {
            logger.warn("already connected!");
            return null;
        }
        SocketClient queuingSocketClient = new SocketClient("localhost", 5809);


        try {
            return new JmeSocket(queuingSocketClient);
        } catch (IOException e) {
            logger.error("connect() failed");
            return null;
        }

    }

    @Override
    public NativeScene getScene() {
        return nativeScene;
    }
}
