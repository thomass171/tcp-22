package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.NativeCanvas;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;

import java.util.HashMap;


/**
 * <p/>
 * Date: 26.03.14
 * <p/>
 * 12.10.15: Texturen sollten einmalig als Resource in den GPU Speicher geladen werden.
 * Diese Klasse hier ist ueber NativeTexture eine Referenz auf eine Textur im GPU Speicher.
 * Die Platform prüft auf mehrfachLaden einer Textur.
 */
public class Texture {
    Log logger = Platform.getInstance().getLog(Texture.class);
    // Das mit dem public ist wohl eine Kruecke. Might be null in case of load error!
    public NativeTexture texture;
    // texture pool jetzt hier statt in platform
    static TexturePool texturePool = new TexturePool();

    private Texture(BundleResource filename, boolean wraps, boolean wrapt) {
        HashMap<NumericType, NumericValue> param = new HashMap<NumericType, NumericValue>();
        if (wraps) {
            param.put(NumericType.TEXTURE_WRAP_S, NumericValue.REPEAT);
        }
        if (wrapt) {
            param.put(NumericType.TEXTURE_WRAP_T, NumericValue.REPEAT);
        }
        //MA36 no longer cache. Doch cachen, sonst droht OOM.
        //texture = ((Platform) Platform.getInstance()).loadTexture( filename, param);
        //texture = Platform.getInstance().buildNativeTexture( filename, param);
        texture =  texturePool.loadTexture(  filename, param);
    }

    private Texture(/*Bundle dummywegensigbundle,*/ BundleResource filename) {
        HashMap<NumericType, NumericValue> param = new HashMap<NumericType, NumericValue>();
        //MA36 no longer cache. Doch cachen, sonst droht OOM.
        //texture = ((Platform) Platform.getInstance()).loadTexture(  filename, param);
        //texture = Platform.getInstance().buildNativeTexture( filename, param);
        texture =  texturePool.loadTexture(  filename, param);
    }

    /**
     * Textur aus eigenen Daten anlegen. So eine Textur geht dann auch direkt in den GPU Speicher.
     *
     * @param imagedata
     */
    public Texture(ImageData imagedata, boolean fornormalmap) {
        texture = Platform.getInstance().buildNativeTexture(imagedata, fornormalmap);
    }

    public Texture(NativeCanvas canvas) {
        texture = Platform.getInstance().buildNativeTexture(canvas);
    }

    public Texture(ImageData imagedata) {
        this(imagedata, false);
    }

    public Texture(NativeTexture texture) {
        this.texture = texture;
    }

    public static Texture buildNormalMap(ImageData image) {
        return new Texture(image, true);
    }

    public static Texture buildBundleTexture(String bundlename, String filename) {
        BundleResource br = new BundleResource(BundleRegistry.getBundle(bundlename), filename);
        return new Texture(br);
    }

    public static Texture buildBundleTexture(String bundlename, String filename, boolean wraps, boolean wrapt) {
        BundleResource br = new BundleResource(BundleRegistry.getBundle(bundlename), filename);
        return new Texture(br, wraps, wrapt);
    }

    /**
     * bundle in br muss gesetzt sein.
     *
     * @param br
     * @param wraps
     * @param wrapt
     * @return
     */
    public static Texture buildBundleTexture(BundleResource br, boolean wraps, boolean wrapt) {
        return new Texture(br, wraps, wrapt);
    }
}
