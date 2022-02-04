package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;

import java.util.List;

/**
 * Created by thomass on 25.04.15.
 */
public class WebGlRenderer {
    static Log logger = new WebGlLog(WebGlRenderer.class.getName());
    public JavaScriptObject renderer;
    static final String canvasid = "maincanvas";
    private boolean vrEnabled, statEnabled;

    private WebGlRenderer(JavaScriptObject renderer, boolean vrEnabled, boolean statEnabled) {
        this.renderer = renderer;
        this.vrEnabled = vrEnabled;
        this.statEnabled = statEnabled;
    }

    public static WebGlRenderer buildRenderer(Dimension dimension, boolean vrready, boolean antialiasing, boolean vrEnabled, boolean statEnabled) {
        //vrready=false;
        WebGlRenderer r = new WebGlRenderer(buildNativeRenderer(canvasid, dimension.getWidth(), dimension.getHeight(),
                Settings.backgroundColor.getARGB(), vrready, antialiasing, vrEnabled), vrEnabled, statEnabled);
        if (statEnabled) {
            enableStatisticsNative();
        }
        return r;
    }

    static int cnt = 0;

    /**
     * Explicit clear() is needed when autoclear in the renderer is switched off.
     */
    public void render(JavaScriptObject scene, List<NativeCamera> cameras) {

        boolean doLog = cnt % 200 == 0;
        cnt++;

        if (vrEnabled) {
            if (cameras.size() > 1) {
                String msg = "";
                for (int i = 0; i < cameras.size(); i++) {
                    WebGlCamera wc = ((WebGlCamera) cameras.get(i));
                    msg += wc.getName() + ",";
                }
                logger.warn("multiple cameras in VR:" + msg);
            }
            // uses autoclear
            WebGlCamera webglcamera = ((WebGlCamera) cameras.get(0));
            //7.5.21 In VR die Camera alleine auch ohne carrier. (MA35). Nee, nur mit carrier.
            //2.2.22 No longer modify the scene graph here. Should be done by application.
            if (webglcamera.getParent() != null && webglcamera.getParent().getParent() != null) {
                //logger.warn("removing carrier parent from camera.carrier");
                //((WebGlObject3D)webglcamera.getParent()).clearParent();
            }
            render(scene, webglcamera);
        } else {
            // Multipass Rendering
            for (int i = 0; i < cameras.size(); i++) {
                WebGlCamera webglcamera = ((WebGlCamera) cameras.get(i));
                if (webglcamera.enabled) {
                    if (doLog) {
                        //logger.debug("Rendering camera "+i+" with layer "+webglcamera.getLayer());
                    }
                    clear(renderer, webglcamera.getClearmode());
                    //logger.debug("dump:"+WebGlObject3D.dumpUp(webglcamera.object3d.object3d));
                    render(scene, webglcamera);
                }
            }
            // stat in VR is useless
            if (statEnabled) {
                statsUpdate();
            }
        }
    }

    private void render(JavaScriptObject scene, WebGlCamera webglcamera) {
        //logger.debug("rendering camera with childs "+webglcamera.getChildCount()+" at "+webglcamera.getPosition()+";"+webglcamera.getRotation());
        render(renderer, scene, webglcamera.getThreeJsCamera());
    }

/*5.5.21 no longer needed useful?
    public void setOrbitControlsScene(NativeScene orbitControlsScene) {
        setOrbitControlsScene(((WebGlScene) orbitControlsScene).scene/*.scene* /);
    }*/

    public void setSize(int width, int height) {
        setSize(renderer, width, height);
    }

    /**
     * used from JS!
     * 5.5.21: still in use? Or available? Or stable?
     *
     * @param isPresenting
     */
    public static void vrChanged(boolean isPresenting) {
        logger.debug("vrChanged: isPresenting=" + isPresenting);
        AbstractSceneRunner rh = AbstractSceneRunner.getInstance();
        if (rh != null) {
            rh.ascene.vrDisplayPresentChange(isPresenting);
        }
    }

    /**
     * used from JS!
     * For VR controller trigger?
     */
    public static void buttonAction(int controller, boolean pressed) {
        //logger.debug("buttonAction: pressed=" + pressed);
        if (pressed) {
            AbstractSceneRunner.getInstance().buttondown.add(controller * 10);
        } else {
            AbstractSceneRunner.getInstance().buttonup.add(controller * 10);
        }
    }

    /**
     * VR controller only?
     */
    public JavaScriptObject getController(int index) {
        return getController(renderer, index);
    }

    public void activeAnimationLoop() {
        setAnimationLoop(renderer);
    }

    private static native void render(JavaScriptObject renderer, JavaScriptObject scene, JavaScriptObject camera) /*-{
        renderer.render(scene, camera);
    }-*/;

    private static native void clear(JavaScriptObject renderer, int mode) /*-{
        if (mode == 0) {
            renderer.clear();
        }
        if (mode == 1) {        
            renderer.clearDepth();
        }
    }-*/;

    private static native void statsUpdate() /*-{
        if ($wnd.stats != null) {
            $wnd.stats.update();
        }
    }-*/;

    private static native JavaScriptObject buildNativeRenderer(String canvasid, int width, int height, int backgroundcolor, boolean vrready, boolean antialiasing, boolean vrEnabled)  /*-{
        //$wnd.alert(antialiasing);
        var container = $wnd.document.getElementById(canvasid);
        // AA kann man nur im Constructor setzen, nicht mehr spaeter. If it works, depends on the browser/platform combination. Safari/Macbook apparently not.
        var renderer = new $wnd.THREE.WebGLRenderer({ antialias: antialiasing });
        renderer.setSize(width,height);
        renderer.shadowMap.enabled = true;
        renderer.shadowMapSoft = true;

        // 5.5.21 autoclear prevents multi pass rendering. But don't switch it off in VR to avoid spoiling the rendering system
        if (!vrEnabled) {
            renderer.autoClear = false;
        }
        renderer.setClearColor( backgroundcolor, 1);

        //renderer.shadowMapEnabled = true;
        //renderer.shadowMapType = $wnd.THREE.PCFSoftShadowMap;
        container.appendChild(renderer.domElement);
        
        // previous checkAvailability() isType deprecated. Its important to enable VR only if its available,
        // otherwise the y-position will be too high. But difficult to detect. And the user might not activate it.
        // 4.5.21: So better use a argv parameter to explicitly activate it. And don't use change events. This only causes trouble.
        // var vravailable = 'getVRDisplays' in $wnd.navigator;
        if (vrready && vrEnabled) {
            renderer.xr.enabled = true;
            //$wnd.document.body.appendChild( $wnd.WEBVR.createButton( renderer ) );
            //VRButton is not available in $wnd
            //$wnd.document.body.appendChild( VRButton.createButton( renderer ) );
            $wnd.document.body.appendChild( $wnd.createVrButton( renderer ) );
            //$wnd.addEventListener( 'vrdisplaypresentchange', function ( event ) {
                //console.log("vrdisplaypresentchange "+event.display.isPresenting);
				//button.textContent = event.display.isPresenting ? 'EXIT VR' : 'ENTER VR';
				//@de.yard.threed.platform.webgl.WebGlRenderer::vrChanged(Z)(event.display.isPresenting);
			//});
            //@de.yard.threed.platform.webgl.WebGlRenderer::vrChanged(Z)(true);
        }
        return renderer;
    }-*/;

    /*5.5.21 private static native JavaScriptObject enableOrbitController(JavaScriptObject renderer, JavaScriptObject camera)  /*-{
        // Den richtigen Container uebergeben, sonst verwendet der OrbitControl das ganze DOM, was zu Darstellungsfehlern fuehrt
        var container = $wnd.document.getElementById('maincanvas');
        var controller = new $wnd.THREE.OrbitControls( camera, container );
        controller.addEventListener( 'change', function() {
            //$wnd.alert("render");
            if ($wnd.orbitcontrols_scene != null) {
                renderer.render($wnd.orbitcontrols_scene, camera);
            }
	    });
        return controller;
    }-* /;*/

    /*5.5.21 private static native void setOrbitControlsScene(JavaScriptObject scene)  /*-{
        $wnd.orbitcontrols_scene = scene;
    }-* /;*/

    private static native void enableStatisticsNative()  /*-{
        var container = $wnd.document.getElementById('maincanvas');
        $wnd.stats = new $wnd.Stats();
        $wnd.stats.domElement.style.position = 'absolute';
        $wnd.stats.domElement.style.bottom = '0px';
        $wnd.stats.domElement.style.zIndex = 100;
        container.appendChild( $wnd.stats.domElement );
    }-*/;

    private static native void setSize(JavaScriptObject renderer, int width, int height) /*-{
        renderer.setSize(width, height);
    }-*/;

    private static native JavaScriptObject getController(JavaScriptObject renderer, int index)  /*-{
        if (renderer.xr != null) {
            //console.log("renderer.vr.enabled="+renderer.vr.enabled);
            if (typeof renderer.xr.getController === "function") {
                var controller = renderer.xr.getController(index);
                //var geometry = new $wnd.THREE.BufferGeometry().setFromPoints( [ new $wnd.THREE.Vector3( 0, 0, 0 ), new $wnd.THREE.Vector3( 0, 0, - 1 ) ] );
	            //var line = new $wnd.THREE.Line( geometry );
	            //line.name = 'line';
	            //line.scale.z = 5;
            	//controller.add( line.clone() );
            	controller.addEventListener( 'selectstart', function(){
                    //console.log("Hello World");
                    @de.yard.threed.platform.webgl.WebGlRenderer::buttonAction(IZ)(index,true);
                });
                controller.addEventListener( 'selectend', function(){
                    //console.log("Hello World");
                    @de.yard.threed.platform.webgl.WebGlRenderer::buttonAction(IZ)(index,false);
                });
                return controller;
            }
            console.log("no function getController()");
        }
        return null;
    }-*/;

    private static native void setAnimationLoop(JavaScriptObject renderer) /*-{
        renderer.setAnimationLoop($wnd.renderCallback);
    }-*/;


}
