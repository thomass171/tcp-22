package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Color;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeLight;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by thomass on 25.04.15.
 */
public class WebGlLight implements NativeLight {
    JavaScriptObject light;
    Color col;
    Vector3 direction;

    // registry for all lights
    static List<WebGlLight> lights = new ArrayList<>();

    private WebGlLight(Color col, Vector3 direction, JavaScriptObject light) {
        this.col = col;
        this.direction = direction;
        this.light = light;
    }

    public static WebGlLight buildPointLight(Color col, double range) {
        return new WebGlLight(null/*??*/, null, buildPointLightNative(col.getARGB(), range));
    }

    public static WebGlLight buildAmbientLight(Color col) {
        WebGlLight l = new WebGlLight(col, null, buildAmbientLightNative(col.getARGB()));
        lights.add(l);
        return l;
    }

    public static WebGlLight buildDirectionalLight(Color col, Vector3 direction) {
        WebGlLight l = new WebGlLight(col, direction, buildDirectionalLightNative(col.getARGB(), WebGlVector3.toWebGl(direction).vector3));
        lights.add(l);
        return l;
    }

    public void setPosition(Vector3 pos) {
        WebGlObject3D.setPosition(light, WebGlVector3.toWebGl(pos).vector3);
    }

    public WebGlVector3 getPosition() {
        return new WebGlVector3(WebGlObject3D.getPosition(light));
    }

    @Override
    public Color getAmbientColor() {
        if (direction == null) {
            return col;
        }
        return null;
    }

    @Override
    public Color getDirectionalColor() {
        if (direction!=null) {
            return col;
        }
        return null;
    }

    @Override
    public Vector3 getDirectionalDirection() {
        return direction;
    }

    private static native JavaScriptObject buildPointLightNative(int col, double range)  /*-{
        var pointLight = new $wnd.THREE.PointLight({color: col, distance: range});
        return pointLight;
    }-*/;

    private static native JavaScriptObject buildAmbientLightNative(int col)  /*-{
        var ambientLight = new $wnd.THREE.AmbientLight(col);
        return ambientLight;
    }-*/;

    /**
     * Die direction ist die Richtung der Lichtquelle, also quais deren relative Position.
     * Es ist nicht die Richtung der Lichtstrahlen.
     *
     * @param col
     * @param direction
     * @return
     */
    private static native JavaScriptObject buildDirectionalLightNative(int col, JavaScriptObject direction)  /*-{
        var light = new $wnd.THREE.DirectionalLight(col);
        light.position.set (direction.x,direction.y,direction.z);
        //light.position.set(2,10,2);
        //22.3.17:mal ohne Schatten, bis klar ist wie das genau gehandhabt wird
        light.castShadow = true;
        //10.10.17 light.shadowDarkness = 0.5;
        light.shadow.camera.near = 0;
        light.shadow.camera.far = 15;
        light.shadow.camera.left = -5;
        light.shadow.camera.right = 7;
        light.shadow.camera.top = 5;
        light.shadow.camera.bottom = -5;
        for (i=0;i<16;i++) {
            light.layers.enable(i);
        }
        return light;
    }-*/;

}
