package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.NativeProgram;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.core.platform.UniformType;
import de.yard.threed.core.platform.AbstractShaderProgram;

/**
 * A material and uniform setter do not belong here because a program might be shared.
 */
public class HomeBrewProgram extends AbstractShaderProgram implements NativeProgram {

    public HomeBrewProgram(String name, BundleResource vertexShader, BundleResource fragmentShader) {
        super(name, loadShader(vertexShader), loadShader(fragmentShader));
    }

    @Override
    public void addSampler2DUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.SAMPLER_2D));
    }

    @Override
    public void addMatrix3Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.MATRIX3));
    }

    @Override
    public void addFloatVec3Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT_VEC3));
    }

    @Override
    public void addFloatVec4Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT_VEC4));
    }

    @Override
    public void addBooleanUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.BOOL));
    }

    @Override
    public void addFloatUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT));
    }

    @Override
    public void compile() {
        // needs uniforms
        //mat = buildProgram();
        //program is build during material building
    }

    /**
     *
     *
     * @return
     */
   /* 1.2.25 needed?  public static HomeBrewMaterial buildProgram(ShaderInfo myShaderInfo) {

        Material mat;
        boolean hasnormalmap = false;

        if (mat.getName() == null) {
            mat.setName(myShaderInfo.name);
        }
        return mat;
    }*/
}
