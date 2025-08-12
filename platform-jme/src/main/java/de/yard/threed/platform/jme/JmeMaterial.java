package de.yard.threed.platform.jme;

import com.jme3.material.Material;
import com.jme3.material.RenderState;

import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeUniform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Uniform;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.engine.platform.common.MaterialDefinition;
import de.yard.threed.core.NumericValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thomass on 25.04.15.
 * <p/>
 * 21.7.16: Die Verwendung von "UseAlpha" scheint keine Auswirkung zu haben. Es indet sich aber in vielen Beispielen.
 */
public class JmeMaterial implements NativeMaterial {
    static Log logger = Platform.getInstance().getLog(JmeMaterial.class);

    Material material;
    private boolean transparent;
    String name;
    // 23.3.17: Braucht er um nachher Normale fuer das MEsh zu berechnen. Grund unklar.
    public boolean hasNormalMap = false;
    JmeTexture basetex;
    //MaterialDefinition definition;
    JmeProgram jmeProgram;
    //
    public Map<String, NativeUniform> uniforms = new HashMap<>();

    private JmeMaterial(String name, Material material, boolean transparent, boolean hasnormalmap, JmeTexture basetex, MaterialDefinition definition) {
        this.name = name;
        this.material = material;
        this.transparent = transparent;
        this.hasNormalMap = hasnormalmap;
        this.basetex = basetex;
        //this.definition = definition;
    }

    private JmeMaterial(String name, Material material, JmeProgram jmeProgram) {
        this.name = name;
        this.material = material;
        this.jmeProgram = jmeProgram;
        for (Uniform uniform : jmeProgram.uniforms) {
            // 8.1.25 Remove prefix "u_" because JME uses "m_" implicitly.
            String jmeUniformName = uniform.name.substring(2);
            switch (uniform.type) {
                case BOOL:
                    uniforms.put(jmeUniformName, new JmeUniform<Boolean>() {
                        @Override
                        public void setValue(Boolean v) {
                            material.setBoolean(jmeUniformName, v);
                        }
                    });
                    break;
                case SAMPLER_2D:
                    uniforms.put(jmeUniformName, new JmeUniform<NativeTexture>() {
                        @Override
                        public void setValue(NativeTexture texture) {
                            if (texture == null) {
                                logger.warn("texture is null");
                                return;
                            }
                            material.setTexture(jmeUniformName, ((JmeTexture) texture).texture);
                        }
                    });
                    break;
                case FLOAT_VEC3:
                    uniforms.put(jmeUniformName, new JmeUniform<Vector3>() {
                        @Override
                        public void setValue(Vector3 v) {
                            material.setVector3(jmeUniformName, new Vector3f(v.getX(), v.getY(), v.getZ()));
                        }
                    });
                    break;
                case FLOAT_VEC4:
                    uniforms.put(jmeUniformName, new JmeUniform<Quaternion>() {
                        @Override
                        public void setValue(Quaternion v) {
                            material.setVector4(jmeUniformName, new Vector4f(v.getX(),v.getY(),v.getZ(),v.getW()));
                        }
                    });
                    break;
                case FLOAT:
                    uniforms.put(jmeUniformName, new JmeUniform<Float>() {
                        @Override
                        public void setValue(Float f) {
                            material.setFloat(jmeUniformName, f);
                        }
                    });
                    break;
                case MATRIX3:
                    uniforms.put(jmeUniformName, new JmeUniform<Matrix3>() {
                        @Override
                        public void setValue(Matrix3 v) {
                            // JME workaround for mat3
                            Vector3 v3 = v.getCol0();
                            material.setVector3(jmeUniformName + "_col0", new Vector3f(v3.getX(), v3.getY(), v3.getZ()));
                            v3 = v.getCol1();
                            material.setVector3(jmeUniformName + "_col1", new Vector3f(v3.getX(), v3.getY(), v3.getZ()));
                            v3 = v.getCol2();
                            material.setVector3(jmeUniformName + "_col2", new Vector3f(v3.getX(), v3.getY(), v3.getZ()));
                        }
                    });
                    break;
                default:
                    throw new RuntimeException("unhandled uniform type " + uniform.type);
            }
        }
    }

    public static NativeMaterial buildMaterial(MaterialDefinition definition/*String name, HashMap<ColorType, Color> colors, HashMap<String, NativeTexture> textures, HashMap<NumericType, NumericValue> params*/ /*Effect effect*/) {
        Color col = (definition.color == null) ? null : definition.color.get(ColorType.MAIN);
        Material mat;
        Float transparency = NumericValue.transparency(definition.parameters);
        boolean hasnormalmap = false;

        JmeTexture diffusemap = null;
        NativeTexture texture = (definition.texture == null) ? null : definition.texture.get("basetex");
        if (NumericValue.unshaded(definition.parameters)) {
            mat = new Material(((PlatformJme) Platform.getInstance()).jmeResourceManager.am,
                    "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
            if (col != null) {
                mat.setColor("Color", PlatformJme.buildColor(col));
            }
            if (texture != null) {
                mat.setTexture("ColorMap", ((JmeTexture) texture).texture); // with Unshaded.j3md
            }

        } else {
            //7.8.25: With JME 3.2.4 we used a custom "MyLighting.j3md" derived from "Lighting.j3md" for doing "flatshading". It is
            // unclear whether that is really needed/helpful.
            // "MyLighting.j3md" doesn't work with JME 3.8.1 any more, so go back to "Lighting.j3md"
            mat = new Material(((PlatformJme) Platform.getInstance()).jmeResourceManager.am, "Common/MatDefs/Light/Lighting.j3md");
            if (col != null) {
                mat.setColor("Diffuse", PlatformJme.buildColor(col)); // with Lighting.j3md
                //29.4.16: ambient mal rausgenommen, weil damit scheinbar das Licht (Pyramide refscene) zu wenig (oder keinen) Einfluss hat.
                //Evtl. widerspricht es auch "Diffuse" und man sollte nur eines der beiden setzen.
                //mat.setColor("Ambient", PlatformJme.buildColor(col)); // with Lighting.j3md
                mat.setBoolean("UseMaterialColors", true); // with Lighting.j3md
            }
            if (texture != null) {
                diffusemap = ((JmeTexture) texture);
                mat.setTexture("DiffuseMap", diffusemap.texture);
            }
            if (NumericValue.flatshading(definition.parameters)) {
                // is 'VertexLighting' for flat shading?
                mat.setBoolean("VertexLighting", true);
            }
            NativeTexture normalmap = (definition.texture == null) ? null : definition.texture.get("normalmap");
            //normalmap=null;
            if (normalmap != null) {
                mat.setTexture("NormalMap", ((JmeTexture) normalmap).texture);
                //hilft nicht 
                //mat.setBoolean("VertexLighting", false);
                //mat.setFloat("Shininess", 2f);
                hasnormalmap = true;
            }
            /*JME hat wohl keine Numpmap. seieh Wiki. NativeTexture bumpmap = (textures == null) ? null : textures.get("bumpmap");
            if (bumpmap != null) {
                mat.setTexture("BumpMap", ((JmeTexture) bumpmap).texture); 
            }*/
        }
        if (transparency != null) {
            if (mat.getParam("UseAlpha") != null) {
                mat.setBoolean("UseAlpha", true);
            }
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }

        // 6.11.15: Backface Culling ausschalten. Wird für Analyse genutzt und sollte grundsätzlich eingeschaltet sein (z.B. wegen twosided meshes). 
        //mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        return new JmeMaterial(definition.name, mat/*, false*/, transparency != null, hasnormalmap, diffusemap, definition);
    }

    public static JmeMaterial buildMaterial(JmeProgram program, boolean opaque) {
        // No idea how jme shares shader. So for now create a new material for each using of a program.
        Material mat = JmeProgram.buildProgram(program);
        //Float transparency = NumericValue.transparency(definition.parameters);
        boolean hasnormalmap = false;

        // Even when using dedicated shader which handle transparency its imported to tell the engine
        // to put these objects at the end of rendering.
        if (!opaque/*effect.transparent*/) {
            //ist fuer Shader, aber eigene haben das nicht.
            if (mat.getParam("UseAlpha") != null) {
                mat.setBoolean("UseAlpha", true);
            }
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }

        // 1.1.25 not sure about params
        return new JmeMaterial(null, mat, program);
    }

    /*@Override
    public void setWireframe(boolean wireframe) {
        material.getAdditionalRenderState().setWireframe(true);
    }*/

    public static JmeMaterial buildWireframeMaterial() {
        Material mat = new Material(((PlatformJme) Platform.getInstance()).jmeResourceManager.am,
                "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat.setColor("Color", PlatformJme.buildColor(Color.BLACK));
        mat.getAdditionalRenderState().setWireframe(true);
        return new JmeMaterial("wireframe", mat, false, false, null, null);
    }

    /**
     * TODO queuebucket wird hier nicht geaendert.
     * Anscheinend kann JME die Transparency nicht mehr aendern, wenn das Objekt schon in OpenGL angelegt wurde. Wirklich??
     * 21.7.16: Jetzt gehts auf einmal(??).
     */
    @Override
    public void setTransparency(boolean enabled) {
        if (!enabled) {

            if (material.getParam("UseAlpha") != null) {
                material.setBoolean("UseAlpha", false);
            }
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Off);
            transparent = false;
        } else {
            if (material.getParam("UseAlpha") != null) {
                material.setBoolean("UseAlpha", true);
            }
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            transparent = true;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public NativeTexture[] getMaps() {
        return new NativeTexture[]{basetex};
    }

    @Override
    public NativeUniform getUniform(String name) {
        // 8.1.25 Remove prefix "u_" because JME uses "m_" implicitly.
        JmeUniform uniform = (JmeUniform)/* jmeProgram.*/uniforms.get(name.substring(2));
        return uniform;
    }

    /*@Override*/
    public boolean isTransparent() {
        return transparent;
    }


}

