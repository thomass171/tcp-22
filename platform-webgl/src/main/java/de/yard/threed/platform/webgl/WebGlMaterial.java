package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeUniform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.engine.Uniform;
import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by thomass on 25.04.15.
 */
public class WebGlMaterial implements NativeMaterial {
    static Log logger = Platform.getInstance().getLog(WebGlMaterial.class);
    JavaScriptObject material;
    //6.7.17 String name;
    WebGlProgram webglProgram;
    public Map<String, NativeUniform> uniforms = null;

    /**
     * Constructor nur um Wrapper anzulegen, nicht das eigentliche Material.
     *
     * @param material
     */
    public WebGlMaterial(JavaScriptObject material) {
        this.material = material;
    }

    private WebGlMaterial(String name, JavaScriptObject material) {
        this.material = material;
        setName(this.material, name);
    }

    public WebGlMaterial(String name, WebGlProgram program, boolean opaque) {

        // uniforms belong to the material, so we need to create WebGlMaterial here
         uniforms = new HashMap<>();
        JavaScriptObject nativeuniforms = buildUniformsForEffect(program /*effect, textures, NumericValue.unshaded(params)*/, uniforms);
        //return new WebGlMaterial(name, buildCustomShaderMaterial(uniforms, vertexshader, fragmentshader, transparency != null));
        material = buildCustomShaderMaterial(nativeuniforms, program.vertexshader, program.fragmentshader, false/*TODO transparency != null*/);

        setName(this.material, name);
        this.webglProgram = program;


        //Float transparency = NumericValue.transparency(definition.parameters);
        boolean hasnormalmap = false;

        // Even when using dedicated shader which handle transparency its imported to tell the engine
        // to put these objects at the end of rendering.
        if (!opaque/*effect.transparent*/) {
            /* TODO
            if (mat.getParam("UseAlpha") != null) {
                mat.setBoolean("UseAlpha", true);
            }
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);*/
        }
    }


    /**
     * ThreeJS verwendet keinen ARGB Wert für Farbe, sondern nur RGB. Man kann aber trozdem ARGB reinstecken. Er dürfte die
     * Werte von rechts rausnehmen, so dass A nicht stoert.
     *
     * @param colors
     * @param textures
     * @param params
     * @return
     */
    public static NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> colors, HashMap<String, NativeTexture> textures, HashMap<NumericType, NumericValue> params) {
        Color col = (colors == null) ? null : colors.get(ColorType.MAIN);
        Float transparency = NumericValue.transparency(params);
        logger.debug("buildMaterial: name=" + name + ",col=" + col + ",params=" + params);

        WebGlTexture texture = (WebGlTexture) ((textures == null) ? null : textures.get("basetex"));
        WebGlTexture normalmap = (WebGlTexture) ((textures == null) ? null : textures.get("normalmap"));

        if (texture == null && col == null) {
            logger.warn("neither texture nor color. Setting blue");
            col = Color.BLUE;
        }

        WebGlMaterial mat;
        boolean transparent = transparency != null;
        if (NumericValue.unshaded(params)) {
            // bei unshaded wir keine normal map verwendet. Das macht ja keinen Sinn.
            //logger.debug("Building unshaded");
            if (texture != null) {
                mat = new WebGlMaterial(name, buildBasicMaterialNative(texture.texture, transparent));
            } else {
                // 25.9.24: Also needs alpha channel for transparency
                mat = new WebGlMaterial(name, buildBasicMaterialNative(col.getARGB()/*col.getRGB()*/, transparency));
            }
        } else {
            // Wenn eine Shininess definiert ist, Phong nehmen, sonst Lambert.
            // Das koennte einigermassen schluessig sein.
            // Nur Phong kennt eine NormalMap, Lambert nicht
            // 3.5.19: Lambert mal deaktiviert, weil es kein flatshading kann, zumindest nicht per BufferGeometry. Ob man auf Lambert vielleicht ganz verzichtet?
            if (true/*params != null && params.get(NumericType.SHININESS) != null*/) {
                if (texture != null) {
                    //logger.debug("buildPhongMaterialNative for texture");
                    mat = new WebGlMaterial(name, buildPhongMaterialNative(texture.texture, (normalmap == null) ? null : normalmap.texture, transparent));
                } else {
                    // hier duerfte normalmap auch keinen Sinn machen.
                    //logger.debug("buildPhongMaterialNative transparent=" + transparent);
                    mat = new WebGlMaterial(name, buildPhongMaterialNative(col.getARGB(), transparency));
                }
            } else {
                if (texture != null) {
                    mat = new WebGlMaterial(name, buildLambertMaterialNative(texture.texture, transparent));
                } else {
                    //logger.debug("Lambert in " + Util.format("0x%x", col.getARGB()));
                    mat = new WebGlMaterial(name, buildLambertMaterialNative(col.getARGB(), transparent));
                }
            }
            //flatshading only for shading material, not unshaded. Not possible for Lambert. Needs computeFlatVertexNormals() later.
            if (NumericValue.flatshading(params)) {
                mat.setFlatShading(true);
            }
        }

        return mat;
    }

    /**
     * No idea how to share shader.
     * So for now create a new material for each using of a program.
     */
    public static NativeMaterial buildMaterial(WebGlProgram program, boolean opaque) {
        // No idea how to share shader. So for now create a new material for each using of a program.

        logger.debug("Building program " + program.name);
        // Default trivial shader for testing and fallback in case of load error
        String vertexshader = "uniform vec3 color;\n" +
                "attribute float size;\n" +
                "\n" +
                "varying vec3 vColor;  // 'varying' vars are passed to the fragment shader\n" +
                "\n" +
                "void main() {\n" +
                "  vColor = color;   // pass the color to the fragment shader\n" +
                "  gl_Position = projectionMatrix * \n" +
                "                        modelViewMatrix * \n" +
                "                        vec4(position,1.0);" +
                "}";
        String fragmentshader = "varying vec3 vColor;\n" +
                "uniform sampler2D texture1;\n" +
                "\n" +
                "void main() {  \n" +
                "  gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);  \n" +
                "}";
        //Testmode is for trivial shader
        boolean testmode = false;
        if (!testmode) {
            try {
                vertexshader = /*loadShader*/(program.vertexshader);
                fragmentshader = /*loadShader*/(program.fragmentshader);
            } catch (Exception e) {
                logger.error("Loading shader failed: " + e.getMessage());
            }
        }

        WebGlMaterial mat = new WebGlMaterial(program.name, program,opaque);
        return mat;
    }


    public void setWireframe(boolean wireframe) {
        setWireframe(material, wireframe);
    }

    @Override
    public void setTransparency(boolean enabled) {
        setTransparency(material, enabled);
    }

    private void setFlatShading(boolean enabled) {
        //logger.debug("setFlatShading " + enabled);
        setFlatShading(material, enabled);
    }

    @Override
    public String getName() {
        return getName(material);
    }

    @Override
    public void setName(String name) {
        setName(material, name);
    }

    @Override
    public NativeTexture[] getMaps() {
        return new NativeTexture[0];
    }

    @Override
    public NativeUniform getUniform(String name) {
        WebGlUniform uniform = (WebGlUniform)uniforms.get(name);
        return uniform;
    }

    private static native JavaScriptObject buildLambertMaterialNative(int col, boolean transparent)  /*-{
        var mat = new $wnd.THREE.MeshLambertMaterial({color: col, transparent : transparent});
        return mat;
    }-*/;

    private static native JavaScriptObject buildPhongMaterialNative(int col, Float transparency)  /*-{
        var mat = null;
        if (transparency == null) {
             mat = new $wnd.THREE.MeshPhongMaterial({color: col});
         } else {
             mat = new $wnd.THREE.MeshPhongMaterial({color: col, transparent: true, opacity: 1.0 - transparency});
        }
        return mat;
    }-*/;

    private static native JavaScriptObject buildBasicMaterialNative(int col, Float transparency)  /*-{
        var mat;
         if (transparency == null) {
             mat = new $wnd.THREE.MeshBasicMaterial({color: col});
         } else {
             mat = new $wnd.THREE.MeshBasicMaterial({color: col, transparent: true, opacity: 1.0 - transparency});
        }
        return mat;
    }-*/;

    private static native JavaScriptObject buildLambertMaterialNative(JavaScriptObject texture, boolean transparent)  /*-{
        var mat = new $wnd.THREE.MeshLambertMaterial({map: texture, transparent : transparent});
        return mat;
    }-*/;

    private static native JavaScriptObject buildPhongMaterialNative(JavaScriptObject texture, JavaScriptObject normalmap, boolean transparent)  /*-{
        var mat = new $wnd.THREE.MeshPhongMaterial({map: texture, normalMap: normalmap, transparent : transparent});
        return mat;
    }-*/;

    private static native JavaScriptObject buildBasicMaterialNative(JavaScriptObject texture, boolean transparent)  /*-{
        var mat = new $wnd.THREE.MeshBasicMaterial({map: texture, transparent : transparent});
        return mat;
    }-*/;

    private static native JavaScriptObject setWireframe(JavaScriptObject mat, boolean wf)  /*-{
       mat.wireframe = wf;
    }-*/;

    private static native JavaScriptObject setTransparency(JavaScriptObject mat, boolean transparent)  /*-{
       mat.transparent = transparent;
    }-*/;

    private static native JavaScriptObject setFlatShading(JavaScriptObject mat, boolean flatShading)  /*-{
       mat.flatShading = flatShading;
       //needed?
       mat.needsUpdate = true;
    }-*/;


    /**
     * unwanted dependency to Uniform(25.1.25 still?)
     *
     * @return
     */
    private JavaScriptObject buildUniformsForEffect(WebGlProgram program/*Effect effect, HashMap<String, NativeTexture> textures, boolean unshaded*/, Map<String, NativeUniform> uniforms) {
        // 25.1.25 why do we need this initial uniform ?? A kind of internal list of uniforms.
        JavaScriptObject nativeUniformMap = buildUniformMap();
        /*25.1.25 moved to new 'for' block and WebGlUniform (Uniform uniform : effect.shader.getUniforms()) {
            //  Uniform uniform = effect.uniforms.get(key);

            if (uniform.name.equals("isunshaded")) {

                uniforms = buildUniformi(uniforms, uniform.name, getThreeJsType(uniform.type), (int) (unshaded ? 1 : 0));
            } else {
                WebGlTexture texture = (WebGlTexture) textures.get(uniform.name);
                if (texture == null) {
                    throw new RuntimeException("uniform not set:" + uniform.name);
                }
                uniforms = buildUniform(uniforms, uniform.name, getThreeJsType(uniform.type), texture.texture);
            }
        }*/
        for (Uniform uniform : program.uniforms) {
            logger.debug("uniform type " + uniform.type);
            switch (uniform.type) {
                case BOOL:
                    buildUniform(nativeUniformMap, uniforms, uniform, new WebGlUniform<Boolean>(uniform.type) {
                        @Override
                        public void setValue(Boolean b) {
                            setBool(b);
                        }
                    });
                    break;
                case SAMPLER_2D:
                    buildUniform(nativeUniformMap, uniforms, uniform, new WebGlUniform<NativeTexture>(uniform.type) {
                        @Override
                        public void setValue(NativeTexture texture) {
                            setObject(((WebGlTexture) texture).texture);
                        }
                    });
                    break;
                case FLOAT_VEC3:
                    buildUniform(nativeUniformMap, uniforms, uniform, new WebGlUniform<Vector3>(uniform.type) {
                        @Override
                        public void setValue(Vector3 v) {
                            setObject(WebGlVector3.toWebGl(v).vector3);
                        }
                    });
                    break;
                case FLOAT_VEC4:
                    Util.notyet();
                    /*uniforms.put(jmeUniformName, new WebGlUniform<Vector4f>() {
                        @Override
                        public void setValue(Vector4f v) {
                            //TODO material.setVector4(jmeUniformName, v);
                        }
                    });*/
                    break;
                case FLOAT:
                    buildUniform(nativeUniformMap, uniforms, uniform, new WebGlUniform<Float>(uniform.type) {
                        @Override
                        public void setValue(Float f) {
                            setFloat(f);
                        }
                    });
                    break;
                case MATRIX3:
                    buildUniform(nativeUniformMap, uniforms, uniform, new WebGlUniform<Matrix3>(uniform.type) {
                        @Override
                        public void setValue(Matrix3 matrix3) {
                            setObject(WebGlMatrix3.toWebGl(matrix3).matrix3);
                        }
                    });
                    break;
                default:
                    throw new RuntimeException("unhandled uniform type " + uniform.type);
            }
        }
        return nativeUniformMap;
    }

    private void buildUniform(JavaScriptObject nativeUniformMap, Map<String, NativeUniform> uniforms, Uniform uniform, WebGlUniform webGlUniform) {

        addUniform(nativeUniformMap, uniform.name, webGlUniform.uniform);
        uniforms.put(uniform.name, webGlUniform);
    }

    private static native JavaScriptObject buildCustomShaderMaterial(JavaScriptObject uniforms, String vertexshader, String fragmentshader, boolean transparent)  /*-{

var attributes = {
  size: { type: 'f', value: [] },
};

for (var i=0; i < 64; i++) {
  attributes.size.value[i] = 5 + Math.floor(Math.random() * 10);
}
        // ShaderMaterial adds many shader code for convenience, which RawShaderMaterial doesn't. However
        // it seems to be the easier way to use that convenience.
        var mat = new $wnd.THREE.ShaderMaterial({
            uniforms: uniforms,
            // 30.1.25 THREE.ShaderMaterial: attributes should now be defined in THREE.BufferGeometry instead. attributes: attributes,
            vertexShader: vertexshader,
            fragmentShader: fragmentshader
        });
        // Even when using dedicated shader which handle transparency its imported to tell the engine
        // to put these objects at the end of rendering.
        if (transparent) {
            mat.transparent = true;
            mat.opacity = 0.5;
        }
        return mat;
    }-*/;

    private static native void addUniform(JavaScriptObject uniformMap, String name, JavaScriptObject uniform)  /*-{
        uniformMap[name] = uniform;
    }-*/;

    private static native JavaScriptObject buildUniformMap()  /*-{
        var uniforms = {};
        return uniforms;
    }-*/;

    private static native void setName(JavaScriptObject mat, String name)  /*-{
       mat.name = name;
    }-*/;

    private static native String getName(JavaScriptObject mat)  /*-{
       return mat.name;
    }-*/;
}
