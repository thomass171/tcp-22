package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeLight;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.NativeScene;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 25.04.15.
 */
public class WebGlScene implements NativeScene {

    // kann nicht ueber die Factory gebaut werden, weil die gerade noch initialisiert wird
    Log logger = new WebGlLog(WebGlScene.class.getName());
    public static WebGlScene webglscene;

    public JavaScriptObject scene;
    boolean enableModelCameracalled;

    private WebGlScene(JavaScriptObject scene) {
        webglscene = this;
        this.scene = scene;
        //addTestObjekt();
    }

    public WebGlScene() {
        this(buildNativeScene());
    }

    public void add(NativeSceneNode objtoadd) {
        if ((((WebGlSceneNode) objtoadd).object3d) == null || (((WebGlSceneNode) objtoadd).object3d.object3d) == null) {
            logger.error("object3d isType null. ignoring add");
            return;
        }
        add(scene, ((WebGlSceneNode) objtoadd).object3d.object3d);
    }

    /*20.7.16 public void add(NativeMesh objtoadd) {
        add(scene, ((WebGlMesh) objtoadd).object3d);
    }*/

    /* @Override
     public void remove(NativeMesh objtoremove) {
         remove(scene,((WebGlMesh)objtoremove).object3d);
         sceneupdater.remove(objtoremove);
     }
 
     @Override
     public void remove(NativeModel objtoremove) {
         remove(scene,((WebGlModel)objtoremove).object3d);
         sceneupdater.remove(objtoremove);
     }*/
    /*20.7.16 @Override
    public void remove(NativeMesh objtoremove) {
        removeFromScene(objtoremove);
    }*/

    public void remove(WebGlSceneNode objtoremove) {
        removeFromScene(objtoremove.object3d);
    }

    public void remove(WebGlLight lighttoremove) {
        remove(lighttoremove.light);
    }

    /**
     * Beim Remove muss der Parent angegeben werden. Der ist nicht unbedingt die rootnode/scene.
     * 1.3.17: Das kann ich aber ThreeJS ueberlassen.
     */
    private void removeFromScene(WebGlObject3D objtoremove) {
        //WebGlObject3D parent = (WebGlObject3D) objtoremove.getParent();
        remove(objtoremove.object3d);
        /*
        if (parent == null) {
            //if (objtoremove instanceof WebGlSceneNode) {
             //   remove(scene, ((WebGlSceneNode) objtoremove).object3d.object3d);
            //} else {
                 logger.debug("removing from scene");
                remove(scene, objtoremove.object3d);
            //}
        } else {
            /*if (objtoremove instanceof WebGlSceneNode) {
                remove(((WebGlObject3D) parent).object3d, ((WebGlSceneNode) objtoremove).object3d.object3d);
            } else {* /
                remove(((WebGlObject3D) parent).object3d,  objtoremove.object3d);
            //}
        }*/
    }

    public void add(JavaScriptObject objtoadd) {
        add(scene, objtoadd);
    }

    public void add(NativeLight light) {
        add(scene, ((WebGlLight) light).light);
    }

    public WebGlObject3D getRootNode() {
        return new WebGlObject3D(getRootNode(scene), null, true);
    }
    
    /*29.9.18@Override
    public void enableModelCamera(NativeSceneNode model, NativeCamera nativecamera, Vector3 position, Vector3 lookat) {
        enableModelCameracalled = true;

        WebGlVector3 upVector = new WebGlVector3(0, 1, 0);
        //((WebGlModel)model).add(camera);
        //camera = new PerspectiveCamera(800, 600);
        WebGlCamera camera = (WebGlCamera) nativecamera;
        if (model != null) {
            // Die Camera kommt einfach so als Objekt da dran
            //TODO  ((WebGlModel) model).addCamera(nativecamera);
        }
        logger.debug("cameraposition.y=" + position.getY());
        camera.setPosition((WebGlVector3) position);
        //camera.setPosition(new WebGlVector3(position.getX(),position.getY(),3));
        camera.lookAt((WebGlVector3) lookat, upVector);

    }*/

    /**
     * Returns Mesh.
     * @return
     */
    public JavaScriptObject addTestObjekt() {
        return addTestObjekt(scene,1);
    }

    @Override
    public Dimension getDimension() {
        return AbstractSceneRunner.getInstance().dimension;
    }

    public List<NativeSceneNode> getObjectByName(String name) {
        List<NativeSceneNode> l = new ArrayList<NativeSceneNode>();

        JavaScriptObject obj = getObjectByName(scene,name,true);
        if (obj == null){
            return l;
        }
        //TODO mehrere?
        l.add(new WebGlSceneNode(obj,true));
        return l;
    }
    
    private static native JavaScriptObject buildNativeScene()  /*-{
        var scene = new $wnd.THREE.Scene();
        //$wnd.alert("scene built:"+scene);
        //var axisHelper = new $wnd.THREE.AxisHelper( 5 );
        //scene.add(axisHelper);
        return scene;
    }-*/;

    private static native void add(JavaScriptObject sc, JavaScriptObject objtoadd)  /*-{
        //$wnd.alert("nativeadd. objtoadd="+objtoadd);
        sc.add(objtoadd);
        //  $wnd.alert("nativeadd2");
    }-*/;

    /*1.3.17: private static native void remove(JavaScriptObject parent, JavaScriptObject objtoadd)  /*-{
        //$wnd.alert("nativeremove");
        parent.remove(objtoadd);
        //  $wnd.alert("nativeadd2");
    }-* /;*/

    private static native void remove(            JavaScriptObject objtoremove)  /*-{
        //$wnd.alert("nativeremove");
        objtoremove.parent.remove(objtoremove);
    }-*/;
    
    private static native JavaScriptObject getRootNode(JavaScriptObject scene)  /*-{
        // die scene selber muesste root sein.
        return scene;
    }-*/;

    public static native JavaScriptObject addTestObjekt(JavaScriptObject scene, double size)  /*-{
        var geometry ;
        if ($wnd.THREE.REVISION >= 71) {
            geometry = new $wnd.THREE.BoxGeometry(size,size,size);
        } else {
            geometry = new $wnd.THREE.CubeGeometry( size, size, size);
        }
        var mat = new $wnd.THREE.MeshBasicMaterial({color: 0xFFFF00});
        var mesh = new $wnd.THREE.Mesh(geometry, mat);
        scene.add(mesh);
        return mesh;
    }-*/;

    private static native JavaScriptObject getObjectByName(JavaScriptObject scene, String name, boolean recursive)  /*-{
        return scene.getObjectByName(name,recursive);
    }-*/;
    
}
