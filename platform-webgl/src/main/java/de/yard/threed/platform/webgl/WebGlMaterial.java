package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.CharsetException;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.ResourceNotFoundException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.Effect;
import de.yard.threed.engine.Uniform;
import de.yard.threed.engine.UniformType;
import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.engine.platform.common.ShaderUtil;


import java.util.HashMap;

/**
 * Created by thomass on 25.04.15.
 */
public class WebGlMaterial implements NativeMaterial {
    static Log logger = Platform.getInstance().getLog(WebGlMaterial.class);
    JavaScriptObject material;
    //6.7.17 String name;

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
    
   /* public static WebGlMaterial buildCustomShaderMaterial(NativeTexture[] texture, Effect effect) {
       
    }*/

    /**
     * ThreeJS verwendet keinen ARGB Wert für Farbe, sondern nur RGB. Man kann aber trozdem ARGB reinstecken. Er dürfte die
     * Werte von rechts rausnehmen, so dass A nicht stoert.
     *
     * @param colors
     * @param textures
     * @param params
     * @return
     */
    public static NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> colors, HashMap<String, NativeTexture> textures, HashMap<NumericType, NumericValue> params, Effect effect) {
        Color col = (colors == null) ? null : colors.get(ColorType.MAIN);
        Float transparency = NumericValue.transparency(params);
        logger.debug("buildMaterial: name=" + name + ",col=" + col + ",params=" + params + ",effect=" + effect);

        if (effect != null) {
            logger.debug("Building effect " + effect.name);
            // Vorbelegung mit Trivialshadern fuer evtl. Testzwecke und als Fallback bei Fehlern beim Laden
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
            //Testmode ist für Trivialshader
            boolean testmode = false;
            if (!testmode) {
                try {
                    vertexshader = loadShader(effect.shader.vertexshader);
                    fragmentshader = loadShader(effect.shader.fragmentshader);
                } catch (ResourceNotFoundException e) {
                    logger.error("Loading shader failed: " + e.getMessage());
                }
            }
            JavaScriptObject uniforms = buildUniformsForEffect(effect, textures, NumericValue.unshaded(params));
            return new WebGlMaterial(name, buildCustomShaderMaterial(uniforms, vertexshader, fragmentshader, transparency != null));
        }
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
     * unwanted dependency to Uniform
     *
     * @return
     */
    private static JavaScriptObject buildUniformsForEffect(Effect effect, HashMap<String, NativeTexture> textures, boolean unshaded) {
        JavaScriptObject uniforms = buildUniform(null, null, null, null);
        for (Uniform uniform : effect.shader.uniforms) {
            //  Uniform uniform = effect.uniforms.get(key);
            //TODO das muss irgendwie generischer gehen mit den uniforms
            if (uniform.name.equals("isunshaded")) {

                uniforms = buildUniformi(uniforms, uniform.name, getThreeJsType(uniform.type), (int) (unshaded ? 1 : 0));
            } else {
                WebGlTexture texture = (WebGlTexture) textures.get(uniform.name);
                if (texture == null) {
                    throw new RuntimeException("uniform not set:" + uniform.name);
                }
                uniforms = buildUniform(uniforms, uniform.name, getThreeJsType(uniform.type), texture.texture);
            }
        }
        return uniforms;
    }

    /**
     * Infos: https://github.com/mrdoob/three.js/wiki/Uniforms-types
     *
     * @param type
     * @return
     */
    private static String getThreeJsType(UniformType type) {
        switch (type) {
            case FLOAT_VEC4:
                return "f";
            case SAMPLER_2D:
                return "t";
            case BOOL:
                // als integer
                return "i";
            default:
                logger.error("unknown uniform type " + type);
                return "unknown";
        }
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

   /* @Override
    public boolean isTransparent() {
        Util.notyet();
        return false;
    }*/

    private static String loadShader(String ressourcename) throws ResourceNotFoundException {
        logger.debug("load shader " + ressourcename);
        String source = null;
        //  try {
        // 20.4.17: Jetzt aus Bundle
        String bytebuf;
        try {
            bytebuf = BundleRegistry.getBundle("engine").getResource(ressourcename).getContentAsString();
        } catch (CharsetException e) {
            // TODO improved eror handling
            throw new RuntimeException(e);
        }
        source = bytebuf;//new String(bytebuf, "UTF-8");
       /* } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException(ressourcename + " UTF-8 decode failed", e);
        }*/
        //HashMap<String,String> translatemap = new HashMap<String, String>();
        source = ShaderUtil.preprocess(source/*,translatemap*/);
        if (ressourcename.endsWith(".vert")) {
            //source = source.replaceAll("MODELVIEWPROJECTIONMATRIX", "g_WorldViewProjectionMatrix");
            source = source.replaceAll("PROJECTIONMATRIX", "projectionMatrix");
            source = source.replaceAll("MODELVIEWMATRIX", "modelViewMatrix");
            source = source.replaceAll("VERTEX", "position");
            source = source.replaceAll("MULTITEXCOORD0", "uv");
            source = source.replaceAll("NORMALMATRIX", "normalMatrix");
            source = source.replaceAll("NORMAL", "normal");
        }
        if (ressourcename.endsWith(".frag")) {
            source = source.replaceAll("FRAGCOLOR", "gl_FragColor");
            source = source.replaceAll("TEXTURE2D", "texture2D");
        }
        // logger.debug("shader source: " + source);
        return source;
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

    private static native JavaScriptObject buildCustomShaderMaterial(JavaScriptObject uniforms, String vertexshader, String fragmentshader, boolean transparent)  /*-{
       // var uniforms = {
            //myColor: { type: "c", value: new $wnd.THREE.Color( 0xffffff ) },
//};
 // uniforms["Texture0"] = { type: "t", value: ptexture  };



var attributes = {
  size: { type: 'f', value: [] },
};

for (var i=0; i < 64; i++) {
  attributes.size.value[i] = 5 + Math.floor(Math.random() * 10);
}

        var mat = new $wnd.THREE.ShaderMaterial({
            uniforms: uniforms,
            attributes: attributes,
            vertexShader: vertexshader,
            fragmentShader: fragmentshader
        });
        if (transparent) {
            mat.transparent = true;
            mat.opacity = 0.5;
        }
        return mat;
    }-*/;

    private static native JavaScriptObject buildUniform(JavaScriptObject puniforms, String name, String ptype, JavaScriptObject pvalue)  /*-{
        if (puniforms == null) {
            var uniforms = {};
            return uniforms;
        }
        puniforms[name] = { type: ptype, value: pvalue  };
        return puniforms;
    }-*/;

    private static native JavaScriptObject buildUniformi(JavaScriptObject puniforms, String name, String ptype, int pvalue)  /*-{
        if (puniforms == null) {
            var uniforms = {};
            return uniforms;
        }
        puniforms[name] = { type: ptype, value: pvalue  };
        return puniforms;
    }-*/;

    private static native void setName(JavaScriptObject mat, String name)  /*-{
       mat.name = name;
    }-*/;

    private static native String getName(JavaScriptObject mat)  /*-{
       return mat.name;
    }-*/;
}
