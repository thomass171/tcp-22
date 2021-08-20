package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeMesh;

/**
 * Created by thomass on 25.04.15.
 *
 * 26.1.16: Ableiten von WebGlObject3D statt Base3D wegen getParent
 * 15.11.16: gar nicht mehr ableiten wegen ECS.
 * 07.10.17: Was hat ECS damit zu tun? Ein Mesh ist ein eigenes ThreeJs object3d, das in eine Node gahenagen wird.
 * 07.10.17: Aber warum ist es nochmal in ein Object3D einbettet? Das ist doch ueberfluessig, zum es in ThreeJS schon eine Anleitung von Object3D ist.Mal direkt.
 */
public class WebGlMesh /*extends /*WebGlBase3D* /WebGlObject3D*/ implements NativeMesh {
    Log logger = new WebGlLog(WebGlMesh.class.getName());
    //public WebGlObject3D object3d;
    public JavaScriptObject mesh;
    /*MA17private*/ WebGlMesh(JavaScriptObject mesh) {
        //super(mesh);
        //7.10.17 object3d=new WebGlObject3D(mesh,null,false);
        //logger.debug("WebGlMesh built. name="+GwtUtil.getName(mesh));
        //4.4.18:line darf es auch sein.
        if (!"Mesh".equals(GwtUtil.getType(mesh)) && !"Line".equals(GwtUtil.getType(mesh))){
            logger.error("mesh type not 'Mesh' but "+GwtUtil.getType(mesh));
        }
        this.mesh = mesh;
    }

    public static WebGlMesh buildMesh(JavaScriptObject geometry, WebGlMaterial mat, boolean castShadow, boolean receiveShadow,boolean isline) {
        return new WebGlMesh(buildNativeMesh(geometry, (mat!=null)?mat.material:null, castShadow, receiveShadow,isline));
    }

  /*2.5.16    public static NativeMesh buildMesh(JsArray vertices, JsArray faces, JsArray uv, WebGlMaterial material, boolean castShadow, boolean receiveShadow) {
        return new WebGlMesh(buildNativeMesh1(vertices, faces, uv, material.material,  castShadow,  receiveShadow));
    }*/

  /*2.5.16  public static NativeMesh buildMesh(List<Vector3> vertices, List<Face3List> faces, NativeMaterial material, List<Vector3> normals, boolean castShadow, boolean receiveShadow) {
    
        /*JsArray ni* / WebGlVBO vbo = WebGlGeometry.moveToNative(vertices, faces, normals/*, nvertices, nnormals, uvs* /);
        //TO DO: normals uebergeben!
        return buildMesh(vbo.vertices, vbo.getTriangles(), vbo.faceuvs, ((WebGlMaterial) material)/*TO DO , nnormals.toArray(new Vector3f[0])* /,  castShadow,  receiveShadow);
    }*/

    public void updateMesh(NativeGeometry nativeGeometry/*List<Vector3> vertices, List<Face3List> faces*/, NativeMaterial material/*,List<Vector3> normals*/) {
        WebGlGeometry geo = (WebGlGeometry)nativeGeometry;
        if (geo!=null/*vertices != null || faces != null*/) {

            // WebGlVBO vbo =  WebGlGeometry.moveToNative(vertices, faces, normals/*, nvertices, nnormals, uvs*/);

           // setMeshBuffer(object3d, vbo.vertices, vbo.getTriangles(), vbo.faceuvs, vbo.normals);
            updateGeometry(/*object3d.object3d*/mesh,geo.geometry);
        }
        if (material != null) {
            setMaterial(mesh/*object3d.object3d*/,((WebGlMaterial) material).material);
        }
     /*   if (material.isTransparent()) {
            //spatial.setQueueBucket(RenderQueue.Bucket.Transparent);
        }*/
    }

    /**
     * Ohne mat als wireframe bauen.
     * Alternativ als line bauen.
     * 
     * @return
     */
    private static native JavaScriptObject buildNativeMesh(JavaScriptObject geometry, JavaScriptObject mat, boolean castShadow, boolean receiveShadow, boolean isline)  /*-{
        //$wnd.alert("buildNativeMesh. mat="+mat+",geometry="+geometry);
        if (isline) {
            var line = new $wnd.THREE.Line(geometry, mat);                
            return line;
        } else {
            if (mat == null){
                mat = new $wnd.THREE.MeshBasicMaterial({color: 0xFFFFFF});
                mat.wireframe = true;
            }
            if (mat.flatShading) {
                // only required for LamberMaterial (https://discourse.threejs.org/t/why-a-flatshading-true-dosent-work/3259/4)
                // but not available in BufferGeometry
                // geometry.computeFlatVertexNormals();
                // 3.5.19 Aber computeVertexNormals() scheint für sowas wie Phong erforderlich. Warum ist unklar. Naja, das muss sich vielleicht noch
                //etwas finden.
                geometry.computeVertexNormals();
            }
            var mesh = new $wnd.THREE.Mesh(geometry, mat);
            mesh.castShadow = castShadow;
            mesh.receiveShadow = receiveShadow;
            //$wnd.alert("mesh built:"+mesh);
    
            //fuer Debuggingzwecke evtl. erzeugte Meshes sammeln
            //if ($wnd.meshes == undefined){
              //  $wnd.meshes = new Array();
            //}
            //$wnd.meshes.push(mesh);
    
            return mesh;
        }
    }-*/;

    private static native void setMaterial(JavaScriptObject mesh, JavaScriptObject mat)  /*-{
        mesh.material = mat;
    }-*/;

    private static native JavaScriptObject getMaterial(JavaScriptObject mesh)  /*-{
        return mesh.material;
    }-*/;

    private static native void updateGeometry(JavaScriptObject mesh, JavaScriptObject geo)  /*-{
        mesh.geometry = geo;        
    }-*/;

    @Override
    public NativeMaterial getMaterial() {
        WebGlMaterial m=new WebGlMaterial(getMaterial(mesh/*object3d.object3d)*/));
        return m;
    }

    @Override
    public void setBoxColliderSizeHint(Vector3 size) {
        // braucht ThreeJS nicht
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return new WebGlSceneNode(WebGlObject3D.getParent(mesh),true);
    }


}
