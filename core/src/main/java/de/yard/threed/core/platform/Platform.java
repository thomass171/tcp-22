package de.yard.threed.core.platform;

import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Extrahiert for Modul core, wo Logging und StringHelper gebraucht wird.
 * <p>
 * Rename "Platform"->"PlatformCore". No good idea because its abstract.
 * <p>
 * Created by thomass on 12.12.18.
 */
public abstract class Platform {

    protected NativeEventBus eventBus = null;

    //25.4.20 jetzt eins höher. Gab es mal eine Zeit lang nicht, ist aber gut um Logger verwenden zu koennen,
    //die es im Modul gar nicht gibt.
    //16.6.21: Moved here from EnginePlatform
    public NativeLogFactory logfactory;

    public NativeScene nativeScene;

    public List<BundleResolver> bundleResolver = new ArrayList<BundleResolver>();

    // we need to know all shader materials for updating light properties
    public List<RegisteredShaderMaterial> shaderMaterials = new ArrayList<RegisteredShaderMaterial>();

    protected static Platform instance;

    // Added to have Scenerunner available in all platforms (like SimpleHeadlessPlatform)
    // Might help to hide AbstractSceneRunner.getInstance() to apps.
    public NativeSceneRunner sceneRunner;

    // 24.10.23 now here
    public Log logger;

    public static Platform getInstance() {
        return instance;
    }

    /**
     * 16.9.16: Die Object3D Komponente wird immer direkt mit erstellt.
     * 18.10.23: TODO Rename to buildSceneNode.
     *
     * @return
     */
    public abstract NativeSceneNode buildModel();

    public abstract NativeSceneNode buildModel(String name);

    /**
     * 15.9.17: Let the platform load a model async. This might be very efficient. Threejs/gltfloader is a good reference.
     * <p>
     * Successor of ModelFactory.buildModelFromBundle().
     * Not for FG XML model!
     * Due to async no exception here but only information via d delegate. The delegate should be called exactly once in every case!
     * 04.10.2018: Even though it is async it is not multithreaded.
     * 18.10.23: No more 'ac', so only gltf any more.
     * 10.11.23: Even though most platforms just use the custom {@ModelLoader}, the entry point is here because a platform *might* have a better GLTF
     * loader. The threejs gltf loader currently is the only platform provided loader and is disabled by default because
     * it cannot handle external material (FG terrain). So
     * this method might be useless at the moment. But it might be an option in the future.
     * 15.2.24: Decoupled from bundle(Resource)
     * 11.10.24: In case of an error no node should be passed to delegate.
     * 06.02.25: Has no option for a material factory because that would be specific for our GLTF loader.
     */
    public abstract void buildNativeModelPlain(ResourceLoader resourceLoader, ResourcePath opttexturepath, ModelBuildDelegate modeldelegate, int options);

    /**
     * Das Bundle wird in jedem Fall asynchron, aber nicht multithreaded ueber die Platform geladen.
     * async analog zu Modeln
     * 20.2.18: Wenn ein Bundle schon geladen wurde, wird es nicht doppelt geladen (Eine Race Condition gibt es aber trotzdem).
     * Das Verhalten ist unabhaengig davon, ob das Model schon geladen wurde oder nicht.
     *
     * 22.7.21: Aus Platform hier hin. Nicht static. Muss von webgl overrided werden!
     * GHenerell async aber ohne MT.
     */
    /*public abstract void loadBundle(String bundlename, BundleLoadDelegate bundleLoadDelegate, boolean delayed);/* {
        AsyncHelper.asyncBundleLoad(bundlename, AbstractSceneRunner.getInstance().invokeLater(bundleLoadDelegate), delayed);
    }* /

    public  void loadBundle(String bundlename, BundleLoadDelegate loadlistener) {
        loadBundle(bundlename, loadlistener, false);
    }*/

    /**
     * Kann auch destroyte enthalten, weil Unity recursiv destroyed.
     * TODO cleanup auf destroyte.
     * MA17: Wird das noch gebraucht? In SceneNode habe ich doch auch einen find. Der ist aber relativ.
     * Dieser hier ist global, jetzt aber ueber die Platform.
     *
     * @param name
     * @return
     */
    public abstract List<NativeSceneNode> findSceneNodeByName(String name);

    /**
     * Entfernen aus Tree, destroy inkl. aller children.
     * 20.2.17: Das ist DIE Methode, um SceneNodes loszuwerden. Das umfasst auch die Childs der Node und das Freigeben von Speicher.
     * <p>
     * Analog zum Unity destroy(). Der macht auch einen destroy auf die Children.
     * Unsichtbar machen (disablen) ist was anderes.
     */
    public void removeSceneNode(NativeSceneNode object3d) {
        object3d.destroy();
    }

    /**
     * Ohne Material wird das Mesh wireframe.
     * 25.9.19: Geometry und Material müssen halbwegs zusammenpassen(zumindest in OpenGL gibt es sonst Probleme im Shader).
     * Sonst wird das Mesh ohne Material (wireframe) angelegt.
     * Z.B. geo ohne normals geht nur mit unshaded, geo ohne uv geht nicht mit Texture.
     * Eine echte Prüfung ist hier z.Z. aber nicht. Geht evtl. nicht so einfach.
     */
    public NativeMesh buildMesh(NativeGeometry nativeGeometry, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        return buildNativeMesh(nativeGeometry, material, castShadow, receiveShadow);
    }

    public abstract NativeMesh buildNativeMesh(NativeGeometry nativeGeometry, NativeMaterial material, boolean castShadow, boolean receiveShadow);

    /**
     * Weil "line" etwas doch spezielles ist und sicherlich kein Mesh, keine Geo braucht und kein Material, bekommt es einen eigene API Call.
     */
    public abstract NativeSceneNode buildLine(Vector3 from, Vector3 to, Color color);

    /**
     * Ein Meshupdate analog zum Bauen.
     * Faces und/oder vertices darf auch null sein, dann erfolgt dafuer
     * kein Update. Entsprechend darf auch material null sein, dann bleibt das
     * alte.
     * <p/>
     * Das ganze geht nur auf Meshes, die als dynamic definiert sind (vor allem wegen ThreeJS).3.5.16:Stimmt das noch?
     *
     * @return
     */
    public abstract void updateMesh(NativeMesh mesh, NativeGeometry nativeGeometry, NativeMaterial material);


    /**
     * Eine <b>zusaetzliche</b> Camera anlegen.
     *
     * @return
     */
    public abstract NativeCamera buildPerspectiveCamera(double fov, double aspect, double near, double far);

    /**
     * TODO Solve Dependency on Effect.
     * Der name hat keine funktionale Bedeutung. Er dient nur der Wiedererkennbarkeit.
     * The names (keys) in the texture map must match to the uniforms defined in the Effect.
     *
     * @param color
     * @param texture
     * @param parameters
     * @return
     */
    public abstract NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap</*TextureType*/String, NativeTexture> texture,
                                                 HashMap<NumericType, NumericValue> parameters /*MA36  Effect Object effect*/);
    //return buildMaterial(new MaterialDefinition(name,color,texture,parameters),effect);

    /**
     * material name is taken from program or can later be set by setName()
     */
    public abstract NativeMaterial buildMaterial(NativeProgram program, boolean opaque);

    /**
     * Ob das so bleiben kann ist fraglich wegen analogie zu Ajax. Aber warum nicht? Ajax ist nur ThreeJS, und da erfolgt das laden transparent im
     * Hintergrund. Bekommt man nichts von mit und irgendwann ist die Textur da.
     * Geht ueber den RessourceManager bzw NativeResource, um zu verdeutlichen, das nicht alles ein Filezugriff ist.
     * Alles aber ohne unnoetig Grafiken zwischenzuspeichern! Die sind nachher ueber kurz oder lang ausschliesslich in der GPU.
     * 15.9.16: Wenn ein Fehler auftritt, wird eine platformabhaengige Standardtextur geliefert, damit keine NPE
     * auftritt. null wird nie geliefert.
     * 5.10.18: protected, weil von aussen nur loadTexture() aufgerufen werden soll.
     * 3.1.19: Das mit der Standardtextur ist doch eine doofe FehlerKaschierung. Das soll der Aufrufer doch pruefen und
     * im Zweifel ohne Material (wireframe) anlegen. Also, liefert jetzt null im Fehlerfall.
     * 19.2.24: No need to change to NativeResource because URL is just more generic. At the end it might not be a difference.
     */
    public abstract NativeTexture buildNativeTexture(/*2.1.24BundleResource*/URL filename, HashMap<NumericType, NumericValue> parameters);

    /**
     * Etwas doof: Unity muss wissen, ob die Textur eine NormalMap ist, weil die anders gespeichert wird.
     *
     * @param imagedata
     * @return
     */
    public abstract NativeTexture buildNativeTexture(ImageData imagedata, boolean fornormalmap);

    public abstract NativeTexture buildNativeTexture(NativeCanvas imagedata);

    public abstract NativeLight buildPointLight(Color argb, double range);

    public abstract NativeLight buildAmbientLight(Color argb);

    /**
     * There is no position
     * direction is the position from where the light is coming, related to (0,0,0). So
     * it is the vector from 0,0,0 to the light origin.
     * <p>
     * Also configured to cast shadows.
     */
    public abstract NativeLight buildDirectionalLight(Color color, Vector3 direction);

    public abstract List<NativeLight> getLights();

    /**
     * Hier kommen schon fertige Dreiecke (Face3) rein, da:
     * - JME eh keine eigene Trianglation hat,
     * - die von ThreeJS auch auf Standardalgorithmen basieren, die ich selber genauso einbauen kann
     * - Meine CustomGeometry ohnehin schon Face3 oder Face4 enthält.
     * Es kann mehrere Surfaces (also Listen von Dreiecken) geben.
     * <p/>
     * Normale mussen in den Faces nicht unbedingt enthalten sein. Dann werden sie in der Plattform berechnet.
     * Ein einzelner Vertex darf mit verschiedenenen Normalen verwendet werden. Dann muss die Platform diesen Vertex duplizieren
     * (oder einfach alle, was natürlich ineffizient ist). Aber man weiss ja nie, welche Art von fertigen Models man so aufgetischt bekommt.
     * Das ist eine Logik zum Duplizieren von Vertices sicher nicht verkehrt.
     * <p/>
     * 12.2.16: Warum es mehrere Facelisten geben kann, ist nicht mehr ganz klar. Das lässt sich aber gut für verschiedene Materialen im Mesh
     * nutzen, so wie es 3DS und OBJ Formate auch vorsehen.
     * <p/>
     * <p/>
     * 13.2.16: Die Normalen liegen in den Faces oder werden explizit als Liste geliefert. Wenn es die Liste normals nicht gibt und auch
     * in den Faces keine gibt, dann erst berechnet die Platform. Ob eine explizite normal Liste wirklich benutzt wird,
     * ist aber noch unklar.
     * Also: 3DS hat keine Normalen, sondern smoothing groups, OBJ hat die Normalen am Face, da muss man selber smoothing groups verwenden/bilden. AC hat
     * anscheinend ueberhaupt keine Normalen, die muss man berechnen und evtl. auch selber smoothen. Lediglich BTG enthält wohl Normale per Vertex. Aber gut,
     * dann lassen wir den Parameter halt drin, und wenn es nur für BTG ist.
     * <p/>
     * Wenn es weniger Material als Face Listen gibt, wird irgendeine der übergebenen verwendet.
     * <p/>
     * 16.2.16: Wirklich nur Face3 zulassen. Es gibt eine Conviniencemethode mit allen Faces, die dann trianguliert und hier weitermacht.
     * 10.3.16: Da auch WebGl und Opengl keine multiple material koennen, den extract aus JME hierauf gezogen. Auch weil es erstmal ganz gut funktioniert.
     * 29.4.16: Wenn Facenormale vorliegen (auch wenn sie in der Platform generiert wurden), wird pro Face3List ein Smoothing durchgeführt (und keine Vertices dupliziert).
     * 02.05.16: Das smoothing zunächst mal in buildMeshG() verlegt. D.h. hier kommen i.d.R. normals rein. Und die Conveniencemethode in die Engine.
     * 02.05.16: Im Sinne der Vereinfachung generell keine multiple material vorsehen, auch wenn Unity es kann. Die Engine extrahiert jetzt Submeshes.
     * 13.7.16: Das mit mehreren Indexlisten fuer multiple material wird doch gar nicht mehr verwendet. Sowas wird doch jetzt immer gesplittet. Darum kommt
     * hier jetzt auch nur noch EINE Face3List rein. Und ein Smoothing bzw. generell die Normalenberechnung soll/muss schon erfolgt sein.
     * 24.7.16: Dann koennen statt Face3 auch direkt die Indizes reinkommen. Datentyp Array wegen Unity. Geht noch nicht wegen uvs. Soll aber mittelfristig.
     * 3.5.19: normals sind optional. Wenn es keine gibt, entscheidet die Platform, z.B. für Flat Shading.
     */
    public abstract NativeGeometry buildNativeGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals);

    public abstract NativeDocument parseXml(String xmltext) throws XmlException;

    public abstract NativeContentProvider getContentProvider(char type, String location, TestPdfDoc docid);

    /**
     * Wo liegen denn x/y? Zentriert oder an einer Ecke?
     * 24.5.16: Etwas vereinfacht: Ohne x/y, wenn es der Aufrufer irgendwo in einem Image positionieren moechte, kann er das
     * mit der overlay Methode machen. Bekommt jetzt auch kein Image mehr zum reinzeichnen. In dem gelieferten Image steht der
     * Text halbwegs zentriert. Die gelieferte Groesse ist nicht konkret definiert.
     * Das gelieferte Image soll da, wo kein Text ist, transparent sein, um auf jede Textur gelegt werden zu können.
     * 1.3.17: Vorläufig deprecated, weil Unity sowas eh nicht kann. Dann kann "gui.Text" auch generell die Fontmap nutzen.
     * Das fehlt dann zwar für "Office", aber, naja, da muss für Unity eh eine andere Lösung her, evtl. per PDF dingens.
     * Siehe auch Wiki "Textdarstellung"
     */
    //@Deprecated
    //public abstract ImageData buildTextImage(/*ImageData image, */String text/*, int x, int y*/, Color textcolor, String font, int fontsize);
    public abstract NativeSplineInterpolationFunction buildNativeSplineInterpolationFunction(double[] x, double[] y);

    public abstract NativeRay buildRay(Vector3 origin, Vector3 direction);

    /**
     * 4.7.21: Das ist doch FS bezogen fuer FileSystemResource und damit ne Kruecke. Gibts in webgl gar nicht. Raus damit. MA36
     */
    //public abstract boolean exists(NativeResource file);

    /**
     * SceneRunner is a singleton. It is created initially when starting a scene.
     * 28.3.23: getter maybe not needed
     */
    /*10.7.21 public abstract NativeSceneRunner getSceneRunner();*/
    public abstract NativeCanvas buildNativeCanvas(int width, int height);


    public abstract long currentTimeMillis();

    /**
     * Returns true when a key went down.
     * Only available in exactly one frame (update().
     */
    public abstract boolean getKeyDown(int keycode);

    /**
     * Returns true when a key went up (was released).
     * Only available in exactly one frame (update()).
     */
    public abstract boolean getKeyUp(int keycode);

    /**
     * Available as long as the key is pressed (also in following frames (update())).
     */
    public abstract boolean getKey(int keycode);

    /**
     * Returns absolute position ((0,0) lower left) to where mouse moved, but only when there was a movement.
     * Without movement null is returned.
     * 8.4.16: (0,0) was top left once, but is lower left now, weil Unity das auch so macht. Und es gibt Aussagen, dass
     * "in 3D y von unten nach oben läuft". openGl convention?
     * 14.5.19: Works the same with "Drag" (move with pressed button or touchmove).
     *
     * @return
     */
    public abstract Point getMouseMove();

    /**
     * Returns the release position when the button was released. Otherwise null.
     * Its not the 'press' to be prepared for drag handling.
     * Only available in exactly one frame (update()).
     * Without button number due to touchpads.
     * (0,0) ist wie bei getMouseMove() links unten.
     *
     * @return
     */
    public abstract Point getMouseUp();

    /**
     * Important for drag and touchmove.
     */
    public abstract Point getMouseDown();

    public abstract String getName();

    /**
     * Nachbildung von System proerties. Koennte ganz praktisch sein, um z.B. auf Android auf eine FG Installation
     * auf einer SD Karte zuzugreifen. Dann koennen Properties wie HOME, FG_ROOT etc hierüber definiert werden.
     * Die Nutzung sollte aber nicht überhand nehmen, um nicht zu viele Abhängigkeiten und Unwägbarkeiten zu haben.
     * Inwiefern es aber in WebGL oder Unity praktikabel ist, muss sich noch zeigen.
     * 16.11.16: Das mit dem setter ist zumindest nicht praktikabel, weil manche Props schon während des Platforminit gebraucht
     * werden koennten (CAHCEDIR). Darum zusätzlich Props beim init der Platform reingeben. Den setter brauchts trotzdem,
     * weil manche Properties nachher abhängig von der Platform gesetztw erden (z.B. Unity handheld).
     * <p>
     * 5.2.23: What is the latest design for properties/configuration? These methods? Or configuration? Or both?
     * It seems to switch to configuration that is supplied by
     * the platform to be more future ready.
     */
    public abstract Configuration getConfiguration();

    /**
     * 17.2.23: TODO check move of eventbus to systemmanager.
     *
     * @return
     */
    public abstract NativeEventBus getEventBus();

    /**
     * Die Ausführung abbrechen und einen Stacktrace anzeigen. Für Analyse. Naja.
     */
    public abstract void abort();


    /**
     * Muesste eigentlich generisch sein, wird z.Z. aber nur fuer GLTF verwendet.
     * 22.9.21: Now like XML handling for replacing model mapper.
     * Assuming NativeJsonValue is fitting for all Json starting with '{' or '['
     *
     * @param jsonstring
     * @return
     */
    public abstract NativeJsonValue parseJson(String jsonstring);

    /**
     * Production oder Entwicklung. Production sollte bedeuten:
     * - kein Debug Log (kein extra Fenster in GWT)
     * - keine Framebegrenzung
     * - WebGL Fullscreen
     * 10.10.18: Umgekehrte Logik isProduction->isDevmode, also devmode muss explizit eingeschaltet werden.
     *
     * @return
     */
    //public abstract boolean isProduction();
    public abstract boolean isDevmode();

    /**
     * per convention: 0 isType left controller, 1 right).
     * <p>
     * Might return null.
     *
     * @param index
     * @return
     */
    public abstract NativeVRController getVRController(int index);

    /**
     * Screen Space Ambient Occlusion
     *
     * @return
     */
    public abstract NativeRenderProcessor buildSSAORenderProcessor();

    public abstract void addRenderProcessor(NativeRenderProcessor renderProcessor);

    public abstract Log getLog(Class clazz);

    public abstract NativeStringHelper buildStringHelper();

    public abstract float getFloat(byte[] buf, int offset);

    public abstract void setFloat(byte[] buf, int offset, float f);

    public abstract double getDouble(byte[] buf, int offset);

    //public abstract Vector3 buildVector3(float x, float y, float z);

    public Vector2Array buildVector2Array(int size) {
        return new Vector2Array(buildByteBuffer(size * 8), 0, size);
    }

    public Vector3Array buildVector3Array(int size) {
        NativeByteBuffer buf = buildByteBuffer(size * 12);
        return new Vector3Array(buf, 0, size);
    }

    public abstract NativeByteBuffer buildByteBuffer(int size);

    /**
     * Establish a (web)socket connection to an arbitray server (most likely a scene server). This is no "EventBus", because its peer2peer.
     * <p>
     * 15.2.21
     */
    public abstract NativeSocket connectToServer(Server server);

    /**
     * "response" will be null in case of network error (ie. no network connection and thus no response)
     */
    public abstract void httpGet(String url, List<Pair<String, String>> parameter, List<Pair<String, String>> header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate);

    /**
     * 23.7.21: NativeScene should be created initially in platform instead of Scenerunner
     */
    public abstract NativeScene getScene();

    public boolean hasOwnAsync() {
        return false;
    }

    public void addBundleResolver(BundleResolver bundleResolver) {
        this.bundleResolver.add(bundleResolver);
    }

    public void addBundleResolver(BundleResolver bundleResolver, boolean before) {
        this.bundleResolver.add(0, bundleResolver);
    }

    /**
     * Very generic way to control platform behaviour. Useful for debugging/analysis of problems.
     * Default implementation doing nothing.
     */
    public static String PLATFORM_OPTION_RENDEREDLAYER = "PLATFORM_OPTION_RENDEREDLAYER";

    public void setOption(String option, String value) {
        logger/*24.10.23 getLog()*/.warn("setOption not implemented: " + option);
    }

    public abstract NativeAudioClip buildNativeAudioClip(BundleResource filename);

    public abstract NativeAudio buildNativeAudio(NativeAudioClip audioClip);

    /**
     * Build a loader for loading a single resource(file) either from some web/HTTP bundle location or from
     * a local(HOSTDIR) bundle. Provides the option to get a single file from a bundle without loading the complete bundle.
     * This method is also used internally for loading a bundle!
     * <p>
     * "bundlename" to make clear its for bundle content loading with resolver (or abs HTTP).
     * if location is null, the resolver will be used.
     * Should location end with bundlename or not? Probably not, because bundlename is standalone parameter.
     * Examples:
     * - buildResourceLoader("engine", null) for loading from a local bundle
     * - buildResourceLoader("some-bundle", "http://somehost:8085/bundles") for loading from a remote bundle
     */
    public abstract NativeBundleResourceLoader buildResourceLoader(String bundlename, String location);

    /**
     * Do a recursive depth first search for a node like SceneNode.findNodeByName(String name, SceneNode startnode) does.
     * This is redundant and we already had this some time ago in the platform and removed it later. But the platform just has more options to do the search
     * more efficiently. SceneNode.findNodeByName() might be a performance killer (~40ms in JME) especially when used many times
     * like for FG animations.
     */
    public abstract List<NativeSceneNode> findNodeByName(String name, NativeSceneNode startnode);

    /**
     * Use latest most generic approach for getting resources (URL/resourceloader)?
     * Sounds nice but breaks sync workflow for material creation.
     */
    public abstract NativeProgram buildProgram(String name, BundleResource vertexShader, BundleResource fragmentShader);

    public void registerAndInitializeShaderMaterial(RegisteredShaderMaterial shaderMaterial) {
        shaderMaterials.add(shaderMaterial);
        shaderMaterial.updateLightUniforms(getLights());
    }

    public void updateShaderMaterials() {
        List<NativeLight> lights = getLights();
        for (RegisteredShaderMaterial m : shaderMaterials) {
            m.updateLightUniforms(lights);
        }
    }
}
