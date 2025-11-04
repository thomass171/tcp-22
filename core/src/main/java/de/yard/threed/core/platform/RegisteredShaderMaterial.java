package de.yard.threed.core.platform;

import de.yard.threed.core.Vector3;

import java.util.List;

/**
 * (Abstract?) super class or helper for all material implementations that use shader.
 * Needed for keeping shader in sync with lighting. So only to be used
 * inside a platform. Be sure to call it *after* setting shader defaults.
 *
 * Will be a challenge to realize when a light moves.
 */
public class RegisteredShaderMaterial {
    Log logger = Platform.getInstance().getLog(RegisteredShaderMaterial.class);
    NativeMaterial mat;

    public RegisteredShaderMaterial(NativeMaterial mat) {
        this.mat = mat;
    }

    public void updateLightUniforms(List<NativeLight> lights) {

        for (NativeLight light : lights) {
            if (light.getAmbientColor() != null && mat.getUniform(Uniform.AMBIENT_LIGHT_COLOR) != null) {
                Vector3 lightColor = light.getAmbientColor().asVector3();
                //logger.debug("Setting Uniform.AMBIENT_LIGHT_COLOR to "+lightColor);
                mat.getUniform(Uniform.AMBIENT_LIGHT_COLOR).setValue(lightColor);//new Vector3(0.2, 0.2, 0.2));
            }
            if (light.getDirectionalColor() != null && mat.getUniform(Uniform.DIRECTIONAL_LIGHT_COLOR) != null) {
                mat.getUniform(Uniform.DIRECTIONAL_LIGHT_COLOR).setValue(light.getDirectionalColor().asVector3());//new Vector3(1, 1, 1));
                // just arbitrary to have a direction (pointing to light origin). (0,0,1) is typical opengl view point.
                mat.getUniform(Uniform.DIRECTIONAL_LIGHT_DIRECTION).setValue(light.getDirectionalDirection());//new Vector3(0, 0, 1));
            }
        }
    }
}
