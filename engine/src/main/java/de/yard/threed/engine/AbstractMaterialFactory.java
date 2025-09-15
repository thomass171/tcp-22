package de.yard.threed.engine;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;

/**
 * Abstraction for providing several options for material building, eg. a custom shader material.
 * For example needed/used for FG effects, but also for other purposes. So no longer in package 'loader'.
 * But do we really need flag 'hasnormals'?
 */
public abstract class AbstractMaterialFactory {

    /**
     * 13.9.25: Considered a good idea to have loader in constructor, but meanwhile I doubt, so keep previous way.
     * @param resourceLoader  Typically this is the loader for the model file, from which further loader eg. for textures can be derived.
     */
    /*public AbstractMaterialFactory(ResourceLoader resourceLoader){
        this.resourceLoader=resourceLoader;
    }*/

    /**
     * Needs to be async because resource(texture) loading might be async.
     * Texture loading is hidden in the platform (desktop can wait, threejs
     * has async), so we can set a texture uniform without really having loaded the texture.
     *
     * @param resourceLoader  Typically this is the loader for the model file, from which further loader eg. for textures can be derived.
     * @param mat
     * @param texturebasepath for bundle and HTTP. Might only be null with absolute texturename. Is relative to resourceloader or absolute path (eg. "engine:cesiumbox").
     * @return
     */
    public abstract Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial mat, ResourcePath texturebasepath, boolean hasnormals);

    /**
     * 23.2.25: Appears not ready for async (teture via http). But why not?
     */
    public Texture resolveTexture(String texturename, ResourceLoader resourceLoader, ResourcePath texturebasepath, boolean wraps, boolean wrapt, Log logger) {

            /*21.12.16 nicht mehr noetig wegen ResourcePath if (texturebasepath == null) {
                texturebasepath = ".";
            }*/
        if (StringUtils.contains(texturename, ":")) {
            // use case isn't gltf but manually build model definitions. Is a kind of absolute texture path.
            int index = StringUtils.indexOf(texturename, ":");
            if (index == -1) {
                logger.warn("No ':' found in texture name");
            }
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
                logger.warn("no texturebasepath. Not building texture.");
                return null;
            }
        }

        if (resourceLoader != null) {
            //BundleResource br = new BundleResource(texturebasepath, texturename);
            //br.bundle = bundle;
            URL br = resourceLoader.fromRootReference(texturebasepath, texturename).getUrl();
            Texture texture = new Texture/*.buildBundleTexture*/(br, wraps, wrapt);
            if (texture.texture == null) {
                // 13.9.23: Better to log this
                logger.warn("failed to build texture from " + texturename + " at " + texturebasepath);
                texturename = texturename;
            }
            return texture;

        } else {
            // 26.4.17: resourceLoader must exist
            logger.error("bundle not set");
            return null;
        }
    }
}
