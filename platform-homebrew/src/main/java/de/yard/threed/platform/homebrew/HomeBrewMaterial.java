package de.yard.threed.platform.homebrew;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.NativeUniform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ShaderPool;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.engine.platform.common.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 2.8.16: Nicht immer neues Shaderprogramm anlegen sondern shader sharen?
 * 25.9.19: Jedes Material hat seine eigenen Shader. Wenn man etwas sharen möchte, dann besser nicht die Shader im Material, sondern das Material im Mesh.
 * Obwohl, die OpenGlShaderProgram werden schon geshared.
 * <p>
 * Created by thomass on 25.01.16.
 */
public class HomeBrewMaterial implements NativeMaterial {
    static Log logger = Platform.getInstance().getLog(HomeBrewMaterial.class);
    static ShaderProgram defaulteffect = ShaderPool.buildUniversalEffect(/*false*/);
    // Material material;
    // Das mit den j3md Dteien ist total verbaut. Dem kann man nur hinten rum
    // etwas unterschieben. Die Map ist über den Wrapper gesynced.
    static SortedMap<String, ShaderPool> effects = Collections.synchronizedSortedMap(new TreeMap<String, ShaderPool>());
    boolean transparent;
    OpenGlShaderProgram sp;
    public List<Uniform> uniforms = new ArrayList<Uniform>();
    public List<Uniform> attributes = new ArrayList<Uniform>();
    public List<Uniform> varyings = new ArrayList<Uniform>();
    private Color color;
    // OpenGlTexture texture;
    //HashMap<String, NativeTexture> textures;
    String name;
    OpenGlTexture basetex = null;


    private HomeBrewMaterial(GlInterface glcontext,String name, Color col, OpenGlTexture diffusemap /*HashMap<String, NativeTexture> textures*/, OpenGlShaderProgram sp, boolean transparent) {
        this.name = name;
        this.color = col;
        if (col != null) {
            //logger.debug("color=" + Util.format("0x%x", col.getARGB()) + "transparent=" + transparent);
            // if (col.getARGB() == 0xFFffc600) {
            //   color = new Color(0xff, 0xc6, 0x00);
            //}
            //color = new Color(col.getRasInt(),col.getGasInt(),col.getBasInt());
        }
        /*this.textures = textures;
        // Pruefen, dass mir hier keine nulls untergeschoben werden. Book(leaf) macht das aber, obwohl es Kappes ist
        if (textures != null) {
            for (String tname : textures.keySet()) {
                NativeTexture texture = textures.get(tname);
                if (texture == null) {
                    //throw new RuntimeException("texture isType null with name " + tname);
                }
                if (tname.equals("basetex")){
                    basetex = (OpenGlTexture) texture;
                }
            }
        }*/
        this.transparent = transparent;
        this.sp = sp;
        this.basetex = diffusemap;

        glcontext.exitOnGLError(glcontext, "OpenGlMaterial.constructor");

    }

    public static HomeBrewMaterial buildMaterial(GlInterface glcontext,MaterialDefinition definition) {
        Color col = (definition.color == null) ? null : definition.color.get(ColorType.MAIN);
        Float transparency = NumericValue.transparency(definition.parameters);
        boolean hasnormalmap = false;
        //NativeTexture texture = (textures == null) ? null : textures.get(TextureType.MAIN);

        List<String> varstobind = new ArrayList<String>();
        //TODO das muss zum VBO und zum Shader passen
        //Da das Mesh auf dem Material aufbaut, muss sich der VBO Builder an das Material halten. Aber der VBO Builder hat vielleicht ein multiple material MEsh und
        //kann ja nur EINE VBO Struktur anlegen. 15.3.16 Nicht einfach zu klären. Erstmal immer UVs anlegen.
        //25.9.19: Der Check auf texture scheint mir doch sinnvoll. Die Lösung hier ist doch eh in die Jahre gekommen. Jetzt mal analog JME
        //Grundsätzlich dürfen die Shader nur Variablen verwenden, die auf auch vorhandene VBO Werte zeigen.
        /*24.1.25 if (effect == null) {
            effect = defaulteffect;
        }*/

        //Die Order der vars ist wichtig! Hach, wie doof.
        addVar(varstobind, "VERTEX");

        OpenGlTexture diffusemap = null;
        NativeTexture texture = (definition.texture == null) ? null : definition.texture.get("basetex");
        if (NumericValue.unshaded(definition.parameters)) {
            //Unshaded
            if (col != null) {
                //mat.setColor("Color", PlatformJme.buildColor(col));
            }
            if (texture != null) {
                diffusemap = ((OpenGlTexture) texture);
                addVar(varstobind, "MULTITEXCOORD0");
            }
            addVar(varstobind, "NORMAL");

        } else {
            if (col != null) {
                //mat.setColor("Diffuse", PlatformJme.buildColor(col)); // with Lighting.j3md
                //mat.setBoolean("UseMaterialColors", true); // with Lighting.j3md
            }
            if (texture != null) {
                diffusemap = ((OpenGlTexture) texture);
                //mat.setTexture("DiffuseMap", diffusemap.texture);
                addVar(varstobind, "MULTITEXCOORD0");
            }
            addVar(varstobind, "NORMAL");

            if (NumericValue.flatshading(definition.parameters)) {
                //??mat.setBoolean("useFlatShading", true);
            }
            NativeTexture normalmap = (definition.texture == null) ? null : definition.texture.get("normalmap");
            //normalmap=null;
            if (normalmap != null) {
                //mat.setTexture("NormalMap", ((JmeTexture) normalmap).texture);

                hasnormalmap = true;
            }
            /*?? NativeTexture bumpmap = (textures == null) ? null : textures.get("bumpmap");
            if (bumpmap != null) {
                mat.setTexture("BumpMap", ((JmeTexture) bumpmap).texture);
            }*/
        }

        /*OpenGlShaderProgram sp = new OpenGlShaderProgram(new OpenGlVertexShader(OpenGlShader.loadFromFile("shader/Universal.vert"), "Universal"),
                new OpenGlFragmentShader(OpenGlShader.loadFromFile("shader/Universal.frag"), "Universal"), varstobind);*/

        OpenGlShaderProgram sp = null;
       /*TODO move to buildProgram 24.1.25 String fragmentshader = defaulteffect.fragmentshader;
        boolean debugMaterial = false;
        if (debugMaterial) {
            // solid color shader for debugging
            logger.warn("Using SolidColor shader!");
            fragmentshader = "shader/SolidColor.frag";
        }
        OpenGlShaderProgram sp = OpenGlShaderProgram.getOrBuildShader(glcontext, effect.shader.vertexshader, fragmentshader, varstobind);

        //OpenGlShaderProgram sp = new OpenGlShaderProgram(new OpenGlVertexShader(OpenGlShader.loadFromFile(effect.shader.vertexshader), "?"),
        //        new OpenGlFragmentShader(OpenGlShader.loadFromFile(effect.shader.fragmentshader), "?"), varstobind);
end TODO
        */
        return new HomeBrewMaterial(glcontext, definition.name, col, diffusemap/* textures*/, sp, transparency != null/*, effect.transparent*/);

    }

    private static void addVar(List<String> varstobind, String var) {
        if (OpenGlShader.use15) {
            //varstobind.add("VERTEX");
            varstobind.add(var);
           /* if (true || (textures != null && textures.size() > 0)) {
                varstobind.add("MULTITEXCOORD0");
            }
            varstobind.add("NORMAL");/*, "in_Color"/*,"in_TextureCoord"*/

        } else {
            // 3.3.16 auch unfertig
            if (var.equals("VERTEX")) {
                varstobind.add("gl_Vertex"/*, "in_Color"/*,"in_TextureCoord"*/);
            } else {
                Util.notyet();
            }
        }
    }

    /*public static OpenGlMaterial buildCustomShaderMaterial(Color col, NativeTexture[] textures, Effect effect) {


        // mat.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f)); // red color
        // mat.setTexture("ColorMap", ((JmeTexture) texture).texture); // bind myTexture for that sampler uniform
        for (int i = 0; i < textures.length; i++) {
            //mat.setTexture("texture" + i, ((JmeTexture) textures[i]).texture); // with Lighting.j3md

        }
        int textureindex = 0;
        // die uniforms sind eine LinkedHashMap. Dadurch ist das keyset immer in der insertion order
        // 30.12.15 ist jetzt normale List
        for (Uniform uniform : effect.uniforms) {
            // Uniform uniform = effect.uniforms.get(key);

            switch (uniform.type) {
                case FLOAT_VEC4:
                    //TODOmat.setColor(uniform.name, ((JmeColor) col).color); // with Lighting.j3md
                    break;
                case SAMPLER_2D:
                    // Reihenfolge ist ueber die LinkedHashMap gesichert
                    //TODOmat.setTexture(uniform.name, ((JmeTexture) textures[textureindex++]).texture);
                    break;
                default:
                    throw new RuntimeException("unknown uniform type " + uniform.type);
                    //todo andere Fehlerbahdnlung
                    //   return null;
            }
        }
        return buildMaterial(null, HashMap<TextureType, NativeTexture> textures, HashMap<NumericType, NumericValue> params, Effect effect) {

            //return new OpenGlMaterial(null, (OpenGlTexture) textures[0], sp, effect.transparent);
    }**/

    /*@Override
    public void setWireframe(boolean wireframe) {
        //OGL TODO  material.getAdditionalRenderState().setWireframe(true);
    }*/

    @Override
    public void setTransparency(boolean enabled) {
        //TODO Util.notyet();
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
        return null;
    }
    
    /*@Override
    public boolean isTransparent() {
        return transparent;
    }*/


    public void setup() {
        /*if (sp != null) {
            sp.setup();
        }*/
    }

    /**
     * Shader aktivieren, uniforms setzen, blending setzen
     */
    public void prepareRender(GlInterface gl, Matrix4 modelmatrix, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        if (sp != null) {
            sp.use(gl);
            if (OpenGlShader.use15) {
                // Das ist jetzt erstmal auf den Universalshader zugeschnitten
                sp.setUniformMatrix4(gl,"PROJECTIONMATRIX", OpenGlBufferUtils.toFloatBuffer(projectionmatrix)/*MatrixUtil.toFloatBuffer(camera.getProjectionMatrix())*/);
                //shaderProgram.setUniform("viewer", MatrixUtil.toFloatBuffer(camera.getViewMatrix()));
                // In 1.2 gibt es keine separate VIEWMATRIX und MODELMATRIX. Darum hier auch nicht
                //sp.setUniform("VIEWMATRIX", viewmatrix/*camera.getViewMatrix()*/.toFloatBuffer());
                //sp.setUniform("MODELMATRIX", modelmatrix/*camera.getViewMatrix()*/.toFloatBuffer());
                Matrix4 modelviewmatrix = viewmatrix.multiply(modelmatrix);
                sp.setUniformMatrix4(gl,"MODELVIEWMATRIX", OpenGlBufferUtils.toFloatBuffer(modelviewmatrix));
                //Ob Model oder ModelView für Normale ist noch nicht ganz klar. 
                // TODO klaeren, optisch erkennt man das nur schlecht.Vieles spricht für modelviewmatrix
                // Als Matrix3 verwenden (siehe Wiki). Die Berechnung scheint korrekt; optisch verglichen mit Berechnung
                // im Shader.
                Matrix4 normalmatrix = ((Matrix4) MathUtil2.transpose(MathUtil2.getInverse(modelviewmatrix)));
                Matrix3 normalmatrix3 = MathUtil2.getInverseAsMatrix3(normalmatrix).transpose();//.extractRotationAndScale();
                //sp.setUniformMatrix4("NORMALMATRIX", normalmatrix.toFloatBuffer());
                sp.setUniformMatrix3(gl,"NORMALMATRIX", OpenGlBufferUtils.toFloatBuffer(normalmatrix3));
                //logger.debug("mat4="+new Matrix4(normalmatrix).dump("\n "));
                //logger.debug("mat3=" + normalmatrix3.dump("\n "));

                if (color != null) {
                    sp.setUniform(gl,"isunicolor", true);
                    sp.setUniform(gl,"unicolor", color);
                }
                if (basetex/*textures*/ != null) {
                    sp.setUniform(gl,"isunicolor", false);
                    int unit = 0;
                    String uniformname = "basetex";
                    //25.9.19: Weiß gar nicht mehr wofuer das war(??). Auf jeden Fall jetzt nur noch diffusemap.
                    //for (String uniformname : textures.keySet()) {
                    OpenGlTexture texture = basetex;//(OpenGlTexture) textures.get(uniformname);
                    if (texture == null) {
                        // Ist das wirklich ein ernster Fehler? Book(leaf) macht das aber, obwohl es Kappes ist
                        //throw new RuntimeException("texture for uniform not found:"+uniformname);
                        //6.4.17: Bei Showroom kommt das auch ständig. Bei Refscene nicht.
                        logger.warn("texture for uniform not found:" + uniformname);
                    } else {


                        texture.active(gl, unit);
                        texture.bind(gl);
                    }
                    sp.setUniform(gl,uniformname, unit);
                    unit++;
                    //}
                }
            } else {
                // 3.3.16 Dieser Zweig ist unfertig!
                sp.setUniformMatrix4(gl,"projection", OpenGlBufferUtils.toFloatBuffer(projectionmatrix)/*MatrixUtil.toFloatBuffer(camera.getProjectionMatrix())*/);
                //shaderProgram.setUniform("viewer", MatrixUtil.toFloatBuffer(camera.getViewMatrix()));
                sp.setUniformMatrix4(gl,"viewer", /*camera.getViewMatrix()*/OpenGlBufferUtils.toFloatBuffer(viewmatrix));
                sp.setUniformMatrix4(gl,"model", /*camera.getViewMatrix()*/OpenGlBufferUtils.toFloatBuffer(modelmatrix));

            }
           gl.exitOnGLError(gl, "material.prepareRender");

            if (lights != null) {
                //TODO es kann mehrere lights geben
                OpenGlLight light = lights.get(0);
                // Nicht jeder Shader verarbeitet Licht. Darum die uniform nur setzen, wenn die Shader sie auch haben.
                if (sp.hasUniform(gl,"lightcolor"))
                    sp.setUniform(gl,"lightcolor", 1, 1, 1); //TODO echten Wert setzen

                // Die Richtung des Lichts ergibt sich aus der Position der Quelle und der Position des beleuchteten
                // Objekts (zumindest wenn die Quelle relativ weit weg ist. 10.7.14: Das ist doch nicht geeignt!
                // Die Direction muss pro Vertex berechnet werden.
                //Vector3 lightdirection = light.position.subtract(this.getPosition());
                //lightdirection = new Vector3(0,-1,0);
                if (sp.hasUniform(gl,"lightposition"))
                    sp.setUniform(gl,"lightposition", light.getPosition());
                if (sp.hasUniform(gl,"u_lightdirection") && light.getDirection() != null) {
                    sp.setUniform(gl,"u_lightdirection", light.getDirection().normalize());
                }
                if (sp.hasUniform(gl,"u_lightcolor") && light.getColor() != null) {
                    sp.setUniform(gl,"u_lightcolor", light.getColor());
                }
                if (sp.hasUniform(gl,"u_ambientlightcolor"))
                    sp.setUniform(gl,"u_ambientlightcolor", new Color(0.3f, 0.3f, 0.3f)); //TODO echten Wert setzen
            }
        }
        if (transparent) {
            gl.enableAlphaBlending();
        } else {
            gl.disableAlphaBlending();
        }
    }

    public boolean isTransparent() {
        return transparent;
    }


}

