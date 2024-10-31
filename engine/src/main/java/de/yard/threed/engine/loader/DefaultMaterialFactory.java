package de.yard.threed.engine.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;

import java.util.HashMap;

/**
 * 29.10.24: Extracted from PortableModelBuilder.java
 */
public class DefaultMaterialFactory implements MaterialFactory {

    Log logger = Platform.getInstance().getLog(DefaultMaterialFactory.class);

    /**
     * 27.12.17: public static, um allgemeingueltig aus einem LoadedMaterial ein Material zu machen. War frueher in LoadedFile.
     * 30.12.18: For loading a texture, the model origin (eg. a bundle) and an optional different path is needed. The texture name is taken from the material.
     * Also an absolute path in texture name like "bundle:/xx/yy/zz.png" is possible. Then it can be loaded without further information.
     * <p>
     * Returns mull, wenn bei Texturen Bundle oder Path fehlen. Aufrufer kann DummyMaterial verwenden oder auch keins (wird dann wireframe).
     * 13.2.24: Shouldn't need a bundle any more.
     *
     * @param mat
     * @param texturebasepath for bundle and HTTP. Might only be null with absolute texturename. Is relative to resourceloader or absolute path (eg. "engine:cesiumbox").
     * @return
     */
    @Override
    public /*10.4.17*/  /*Native*/Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial mat, ResourcePath texturebasepath, boolean hasnormals) {
        NativeMaterial nmat;
        //SHADED ist der Defasult
        HashMap<NumericType, NumericValue> parameters = new HashMap<NumericType, NumericValue>();
        if (!mat.isShaded()) {
            parameters.put(NumericType.SHADING, new NumericValue(NumericValue.UNSHADED));
        } else {
            if (!hasnormals) {
                parameters.put(NumericType.SHADING, new NumericValue(NumericValue.FLAT));
            }
        }

        if (mat.getTexture() != null) {
            String texturename = mat.getTexture();
            /*21.12.16 nicht mehr noetig wegen ResourcePath if (texturebasepath == null) {
                texturebasepath = ".";
            }*/
            if (StringUtils.contains(texturename, ":")) {
                // use case isn't gltf but manually build model definitions. Is a kind of absolute texture path.
                int index = StringUtils.indexOf(texturename, ":");
                texturebasepath = null;
                // texturename in resourceloader will be replaced later anyway, so just "" is ok.
                Bundle bundle = BundleRegistry.getBundle(StringUtils.substring(texturename, 0, index));
                if (bundle == null) {
                    logger.warn("bundle not found:" + StringUtils.substring(texturename, 0, index));
                }
                resourceLoader = new ResourceLoaderFromBundle(new BundleResource(bundle, null, ""));
                texturename = StringUtils.substring(texturename, index + 1);
            } else {
                if (texturebasepath == null) {
                    logger.warn("no texturebasepath. Not building material.");
                    return null;
                }
            }
            Texture texture;
            HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();

            if (resourceLoader != null) {
                //BundleResource br = new BundleResource(texturebasepath, texturename);
                //br.bundle = bundle;
                URL br = resourceLoader.fromRootReference(texturebasepath, texturename).getUrl();
                texture = new Texture/*.buildBundleTexture*/(br, mat.getWraps(), mat.getWrapt());
                if (texture.texture == null) {
                    // 13.9.23: Better to log this
                    logger.warn("failed to build texture from " + texturename + " at " + texturebasepath);
                    texturename = texturename;
                }
                map.put("basetex", texture.texture);

            } else {
                // 26.4.17: resourceLoader muat exist
                logger.error("bundle not set");

            }
            //map.put("normalmap",normalmap.texture);
            //TODO die anderen Materialparameter
            nmat = Platform.getInstance().buildMaterial(null, null, map, parameters, null);
        } else {
            HashMap<ColorType, Color> color = new HashMap<ColorType, Color>();
            color.put(ColorType.MAIN, mat.getColor());
            //TODO die restlichen colors
            // 25.4.19 unshaded wird oben zwar schon eingetragen, aber nicht immer. "shaded" ist eh etwas unklar. Auf jeden Fall bleibt ein Material mit Color in JME sonst schwarz.
            //Darum erstmal immer setzen, bis klar ist, was mit Property "shaded" ist. 28.4.19: Das ist aber doof, nur weil JME die combination shaded/ambientLight schwarz darstellt.
            //Evtl. wegen Normale?
            //parameters.put(NumericType.UNSHADED, new NumericValue(1));
            // 10.8.24: consider transparency
            if (mat.getTransparency() != null) {
                parameters.put(NumericType.TRANSPARENCY, new NumericValue(mat.getTransparency().floatValue()));
            }
            nmat = Platform.getInstance().buildMaterial(null, color, null, parameters, null);
        }
        return new Material(nmat);
    }
}
