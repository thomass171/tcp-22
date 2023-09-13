package de.yard.threed.engine;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.EffectShader;

/**
 * Eine abstrakte Abbildung von sowohl eines FG effect (*.eff Datei) und einer JME
 * Materialdefinition (*.j3md).
 * <p/>
 * 2.2.16: So ganz rund ist das nicht, denn Effects koennen sowohl Materialen wie auch Partikel betreffen. Da kann man dann
 * ja nicht einfach irgendwelche Effekte reinstecken.
 * <p/>
 * 20.7.2016: Der Sinn der Klasse ist mittlerweile unklar. Was ist überhaupt ein Effekt? Transparenz z.B. ist doch eine Materialeigenschaft.
 * Und warum sollte man fuer einen Effekt einen eigenen Shader haben, wenn es doch schon fertige gibt; vor allem in Unity.
 * Darum zunächst man transparency in Material verschoben. 
 * 21.7.16: Naja, iregdnwie ist Transparenz aber ja doch auch ein Effekt. Oder er ist ein Efekt, der mit der
 * Materialeigenschaft transparency umgesetzt wird. Das muss sich noch entwickeln.
 * <p/>
 * 10.8.16: Entwickelt sich jetzt zu einem FG Effect. Statt aber hier zu Shadern zu verbinden, wird hier die Verbindung zu einem Platform Material hergestellt. Es
 * gibt die Abbildung: Genau ein Effect entspricht genau einem Platform Material.
 * Wiki.
 * 
 * Vorläufig:Effect=Custom shader aber != materialeigenschaft
 * 27.12.17: Durch preprocess/GLTF ist der Effect jetzt eine Abstraktionebene höher und hat LoadedMaterial statt Material.
 * 16.01.18: Vielleicht kann hier doch einmalig das Material fertig erstellt werden (buidlTechnique()?), dann kann man
 * die PropertyNodes wieder freigegeben (wegen memory). Ist auch im Sinne von Material sharing. Aber ob das wirklich die Intention ist?
 *
 * 9.3.21: MA31: Zerlegt in engine und FG(FGEffect).
 *
 * 25.2.22: platform independent Shader still not work in ThreeJs and Unity. Maybe its a bad approach.
 * Created by thomass on 30.10.15.
 */
public class Effect {
    Log logger = Platform.getInstance().getLog(Effect.class);
    // 27.12.17: Jetzt eine Abnstraktionsstufe hoeher wegen preProcess 
    PortableMaterial materialdefinition = null;
    //Material material = null;
    public String name;
    //9.3.21 public SGPropertyNode root, parametersProp;

    //20.7.16 public boolean transparent = false;
    //11.3.16: Der Shader ist optional. Wenn er nicht angegeben ist, muss die Platform sehen, wie sie den Effekt hinbekommt.
    // Wenn ein Shader angegebn ist, wird er in allen Platformen verwendet.
    public EffectShader shader = null;

    public Effect() {

    }

    /**
     * Erstmal zum Einstieg
     */
    private Effect(String name) {
        this.name = name;
        shader = new EffectShader();

    }

    @Deprecated
    public static Effect buildSolidColorEffect() {
        Effect effect = new Effect("SolidColor");
        effect.shader.vertexshader = "shader/SolidColor.vert";
        effect.shader.fragmentshader = "shader/SolidColor.frag";
        effect.shader.uniforms.add(/*"Color",*/ new Uniform("Color", UniformType.FLOAT_VEC4));
        return effect;
    }

    /*21.9.19 noch in verwendung*/@Deprecated
    public static Effect buildSimpleTextureEffect() {
        return buildUniversalEffect(/*false*/);
       /* Effect effect = new Effect("SimpleTexture");
        effect.vertexshader = "shader/SimpleTexture.vert";
        effect.fragmentshader = "shader/SimpleTexture.frag";
        effect.uniforms.put("Color", new Uniform("Color",UniformType.FLOAT_VEC4));
        effect.uniforms.put("Texture0", new Uniform("Texture0",UniformType.SAMPLER_2D));
        return effect;*/
    }

    public static Effect buildPhotoalbumEffect() {
        Effect effect = new Effect("PhotoAlbum");
        effect.shader.vertexshader = "shader/PhotoAlbum.vert";
        effect.shader.fragmentshader = "shader/PhotoAlbum.frag";
        effect.shader.uniforms.add(/*"texture0", */new Uniform("texture0", UniformType.SAMPLER_2D));
        effect.shader.uniforms.add(/*"texture1",*/ new Uniform("texture1", UniformType.SAMPLER_2D));
        return effect;
    }

    public static Effect buildModelCombinedEffect() {
        Effect effect = new Effect("model-combined");
        effect.shader.uniforms.add(/*"BaseTex",*/ new Uniform("BaseTex", UniformType.SAMPLER_2D));
        effect.shader.vertexshader = "shader/ubershader.vert";
        effect.shader.fragmentshader = "shader/model-ALS-ultra.frag";

        return effect;
    }

    /**
     * 8.10.17: deprecated, weil so was wie universal die Platform schon können könnte und ich dafuer keine custom shader brauche.
     * 21.9.19 Aufruf wirklich verhindern. Der UniversalShader wandert in die Platform OpenGL.
     *
     * @return
     */
    /*21.9.19*wird intern noch verwendet */ @Deprecated
    public static Effect buildUniversalEffect(/*boolean transparent*/) {
        Effect effect = new Effect("universal");
        effect.shader.uniforms.add(/*"BaseTex",*/ new Uniform("basetex", UniformType.SAMPLER_2D));
        effect.shader.uniforms.add(/*"BaseTex",*/ new Uniform("isunshaded", UniformType.BOOL));
        effect.shader.vertexshader = "shader/Universal.vert";
        effect.shader.fragmentshader = "shader/Universal.frag";
        //20.7.16 effect.transparent = transparent;
        return effect;
    }

    public boolean valid() {
        return true;//TODO Util.notyet();
    }



    /*public Material getMaterialD() {

        return material;
    }*/

    public PortableMaterial getMaterialDefinition() {

        return materialdefinition;
    }
    public void setName(String name) {
        this.name = name;
    }
}

