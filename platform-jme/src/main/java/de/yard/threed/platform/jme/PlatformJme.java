package de.yard.threed.platform.jme;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.URL;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.ShapeGeometry;

import de.yard.threed.core.buffer.SimpleByteBuffer;


import de.yard.threed.engine.platform.common.*;
import de.yard.threed.javacommon.*;
import de.yard.threed.javacommon.JavaXmlDocument;
import de.yard.threed.javacommon.Util;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 5.7.21: Now extending SimpleHeadlessPlatform.
 * 25.1.23: Why? This doesn't really make sense.
 * <p>
 * Created by thomass on 20.04.15.
 */
public class PlatformJme extends SimpleHeadlessPlatform {
    JmeResourceManager jmeResourceManager;
    private static int uniqueid = 1;
    // 29.12.23: Moved from JmeSceneRunner to here for having JME statics (JmeScene) inited earlier.
    public JmeSimpleApplication app;

    private PlatformJme(Configuration configuration) {
        // Stringhelper and logfactory are build in super
        super(null, configuration);
        this.configuration = configuration;
        // replace super logger
        logger = getLog(PlatformJme.class);
    }

    @Override
    public int getDefaultLogLevel() {
        return DefaultLog.LEVEL_INFO;
    }

    /**
     * 16.11.16: Umbenannt von getInstance zu init, um die Bedeutung zu verdeutlichen. Braucht Properties, die schon in der Platform
     * gebraucht werden.
     * <p>
     * 23.7.21: JmeScene kann hier noch nicht angelegt werden. Kommt spaeter.
     * 2.8.21: Returns PlatformInternals now instead of just Platform
     *
     * @return
     */
    public static PlatformInternals init(Configuration configuration) {
        instance = new PlatformJme(configuration);
        //  Als default texture sowas wie void.png o.ae. nehmen
        //((EngineHelper)EngineHelper.instance).defaulttexture = JmeTexture.loadFromFile(new BundleResource("FontMap.png"));
        //}


        PlatformInternals platformInternals = new PlatformInternals();
        DefaultResourceReader resourceReader = new DefaultResourceReader();
        instance.bundleResolver.add(new SimpleBundleResolver(((PlatformJme) instance).hostdir + "/bundles", resourceReader));
        instance.bundleResolver.addAll(SimpleBundleResolver.buildFromPath(configuration.getString("ADDITIONALBUNDLE"), resourceReader));

        // 29.12.23: Moved from JmeSceneRunner to here for having JME statics (JmeScene) inited earlier.
        ((PlatformJme) instance).app = new JmeSimpleApplication();
        JmeScene.init(((PlatformJme) instance).app, ((PlatformJme) instance).app.getFlyCam());

        return platformInternals/*instance*/;
    }

    public void postInit(JmeResourceManager rm) {
        ((PlatformJme) instance).jmeResourceManager = rm;
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
    public void buildNativeModelPlain(ResourceLoader resourceLoader, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {

        if (!resourceLoader.nativeResource.getExtension().equals("gltf")) {
            logger.error("Only GLTF allowed any more: " + resourceLoader.getUrl());
            de.yard.threed.core.Util.nomore();
        }

        //logger.debug("buildNativeModel "+filename+", delegateid="+delegateid);
        ModelLoader.buildModel(resourceLoader, opttexturepath, options, delegate);
    }

    @Override
    public void httpGet(String url, List<Pair<String, String>> params, List<Pair<String, String>> header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {
        NativeFuture<AsyncHttpResponse> future = new JavaWebClient().httpGet(url, params, header);
        sceneRunner.addFuture(future, asyncJobDelegate);
    }

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

        NativeMaterial material = JmeMaterial.buildMaterial(materialDefinition);
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
    public NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap</*TextureType*/String, NativeTexture> texture,
                                        HashMap<NumericType, NumericValue> parameters /*MA36  Effect Object effect*/) {
        return JmeMaterial.buildMaterial(new MaterialDefinition(name, color, texture, parameters)/*, (Effect) effect*/);
    }
    //return buildMaterial(new MaterialDefinition(name,color,texture,parameters),effect);

    @Override
    public NativeMaterial buildMaterial(NativeProgram program, boolean opaque) {
        return JmeMaterial.buildMaterial((JmeProgram) program, opaque);
    }

    /*@Override
    public NativeMaterial buildLambertMaterialWithNormalMap(Color colorargb) {
        return JmeMaterial.buildLambertMaterialWithNormalMap(colorargb, null);
    }

    @Override
    public NativeMaterial buildLambertMaterialWithNormalMap(NativeTexture texture) {
        return JmeMaterial.buildLambertMaterialWithNormalMap(null, texture);
    }*/

    private NativeTexture buildNativeTextureJme(/*2.1.24BundleResource*/URL textureresource, BufferedImage li, HashMap<NumericType, NumericValue> params) {
        //logger.debug("buildNativeTexture " + textureresource.getName());
        JmeTexture tex = JmeTexture.loadFromFile(textureresource.getAsString()/*getFullName()*/, li);
        if (tex == null) {
            logger.warn("Loading texture " + textureresource.getUrl() + " failed. Using default");
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
    public NativeTexture buildNativeTexture(/*2.1.24BundleResource*/URL filename, HashMap<NumericType, NumericValue> parameters) {
        // existing code extracted to JavaBundleHelper.loadBundleTexture
        // load texture from FS, cache or web
        BufferedImage li = JavaBundleHelper.loadBundleTexture(filename);
        return buildNativeTextureJme(filename, li, parameters);
    }

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
    public NativeLight buildPointLight(Color argb, double range) {
        return JmeLight.buildPointLight(argb, range);
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
    public NativeGeometry buildNativeGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        if (ShapeGeometry.debug) {
            logger.debug("buildGeometry with " + vertices.size() + " vertices and  " + indices.length + " indices");
        }
        return JmeGeometry.buildGeometry(vertices, indices, uvs, normals);
    }

    /*10.7.21 @Override
    public NativeSceneRunner getSceneRunner() {
        return JmeSceneRunner.getInstance();
    }*/

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
        return new DefaultJavaStringHelper();
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean getKeyDown(int keycode) {
        return AbstractSceneRunner.getInstance().keyPressed(keycode);
    }

    @Override
    public boolean getKeyUp(int keycode) {
        return AbstractSceneRunner.getInstance().keyReleased(keycode);
    }

    @Override
    public Point getMouseUp() {
        return AbstractSceneRunner.getInstance().getMouseClick();
    }

    @Override
    public Point getMouseDown() {
        return AbstractSceneRunner.getInstance().getMousePress();
    }

    @Override
    public boolean getKey(int keycode) {
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

    @Override
    public NativeEventBus getEventBus() {
        return JAEventBus.getInstance();
    }

    @Override
    public void abort() {
        de.yard.threed.core.Util.notyet();
    }

    @Override
    public boolean isDevmode() {
        return true;
    }

    /*24.10.23 try without @Override
    protected Log getLog() {
        return logger;
    }*/

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

    @Override
    public NativeSocket connectToServer(Server server) {
        return JavaSocket.build(server.getHost(), server.getPort());
    }

    @Override
    public NativeScene getScene() {
        return nativeScene;
    }

    @Override
    public NativeAudioClip buildNativeAudioClip(BundleResource br) {
        JmeAudioClip audioClip = JmeAudioClip.loadFromFile(br, jmeResourceManager.am);
        return audioClip;
    }

    @Override
    public NativeAudio buildNativeAudio(NativeAudioClip audioClip) {
        JmeAudio audio = JmeAudio.createAudio((JmeAudioClip) audioClip);
        return audio;
    }

    @Override
    public List<NativeSceneNode> findNodeByName(String name, NativeSceneNode startnode) {
        return ((JmeSceneNode) startnode).object3d.findNodeByName(name, ((JmeSceneNode) startnode).object3d.spatial);
    }

    @Override
    public NativeProgram buildProgram(String name, BundleResource vertexShader, BundleResource fragmentShader) {
        return new JmeProgram(name, vertexShader, fragmentShader);
    }

    /**
     * 29.12.23: Moved here from JmeScene to be avialable earlier (before scenerunner).
     */
    synchronized public String getUniqueName() {
        return "name" + uniqueid++;
    }
}
