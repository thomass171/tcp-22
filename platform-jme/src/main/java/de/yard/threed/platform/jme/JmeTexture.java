package de.yard.threed.platform.jme;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.javacommon.FileReader;
import de.yard.threed.javacommon.ImageUtil;
import de.yard.threed.javacommon.ImageUtils;
import de.yard.threed.javacommon.LoadedImage;


import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by thomass on 16.06.15.
 */
public class JmeTexture implements NativeTexture {
    static Log logger = Platform.getInstance().getLog(JmeTexture.class);
    Texture texture;
    String name;


    public JmeTexture(Texture texture, String name) {
        this.texture = texture;
        this.name = name;
        //der steht by default auf 0
        //bringt bei Office aber nichts sichtbares
        //20.5.19 texture.setAnisotropicFilter(4);
        Settings settings = JmeSceneRunner.getInstance().scsettings;
        if (settings.minfilter != null) {
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
        }
    }

    /**
     * 9.11.15: No longer uses the assetmanager, that is focusing on relative directories.
     * 11.4.17: Returns null on (already logged) error.
     *
     * @return
     */
    static JmeTexture loadFromFile(NativeResource textureresource) {
        long starttime = System.currentTimeMillis();
        //4.5.16: Ohne Zwischenschritt ImageData versuchen. Erscheint aber nicht wirklich schneller
        //JmeTexture tex = buildFromImage(ImageUtil.loadImageFromFile(new File(filename)));
        JmeTexture tex = null;
        // AwtLoader/ImageIO is slow.
        // 16.10.18: jpg is also slow, but caching it bloats the cache tremendously. So only use cache for 'png'.
        // 26.8.23: Try again jpg.
        if (textureresource.getName().toUpperCase().endsWith(".PNG") || textureresource.getName().toUpperCase().endsWith(".JPG")) {
            // optionally use cache
            BufferedImage li = ImageUtil.loadCachableImage(textureresource);
            if (li == null) {
                return null;
            }
            // 5.9.16: LoadedImage already has a bytebuffer, but Jme needs a BufferedImage for converting it back to bytebuffer.
            // Thats inefficient, but currently no other option. There is no way without JMEs AWTLoader to get it into one of JMEs formats.
            //logger.debug("loaded image from cache "+textureresource.getName()+" "+ ((li.width*li.height)/1024)+" kB");
            // 28.8.23: Since there is no longer an intermediate LoadedImage (which contained a unintended flip conversion),
            // flip needs to be done here.
            Image img = new AWTLoader().load(li/*Image.Format.BGRA8,li.width,li.height,li.buffer*/, true);
            tex = new JmeTexture(new Texture2D(img), textureresource.getName());
        } else {
            try {
                tex = buildFromInputStream(FileReader.getInputStream(FileReader.getFileStream(textureresource)));
            } catch (IOException e) {
                //2.10.19: Kein Stacktrace, kann bei rgb Textures (bluebird) schon mal sein.
                logger.error("IO Exception", e/*+ e.getMessage()/*2.10.19, e*/);
                return null;
            }
        }
        logger.debug(String.format("building JmeTexture for %s took %d ms",
                textureresource.getFullName(), System.currentTimeMillis() - starttime));

        return tex;
    }

    static JmeTexture buildFromImage(ImageData imagedata) {
        return new JmeTexture(new com.jme3.texture.Texture2D(JmeImageUtil.buildJmeImage(imagedata)), "imagedata");
    }

    static JmeTexture buildFromImage(BufferedImage image) {
        return new JmeTexture(new com.jme3.texture.Texture2D(JmeImageUtil.buildJmeImage(image)), "image");
    }

    static JmeTexture buildFromInputStream(java.io.InputStream ins) throws IOException {
        Image img = JmeImageUtil.buildJmeImage(ins);
        // Fehler wird spaeter moch geloggt.-
        if (img == null)
            return null;
        return new JmeTexture(new com.jme3.texture.Texture2D(img), "stream");
    }

    @Override
    public String getName() {
        return name;
    }
}
