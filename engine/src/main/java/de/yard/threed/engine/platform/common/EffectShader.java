package de.yard.threed.engine.platform.common;



import de.yard.threed.engine.Uniform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 11.03.16.
 */
public class EffectShader {
    //public LinkedHashMap<String,Uniform> uniforms = new LinkedHashMap<String,Uniform>();
    public List<Uniform> uniforms = new ArrayList<Uniform>();

    public String vertexshader;
    public String fragmentshader;

}
