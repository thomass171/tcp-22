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
     * 9.11.15: Verwendet nicht mehr den so auf relative Verzeichnisse fixierten Assetmanager
     * 11.4.17: Liefert null bei (already logged) error.
     *
     * @return
     */
    static JmeTexture loadFromFile(NativeResource textureresource) {
        //long starttime = Platfo.currentTimeMillis();
        //13.11.15: zu langsam: return buildFromImage(JmeImageUtil.buildImageData(JmeImageUtil.loadImageFromFile(new File(filename))));
        //4.5.16: Ohne Zwischenschritt ImageData versuchen. Erscheint aber nicht wirklich schneller
        //JmeTexture tex = buildFromImage(ImageUtil.loadImageFromFile(new File(filename)));
        JmeTexture tex = null;
        // AwtLoader ist langsam.
        // Nicht nur fuer PNG, denn wer weiss, ob bei JPg o.ae. nicht aehnliches gilt.
        // 5.9.16: Ich krieg das aber ohne AWTLoader in keines der JME Formate.
        // 16.10.18: Mit jpg ist es zwar auch langsam, aber speiochern in eigenem Format bläht den Cache enorm auf. Darum lass ich
        // anderes ausser png
        if (textureresource.getName().toUpperCase().endsWith(".PNG")) {
            LoadedImage li = ImageUtil.loadPNG(textureresource);
            if (li == null) {
                return null;
            }

            //27.7.21 Warum braucht der hier auf einmal so viel memory (OOM in direct buffer)? Wegen fehlendem Texturepool
            //logger.debug("loaded image from cache "+textureresource.getName()+" "+ ((li.width*li.height)/1024)+" kB");
            BufferedImage bufferedimage = new BufferedImage(li.width, li.height, BufferedImage.TYPE_INT_ARGB);
            int[] argb = ImageUtils.toARGB(li.width * li.height, li.buffer);

            bufferedimage.setRGB(0, 0, li.width, li.height, argb, 0, li.width);
            Image img = new AWTLoader().load(bufferedimage/*Image.Format.BGRA8,li.width,li.height,li.buffer*/, false);
            tex = new JmeTexture(new Texture2D(img), textureresource.getName());
            li.buffer.clear();
        } else {
            try {
                tex = buildFromInputStream(FileReader.getInputStream(FileReader.getFileStream(textureresource)));
            } catch (IOException e) {
                //2.10.19: Kein Stacktrace, kann bei rgb Textures (bluebird) schon mal sein.
                logger.error("IO Exception" ,e/*+ e.getMessage()/*2.10.19, e*/);
                return null;
            }
        }
        //logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));

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
