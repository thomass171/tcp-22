package de.yard.threed.platform.webgl;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeProgram;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Uniform;
import de.yard.threed.engine.UniformType;
import de.yard.threed.engine.platform.AbstractShaderProgram;
import de.yard.threed.engine.platform.common.ShaderUtil;

/**
 * A material and uniform setter do not belong here because a program might be shared.
 */
public class WebGlProgram extends AbstractShaderProgram implements NativeProgram {
    static Log logger = Platform.getInstance().getLog(WebGlProgram.class);

    public WebGlProgram(String name, BundleResource vertexShader, BundleResource fragmentShader) {
        super(name,convertShader(vertexShader, loadShader(vertexShader)), convertShader(fragmentShader,loadShader(fragmentShader)));
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
        // Hmmm. Done in material?
    }


    private static String convertShader(BundleResource ressourcename, String bytebuf) {
        String source = bytebuf;
        //HashMap<String,String> translatemap = new HashMap<String, String>();
        source = ShaderUtil.preprocess(source/*,translatemap*/);
        if (ressourcename.getFullName().endsWith(".vert")) {
            //source = source.replaceAll("MODELVIEWPROJECTIONMATRIX", "g_WorldViewProjectionMatrix");
            source = source.replaceAll("PROJECTIONMATRIX", "projectionMatrix");
            source = source.replaceAll("MODELVIEWMATRIX", "modelViewMatrix");
            source = source.replaceAll("VERTEX", "position");
            source = source.replaceAll("MULTITEXCOORD0", "uv");
            source = source.replaceAll("NORMALMATRIX", "normalMatrix");
            source = source.replaceAll("NORMAL", "normal");
            source = source.replaceAll("OUT", "out");
        }
        if (ressourcename.getFullName().endsWith(".frag")) {
            source = source.replaceAll("FRAGCOLOR", "gl_FragColor");
            source = source.replaceAll("TEXTURE2D", "texture2D");
            source = source.replaceAll("IN", "in");
        }
        logger.debug("final shader source: " + source);
        return source;
    }

}

