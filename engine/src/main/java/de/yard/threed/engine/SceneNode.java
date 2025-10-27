package de.yard.threed.engine;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.ecs.DefaultBusConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 21.05.14
 * <p/>
 * 15.6.16: Once was named "Container" and "Model", now its "SceneNode". Like a GameObject in Unity, but not an ECS entiy.
 * 16.9.16: Not extending Object3D any longer. Now a component container for components like Transform etc.
 * Optional names are SceneEntity, Gameobject or SceneElement.
 */
public class SceneNode {
    public NativeSceneNode nativescenenode;
    static Log logger = Platform.getInstance().getLog(SceneNode.class);

    /**
     * An empty SceneNode without any component.
     */
    public SceneNode() {
        nativescenenode = Platform.getInstance().buildModel();
        if (DefaultBusConnector.nodeSyncEnabled) {
            Platform.getInstance().getEventBus().publish(new Event(DefaultBusConnector.EVENT_NODECREATED, new Payload(new Object[]{this})));
        }
    }

    public SceneNode(String name) {
        this();
        setName(name);
    }

    /**
     * Wrapper constructor for an existing SceneNode.
     */
    public SceneNode(NativeSceneNode nativebase3d) {
        nativescenenode = nativebase3d;
    }

    /**
     * Constructor for convenience.
     */
    public SceneNode(Mesh mesh) {
        this();
        //logger.debug("model built");
        setMesh(mesh);
    }

    /**
     * Convenience for parent.
     */
    public SceneNode(SceneNode child) {
        this();
        // Just a default name for this node
        setName("ParentNode");
        attach(child);
    }

    /**
     * Build with intermediate node for transformation decoupling.
     */
    public static SceneNode buildSceneNode(SceneNode child, Vector3 position, Quaternion rotation) {
        SceneNode newnode = new SceneNode(child);
        // Just the default name for this node
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
     * For convenience. In principle (analog Unity) just setting the parent.
     * Node is detached from current parent, however, there is no explicit detach().
     */
    public void attach(SceneNode n) {
        n.nativescenenode.getTransform().setParent(nativescenenode.getTransform());

        if (DefaultBusConnector.nodeSyncEnabled) {
            Platform.getInstance().getEventBus().publish(new Event(DefaultBusConnector.EVENT_NODEPARENTCHANGED, new Payload(new Object[]{n})));
        }
    }

    /**
     * 8.2.22: Reintroduced as (recursive) remove. This object instance (and those of childs) still exists, while the node was removed from
     * the scene graph and destroyed . The nativescenenode is set to null as an indicator.
     */
    public void remove() {
        (Platform.getInstance()).removeSceneNode(nativescenenode);
        nativescenenode = null;
    }

    public void setMesh(Mesh o) {
        nativescenenode.setMesh(o.nativemesh);

    }

    public Mesh getMesh() {
        NativeMesh m = nativescenenode.getMesh();
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

    public void setName(String name) {
        nativescenenode.setName(name);
        if (DefaultBusConnector.nodeSyncEnabled) {
            Platform.getInstance().getEventBus().publish(new Event(DefaultBusConnector.EVENT_NODECHANGED, new Payload(new Object[]{this})));
        }
    }

    public String getName() {
        return nativescenenode.getName();
    }

    public Transform getTransform() {
        return new Transform(nativescenenode.getTransform());
    }

    /**
     * Relative in Tree (recursive) or just on this level. But only below, never above.
     */
    public List<SceneNode> findNodeByName(String name) {
        return findNodeByName(name, this);
    }

    /**
     * @param s
     */
    public static void removeSceneNodeByName(String s) {
        List<NativeSceneNode> nl = (Platform.getInstance()).findSceneNodeByName(s);
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
     * Light is a component like mesh.
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
            // 'scale' is useful to see if a object was made invisible by that
            detailstring = "(pos=" + getTransform().getPosition() + ",scale=" + getTransform().getScale() + ",";
            String mapstring = "";
            Mesh mesh;
            if ((mesh = getMesh()) != null) {
                //4.10.17: knifflig
                Material material = getMesh().getMaterial();
                material.material.getName();
                NativeTexture[] maps = material.material.getTextures();
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

    public static SceneNode buildLineMesh(Vector3 from, Vector3 to, Color color) {
        return new SceneNode(Platform.getInstance().buildLine(from, to, color));
    }

    /**
     * Lieber direkt per Ray statt Camera wegen Depenency
     */
    @Deprecated
    public List<NativeCollision> getHits(Point mouselocation, Camera camera) {
        Ray pickingray = camera.buildPickingRay(camera.getCarrier().getTransform(), mouselocation);
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

    /**
     * Platform independent node search.
     * Relativ im Tree (recursive) bzw nur auf dieser Ebene. Aber immer nur unterhalb von hier.
     * 6.10.17: Bei Unity kann er hier auch destryte SceneNodes finden. Die liefern dann aber keine Childs.
     * 24.3.18: Das ist die Nachbildung einer Subtree Suche laut MA22 für alle Platformen, auch die, die das selber könnten.
     * 18.7.21: Moved here from EnginePlatform and then Platform.
     * 3.3.24: Also check 'this'.
     */
    public static List<SceneNode> findNode(NodeFilter filter, SceneNode startnode) {
        List<SceneNode> nodelist = new ArrayList<SceneNode>();
        // 3.1.18: Also check 'this'.
        if (filter.matches(startnode)) {
            nodelist.add(startnode);
        }
        for (Transform child : startnode.getTransform().getChildren()) {
            // sollte nicht null sein koennen. Das durfte ein Fehler irgendwo sein.
            if (child != null) {
                SceneNode csn = child.getSceneNode();
                // 5.1.17: Wie kann es denn Transforms ohne SceneNode geben? TODO klaeren
                if (csn != null) {
                    nodelist.addAll(findNode(filter, csn));
                }
            }
        }
        return nodelist;
    }

    public static List<SceneNode> findNodeByName(String name, SceneNode startnode) {
        List<SceneNode> result = findNode(new NodeFilter() {
            @Override
            public boolean matches(SceneNode n) {
                return name.equals(n.getName());
            }
        }, startnode);
        return result;
    }
}
