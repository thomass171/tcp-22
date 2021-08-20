package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.NativeTransform;
import de.yard.threed.engine.Scene;


import java.util.ArrayList;
import java.util.List;

/**
 * Der Wrapper fuer ein ThreeJS.Object3D.
 * <p>
 * Created by thomass on 25.04.15.
 * <p/>
 * ThreeJs verwendet Quaternions und hat keine diskreten Rotationswerte fuer einzelne Achsen.
 * Ich bleib mal beim Namen WebGlObject statt WebGlTransform, weil das Ding in ThreeJS ja
 * nun mal Object3D heisst und an verschiedenen Stellen eingesetzt wird (z.B.Mesh).
 */
public class WebGlObject3D implements NativeTransform {
    static Log logger = new WebGlLog(WebGlObject3D.class.getName());
    // Das JavaScriptObject ist ein ThreeJS.Object3D.
    JavaScriptObject object3d;
    // Die SceneNode, deren Komponente dies Object3D ist. null bei camera?
    public WebGlSceneNode parentscenenode;
    // 5.1.17: MErken ob hier ein Mesh als Child drinhaengt, denn das ist kein Child im Sinne der Platform.
    public int meshholderindex = -1;
    private Object meshHolder;
    private int layer;

    WebGlObject3D(JavaScriptObject jo) {
        this.object3d = jo;
    }

    WebGlObject3D(JavaScriptObject o, WebGlSceneNode parentscenenode, boolean existing) {
        this(o);
        this.parentscenenode = parentscenenode;
        if (!existing) {
            //logger.debug("registering new object3d with id "+getId(object3d)+",parentscenenode="+parentscenenode);
            //MA17 Platform.getInstance().native2nativeobject3d.put(getId(object3d), this);
        }
    }

    public int add(NativeTransform obj) {
        return add(object3d, ((WebGlObject3D) obj).object3d);
    }

    public int add(WebGlMesh mesh) {
        return add(object3d, mesh.mesh);
    }

    public void remove(WebGlMesh mesh) {
        remove(object3d, mesh.mesh);
    }

    @Override
    public void rotateOnAxis(Vector3 axis, double angle) {
        //GWT.log("object3d=" + object3d + ",angle=" + angle);
        rotateOnAxis(object3d, WebGlVector3.toWebGl(axis).vector3, angle);
    }

    @Override
    public void translateOnAxis(Vector3 axis, double distance) {
        //logger.debug("translateOnAxis: x= "+axis.getX()+",y="+axis.getY()+",z="+axis.getZ()+",distance="+distance);
        translateOnAxis(object3d, WebGlVector3.toWebGl(axis).vector3, distance);
    }

    @Override
    public void setScale(Vector3 scale) {
        //GWT.log("object3d=" + object3d + ",angle=" + angle);
        setScale(object3d, WebGlVector3.toWebGl(scale).vector3);
    }

    @Override
    public Vector3 getScale() {
        return WebGlVector3.fromWebGl(new WebGlVector3(getScale(object3d)));
    }

    @Override
    public void setPosition(Vector3 pos) {
        //logger.debug("setPosition: x= "+pos.getX()+",y="+pos.getY()+",z="+pos.getZ());
        setPosition(object3d, WebGlVector3.toWebGl(pos).vector3);
    }

    @Override
    public Vector3 getPosition() {
        return WebGlVector3.fromWebGl(new WebGlVector3(getPosition(object3d)));
    }

    @Override
    public Quaternion getRotation() {
        return WebGlQuaternion.fromWebGl(new WebGlQuaternion(getQuaternion(object3d)));
    }

    @Override
    public void setRotation(Quaternion q) {
        setRotationFromQuaternion(object3d, WebGlQuaternion.toWebGl(q).quaternion);
    }

    /**
     * Die local matrix steht in "matrix".
     *
     * @return
     */
    @Override
    public Matrix4 getLocalModelMatrix() {
        return WebGlMatrix4.fromWebGl(new WebGlMatrix4(getMatrix(object3d)));
    }

    public boolean isSame(WebGlObject3D obj) {
        return isSame(object3d, obj.object3d);
    }

    public void setName(String name) {
        setName(object3d, name);
    }

    public String getName() {
        return getName(object3d);
    }

    @Override
    public NativeTransform getParent() {
        JavaScriptObject parent = getParent(object3d);
        //MA17 return (WebGlObject3D) Platform.getInstance().findObject3DById(getId(parent));
        if (parent == null) {
            return null;
        }
        //logger.debug("got parent "+getName(parent)+"-"+getId(parent)+" of "+getName(object3d));
        return new WebGlObject3D(parent, null, true);
    }

    @Override
    public void setParent(NativeTransform parent) {
        setParent1((WebGlObject3D) parent);
    }

    /**
     * 7.5.21:Really set parent to null
     */
    public void clearParent() {
        setParent(object3d,null);
    }

    /**
     * Keine Override, weil das in der WebglObject3d passiert
     *
     * @return
     */
    public WebGlObject3D getParent1() {
        //return /*new WebGlBase3D(*/getParent(object3d);
        JavaScriptObject parent = getParent(object3d);
        //MA17 return (WebGlObject3D) Platform.getInstance().findObject3DById(getId(parent));
        if (parent == null) {
            return null;
        }
        return new WebGlObject3D(parent, null, true);
    }

    //public static JavaScriptObject getParent(JavaScriptObject obj) {

    /**
     * 23.1.17: Hier muss der meshholder beachtet werden, der beim Count rausgerechnet wurde.
     *
     * @param index
     * @return
     */
    @Override
    public NativeTransform getChild(int index) {
        /*MA17if (meshholderindex != -1 && index >= meshholderindex) {
            index++;
        }*/
        return getChildren().get(index);
    }

    @Override
    public int getChildCount() {
        /*MA17 int cnt = getChildCount(object3d);
        if (meshholderindex != -1) {
            // Im JME Sinne ist das ein Child, aber nicht im Sinne der Platform
            cnt--;
        }
        logger.debug("found " + cnt + " childs. meshholderindex=" + meshholderindex);*/
        int cnt = getChildren().size();
        return cnt;
    }

    @Override
    public List<NativeTransform> getChildren() {
        List<NativeTransform> l = new ArrayList<NativeTransform>();
        JsArray children = getChildren(object3d);

        for (int i = 0; i < children.length(); i++) {
            JavaScriptObject child = children.get(i);
            //24.9.19: Auch nicht die CameraNode mitzaehlen. Das ist ja eine Component.
            if (child != null && !isMeshholder(child) && !isCamera(child)) {
                NativeTransform c = new WebGlObject3D(child);
                //logger.debug("child=" + c+",type="+GwtUtil.getType(child));
                l.add(c);
            }
        }
        return l;
    }

    private boolean isMeshholder(JavaScriptObject child) {
        String type = GwtUtil.getType(child);
        //logger.debug("child.type="+type);
        return type.equals("Mesh");
    }

    private boolean isCamera(JavaScriptObject child) {
        String type = GwtUtil.getType(child);
        //logger.debug("child.type="+type);
        return type.equals("Camera") || type.equals("PerspectiveCamera");
    }

    /**
     * Einer der Children muss das Child fuer den Mesh sein.
     *
     * @return
     */
    public WebGlMesh getMeshHolder() {
        int cnt = getChildCount(object3d);
        for (int i = 0; i < cnt; i++) {
            JavaScriptObject child = getChild(object3d, i);
            if (isMeshholder(child)) {
                return new WebGlMesh(child);
            }
        }
        //A ndde having no mesh isType no reason for logging
        //logger.debug("no mesholder found");
        return null;
    }

    /*Na private boolean isCameraholder(JavaScriptObject child) {
        String type = GwtUtil.getType(child);
        logger.debug("child.type="+type);
        return type.equals("Camera");
    }

    public WebGlCamera getCameraHolder() {
        int cnt = getChildCount(object3d);
        for (int i = 0; i < cnt; i++) {
            JavaScriptObject child = getChild(object3d, i);
            if (isCameraholder(child)) {
                return new WebGlCamera(child);
            }
        }
        //A ndde having no mesh isType no reason for logging
        //logger.debug("no mesholder found");
        return null;
    }*/

    @Override
    public NativeSceneNode getSceneNode() {
        /*MA17if (parentscenenode==null){
            logger.warn("parentscenenode isType null");
        }
        return parentscenenode;*/
        return new WebGlSceneNode(object3d, true);
        //return Platform.getInstance().findSceneNodeById(((Integer) spatial.getUserData("uniqueid")));
    }

    public WebGlObject3D getChild1(int index) {
        Util.nomore();//MA17
        JavaScriptObject ch = getChild(object3d, index);
        //MA17 return (WebGlObject3D) Platform.getInstance().findObject3DById(getId(ch));
        return new WebGlObject3D(ch, null, true);
    }

    /**
     * parent==null kommt an world. 7.5.21: das ist doch irgendwie Kappes
     * 1.11.19: layer des parent uebernehmen.
     *
     * @param parent
     */
    public void setParent1(WebGlObject3D parent) {
        /*new WebGlBase3D(*/
        if (parent == null) {
            setParent(object3d, ((WebGlObject3D) Scene.getWorld().getTransform().transform).object3d);
            //logger.debug("set layer 0 from null parent");
            setLayer(0);
        } else {
            setParent(object3d, parent.object3d);
            int layer = parent.getLayer();
            //logger.debug("set layer "+layer+" from parent");
            setLayer(layer);
        }
    }

    /**
     * Muss im Mesh gesetzt werden, weil das ja gerendered wird. Layer isType an index, no bitmask.
     * Das Mesh ist ein Child von this(?). Zusaetzlich in node setzen, damit der getter geht (nicht jede Node hat ein Mesh).
     * Arbeitet rekursiv. Aber mit nativem setLayerRecursive kann man die Camera ueberschreiben!
     *
     * @param layer
     */
    @Override
    public void setLayer(int layer) {
        /*if (getMesh() != null) {
            ((WebGlMesh) getMesh()).setLayer(layer);
        }*/
        WebGlSceneNode sceneNode = (WebGlSceneNode) getSceneNode();
        //logger.debug("setLayer "+layer+" for node "+sceneNode.getName());
        setLayer(sceneNode.object3d.object3d, layer);
        WebGlMesh mesh = (WebGlMesh) sceneNode.getMesh();
        if (mesh != null) {
            setLayer(mesh.mesh, layer);
        }
        // mark all subnodes, the complete subtree
        //31.10.19 geht nicht in JS, weil das Mesh nicht die children hat.
        //ueber die children auch nicht, da wird iregnwas vergessen. TODO klaeren warum.Das stimmt was mit dem getchildren doch nicht??
        List<NativeTransform> children = /*getTransform().*/getChildren();
        for (NativeTransform c : children) {
            //logger.debug("c.setLayer");
            //c.getSceneNode().setLayer(layer);
            c.setLayer(layer);
        }
        //setLayerRecursive(object3d, layer);
    }

    /**
     * returns bitmask. No longer.
     * Layer liegt im Mesh, aber auch in Node (nicht jede Node hat ein Mesh).
     *
     * @return
     */
    @Override
    public int getLayer() {
        int layer = 0;
        WebGlSceneNode sceneNode = (WebGlSceneNode) getSceneNode();
        //WebGlMesh mesh = (WebGlMesh) sceneNode.getMesh();
        //if (mesh != null) {
            layer = decodeLayer(getLayerMask(sceneNode.object3d.object3d));
        //}
        return layer;
    }

    public static int decodeLayer(int mask) {
        int pattern = 1;
        for (int shift = 0; shift < 16; shift++) {
            pattern <<= 1;
            //logger.debug("mask="+mask+",pattern="+pattern);
            if ((mask & pattern) > 0) {
                return shift + 1;
            }
        }
        return 0;
    }


    /**
     * Die world matrix steht in "matrixWorld".
     *
     * @return
     */
    @Override
    public Matrix4 getWorldModelMatrix() {
        return WebGlMatrix4.fromWebGl(new WebGlMatrix4(getMatrixWorld(object3d)));
    }

    protected static native JavaScriptObject buildObject3D()  /*-{
        var o = new $wnd.THREE.Object3D();
        //$wnd.alert("mesh built:"+mesh);
        //$wnd.logger.debug("object3d built with id"+o.id);
        return o;
    }-*/;

    static native JavaScriptObject getPosition(JavaScriptObject object3d)  /*-{
        return object3d.position.clone();
    }-*/;

    private static native JavaScriptObject getScale(JavaScriptObject object3d)  /*-{
        return object3d.scale.clone();
    }-*/;

    private static native JavaScriptObject getQuaternion(JavaScriptObject object3d)  /*-{
        return object3d.quaternion.clone();
    }-*/;

    /**
     * Die Matrix wird nicht immer angepasst. Darum der update vorher.
     *
     * @param object3d
     * @return
     */
    private static native JavaScriptObject getMatrix(JavaScriptObject object3d)  /*-{
        object3d.updateMatrix();
        //alert( object3d.matrix.elements[12]);
        return object3d.matrix;
    }-*/;

    /**
     * Die Matrix wird nicht immer angepasst. Darum der update vorher.
     *
     * @param object3d
     * @return
     */
    public static native JavaScriptObject getMatrixWorld(JavaScriptObject object3d)  /*-{
        object3d.updateMatrixWorld();
        return object3d.matrixWorld;
    }-*/;

    private static native void translateOnAxis(JavaScriptObject object3d, JavaScriptObject axis, double distance)  /*-{
        object3d.translateOnAxis(axis, distance);
    }-*/;

    private static native void rotateOnAxis(JavaScriptObject object3d, JavaScriptObject axis, double angle)  /*-{
        object3d.rotateOnAxis(axis, angle);
    }-*/;

    private static native void setScale(JavaScriptObject object3d, JavaScriptObject scale)  /*-{
       // object3d.scale = scale;
       object3d.scale.x=scale.x;
        object3d.scale.y=scale.y;
        object3d.scale.z=scale.z;

    }-*/;

    public static native int add(JavaScriptObject object3d, JavaScriptObject obj)  /*-{
        //$wnd.alert("nativeadd. obj="+obj);
        object3d.add(obj);
        return object3d.children.length;
    }-*/;

    static native void setPosition(JavaScriptObject object3d, JavaScriptObject pos)  /*-{
        //object3d.position = pos;
        //var p = new $wnd.THREE.Vector3(pos.x,pos.y,pos.z);
        object3d.position.x=pos.x;
        object3d.position.y=pos.y;
        object3d.position.z=pos.z;
        //object3d.position = p;
    }-*/;

    private static native void setRotationFromQuaternion(JavaScriptObject object3d, JavaScriptObject q)  /*-{
        object3d.setRotationFromQuaternion(q);
    }-*/;

    static native void setName(JavaScriptObject object3d, String name)  /*-{
        object3d.name = name;
    }-*/;

    static native String getName(JavaScriptObject object3d)  /*-{
        return object3d.name;
    }-*/;

    static native JavaScriptObject getParent(JavaScriptObject object3d)  /*-{
        var p = object3d.parent;
        if (p == null) {
            return null;
        }
        if (p.type == 'Scene') {
            // reached root of tree.
            return null;
        }
        return p;
    }-*/;

    /**
     * 7.5.21 Was genu ein setparent(null) sein soll, ist nicht ganz klar (definiert).
     * @param object3d
     * @param parent
     */
    static native void setParent(JavaScriptObject object3d, JavaScriptObject parent)  /*-{
        //23.12.16: einfach parent setzen ist doch wohl nicht richtig?
        //Zumindest ist der Flackereffekt (MA24?) jetzt weg. TODO scene remove fehlt doch auch? siehe ThreeJS.SceneUtils
        //object3d.parent = parent;
        if (parent == null) {
            //THREE.SceneUtils.detach( object3d, parent, scene );
            //7.5.21 doch mal nullen. Wichtig evtl. fuer VR camera?
            object3d.parent = null;
        } else {
            parent.add(object3d);
        }
    }-*/;

    static native void remove(JavaScriptObject object3d, JavaScriptObject obj)  /*-{
        object3d.remove(obj);
    }-*/;

    static native int getId(JavaScriptObject object3d)  /*-{
        return object3d.id;
    }-*/;

    static native JavaScriptObject getChild(JavaScriptObject object3d, int index)  /*-{
        return object3d.children[index];
    }-*/;

    static native int getChildCount(JavaScriptObject object3d)  /*-{
        return object3d.children.length;
    }-*/;

    static native JsArray getChildren(JavaScriptObject object3d)  /*-{
        return object3d.children;
    }-*/;

    static native boolean hasUserdataProperty(JavaScriptObject object3d, String key)  /*-{
        var hp = object3d.userData.hasOwnProperty(key);
        return hp;
    }-*/;

    static native boolean setMeshholder(JavaScriptObject object3d)  /*-{
        object3d.userData.meshholder = 1;        
    }-*/;


    static native boolean isSame(JavaScriptObject object3d1, JavaScriptObject object3d2)  /*-{
        return object3d1.uuid == object3d2.uuid;
    }-*/;

    /**
     * Die Hierarchie aufwärts bis root dumpen
     */
    public static native String dumpUp(JavaScriptObject object3d)  /*-{
        var s = "" + object3d.name;
        while (object3d.parent != null) {
            object3d = object3d.parent;
            s = object3d.name + "->" + s;
        }
        return s;        
    }-*/;

    /**
     * https://github.com/mrdoob/three.js/issues/10959
     * Rekursiv aufs Mesh bringt nichts, weil das Mesh nicht die children hat.
     * 15.11.19: Lieber nicht hier rekursiv, weil man damit den Layer einer Camra ueberschreiben kann.
     */
    static native void setLayerRecursive(JavaScriptObject object3d, int layer)  /*-{
        object3d.layers.set(layer);
        object3d.traverse( function(child) {
            child.layers.set(layer);
        });
        //$wnd.logger.debug("object3d layers.mask="+object3d.layers.mask);
    }-*/;

    static native void setLayer(JavaScriptObject object3d, int layer)  /*-{
        object3d.layers.set(layer);
        //$wnd.logger.debug("object3d layers.mask="+object3d.layers.mask);
    }-*/;

    static native int getLayerMask(JavaScriptObject object3d)  /*-{
        return object3d.layers.mask;
    }-*/;
}
