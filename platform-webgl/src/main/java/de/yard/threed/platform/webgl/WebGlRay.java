package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.*;



import java.util.ArrayList;
import java.util.List;

/**
 * Warum jeweils nur eine intersection kommt, mag ThreeJS spezifisch sein.
 * Eine Intersection enthält: distance: distance,
 *   point: intersectionPoint,
 *   face: new THREE.Face3( a, b, c, THREE.Triangle.normal( vA, vB, vC ) ),
 *   faceIndex: null,
 *   object: this
 *   
 * Created by thomass on 28.11.15.
 */
public class WebGlRay implements NativeRay {
    Log logger = Platform.getInstance().getLog(WebGlRay.class);

    JavaScriptObject raycaster;

    private WebGlRay(JavaScriptObject raycaster) {
        this.raycaster = raycaster;
    }

    public static WebGlRay buildRay(Vector3 origin, Vector3 direction) {
        JavaScriptObject raycaster = buildRaycaster(WebGlVector3.toWebGl(origin).vector3, WebGlVector3.toWebGl(direction).vector3);
        return new WebGlRay(raycaster);
    }

    @Override
    public Vector3 getDirection() {
        return WebGlVector3.fromWebGl(new WebGlVector3(getDirection(raycaster)));
    }

    @Override
    public Vector3 getOrigin() {
        return WebGlVector3.fromWebGl(new WebGlVector3(getOrigin(raycaster)));
    }

    @Override
    public List<NativeCollision> intersects(NativeSceneNode model) {
        JsArray objects = null;
        objects = (JsArray) JavaScriptObject.createArray();
        if (model != null) {
            objects.push(((WebGlSceneNode) model).object3d.object3d);
        } else {
            // dann von der root node aus.
            objects.push(((WebGlScene) WebGlSceneRenderer.getInstance().scene.scene).getRootNode().object3d);
        }
        // logger.debug("name="+((JavaScriptObject)objects.get(0)).getClass().getName());
        JsArray intersectedobjects = intersectObjects(raycaster, objects, true);
        // logger.debug("number of intersects "+objects.get(0)+"("+model.getName()+") isType "+intersectedobjects.length());
        List<NativeCollision> na = new ArrayList<NativeCollision>();
        for (int i = 0; i < intersectedobjects.length(); i++) {
            JavaScriptObject point = getIntersectionPoint(intersectedobjects.get(i));
            JavaScriptObject intersectedobject = getIntersectionObject(intersectedobjects.get(i));
            // das intersectedobject ist wohl ein Mesh. Der parent ist mein registriertes Object3D
            // 25.9.17: Bei importieren gltf Modellen stimmt das aber trotzdem noch nicht unbedingt (z.B. Tower.mesh0).
            intersectedobject = WebGlObject3D.getParent(intersectedobject);
            // 21.12.16: model kann ja auch null sein.
            WebGlSceneNode intersectedmodel = null;
            if (model == null) {
                //logger.debug("io="+WebGlCommon.getClassname(intersectedobject));
                
                /*MA17int uniqueid = WebGlObject3D.getId(intersectedobject);

                NativeTransform obj = Platform.getInstance().findObject3DById(uniqueid);
                if (obj == null) {
                    logger.warn("object not found by id " + uniqueid);
                    intersectedmodel = null;
                } else {
                    logger.debug("object found by id " + uniqueid);
                    intersectedmodel = (WebGlSceneNode) obj.getSceneNode();
                }*/
                intersectedmodel = new WebGlSceneNode(intersectedobject,true);
                na.add(new WebGlCollision(intersectedmodel, new WebGlVector3(point)));
            }else {
                na.add(new WebGlCollision((WebGlSceneNode) model, new WebGlVector3(point)));
            }
        }
        return na;
    }

    @Override
    public List<NativeCollision> getIntersections() {
        return intersects(null);
    }

    private static native JavaScriptObject buildRaycaster(JavaScriptObject origin, JavaScriptObject direction)  /*-{
        var ray = new $wnd.THREE.Raycaster(origin, direction);
        return ray;
    }-*/;

    private static native JavaScriptObject getOrigin(JavaScriptObject raycaster)  /*-{
        //alert("raycaster="+raycaster);
        return raycaster.ray.origin;
    }-*/;

    private static native JavaScriptObject getDirection(JavaScriptObject raycaster)  /*-{
        return raycaster.ray.direction;
    }-*/;

    /**
     * Die Objekte in dem Array sind synthetische propertysammlungen, mit denen man so erstmal nichts anfangen kann.
     * 
     */
    private static native JsArray intersectObjects(JavaScriptObject raycaster, JsArray objects, boolean recursive)  /*-{
        //var intersects = raycaster.intersectObjects( $wnd.meshes );
        //fuehrt zu stackoverflow $wnd.dumpObject(objects[0]," ","");

        // JsArray ist wegen des Cross-Frame Problems kein JS array (im Sinne von instanceof)
        var nobjects = new $wnd.Array();
        for (var o in objects){
            //$wnd.logger.debug("name="+o.name);
           // for (var key in Object.keys(o)) {
                //if (o.hasOwnProperty(key)) {
             //    var property = o[key];
               //    $wnd.logger.debug(key+"="+property);
                 //      }
              //}
            nobjects.push(objects[o]);
        }
        //return new Array();
        return raycaster.intersectObjects(nobjects,recursive);
    }-*/;

    private static native JavaScriptObject getIntersectionPoint(JavaScriptObject intersection)  /*-{
        return intersection.point;
    }-*/;

    private static native JavaScriptObject getIntersectionObject(JavaScriptObject intersection)  /*-{
        return intersection.object;
    }-*/;

}
