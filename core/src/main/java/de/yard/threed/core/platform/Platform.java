package de.yard.threed.core.platform;

import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;

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

    public NativeSocket nativeSocket;

    //25.4.20 jetzt eins höher. Gab es mal eine Zeit lang nicht, ist aber gut um Logger verwenden zu koennen,
    //die es im Modul gar nicht gibt.
    //16.6.21: Moved here from EnginePlatform
    public NativeLogFactory logfactory;

    public NativeScene nativeScene;

    public NativeBundleLoader bundleLoader;

    public List<BundleResolver> bundleResolver = new ArrayList<BundleResolver>();

    protected static Platform instance;

    public static Platform getInstance() {
        return instance;
    }

    /**
     * 16.9.16: Die Object3D Komponente wird immer direkt mit erstellt.
     *
     * @return
     */
    public abstract NativeSceneNode buildModel();

    public abstract NativeSceneNode buildModel(String name);

    /**
     * 15.9.17: Ueber die Platform ein Model laden. Das kann sehr effizient sein. Und weil threejs/gltf hier
     * das Mass der Dinge ist, asynchron. Ob das aus bundle Sicht preloaded sein wird, ist hier unerheblich.
     * Ist Nachfolger fure ModelFactory.buildModelFromBundle().
     * Geht aber nicht mit FG XML model!
     * Eine Exception bei einem Fehler gibt es hieraus dann auch nicht, sondern nur eine FM über den Delegate.
     * 04.10.2018: Das ist trotz async aber nicht multithreaded.
     *
     * @param filename
     * @return
     */
    public abstract void buildNativeModelPlain(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate modeldelegate, int options);

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
     * TODO Unerwenschte Dependency auf Effect.
     * Der name hat keine funktionale Bedeutung. Er dient nur der Wiedererkennbarkeit.
     *
     * @param color
     * @param texture
     * @param parameters
     * @return
     */
    public abstract NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap</*TextureType*/String, NativeTexture> texture,
                                                 HashMap<NumericType, NumericValue> parameters, /*MA36 TODO Effect*/ Object effect);
    //return buildMaterial(new MaterialDefinition(name,color,texture,parameters),effect);

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
     */
    public abstract NativeTexture buildNativeTexture(BundleResource filename, HashMap<NumericType, NumericValue> parameters);

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
     * Die Position bei einem directional light gibt es nicht, nur die Richtung.
     * Die Direction ist die Richtung, aus der das Licht kommt, nicht die Richtung, in die es scheint.
     * Also eigentlich doch die Position. Es scheint dann in Richtung (0,0,0).
     * Das Light wird zugleich als Schattenverursacher eingerichtet.
     *
     * @param argb
     * @return
     */
    public abstract NativeLight buildDirectionalLight(Color argb, Vector3 direction);

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
     * Der SceneRunner ist Singleton. Er wird beim ersten Aufruf angelegt.
     *
     * @return
     */
    /*10.7.21 public abstract NativeSceneRunner getSceneRunner();*/
    public abstract NativeCanvas buildNativeCanvas(int width, int height);


    public abstract long currentTimeMillis();

    /**
     * Kann nur in einem einzigen Frame (update()) abgefragt werden.
     *
     * @return
     */
    public abstract boolean GetKeyDown(int keycode);

    /**
     * Kann solange abgefragt werden, wie die Taste gedrückt ist.
     *
     * @return
     */
    public abstract boolean GetKey(int keycode);

    /**
     * Liefert die absolute Position (mit (0,0) links unten) zu der sich die Maus bewegt hat. Aber nur bei Bewegung.
     * Liefert null, wenn sich die Maus nicht bewegt hat.
     * 8.4.16: (0,0) war mal links oben, ist jetzt links unten, weil Unity das auch so macht. Und es gibt Aussagen, dass
     * "in 3D y von unten nach oben läuft". Ist wohl openGl convention.
     * 14.5.19: Arbeitet bei "Drag" (move mit pressed button oder touchmove) genauso.
     *
     * @return
     */
    public abstract Point getMouseMove();

    /**
     * Liefert die Releaseposition (bzw die aktuelle Mausposition), wenn die Taste released wird. Sonst null.
     * Release statt press, um evtl. auch Drag verwenden zu können.
     * Kann nur in einem einzigen Frame (update()) abgefragt werden.
     * Bewusst ohne button number wegen Smartphone.
     * (0,0) ist wie bei getMouseMove() links unten.
     *
     * @return
     */
    public abstract Point getMouseClick();

    /**
     * Ergänzung zu getMouseClick(), wenn Mouse Button gedrueckt wird. Wichtig fuer Drag.
     * Auch fuer begin touchmove.
     *
     * @return
     */
    public abstract Point getMousePress();

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
     * 16.7.18: Parameter aus der command line sind mit dem prefix "argv." eingetragen, z.B. basename bei viewScenery.
     */
    public abstract void setSystemProperty(String key, String value);

    public abstract String getSystemProperty(String key);


    //private void addSceneNode(String ke)

    public abstract NativeEventBus getEventBus();

    /**
     * Die Ausführung abbrechen und einen Stacktrace anzeigen. Für Analyse. Naja.
     */
    public abstract void abort();

    /**
     * Relativ im Tree (recursive) bzw nur auf dieser Ebene. Aber immer nur unterhalb von hier.
     * 6.10.17: Bei Unity kann er hier auch destryte SceneNodes finden. Die liefern dann aber keine Childs.
     * 24.3.18: Das ist die Nachbildung einer Subtree Suche laut MA22 für alle Platformen, auch die, die das selber könnten.
     * 18.7.21:Moved here from EnginePlatform. TODO needs a better location
     *
     * @param name
     * @return
     */
    public static List<NativeSceneNode> findNodeByName(String name, NativeTransform startnode, boolean recursive) {
        List<NativeSceneNode> nodelist = new ArrayList<NativeSceneNode>();
        for (NativeTransform child : startnode.getChildren()) {
            // sollte nicht null sein koennen. Das durfte ein Fehler irgendwo sein.
            if (child != null) {
                NativeSceneNode csn = child.getSceneNode();
                // 5.1.17: Wie kann es denn Transforms ohne SceneNode geben? TODO klaeren
                if (csn != null) {
                    if (csn.getName() != null && csn.getName().equals(name)) {
                        nodelist.add(csn);
                    }
                    if (recursive) {
                        nodelist.addAll(findNodeByName(name, csn.getTransform(), recursive));

                    }
                }
            }
        }
        return nodelist;
    }

    /**
     * Muesste eigentlich generisch sein, wird z.Z. aber nur fuer GLTF verwendet.
     * 22.9.21: Now like XML handling for replacing model mapper.
     * Assuming NativeJsonValue is fitting for all Json starting with '{' or '['
     * @param jsonstring
     * @return
     */
    public abstract NativeJsonValue parseJson(String jsonstring);

    /**
     * Ist das GWT/C# safe?
     * 23.5.2020
     * C# kann den return null nicht und mag die ganze Konstruktion nicht. Darum ohne Generics.
     * public abstract <T> T parseJsonToModel(String jsonstring, Class clazz);
     * public abstract <T> String modelToJson(T model);
     *
     * @return
     */
    /*22.9.21: Better not. Requires kind of reflection and is tricky (if possible at all) in GWT.
    Better XML like.
    public abstract <T> Object parseJsonToModel(String jsonstring, Class clazz);

    public abstract <T> String modelToJson(Object model);
    */

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


    protected abstract Log getLog();

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
     * Establish a (web)socket connection to a MP Server. This isType no "EventBus", because its peer2peer.
     * <p>
     * Noch nicht abstract, weils sonst so oft implementiert werden muss. Erstmal Prototyp aus JME.
     * <p>
     * Gabs da schon mal einen anderen Ansatz? Find ich aber nicht mehr.
     * 15.2.21
     */
    public NativeSocket connectToServer() {
        return (NativeSocket) Util.notyet();
    }

    /*MA36 ueber runner public abstract void sendHttpRequest(String url, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate );*/

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
}
