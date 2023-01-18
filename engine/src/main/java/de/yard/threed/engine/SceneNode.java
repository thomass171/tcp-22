package de.yard.threed.engine;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.platform.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 21.05.14
 * <p/>
 * 30.1.15: Umbenannt von Container nach Model. Das scheint ganz gut zu passen.
 * 26.1.16: Warum war das denn von Base3D abgeleitet. Dann hat es doch z.B. keinen Parent
 * 15.6.16: Umbenannt von Model nach SceneNode. Entspricht einem GameObject in Unity und Entiy in ECS (26.1.17: Entity wohl nicht).
 * 16.9.16: Nichr mehr von Object3D ableitet, sondern das als Component. Sollte vielleicht SceneEntity, Gameobject oder SceneElement heissen.
 */
public class SceneNode /*extends Object3D*/ {
    public NativeSceneNode nativescenenode;
    static Log logger = Platform.getInstance().getLog(SceneNode.class);

    /**
     * Hier muss ein leeres Objekt als Container angelegt werden.
     */
    public SceneNode() {
        nativescenenode/*object3d*/ = ( /*Engine*/Platform.getInstance()).buildModel();
        //object3d = new Transform(nativescenenode.getTransform());
        /*((EngineHelper) Engine*/Platform.getInstance().getEventBus().publish(new Event(EventType.EVENT_NODECREATED, new Payload(new Object[]{this})));
    }

    public SceneNode(String name){
        this();
        setName(name);
    }

    /**
     * 30.11.15: Dieser CVosntructor ist private, weil unklar ist, ob seine
     * Verwendung von ausserhalbt guenstig ist.
     * 26.1.16: Jetzt public wegen getParent in Model.
     */
    public SceneNode(/*NativeBase3D*/NativeSceneNode nativebase3d) {
        /*object3d*/
        nativescenenode = nativebase3d;
        //object3d = new Transform(nativescenenode.getTransform());
    }

    /**
     * Ein Constructor wegen Convenience.
     *
     * @param mesh
     */
    public SceneNode(Mesh mesh) {
        this();
        //logger.debug("model built");
        setMesh(mesh);
        //((NativeModel)object3d).add(mesh);
        //logger.debug("mesh added");

    }

    /**
     * Convenience fuer Parent.
     *
     * @param child
     */
    public SceneNode(SceneNode child) {
        this();
        // Der Name als Defaultname für diese Node
        setName("ParentNode");
        attach(child);
    }

    /**
     * Eine Zwischennode zur Transformation einziehen. Um mögliches Ueberschreiben sowohl in der aktuellen wir der neuen zu vermeiden,
     * werden eigentlich sogar zwei zusaetzliche erzeugt.
     * static Factorymethode weil es nicht/schlecht als Constructor geht.
     *
     * @param rotation
     * @return
     */
    public static SceneNode buildSceneNode(SceneNode child, Vector3 position, Quaternion rotation) {
        SceneNode newnode = new SceneNode(child);
        // Der Name als Defaultname für diese Node
        newnode.setName("TransformNode");
        if (rotation != null) {
            newnode.getTransform().setRotation(rotation);
        }
        if (position != null) {
            newnode.getTransform().setPosition(position);
        }
        return new SceneNode(newnode);
    }




    /**
     * Eine Conveniencemethode, weil
     * Stattdessen? Ich glaube, setParent.
     *
     * @param n
     */
    /*@Deprecated
    public void add(SceneNode n) {
        n.object3d.setParent(object3d);
        Platform.getInstance().getEventBus().publish(new Event(EventType.EVENT_NODEPARENTCHANGED, n));
    }*/

    /**
     * Eine Conveniencemethode. Im Prinzip (analog Unity) nur der parent set.
     * Das hat aber nichts damit zu tun, dass SceneNode keinen add mehr haben soll.
     * Die Node wird bei einem evtl. existierenden Parent detached.
     * Einen expliziten detach gibt es aber trotzdem nicht.
     *
     * @param n
     */
    public void attach(SceneNode n) {
        n.nativescenenode.getTransform().setParent(nativescenenode.getTransform());

        Platform.getInstance().getEventBus().publish(new Event(EventType.EVENT_NODEPARENTCHANGED, new Payload(new Object[]{n})));
    }

    /**
     * 8.2.22: Reintroduced as (recursive) remove. This object instance (and those of childs) still exists, while the node was removed from
     * the scene graph and destroyed . The nativescenenode is set to null as an indicator.
     */
    public void remove() {
        (Platform.getInstance()).removeSceneNode(nativescenenode);
        nativescenenode = null;
    }

    /*public SceneNode find(String name) {
        NativeObject3D n = ((NativeSceneNode) object3d).find(name);
        if (n != null) {
            return new SceneNode(n);
        }
        return null;
    }*/

   /* public void add(Mesh o) {
        ((NativeSceneNode) object3d).setMesh((NativeMesh) o.nativemesh);

    }*/

    public void setMesh(Mesh o) {
        nativescenenode/* ((NativeSceneNode) ob.object3d)*/.setMesh(o.nativemesh);

    }

    public Mesh getMesh() {
        NativeMesh m = nativescenenode/*((NativeSceneNode) ob.object3d)*/.getMesh();
        if (m == null) {
            // when node has no mesh
            return null;
        }
        return new Mesh(m);
    }

    public static void removeSceneNode(SceneNode n) {
        (Platform.getInstance()).removeSceneNode(n.nativescenenode);
    }

    public static List<NativeSceneNode> findByName(String objname) {
        List<NativeSceneNode> n = Platform.getInstance().findSceneNodeByName(objname);
        return n;
    }

    public static SceneNode findFirst(String objname) {
        List<NativeSceneNode> n = findByName(objname);
        if (n.size() == 0) {
            return null;
        }
        return new SceneNode(n.get(0));
    }
    
    /*public void addCamera(Camera o) {
        // 10.6.15: Ob das so geht??
       ((NativeModel) object3d).addCamera(o.getNativeCamera());

    }*/

    /*26.1.16 public Model getParent() {
        return new Model(((NativeModel)object3d).getParent());
    }*/


    /*MA17public int getUniqueId() {
        return nativescenenode.getUniqueId();
    }*/

    public void setName(String name) {
        nativescenenode.setName(name);
        Platform.getInstance().getEventBus().publish(new Event(EventType.EVENT_NODECHANGED, new Payload(new Object[]{this})));
    }


    public String getName() {
        return nativescenenode.getName();
    }

    public Transform getTransform() {
        return new Transform(nativescenenode.getTransform());
    }

    /**
     * Relativ im Tree (recursive) bzw nur auf dieser Ebene. Aber immer nur unterhalb von hier.
     * 3.1.18: Auch this pruefen.
     *
     * @param name
     * @return
     */
    public List<NativeSceneNode> findNodeByName(String name, boolean recursive) {
        List<NativeSceneNode> l;
        if (nativescenenode.getName().equals(name)) {
            l = new ArrayList<NativeSceneNode>();
            l.add(nativescenenode);
        } else {
            l = Platform.findNodeByName(name, nativescenenode.getTransform(), recursive);
        }
        return l;
    }

    /**
     * @param s
     */
    public static void removeSceneNodeByName(String s) {
        List<NativeSceneNode> nl = ( Platform.getInstance()).findSceneNodeByName(s);
        if (nl.size() > 0) {
            SceneNode n = new SceneNode(nl.get(0));
            if (n == null) {
                logger.warn("Object not found:" + s);
            } else {
                SceneNode.removeSceneNode(n);
            }
        }

    }

    /**
     * Light ist genauso wie Mesh eine Component einer Node.
     */
    public void setLight(Light light) {
        nativescenenode.setLight(light.nativelight);
    }

    /**
     * Details:
     * 0x01: material
     */
    public String dump(String indent, int details) {
        String detailstring = "";
        if (details > 0) {
            detailstring = "(";
            String mapstring = "";
            Mesh mesh;
            if ((mesh = getMesh()) != null) {
                //4.10.17: knifflig
                Material material = getMesh().getMaterial();
                material.material.getName();
                NativeTexture[] maps = material.material.getMaps();
                mapstring = "[";
                for (int i = 0; i < maps.length; i++) {
                    NativeTexture t = maps[i];
                    if (i > 0) {
                        mapstring += ",";
                    }
                    if (t == null) {
                        mapstring += "-";
                    } else {
                        mapstring += t.getName();
                    }
                }
                mapstring += "]";
            } else {
                mapstring = "nomesh";
            }
            detailstring += mapstring + ")";
        }

        String s = indent + getName() + detailstring + "\n";
        //for (int i = 0; i < nativescenenode.getTransform().getChildCount(); i++) {
        //    s += new SceneNode(nativescenenode.getTransform().getChild(i).getSceneNode()).dump(indent + "  ");
        //}
        for (NativeTransform n : nativescenenode.getTransform().getChildren()) {
            s += new SceneNode(n.getSceneNode()).dump(indent + "  ", details);
        }
        return s;
    }

    /**
     * Liefert den Weg von der root node bis hierhin. Praktisch um Suchergebnisse einzuordnen.
     *
     * @return
     */
    public String getPath() {
        String p = "";
        Transform parent = getTransform().getParent();
        if (parent != null) {
            p = parent.getSceneNode().getPath() + "/";
        }
        p += getName();
        return p;
    }


    /*public static Mesh buildLineMesh(GenericGeometry xg, Material mat) {
        return new Mesh(xg,mat,false,false,true);
    }*/
    public static SceneNode buildLineMesh(Vector3 from, Vector3 to, Color color) {
        return new SceneNode(Platform.getInstance().buildLine(from, to, color));
    }

    /**
     * Lieber direkt per Ray statt Camera wegen Depenency
     */
    @Deprecated
    public List<NativeCollision> getHits(Point mouselocation, Camera camera) {
        Ray pickingray = camera.buildPickingRay(camera.getCarrier().getTransform(),mouselocation);
        List<NativeCollision> intersects = pickingray.getIntersections(this, true);
        return intersects;
    }

    public List<NativeCollision> getHits(Ray pickingray) {
        List<NativeCollision> intersects = pickingray.getIntersections(this, true);
        return intersects;
    }

    public SceneNode getParent() {
        Transform parent = getTransform().getParent();
        if (parent == null) {
            return null;
        }
        return parent.getSceneNode();
    }



    public Camera getCamera() {
        NativeCamera cam = nativescenenode.getCamera();
        if (cam == null) {
            return null;
        }
        return new Camera(cam);
    }


}
