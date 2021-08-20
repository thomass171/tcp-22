package de.yard.threed.engine;


import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;


import java.util.HashMap;

/**
 * Date: 12.05.14
 * <p/>
 * Die Eigenschaften einer Oberflaeche.
 * <p/>
 * 06.10.14: Eine UV Map hat im Material nichts zu suchen, sondern gehoert zur Verbindung Geometrie->Material, dem Mesh
 * 20.07.16: Entschlackt und nicht mehr abstract. Auch geeignet, ein Material aus der Platform auszulesen. Keinen
 * Defaultconstructor mehr, weil es ein Material nur ueber die Platform geben kann.
 */
public class Material {
    public NativeMaterial material;

    public Material(NativeMaterial material) {
        this.material = material;
    }

    protected static HashMap<String, NativeTexture> buildTextureMap(Texture texture) {
        HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();
        map.put("basetex", texture.texture);
        return map;
    }

    protected static HashMap<String, NativeTexture> buildTextureMap(Texture texture, Texture normalmap) {
        HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();
        map.put("basetex", texture.texture);
        map.put("normalmap", normalmap.texture);
        return map;
    }

    public static HashMap<String, NativeTexture> buildTextureMapNormal(Texture normalmap) {
        HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();
        map.put("normalmap", normalmap.texture);
        return map;
    }

    private static void addNormalMap(HashMap<String, NativeTexture> map, Texture normalmap) {
        map.put("normalmap", normalmap.texture);
    }

    private static void addBumpMap(HashMap<String, NativeTexture> map, Texture bumpmap) {
        map.put("bumpmap", bumpmap.texture);
    }

    public static HashMap<ColorType, Color> buildColorMap(Color color) {
        HashMap<ColorType, Color> map = new HashMap<ColorType, Color>();
        map.put(ColorType.MAIN, color);
        return map;
    }

    public static HashMap<NumericType, NumericValue> buildParam(NumericType paramtype, NumericValue value) {
        HashMap<NumericType, NumericValue> map = new HashMap<NumericType, NumericValue>();
        map.put(paramtype, value);
        return map;
    }

    protected static HashMap<NumericType, NumericValue> buildParamList(boolean transparent, int shading) {
        HashMap<NumericType, NumericValue> paramlist = new HashMap<NumericType, NumericValue>();

        if (transparent) {
            //TODO stimmt die 1?, oder wie ist der Wertebereich?
            paramlist.put(NumericType.TRANSPARENCY, new NumericValue(1));
        }
        paramlist.put(NumericType.SHADING, new NumericValue(shading));
        return paramlist;
    }

    /**
     * Phong
     * <p>
     * Date: 12.05.14
     * <p/>
     * Per Fragment Lighting (per pixel) using phong shading model.
     * <p/>
     * Geeignet z.B. fuer glänzenden, glatten Kunststoff.
     * <p/>
     * In Anlehnung an ThreeJS (A material for shiny surfaces).
     * Der Wert fuer die shininess wird willkürlich auf 20 gesetzt, Hauptsache sie ist gesetzt.
     * <p/>
     * Alternative zu LambertMaterial. Braucht eine Lichtquelle.
     */
    public static Material buildPhongMaterial(Texture texture) {
        //super(texture);
        return new Material(Platform.getInstance().buildMaterial(null, null, buildTextureMap(texture), buildParam(NumericType.SHININESS, new NumericValue(20)), null));
    }

    public static Material buildPhongMaterial(Color color) {
        //super(color);
        return new Material(Platform.getInstance().buildMaterial(null, buildColorMap(color), null, buildParam(NumericType.SHININESS, new NumericValue(20)), null));

    }

    public static Material buildPhongMaterialWithNormalMap(Texture texture, Texture normalmap) {
        return buildPhongMaterialWithNormalMap(texture, normalmap, false);
    }

    /**
     * 5.10.17: Ist das mit transparency nicht Unsinn? Kommt der Wert nicht aus dem Alpha Channel?
     * 25.9.19: Transparency ist wohl nur ein Flag, ob Alpha Blending gemacht wird. Das Resultat haengt dann
     * vom Alpha Wert ab.
     */
    public static Material buildPhongMaterialWithNormalMap(Texture texture, Texture normalmap, boolean transparency) {
        HashMap<String, NativeTexture> map = buildTextureMap(texture);
        addNormalMap(map, normalmap);
        HashMap<NumericType, NumericValue> paramlist = buildParam(NumericType.SHININESS, new NumericValue(20));
        if (transparency) {
            //Wert spielt keine Rolle
            paramlist.put(NumericType.TRANSPARENCY, new NumericValue(0));
        }
        return new Material(Platform.getInstance().buildMaterial(null, null, map, paramlist, null));
    }

    public static Material buildPhongMaterial(Color color, int shading) {
        HashMap<NumericType, NumericValue> paramlist = buildParam(NumericType.SHININESS, new NumericValue(20));
        paramlist.put(NumericType.SHADING, new NumericValue(shading));
        return new Material(Platform.getInstance().buildMaterial(null, buildColorMap(color), null, paramlist, null));
    }

    /*THREED TODO @Override
    public ShaderProgram getDefaultShader() {
        return (texture!=null) ? ShaderProgram.buildDefaultLightedTextured() : ShaderProgram.buildDefaultLighted() ;
    }*/

    /**
     * Lambert
     * <p>
     * Date: 12.05.14
     * <p/>
     * <p/>
     * <p/>
     * In Anlehnung an ThreeJS (A material for non-shiny (Lambertian) surfaces).
     * Für stumpfe, matte Flächen.
     * <p/>
     * Geeignet z.B. f�r Papier
     * <p/>
     * Alternative zu PhongMaterial. Braucht eine Lichtquelle. 21.3.17: Hat keine Normalmap.
     * 2.5.19: Unshaded geht das also nicht.
     */

    public static Material buildLambertMaterial(Color color) {
        //super(color);
        return new Material(Platform.getInstance().buildMaterial(null, buildColorMap(color), null, null, null));
    }

    public static Material buildLambertMaterial(Color color, HashMap<String, NativeTexture> texturemap) {
        //super(color);
        return new Material(Platform.getInstance().buildMaterial(null, buildColorMap(color), texturemap, null, null));
    }

    public static Material buildLambertMaterial(Texture texture) {
        return new Material(Platform.getInstance().buildMaterial(null, null, buildTextureMap(texture), null, null));
    }

    public static Material buildLambertMaterial(Texture texture, boolean transparent, boolean flatshading) {
        return new Material(Platform.getInstance().buildMaterial(null, null, buildTextureMap(texture),
                buildParamList(transparent, (flatshading) ? NumericValue.FLAT : NumericValue.SMOOTH), null));
    }

    /**
     * Basic.
     * Einfaches Material, dass ohne Lichtquelle sichtbar ist.
     * <p/>
     * In Anlehnung an ThreeJS und JME unshaded.
     * <p>
     * Kann zumindest in Unity keine Schatten darstellen. Ist ja auch logisch. unshaed duerfte nirgends Schatten darstellen. unshaded halt.
     * 29.4.19: Obwohl unshaded, wird es in JME offenbar doch etwas geshaed (z.B. manche linke Seite tower, ander aber nicht??)
     */
    public static Material buildBasicMaterial(Texture texture) {
        return buildBasicMaterial(texture, null, false);
    }

    public static Material buildBasicMaterial(Texture texture, Effect effect) {
        return buildBasicMaterial(texture, effect, false);
    }

    public static Material buildBasicMaterial(Texture texture, Effect effect, boolean transparent) {
        //super(texture);
        return new Material(Platform.getInstance().buildMaterial(null, null,
                buildTextureMap(texture), buildParamList(transparent, NumericValue.UNSHADED), effect));
    }

    public static Material buildBasicMaterial(Color color) {
        //super(color);
        return new Material(Platform.getInstance().buildMaterial(null, buildColorMap(color),
                null, buildParam(NumericType.SHADING, new NumericValue(NumericValue.UNSHADED)), null));
    }

    //28.4.21
    public static Material buildBasicMaterial(Color color, boolean transparent) {
        //super(color);
        return new Material(Platform.getInstance().buildMaterial(null, buildColorMap(color),
                null, buildParamList(transparent, NumericValue.UNSHADED), null));
    }

    /**
     * Date: 26.10.15
     */
 
    /*public CustomShaderMaterial(Texture texture, String shader) {
        this(new Texture[]{texture}, shader);
    }*/

    //TODO 11.3.16: der effect wird so evtl. nicht mehr gehen
    public static Material buildCustomShaderMaterial(String uniformname, Texture texture, Effect effect, boolean transparent) {
        //super((Color)null);
        HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();
        map.put(uniformname, texture.texture);
        return new Material(Platform.getInstance().buildMaterial(null, null, map, buildParamList(transparent, NumericValue.SMOOTH), effect));
    }

    public static Material buildCustomShaderMaterial(Texture texture) {
        // super(texture);
        return new Material(Platform.getInstance().buildMaterial(null, null, buildTextureMap(texture), null, null));
    }

    public static Material buildCustomShaderMaterial(HashMap<String, NativeTexture> map, Effect effect) {
        // super((Color)null);
        return new Material(Platform.getInstance().buildMaterial(null, null, map, null, effect));
    }

    public void setTransparency(boolean enabled) {
        material.setTransparency(enabled);
    }

    public String getName() {
        return material.getName();
    }

    public void setName(String name) {
        material.setName(name);
    }
}

