package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeVRController;

import de.yard.threed.core.platform.Platform;

/**
 * 19.10.18
 */
public class WebGlVRController extends WebGlSceneNode implements NativeVRController {
    Log logger = Platform.getInstance().getLog(WebGlVRController.class);
    //scene node isType the controller
    //JavaScriptObject controller;

    private WebGlVRController(JavaScriptObject controller) {
        // SceneNode adds the object to the scene. Is this a problem as it already might exist?
        super(controller);
        //this.controller = controller;
    }

    public static WebGlVRController buildController( JavaScriptObject controller) {
        return new WebGlVRController(controller);
    }

}
