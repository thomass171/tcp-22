package de.yard.threed.engine.platform;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.*;
import de.yard.threed.engine.platform.common.*;

/**
 * Interface/Adapter to a Game/Grafik Engine. EG. JME, ThreeJS, Unity,...
 * <p>
 * 14.3.17: Jetzt kommt das Bauen von Primitives (z.B. buildCubeGeometry()) doch ganz raus. Es hat eigene Primitives.
 * <p>
 * Created by thomass on 20.04.15.
 */
public /*abstract*/ class EngineHelper /*extends Platform*/ {
    //MA36 public NativeSceneRunner runner;
    // die root node der ganzen Scene. Zeichnet sich dadurch aus, als einizige keinen parent zu haben.
    // Auch für Spiegelung in Unity
    // obwohl es ja auch mehrere Trees geben koennte?
    // sollte eigentlich private sein, aber machmal zu Tests ganz praktisch
    // 7.5.21: Wird für VR in den VR space verschoben.
    //MA36 moved to Scene private World world;
    // Textur, die im Fehlerfall geliefert wird.
    // 3.1.19: Das mit der Standardtextur ist doch eine doofe FehlerKaschierung. Das soll der Aufrufer doch pruefen und
    // im Zweifel ohne Material (wireframe) anlegen.
    //public NativeTexture defaulttexture;
    public static int LOADER_USEACPP = 0x01;
    //use GLTF instead of requested ac if available. LOADER_USEGLTF overrules LOADER_USEACPP
    public static int LOADER_USEGLTF = 0x02;
    // ThreeJS eg. has its own GLTF Loader. This can be used instead of the out own.
    public static int LOADER_USEPLATFORMNATIVEGLTF = 0x04;
    public static int LOADER_APPLYACPOLICY = 0x08;
    static Statistics statistics = new Statistics();


    // TextureMagFilter from gl3.h
    public static final int GL_NEAREST = 0x2600;
    public static final int GL_LINEAR = 0x2601;
    // extureMinFilter from gl3.h
    public static final int GL_NEAREST_MIPMAP_NEAREST = 0x2700;
    public static final int GL_LINEAR_MIPMAP_NEAREST = 0x2701;
    public static final int GL_NEAREST_MIPMAP_LINEAR = 0x2702;
    //GL_LINEAR_MIPMAP_LINEAR = TriLinear?
    public static final int GL_LINEAR_MIPMAP_LINEAR = 0x2703;
    public static final int TRILINEAR = GL_LINEAR_MIPMAP_LINEAR;


    /**
     * MA36: Only convenience? Made static
     * 18.10.23: core loader no more 'ac', so only gltf any more. ac file mapping extracted to tcp-flightgear.
     * 10.11.23: This wrapper seems useless now. Set to deprecated.
     */

    /**
     * MA36: Only convenience? Made static
     */
    /*19.2.24 public static void buildNativeModel(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate modeldelegate) {
        buildNativeModel(filename, opttexturepath, modeldelegate, 0);
    }*/


    /**
     * MA36: Only convenience? Made static
     */
    public static NativeMaterial buildMaterial(MaterialDefinition def, ShaderPool effect) {
        return Platform.getInstance().buildMaterial(def.name, def.color, def.texture, def.parameters);
    }



    /*public abstract void executeAsyncJobNurFuerRunnerhelper(AsyncJob job/*, NativeContentProvider contentprovider, int page* /);*/
    /*MA36 jetzt in runnerpublic void addAsyncJob(final AsyncJob job) {
        addAsyncJob(job, 0);
    }    public void addAsyncJob(final AsyncJob job, int delaymillis) {
        AbstractSceneRunner.getInstance().addNewJob(job, delaymillis);
    }*/

    public Dimension getDimension() {
        return AbstractSceneRunner.getInstance().dimension;
    }


    public static Statistics getStatistics() {
        return statistics;
    }

    public static void addAsyncJob(final AsyncJob job) {
        addAsyncJob(job, 0);
    }

    public static void addAsyncJob(final AsyncJob job, int delaymillis) {
        AbstractSceneRunner.getInstance().addNewJob(job, delaymillis);
    }

    /**
     * MA36: Only convenience? For statistics. Statistics also static.
     */
    public static NativeGeometry buildGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        statistics.vertices += vertices.size();
        statistics.geometries++;
        if (normals != null) {
            statistics.normals += normals.size();
        }
        statistics.indices += indices.length;
        if (uvs != null) {
            statistics.uvs += uvs.size();
        }
        return Platform.getInstance().buildNativeGeometry(vertices, indices, uvs, normals);
    }

    public static boolean isEnabled(String property) {
        Boolean b;
        if ((b = Platform.getInstance().getConfiguration().getBoolean(property)) != null && (boolean) b) {
            return true;
        }
        return false;
    }

    /**
     * Ein Laden anstossen und ueber Callback weitermachen.
     *
     * @return
     */
    /*11.10.18 gibt doch bundle public void loadResource(final NativeResource resource, ResourceLoadingListener loadlistener, boolean binary) {
        Platform.getInstance().getRessourceManager().loadRessource(resource, loadlistener,binary);
    }*/

    /**
     * 07.06.16: Eine Ressource synchron speichern. 
     * 16.10.18: das ist konzeptionell doch ueberholt.
     * @return
     */
    /*22.12.16 public void saveResourceSync(final NativeResource resource, byte[] data) throws ResourceSaveException {
        Platform.getInstance().getRessourceManager().saveResourceSync(resource, data);
    }*/
    /*public NativeOutputStream saveResourceSync(final NativeResource resource) throws ResourceSaveException {
        return Platform.getInstance().getRessourceManager().saveResourceSync(resource);
    }*/
    /**
     * Ein Laden erfolgt hier nicht.
     * Liefert null, wenn es keine gecachte Ressource mit diesem Namen gibt.
     *    * Aufruf von getCachedResource erhaeltlich. Oder auch nicht.
     * @param name
     * @return
     */
  /*24.12.15  public CachedResource getCachedResource(final String name) {

        return new CachedResource(cachedresource.get(name));
    }*/




   /*MA36 public World getWorld() {
        return world;
    }*/


    /*MA36 public void setWorld(World world){
        this.world = world;
    }*/


    /*public abstract byte[] serialize(Object obj);
    public abstract Object deserialize(byte[] obj);*/


    /**
     * One time init opportunity.
     * 25.4.20
     * @param nativeEventBus
     */
    /*MA36 public void setEventBus(NativeEventBus nativeEventBus) {
        if (eventBus != null) {
            throw new RuntimeException("event bus already exists");
        }
        eventBus = nativeEventBus;
    }*/

    /**
     * One time init opportunity.
     * 25.4.20
     */
    /*MA36 public void setLogFactory(NativeLogFactory nativeLogFactory) {
        if (logfactory != null) {
            throw new RuntimeException("logfactory already exists");
        }
        logfactory = nativeLogFactory;
    }*/


    /**
     * deprecated?
     * @param baseUrl
     * @return
     */
    /*MA36 public NativeWebClient getWebClient(String baseUrl) {
        return null;
    }*/


}
