package de.yard.threed.engine;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.NativeCanvas;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.resource.URL;

import java.util.HashMap;


/**
 * <p/>
 * Date: 26.03.14
 * <p/>
 * Textures are loaded once into GPU memory. This is via NativeTexture just a reference to it.
 * <p>
 * There is also a TexturePool.
 * 24.10.23: But this is nasty. Texture should be shared on the GPU level, not here. But currently without causes OOM.
 */
public class Texture {
    Log logger = Platform.getInstance().getLog(Texture.class);
    // Das mit dem public ist wohl eine Kruecke. Might be null in case of load error!
    public NativeTexture texture;
    // texture pool jetzt hier statt in platform
    static TexturePool texturePool = new TexturePool();

    /**
     * 13.2.24: Deprecated in favor of below URL based.
     * No need to change to NativeResource because URL is more generic,
     */
    @Deprecated
    public Texture(BundleResource filename, boolean wraps, boolean wrapt) {
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
        texture = texturePool.loadTexture(filename, param);
    }

    /**
     * 13.2.24: Successor of above
     */
    public Texture(URL url, boolean wraps, boolean wrapt) {
        HashMap<NumericType, NumericValue> param = new HashMap<NumericType, NumericValue>();
        if (wraps) {
            param.put(NumericType.TEXTURE_WRAP_S, NumericValue.REPEAT);
        }
        if (wrapt) {
            param.put(NumericType.TEXTURE_WRAP_T, NumericValue.REPEAT);
        }
        texture = texturePool.loadTexture(url, param);
    }

    private Texture(/*Bundle dummywegensigbundle,*/ BundleResource filename) {
        HashMap<NumericType, NumericValue> param = new HashMap<NumericType, NumericValue>();
        //MA36 no longer cache. Doch cachen, sonst droht OOM.
        //texture = ((Platform) Platform.getInstance()).loadTexture(  filename, param);
        //texture = Platform.getInstance().buildNativeTexture( filename, param);
        texture = texturePool.loadTexture(filename, param);
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
        Bundle bundle = BundleRegistry.getBundle(bundlename);
        if (bundle == null) {
            Platform.getInstance().getLog(Texture.class).error("bundle not registered: " + bundlename);
            // 2.1.24 questionable, but probably was done that way similar in the past
            return new Texture((NativeTexture) null);
        }
        BundleResource br = new BundleResource(bundle, filename);
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

    /**
     * Only for tests
     */
    public static boolean hasTexture(String name) {
        return texturePool.hasTexture(name);
    }

    /**
     * Needed in tests when platforms are mixed and eg. DummyTexture and OpenglTexture interfere.
     */
    public static void resetTexturePool() {
        texturePool = new TexturePool();
    }

    public static int texturePoolSize() {
        return texturePool.size();
    }

    /**
     * Assume the original uvs are for the top left element (0,0).
     * Logical 'y' counts from bottom here(!) like 'v' in 'uv'.
     */
    public static Matrix3 getTextureMatrixForGridElement(int wsegments, int hsegments, int x, int y) {
        ProportionalUvMap uvMap = ProportionalUvMap.buildForGridElement(wsegments, x, y, false);

        Matrix3 matrix3 = new Matrix3();
        Vector2 translation = uvMap.getUvFromNativeUv(new Vector2());
        // Need to negate 'v' because reference is the top left element (0,0)
        translation = new Vector2(translation.getX(), translation.getY());
        matrix3.setTranslation(translation);
        return matrix3;
    }
}
