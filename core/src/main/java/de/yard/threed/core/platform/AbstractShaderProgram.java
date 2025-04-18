package de.yard.threed.core.platform;


import de.yard.threed.core.CharsetException;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Uniform;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract super class for all NativeProgram implementations. So only to be used
 * inside a platform.
 */
public abstract class AbstractShaderProgram {

    public List<Uniform> uniforms = new ArrayList<>();
    public String name, vertexshader, fragmentshader;

    public AbstractShaderProgram(String name, String vertexShader, String fragmentShader) {
        this.name = name;
        this.vertexshader = vertexShader;
        this.fragmentshader = fragmentShader;
    }

    // the source file names
   /* public String vertexshader;
    public String fragmentshader;
*/
   /* public <T> void addUniform(String name, NativeUniform uniform) {
        //uniforms.put(name, uniform);
        uniforms.put(name, uniform);
    }*/
/*
    public List<Uniform> getUniforms() {
        return uniforms;//new ArrayList(uniforms.keySet());
    }*/

    public void validate(String name) {
        if (!name.startsWith("u_")) {
            // 'u_' is helpful for string replacement in JME
            throw new RuntimeException("Uniform names should have prefix 'u_'");
        }
    }

    public void addSampler2DUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.SAMPLER_2D));
    }

    public void addMatrix3Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.MATRIX3));
    }

    public void addFloatVec3Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT_VEC3));
    }

    public void addFloatVec4Uniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT_VEC4));
    }

    public void addBooleanUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.BOOL));
    }

    public void addFloatUniform(String name) {
        validate(name);
        uniforms.add(new Uniform(name, UniformType.FLOAT));
    }

    protected static String loadShader(BundleResource shader) {
        Bundle bundle = shader.bundle;
        if (bundle == null) {
            BundleRegistry.getBundle(shader.getBundlename());
        }
        try {
            return bundle.getResource(shader).getContentAsString();
        } catch (CharsetException e) {
            throw new RuntimeException(e);
        }
    }
}
