package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeAudioClip;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;

/**
 * Use native threejs loader like for textures to avoid binary data muddle.
 */
public class WebGlAudioClip implements NativeAudioClip {

    static Log logger = Platform.getInstance().getLog(WebGlAudioClip.class);
    JavaScriptObject audioClip;
    int id;
    static int gid = 1;

    private WebGlAudioClip(BundleResource br) {
        id =gid++;
        audioClip = loadClip(br.getFullName(), this, id);
    }

    public static WebGlAudioClip loadFromBundle(BundleResource br) {
        return new WebGlAudioClip(br);
    }

    /**
     * Callback for async loading
     */
    public void clipLoaded() {
        logger.debug("clip loaded for id " + id);
    }

    private static native JavaScriptObject loadClip(String filename, WebGlAudioClip instance, int id)  /*-{
        var audioLoader = new $wnd.THREE.AudioLoader();
        audioLoader.load(
	        filename,
            // onLoad callback
            function(buffer) {
                console.log("clip loaded");
                $wnd.loadedaudiobuffer.set(id, buffer);
                instance.@WebGlAudioClip::clipLoaded()();
            }//,
            // onProgress callback
          //  function (xhr) {
          //      console.log( (xhr.loaded / xhr.total * 100) + '% loaded' );
          //  },
            // onError callback
           // function ( err ) {
              //  console.log( 'load audio clip failed', err );
            //}
        );
    }-*/;
}
