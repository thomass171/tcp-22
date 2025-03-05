package de.yard.threed.engine.platform.common;


import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeProgram;
import de.yard.threed.core.platform.NativeUniform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;

import java.util.HashMap;
import java.util.Map;

/**
 * 30.12.24 Renamed from EffectShader. Threejs uses name ShaderMaterial.
 * Created by thomass on 11.03.16.
 */
public class ShaderProgram {
    //public LinkedHashMap<String,Uniform> uniforms = new LinkedHashMap<String,Uniform>();
    // private List<Uniform> uniforms = new ArrayList<Uniform>();
    private Map<String, NativeUniform> uniforms = new HashMap<>();
    public NativeProgram program;
    public GeneralParameterHandler<NativeMaterial> defaultSetter;

    public ShaderProgram(String name, BundleResource vertexShader, BundleResource fragmentShader) {
        program = Platform.getInstance().buildProgram(name, vertexShader, fragmentShader);
    }

    /*public <T> void addUniform(String name, NativeUniform uniform) {
        //uniforms.put(name, uniform);
        uniforms.put(name, uniform);
    }*/
/*
    public List<Uniform> getUniforms() {
        return uniforms;//new ArrayList(uniforms.keySet());
    }*/


}
