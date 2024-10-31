package de.yard.threed.engine.loader;

import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.Material;

/**
 * Abstraction needed/used for FG effects
 */
public interface MaterialFactory {
    Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial mat, ResourcePath texturebasepath, boolean hasnormals);
}
