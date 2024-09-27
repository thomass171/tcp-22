package de.yard.threed.platform.jme;

import com.jme3.asset.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Effect;
import de.yard.threed.engine.Uniform;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.engine.platform.common.MaterialDefinition;
import de.yard.threed.core.NumericValue;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by thomass on 25.04.15.
 * <p/>
 * 21.7.16: Die Verwendung von "UseAlpha" scheint keine Auswirkung zu haben. Es indet sich aber in vielen Beispielen.
 */
public class JmeMaterial implements NativeMaterial {
    static Log logger = Platform.getInstance().getLog(JmeMaterial.class);

    Material material;
    // Das mit den j3md Dteien ist total verbaut. Dem kann man nur hinten rum
    // etwas unterschieben. Die Map ist über den Wrapper gesynced.
    static SortedMap<String, Effect> effects = Collections.synchronizedSortedMap(new TreeMap<String, Effect>());
    private boolean transparent;
    String name;
    // 23.3.17: Braucht er um nachher Normale fuer das MEsh zu berechnen. Grund unklar.
    public boolean hasNormalMap = false;
    JmeTexture basetex;
    //MaterialDefinition definition;

    private JmeMaterial(String name, Material material, boolean transparent, boolean hasnormalmap, JmeTexture basetex, MaterialDefinition definition) {
        this.name = name;
        this.material = material;
        this.transparent = transparent;
        this.hasNormalMap = hasnormalmap;
        this.basetex = basetex;
        //this.definition = definition;
    }

    /*public static JmeMaterial buildCustomShaderMaterial(Color col, NativeTexture[] textures, Effect effect) {
       
    }*/
    public static NativeMaterial buildMaterial(MaterialDefinition definition/*String name, HashMap<ColorType, Color> colors, HashMap<String, NativeTexture> textures, HashMap<NumericType, NumericValue> params*/, Effect effect) {
        Color col = (definition.color == null) ? null : definition.color.get(ColorType.MAIN);
        Material mat;
        Float transparency = NumericValue.transparency(definition.parameters);
        boolean hasnormalmap = false;

        if (effect != null) {
            AssetManager am = ((PlatformJme) Platform.getInstance()).jmeResourceManager.am;
            effects.put(effect.name, effect);

            // Der Suffix j3md ist erfoderlich, damit der richige Loader verwendet wird.
            mat = new Material(am/**/, effect.name + ".j3md");

            if (transparency != null/*effect.transparent*/) {
                //ist fuer Shader, aber eigene haben das nicht. 
                if (mat.getParam("UseAlpha") != null) {
                    mat.setBoolean("UseAlpha", true);
                }
                mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            }                       
           /* for (int i = 0; i < textures.length; i++) {
                //mat.setTexture("texture" + i, ((JmeTexture) textures[i]).texture); // with Lighting.j3md

            }*/
            int textureindex = 0;
            // die uniforms sind eine LinkedHashMap. Dadurch ist das keyset immer in der insertion order
            // 30.12.15 ist jetzt normale List
            for (Uniform uniform : effect.shader.uniforms) {
                // Uniform uniform = effect.uniforms.get(key);

                switch (uniform.type) {
                    case FLOAT_VEC4:
                        //TODOmat.setColor(uniform.name, ((JmeColor) col).color); // with Lighting.j3md
                        break;
                    case SAMPLER_2D:
                        JmeTexture texture = (JmeTexture) definition.texture.get(uniform.name);
                        if (texture == null) {
                            throw new RuntimeException("uniform not set:" + uniform.name);
                        }
                        mat.setTexture(uniform.name, ((JmeTexture) texture)/*s[textureindex++])*/.texture);

                        break;
                    case BOOL:
                        //TODO Die Zuordnung passt so nicht. Nur abhängig vom Typ ein uniform setzen?
                        if (NumericValue.unshaded(definition.parameters)) {
                            mat.setBoolean("isunshaded", true);
                        }
                        break;
                    default:
                        throw new RuntimeException("unknown uniform type " + uniform.type);
                        //todo andere Fehlerbahdnlung
                        //   return null;
                }
            }
            // 3.11.15: Backface Culling ausschalten. Wird z.Z. nur für Leaf gebraucht (als vorübergehende Krücke) und sollte mittelfristig
            //generell eingeschaltet sein. 
            //mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
            return new JmeMaterial(definition.name, mat/*, effect.transparent*/, transparency != null, false, null, definition);
        }

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
            //2.5.19: Angepassten Lighting.j3md für Flatshading. Rein otpisch kann er vielleicht auch ohne ergänztes Flatshading, zumindest mit materialColor?
            //mat = new Material(((JmeResourceManager) JmeResourceManager.getInstance()).am, "Common/MatDefs/Light/Lighting.j3md");
            mat = new Material(((PlatformJme) Platform.getInstance()).jmeResourceManager.am, "MyLighting.j3md");
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
                mat.setBoolean("useFlatShading", true);
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

    /*@Override*/
    public boolean isTransparent() {
        return transparent;
    }


}

