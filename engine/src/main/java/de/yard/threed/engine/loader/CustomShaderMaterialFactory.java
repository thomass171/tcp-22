package de.yard.threed.engine.loader;

import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.AbstractMaterialFactory;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.platform.common.ShaderProgram;

/**
 *
 */
public class CustomShaderMaterialFactory extends AbstractMaterialFactory {

    Log logger = Platform.getInstance().getLog(CustomShaderMaterialFactory.class);

    ShaderProgram program;

    /**
     * Assume program is shared across several materials.
     */
    public CustomShaderMaterialFactory(ShaderProgram program) {
        this.program = program;
    }

    /**
     * 24.2.25: See super class for parameter specification
     * @return
     */
    @Override
    public Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial pmat, ResourcePath texturebasepath, boolean hasnormals) {

        if (pmat.getTexture() == null) {
            logger.warn("No texture. Falling back to default factory");
            DefaultMaterialFactory materialFactory = new DefaultMaterialFactory();
            return materialFactory.buildMaterial(resourceLoader, pmat, texturebasepath, hasnormals);
        }
        Texture texture = resolveTexture(pmat.getTexture(), resourceLoader, texturebasepath, pmat.getWraps(), pmat.getWrapt(), logger);
        if (texture == null) {
            logger.warn("no texture. Not building material.");
            return null;
        }

        Material mat = Material.buildCustomShaderMaterial(program, true);

        mat.material.getUniform(Uniform.TEXTURE).setValue(texture.texture);
        // other uniforms have good defaults

        //SHADED ist der Defasult
        /*TODO set unshaded??? if (!pmat.isShaded()) {
            parameters.put(NumericType.SHADING, new NumericValue(NumericValue.UNSHADED));
        } else {
            if (!hasnormals) {
                parameters.put(NumericType.SHADING, new NumericValue(NumericValue.FLAT));
            }
        }*/
        return mat;
    }
}
