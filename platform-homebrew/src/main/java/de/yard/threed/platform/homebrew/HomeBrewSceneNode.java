package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 21.05.14
 */
public class HomeBrewSceneNode /*16.9.16 extends OpenGlObject3D*/ implements NativeSceneNode {
    Log logger = Platform.getInstance().getLog(HomeBrewSceneNode.class);
    // 23.9.19: Die Platform muss selber wissen, welche nodes es - unabhängig von parent/child - gibt.
    static List<HomeBrewSceneNode> sceneNodes = new ArrayList<>();
    private String name;
    HomeBrewMesh mesh;
    public HomeBrewTransform object3d;
    boolean destroyed = false;
    int uniid;
    private static int uniqueid = 1;
    OpenGlLight light;

    public HomeBrewSceneNode(String name) {
        object3d = new HomeBrewTransform(this);
        //object3d.parentscenenode=this;
        uniid = uniqueid++;
        if (name == null) {
            name = "model" + uniid;
        }
        setName(name);
        sceneNodes.add(this);
    }

    /**
     * Ein Constructor wegen Convenience.
     *
     * @param mesh
     */
    public HomeBrewSceneNode(HomeBrewMesh mesh) {
        this((String) null);
        this.mesh = mesh;
        this.mesh.parentscenenode = this;
        //15.6.16: TODO irgendwie add((de.yard.threed.platform.NativeMesh) mesh);1.8.16:Immer noch, nachdem es die globale Liste gibt?
    }



    @Override
    public void setMesh(NativeMesh mesh) {
        if (this.mesh != null) {
            this.mesh.parentscenenode = null;
        }
        this.mesh = (HomeBrewMesh) mesh;
        if (mesh != null) {
            this.mesh.parentscenenode = this;
        }
    }

    @Override
    public NativeMesh getMesh() {
        return mesh;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NativeTransform getTransform() {
        return object3d;
    }



    @Override
    public NativeCamera getCamera() {
        //wie JME, obwohl es hier wohl auch direkter ginge.
        for (NativeCamera nc : AbstractSceneRunner.getInstance().getCameras()) {
            HomeBrewCamera c = (HomeBrewCamera) nc;
            if (c.carrier.object3d == this.object3d) {
                return c;
            }
        }
        return null;
    }

    @Override
    public void setLight(NativeLight plight) {
        if (light != null) {
            // bestehendes light entfernen TODO
            Util.notyet();
        }
        light = (OpenGlLight) plight;
        //Node n = (Node) object3d.spatial;
        // n.addLight(light.light/*spatial*/);
        ((HomeBrewScene) Platform.getInstance().getScene()).add(plight);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void destroy() {
        object3d.remove((HomeBrewTransform) this.getTransform());
        destroyed = true;
    }

    public static void collectRenderables(Renderables renderables) {

        for (HomeBrewSceneNode n:sceneNodes) {
            //Ein name ist nicht mandatory
            //if (n.getName() == null){
            //    throw new RuntimeException("object name isType null");
            //}
            //23.9.19: destroyed sollte die doch wohl entfernen TODO
            if (!n.isDestroyed()) {
                HomeBrewSceneNode sn = (HomeBrewSceneNode) n;
                HomeBrewMesh mesh = (HomeBrewMesh) sn.getMesh();
                if (mesh != null) {
                    // wirfeframe hat kein material
                    if (((HomeBrewMaterial) mesh.getMaterial()) != null && ((HomeBrewMaterial) mesh.getMaterial()).isTransparent()) {
                        renderables.transparent.add(mesh);
                    } else {
                        renderables.renderables.add(mesh);
                    }
                }
            }
        }
    }


    /*MA17*/@Override
    public int getUniqueId() {
        return uniid;
    }

    /*@Override
    public NativeObject3D find(String name) {
        Util.notyet();
        return null;
    }*/


    /*3.3 parentscenenode world wird immer berechnet @Override
    protected OpenGlMatrix4 dorender(GlInterface gl, OpenGlMatrix4 projectionmatrix, OpenGlMatrix4 viewmatrix,/* Matrix4 parenttransformation,* / List<OpenGlLight> light) {
        // 19.8.14: ThreeJS multipliziert aber umgekehrt (three-62.js:7653)
        OpenGlMatrix4 transformation = (OpenGlMatrix4) super.getLocalModelMatrix();//.multiply(parenttransformation);
        //Threejs: Matrix4 transformation = parenttransformation.multiply(buildModelMatrix());
        return transformation;
    }*/

   /*3.3.16 @Override
    public void setup(GlInterface gl) {
        // nichts zu tun
    }

    @Override
    public boolean needsSetup() {
        return false;
    }*/


}
