package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.platform.common.Settings;

/**
 * Created by thomass on 07.05.15.
 */
public class WebGlTexture implements NativeTexture {
    static Log logger = Platform.getInstance().getLog(WebGlTexture.class);

    // Das JS Object ist ein THREE.Texture
    JavaScriptObject texture;

    private WebGlTexture(JavaScriptObject texture) {
        this.texture = texture;
        Settings settings = ((WebGlSceneRunner) WebGlSceneRunner.getInstance()).scsettings;
        // ThreeJs seems to have bi/tri linear as default already.
        /*if (settings.minfilter != null) {
            switch (settings.minfilter) {
                case EngineHelper.GL_NEAREST_MIPMAP_NEAREST:
                    texture.setMinFilter(Texture.MinFilter.NearestNearestMipMap);
                    break;
                case EngineHelper.GL_LINEAR_MIPMAP_NEAREST:
                    texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
                    break;
                case EngineHelper.GL_NEAREST_MIPMAP_LINEAR:
                    texture.setMinFilter(Texture.MinFilter.NearestLinearMipMap);
                    break;
                case EngineHelper.GL_LINEAR_MIPMAP_LINEAR:
                    texture.setMinFilter(Texture.MinFilter.Trilinear);
                    break;
                default:
                    logger.warn("unknown minfilter " + settings.minfilter);
            }
        }
        if (settings.magfilter != null) {
            switch (settings.magfilter) {
                case EngineHelper.GL_NEAREST:
                    texture.setMagFilter(Texture.MagFilter.Nearest);
                    break;
                case EngineHelper.GL_LINEAR:
                    texture.setMagFilter(Texture.MagFilter.Bilinear);
                    break;
                default:
                    logger.warn("unknown magfilter " + settings.magfilter);
            }
        }
        if (settings.anisotropicFilter != null) {
            texture.setAnisotropicFilter(settings.anisotropicFilter);
        }*/
    }

    /**
     * "filename" is a complete URL here. 2.1.24: This seems only true with full qualified bundle.
     * Otherwise its only a path starting with "/bundles".
     */
    public static WebGlTexture loadTexture(/*2.1.24BundleResource*/URL url) {
        String filename=url.getUrl();
        logger.debug("Loading texture " + filename);
        return new WebGlTexture(loadTextureNative(filename));
    }

    public static WebGlTexture createTexture(ImageData imagedata) {
        JsArrayInteger data = (JsArrayInteger) JsArrayInteger.createArray();
        for (int i = 0; i < imagedata.pixel.length; i++) {
            data.push(imagedata.pixel[i]);
        }
        return new WebGlTexture(createTextureNative(imagedata.width, imagedata.height, data));
    }

    public static WebGlTexture buildFromCanvas(WebGlCanvas canvas) {
        return new WebGlTexture(createTextureNative(canvas.canvas));
    }

    public void setWrapS() {
        setWrapS(texture);
    }

    public void setWrapT() {
        setWrapT(texture);
    }

    @Override
    public String getName() {
        return "not yet";
    }

    /**
     * Wenn diese MEthode returned, ist das Image selber noch gar nicht geladen. Das erfolgt dann async.
     *
     * @param filename
     * @return
     */
    private static native JavaScriptObject loadTextureNative(String filename)  /*-{
        //$wnd.alert("cameralight");
        //return $wnd.THREE.ImageUtils.loadTexture(filename);
        var loader = new $wnd.THREE.TextureLoader();
        return loader.load(filename);
    }-*/;

    /**
     * Aus:
     * http://learningthreejs.com/blog/2013/08/02/how-to-do-a-procedural-city-in-100lines/  (
     * und
     * http://stackoverflow.com/questions/16370617/is-it-possible-to-use-a-2d-canvas-as-a-texture-for-a-cube
     *
     * @param width
     * @param height
     * @param data
     * @return
     */
    private static native JavaScriptObject createTextureNative(int width, int height, JsArrayInteger data)  /*-{
        //alert(height);
        //alert("0x"+data[0].toString(16));
        var canvas  = $wnd.document.createElement( 'canvas' );
        canvas.width = width;
        canvas.height = height;
        var context = canvas.getContext( '2d' );
        var imgData = context.createImageData(width,height);
        for (var i=0;i<imgData.data.length;i+=4) {
            //Daten sind im ARGB Format und werden in RGBA erwartet.
            var pixel = data[i/4];
            imgData.data[i+0]=((pixel >> 16) & 0xFF);
            imgData.data[i+1]=((pixel >> 8) & 0xFF);
            imgData.data[i+2]=((pixel >> 0) & 0xFF);
            imgData.data[i+3]=((pixel >> 24) & 0xFF);
        }
        context.putImageData(imgData,0,0);
        var texture = new $wnd.THREE.Texture( canvas);
        texture.needsUpdate = true;
        return texture;
    }-*/;

    private static native JavaScriptObject createTextureNative(JavaScriptObject canvas)  /*-{
        var texture = new $wnd.THREE.Texture( canvas);
        texture.needsUpdate = true;
        return texture;
    }-*/;

    private static native JavaScriptObject setWrapS(JavaScriptObject texture)  /*-{
        texture.wrapS = $wnd.THREE.RepeatWrapping;
    }-*/;

    private static native JavaScriptObject setWrapT(JavaScriptObject texture)  /*-{
        texture.wrapT = $wnd.THREE.RepeatWrapping;
    }-*/;
}
