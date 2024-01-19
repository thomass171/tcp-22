package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.google.gwt.xml.client.XMLParser;
import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.*;
import de.yard.threed.engine.platform.common.AsyncHelper;
import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.platform.SimpleEventBus;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.NumericValue;
import de.yard.threed.engine.platform.common.InitExecutor;
import de.yard.threed.engine.platform.common.NativeInitChain;
import de.yard.threed.engine.platform.common.SampleContentProvider;
import de.yard.threed.core.platform.TestPdfDoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomass on 20.04.15.
 */
public class PlatformWebGl extends Platform {
    // kann nicht ueber die Factory gebaut werden, weil die gerade noch initialisiert wird
    Log logger = new WebGlLog(/*LogFactory.getLog(*/PlatformWebGl.class.getName());
    //Scenerunner ist Singleton
    //31.7.21 private WebGlSceneRunner scenerunner = null;
    //6.7.17: Der Simple muesste es hier eigentlich vorerst auch tun.
    private NativeEventBus eventbus = new SimpleEventBus();//new WebGlEventBus();
    //Map<String, String> properties = new HashMap<String, String>();
    // not the GWT devmode!
    public static boolean isDevmode;
    private Configuration configuration;

    private PlatformWebGl(Configuration configuration) {
        this.configuration = configuration;
        StringUtils.init(buildStringHelper());
        logfactory = new LevelLogFactory(configuration, clazz -> new WebGlLog(clazz.getName()), isDevmode ? DefaultLog.LEVEL_DEBUG : DefaultLog.LEVEL_INFO);
    }

    /**
     * 5.10.18: Wie in JME umbenannt von getInstance zu init, um die Bedeutung zu verdeutlichen. Braucht Properties, die schon in der Platform
     * gebraucht werden.
     *
     * @return
     */
    public static PlatformInternals init/*getInstance*/(Configuration configuration) {
        if (Platform.instance == null || !(Platform.instance instanceof PlatformWebGl)) {
            Platform.instance = new PlatformWebGl(configuration);

            // TODO: Als default texture sowas wie void.png o.ae. nehmen. 5.10.18: Hat WebGL sowas nicht?
            //Platform.instance.defaulttexture = JmeTexture.loadFromFile(new BundleResource("FontMap.png"));
            instance.nativeScene = new WebGlScene();
        }
        PlatformInternals platformInternals = new PlatformInternals();
        // resolver order is important. most specific first.
        String additionalBundle = configuration.getString("ADDITIONALBUNDLE");
        if (additionalBundle != null && additionalBundle.contains(" ")) {
            // might be the result of a '+' which is valid in base64
            additionalBundle = additionalBundle.replace(" ", "+");
        }
        instance.bundleResolver.addAll(((PlatformWebGl) instance).buildBundleResolverFromPath(additionalBundle));
        // lowest priority default resolver for HostPageBaseURL(origin)
        instance.bundleResolver.add(new HttpBundleResolver());
        return platformInternals;//(Platform) Platform.instance;
    }
    
    /*public static Platform getInstance() {
        if (instance == null || !(instance instanceof PlatformWebGl)) {
            instance = new PlatformWebGl();
        }
        return instance;
    }*/

    @Override
    public NativeSceneNode buildModel() {
        return buildModel(null);
    }

    @Override
    public NativeSceneNode buildModel(String name) {
        WebGlSceneNode n = new WebGlSceneNode(name);
        //MA17 native2nativewrapper.put(n.getUniqueId(), n);
        return n;
    }

    /**
     * 12.1.18: Has a branch for threejs internal GLTF loader and our custom.
     * This is a good location, because for threejs data doesn't need to be in a bundle,
     * because data is reloaded anyway. Thus AsyncHelper doesn't help here.
     * 18.10.23: No more 'ac', so only gltf any more.
     */
    @Override
    public void buildNativeModelPlain(BundleResource file, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {
        int delegateid = AbstractSceneRunner.getInstance().invokeLater(delegate);

        //logger.debug("buildNativeModel " + file + ", delegateid=" + delegateid);
        // In WebGL kann das "natuerlich" async gehen. Das mach ich aber erstmal nicht fuer die eigenen, die weiter pseudo async.
        //BuildResult r = ModelLoader.buildModelFromBundle(filename);
        //delegate.modelBuilt(r);
        //TODO: Den Abzweig besser unterbringen. LoaderRegistry
        //TOD Fehlerbehandlung. Wenn der load scheitert, steht das nur in der JS Console
        //10.10.17: Beim 777 CDU überzeugt die Darfstellung im Verglcih zum eigenen AC Loader aber (noch) nicht
        //28.3.18: Der interne bleibt aber fuer Vergleichstests wichtig
        //4.4.18: Beim internen scheine auch die AGAnimations nicht zu gehen.
        boolean usethreejsgltfloader = false;
        if (!file.getExtension().equals("gltf")) {
            Util.nomore();
        }
        if (usethreejsgltfloader) {
            String basename = file.getBasename();
            BundleResource gltfile = new BundleResource(file.bundle, file.path, basename + ".gltf");
            logger.debug("probing " + gltfile);
            if (gltfile.bundle.exists(gltfile)) {
                logger.debug("using ThreeJS async gltf instead of ac");
                //String bundlebasedir = BundleRegistry.getBundleBasedir(file.bundle.name, true);
                String bundlebasedir = BundleResolver.resolveBundle(file.bundle.name, Platform.getInstance().bundleResolver).getPath();
                // TODO don't use filename/bundlebasedir for HTTP part
                BundleResource resource = new BundleResource(bundlebasedir + "/" + gltfile.getFullName());
                WebGlLoader.loadGLTFbyThreeJS(resource, delegateid, basename);
                return;
            } else {
                logger.debug("" + gltfile + " not found");
            }
        }
        // build model like in all other platforms.
        AsyncHelper.asyncModelBuild(file, opttexturepath, options, delegateid);
    }

    /*MA36 now in SceneRunner
    public void loadBundle(String bundlename, BundleLoadDelegate delegate, boolean delayed) {
        WebGlResourceManager.getInstance().loadBundlePlatformInternal(bundlename, delegate, delayed);
    }*/

    /**
     * Points to HostPageBaseURL(origin) without full qualified URL.
     * Uses AsyncHelper similar to other platforms for forwarding the result to have a consistent program flow.
     */
    @Override
    public void httpGet(String url, List<Pair<String, String>> params, List<Pair<String, String>> header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {

        logger.debug("httpGet url=" + url);
        // 10.11.23: RequestBuilder only returns text data (internal pre conversion?). So prefer the low level XMLHttpRequest
        XMLHttpRequest request = XMLHttpRequest.create();
        request.open("GET", url);
        // Add a header to prevent the browser from deciding no preflight is needed
        // because of allegedly standard request. These might fail later due to some 'who knows what browser think'.
        // 18.12.23: There seems to be no such header and no way to force a preflight. Instead this header leads to an error
        // with devmode->jetty/bundle apache
        //request.setRequestHeader("X-forcecpreflight","v");
        request.setResponseType(XMLHttpRequest.ResponseType.ArrayBuffer);
        request.setOnReadyStateChange(xhr -> {
            if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                // 2xx is considered ok
                // response needs to be send in any case. Otherwise eg. bundle loading might wait infinitely. Codes can differ between devmode and
                // compiled.
                AsyncHttpResponse r;
                if (xhr.getStatus() >= 300) {
                    logger.error("XHR Status code " + xhr.getStatus() + " for url " + url);
                    r = new AsyncHttpResponse(xhr.getStatus(), null, null);
                } else {
                    ArrayBuffer buffer = xhr.getResponseArrayBuffer();
                    if (buffer == null) {
                        logger.error("no data (CORS problem?) from url " + url);
                        r = new AsyncHttpResponse(xhr.getStatus(), null, null);
                    } else {
                        logger.debug("onReadyStateChange for " + url + ". size=" + buffer.byteLength());
                        r = new AsyncHttpResponse(xhr.getStatus(), null, new WebGlByteBuffer(buffer));
                    }
                }
                NativeFuture<AsyncHttpResponse> future = new WebGlFuture<AsyncHttpResponse>(r);
                sceneRunner.addFuture(future, asyncJobDelegate);
            }
        });
        request.send();
    }

    @Override
    public List<NativeSceneNode> findSceneNodeByName(String name) {
        return WebGlScene.webglscene.getObjectByName(name);
    }

    @Override
    public NativeMesh buildNativeMesh(NativeGeometry nativeGeometry, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        return WebGlMesh.buildMesh(((WebGlGeometry) nativeGeometry).geometry, (WebGlMaterial) material, castShadow, receiveShadow, false);
    }

    @Override
    public NativeSceneNode buildLine(Vector3 from, Vector3 to, Color color) {
        Vector3Array vertices = buildVector3Array(2);
        vertices.setElement(0, from);
        vertices.setElement(1, to);
        Vector2Array uvs = buildVector2Array(2);
        uvs.setElement(0, new Vector2());
        uvs.setElement(1, new Vector2());
        NativeGeometry geometry = WebGlGeometry.buildGeometry(vertices, new int[]{0, 1}, uvs, vertices);
        //MaterialDefinition materialDefinition = new MaterialDefinition("mat", Material.buildColorMap(color), null, null);

        NativeMaterial material = WebGlMaterial.buildMaterial("mat", Material.buildColorMap(color), null, null, null);
        WebGlMesh mesh = WebGlMesh.buildMesh(((WebGlGeometry) geometry).geometry, (WebGlMaterial) material, false, false, true);
        WebGlSceneNode n = new WebGlSceneNode("");
        n.setMesh(mesh);
        return n;
    }

    @Override
    public void updateMesh(NativeMesh mesh, NativeGeometry nativeGeometry, NativeMaterial material) {
        ((WebGlMesh) mesh).updateMesh(nativeGeometry, material);
    }

    @Override
    public NativeByteBuffer buildByteBuffer(int size) {
        //return new JmeVector3Array(size);
        NativeByteBuffer buf = new WebGlByteBuffer(size);
        return buf;
        //return new Vector3Array(buf,0,size);
    }
    
    /*public NativeColor buildColor(double r, double g, double b, double a) {
        return new WebGlColor(r, g, b, a);
    }*/

    /*public NativeScene buildScene() {
        return new WebGlScene();
    }*/

    public NativeCamera buildPerspectiveCamera(double fov, double aspect, double near, double far) {
        //TODO den scene Parameter woanders herholen
        WebGlCamera camera = WebGlCamera.buildPerspectiveCamera((WebGlScene) Scene.getCurrent().scene, fov, aspect, near, far);
        return camera;
    }

    @Override
    public NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap<String, NativeTexture> texture, HashMap<NumericType, NumericValue> params, Object/*Effect*/ effect) {
        return WebGlMaterial.buildMaterial(name, color, texture, params, (Effect) effect);
    }

    private NativeTexture buildNativeTextureWebGl(/*2.1.24BundleResource*/URL filename, HashMap<NumericType, NumericValue> params) {
        //logger.debug("buildNativeTextureWebGl " + filename.getFullName());
        //20.8.23:How is bundle name handled?
        WebGlTexture texture = WebGlTexture.loadTexture(filename/*.getFullName()*/);
        NumericValue wraps = params.get(NumericType.TEXTURE_WRAP_S);
        if (wraps != null) {
            if (wraps.equals(NumericValue.REPEAT))
                texture.setWrapS();
        }
        NumericValue wrapt = params.get(NumericType.TEXTURE_WRAP_T);
        if (wrapt != null) {
            if (wrapt.equals(NumericValue.REPEAT))
                texture.setWrapT();
        }
        //kann man nicht richtig pruefen, weils irgendwie async laeuft.
        //logger.debug("buildNativeTexture "+texture.texture);
        return texture;
    }

    @Override
    public NativeTexture buildNativeTexture(/*2.1.24BundleResource*/URL filename, HashMap<NumericType, NumericValue> parameters) {
        logger.debug("buildNativeTexture " + filename);
       /* if (filename.bundle == null) {
            logger.error("buildNativeTexture:bundle not set for file " + filename.getFullName());
            return null;//defaulttexture;
        }*/
        //String bundlebasedir = BundleRegistry.getBundleBasedir(filename.bundle.name, true);
        // don't use filename/bundlebasedir for HTTP part, ie. resolving again via bundle name might end in default bundle resolver point to origin.
        /*String bundlebasedir;
        if (filename.bundle.getBasePath().startsWith("http")) {
            bundlebasedir = filename.bundle.getBasePath();
        } else {
            bundlebasedir = BundleResolver.resolveBundle(filename.bundle.name, Platform.getInstance().bundleResolver).getPath();
        }
        logger.debug("bundlebasedir=" + bundlebasedir);

        BundleResource resource = new BundleResource(bundlebasedir + "/" + filename.getFullName());
        return buildNativeTextureWebGl(resource, parameters);*/
        return buildNativeTextureWebGl(filename, parameters);
    }

    @Override
    public NativeTexture buildNativeTexture(ImageData imagedata, boolean fornormalmap) {
        return WebGlTexture.createTexture(imagedata);
    }

    @Override
    public NativeTexture buildNativeTexture(NativeCanvas canvas) {
        WebGlTexture texture = WebGlTexture.buildFromCanvas(((WebGlCanvas) canvas));
        return texture;
    }

    @Override
    public NativeLight buildPointLight(Color argbcolor, double range) {
        return WebGlLight.buildPointLight(argbcolor.getARGB(), range);
    }

    @Override
    public NativeLight buildAmbientLight(Color argbcolor) {
        return WebGlLight.buildAmbientLight(argbcolor.getARGB());
    }

    @Override
    public NativeLight buildDirectionalLight(Color argbcolor, Vector3 direction) {
        return WebGlLight.buildDirectionalLight(argbcolor.getARGB(), WebGlVector3.toWebGl(direction).vector3);
    }

    @Override
    public NativeGeometry buildNativeGeometry(Vector3Array vertices, /*List<* /Face3List*/int[] indices, Vector2Array uvs, Vector3Array normals) {
        return WebGlGeometry.buildGeometry((Vector3Array) vertices, indices, (Vector2Array) uvs, (Vector3Array) normals);
    }


   /*MA36 @Override
    public NativeSceneRunner getSceneRunner() {
        //19.10.18: Das durfte doch Unsinn sein,oder? TODO
        if (scenerunner == null) {
            scenerunner = new WebGlSceneRunner();
        }
        return scenerunner;
    }*/

    @Override
    public Log getLog(Class clazz) {
        return logfactory.getLog(clazz);
    }

    @Override
    public NativeDocument parseXml(String xmltext) throws XmlException {
        return new GwtDocument(XMLParser.parse(xmltext));
    }

    @Override
    public NativeCanvas buildNativeCanvas(int width, int height) {
        return WebGlCanvas.create(width, height);
    }

    @Override
    public NativeContentProvider getContentProvider(char type, String location, TestPdfDoc docid) {
        // Das mit dem Sample ist erstmal nur ne Krücke
        return new SampleContentProvider(11);
    }

   /* @Override
    public NativePdfDocument buildNativePdfDocument(String name) {
        throw new RuntimeException("not yet");
    }*/

    /*1.3.17 @Override
    public ImageData buildTextImage(String text, Color textcolor, String font, int fontsize) {
        //TODO color, ob die Groesse so passt, muss sich noch zeigen.
        Dimension size = ImageFactory.calcSize(text.length(), fontsize);
        logger.debug("buildTextImage width="+size.width+",height="+size.height);
        WebGlCanvas canvas = WebGlCanvas.create(size.width,size.height);
        //canvas.drawImage(image, 0, 0);
        //TODO PArameter um halbwegs zu zentrieren
        canvas.drawString(text, 5, 5, font, fontsize);
        return canvas.getImageData();
    }*/

    @Override
    public NativeSplineInterpolationFunction buildNativeSplineInterpolationFunction(double[] x, double[] y) {
        throw new RuntimeException("not yet");
    }

    @Override
    public NativeRay buildRay(Vector3 origin, Vector3 direction) {
        return WebGlRay.buildRay(origin, direction);
    }

    /*@Override
    public boolean exists(NativeResource file) {
        return (Boolean) Util.notyet();
    }*/

    @Override
    public float getFloat(byte[] buf, int offset) {
        return WebGlCommon.getFloat(buf, offset);
    }

    @Override
    public void setFloat(byte[] buf, int offset, float f) {
        WebGlCommon.setFloat(buf, offset, f);
    }

    @Override
    public double getDouble(byte[] buf, int offset) {
        Util.notyet();
        return 0;
    }

    @Override
    public NativeStringHelper buildStringHelper() {
        return new WebGlJavaStringHelper();
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
    public Point getMouseClick() {
        return AbstractSceneRunner.getInstance().getMouseClick();
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
    public Point getMousePress() {
        return AbstractSceneRunner.getInstance().getMousePress();
    }

    @Override
    public String getName() {
        return "WebGL";
    }

    @Override
    public boolean hasOwnAsync() {
        return true;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public NativeEventBus getEventBus() {
        return eventbus;
    }

    @Override
    public void abort() {
        //MiscWrapper.alert("abort");
        //26.4.17: geht nicht MiscWrapper.printStackTrace();

    }

    /**
     * GWT JsonParser doesn't like line breaks!
     */
    @Override
    public NativeJsonValue parseJson(String jsonstring) {
        JSONValue parsed = JSONParser.parseStrict(jsonstring);
        if (parsed == null) {
            logger.error("parseStrict failed");
            return null;
        }
        NativeJsonValue value = WebGlJsonValue.buildJsonValue(parsed);
        return value;
    }

    /**
     * Dev Mode ist Entwicklung, über HTTP Server ist Production.
     *
     * @return
     */
    @Override
    public boolean isDevmode() {
        return isDevmode;
    }

   /* @Override
    protected Log getLog() {
        return logger;
    }*/

    @Override
    public NativeVRController getVRController(int index) {

        if (((WebGlSceneRunner) AbstractSceneRunner.getInstance()).sceneRenderer.renderer == null) {
            return null;
        }
        JavaScriptObject controller = ((WebGlSceneRunner) AbstractSceneRunner.getInstance()).sceneRenderer.renderer.getController(index);
        if (controller == null) {
            return null;
        }
        return WebGlVRController.buildController(controller);
    }

    @Override
    public NativeRenderProcessor buildSSAORenderProcessor() {
        return null;
    }

    @Override
    public void addRenderProcessor(NativeRenderProcessor renderProcessor) {

    }

    @Override
    public NativeSocket connectToServer(Server server) {
        // websocket port is one higher than base port (unix socket). But don't increase it here implicitly. The property should be set accordingly.
        return WebGlSocket.buildSocket(server.getHost(), server.getPort() /* + 1*/, server.getPath());
    }

    @Override
    public NativeScene getScene() {
        return nativeScene;
    }

    /**
     * Mal etwas zeitgemaesser? Z.B. fuer REST.
     * Das ist aber nicht mehr fuer binary.
     * 10.11.23: Shouldn't this be a 'httpPost'?
     * TODO: use AsyncHelper for forwarding the result for consistent program flow and merge with httpGet
     * <p>
     * 17.5.2020
     */
    public static void sendHttpRequest(String suburl, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> /*HttpRequestDelegate*/ loadlistener) {
        //logger.debug("loadRessource:" + ressource.getFullName());
        //30.9.18: Mit base url prefix
        final String url = GWT.getHostPageBaseURL() + suburl;
        String cachedisablequerystring = "?timestamp=" + Platform.getInstance().currentTimeMillis();


        XMLHttpRequest request = XMLHttpRequest.create();
        //request.open("GET", "http://127.0.0.1:8888/"+name);
        request.open("GET", url/*name*/);
        request.setResponseType(XMLHttpRequest.ResponseType.ArrayBuffer);

        request.setOnReadyStateChange(new ReadyStateChangeHandler() {

            @Override
            public void onReadyStateChange(XMLHttpRequest xhr) {
                if (xhr.getReadyState() == XMLHttpRequest.DONE) {

                    if (xhr.getStatus() == 200) {
                        ArrayBuffer buffer = xhr.getResponseArrayBuffer();
                        if (BundleRegistry.bundledebuglog) {
                            //logger.debug("onReadyStateChange. size=" + buffer.byteLength());
                        }
                        Uint8Array array = TypedArrays.createUint8Array(buffer);

                        loadlistener.completed(new AsyncHttpResponse(xhr.getStatus(), HttpHelper.buildHeaderList(), new WebGlByteBuffer(buffer)));


                    } else {
                        //logger.error("XHR Status code " + xhr.getStatus() + " for resource with url " + url);
                        loadlistener.completed(new AsyncHttpResponse(xhr.getStatus(), HttpHelper.buildHeaderList()));
                    }
                }
            }
        });
        request.send();
    }

    @Override
    public NativeAudioClip buildNativeAudioClip(BundleResource br) {
        String bundlebasedir = BundleResolver.resolveBundle(br.bundle.name, Platform.getInstance().bundleResolver).getPath();
        // TODO don't use filename/bundlebasedir for HTTP part
        BundleResource resource = new BundleResource(bundlebasedir + "/" + br.getFullName());

        WebGlAudioClip audioClip = WebGlAudioClip.loadFromBundle(resource);
        return audioClip;
    }

    @Override
    public NativeAudio buildNativeAudio(NativeAudioClip audioClip) {
        WebGlAudio audio = WebGlAudio.createAudio((WebGlAudioClip) audioClip);
        return audio;
    }

    @Override
    public NativeBundleResourceLoader buildResourceLoader(String bundlename, String location) {
        if (location != null && StringUtils.startsWith(location, "http")) {
            return new HttpBundleResourceLoader(location + "/" + bundlename);
        }
        // let resolver decide to cover HOSTDIR a.s.o. instead of hard coded GWT.getHostPageBaseURL()
        ResourcePath bundlebasedir = BundleResolver.resolveBundle(bundlename, bundleResolver);
        logger.debug("bundlebasedir=" + bundlebasedir.path);
        return new HttpBundleResourceLoader(bundlebasedir.path);
    }

    public NativeInitChain buildInitChain(InitExecutor initExecutor) {
        return new WebGlInitChain(initExecutor/*,new WebGlAsyncRunner()*/);
    }

    /**
     * Used to parse 'ADDITIONALBUNDLE'
     */
    private List<BundleResolver> buildBundleResolverFromPath(String bundlepathFromEnv) {
        List<BundleResolver> l = new ArrayList<BundleResolver>();

        if (bundlepathFromEnv != null) {
            String[] parts = StringUtils.split(bundlepathFromEnv, ":");
            for (int i = 0; i < parts.length; i++) {
                // base64 natively uses '+','=' and '/' and might replace these by URL conform letters like '-'. Very confusing
                // and finally not very helpful. So also accept pure (URL encoded) strings. But these conflict with ':' separator.
                // So for now stay with base64.
                String subPart = parts[i];
                logger.debug("Found bundle sub path " + subPart);
                if (false && StringUtils.contains(subPart, "@http")) {
                    l.add(new HttpBundleResolver(subPart));
                } else {
                    l.add(new HttpBundleResolver(WebGlCommon.atob(subPart)));
                }
            }
        }
        return l;
    }
}


