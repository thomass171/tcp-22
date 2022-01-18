package de.yard.threed.engine.loader;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Texture;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.buffer.ByteArrayInputStream;

import java.util.HashMap;


/**
 * 22.12.17: MA19 Das ist gleichzeitig auch die Abbildung eines GLTF Material. Evtl. umbenennen, evtl aber auch nicht.
 * 7.6.18: LoadedMaterial->PortableMaterial
 * <p>
 * See also https://github.com/KhronosGroup/glTF/tree/master/extensions/1.0/Khronos/KHR_materials_common.
 * <p>
 * Created by thomass on 12.02.16.
 */
public class PortableMaterial {
    static Log logger = Platform.getInstance().getLog(PortableMaterial.class);

    public String name;
    // color ist die Grundfarbe
    public Color color, ambient, specular;
    //TODO rename. nicht prozent, sondern 0-1
    public FloatHolder shininess/*22.1.18percent*/ = new FloatHolder(0);
    //22.1.18 public FloatHolder shininessstrengthpercent = new FloatHolder(0);
    public FloatHolder transparencypercent = new FloatHolder(0);

    // AcColor rgb;
    public /*19.1.18Ac*/ Color emis;
    // AcColor spec;
    // float shi, trans;
    //3.1.19 shaded default true ist schon vernuenftig. Aber ob es so eine Property ueberhaupt geben sollte, stammt ja aus AC. Siehe Header
    //2.5.19: Doch, die kann es geben. Es gibt die GLTF Extension "KHR_materials_unlit". Unlit, d.h. ohne Einfluss durch Beleuchtung.
    public boolean shaded = true;
    // AC hat default texrep 1,1 und viele Modelle verlassen sich darauf. D.h. als default kein wrap.
    // TODO 8.6.18: wrap sollte ein Attribut mit Werten Repeaet, clamp, ... sein?
    public boolean wraps, wrapt;
    // PreprocessedLoadedObject hat auch eine deprecated texture aus der AC Historie
    public String texture;

    public PortableMaterial() {

    }

    public PortableMaterial(String name, String texture) {
        this.name = name;
        this.texture = texture;
    }

    public PortableMaterial(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Wiederherstellen aus serialisiertem Objekt
     * acpp reader? deprecated?
     *
     * @param ins
     */
    public PortableMaterial(ByteArrayInputStream ins) {
        name = ins.readString();
        color = new Color(ins);
        ambient = new Color(ins);
        specular = new Color(ins);
        shininess = new FloatHolder(ins.readFloat());
        /* shininessstrengthpercent =*/
        new FloatHolder(ins.readFloat());
        transparencypercent = new FloatHolder(ins.readFloat());
        shaded = ins.readInt() != 0;
        wraps = ins.readInt() != 0;
        wrapt = ins.readInt() != 0;
    }


    public float getShininess() {
        if (shininess != null) {
            return shininess.value;
        }
        return 0;
    }

    public float getTransparencypercent() {
        if (transparencypercent != null) {
            return transparencypercent.value;
        }
        return 0;
    }

    public void serialize(NativeOutputStream outs) {
        outs.writeString(name);
        //  die drei Farben sind nie null
        color.serialize(outs);
        ambient.serialize(outs);
        specular.serialize(outs);
        outs.writeFloat(shininess.value);
        outs.writeFloat(0/*shininessstrengthpercent.value*/);
        outs.writeFloat(transparencypercent.value);
        outs.writeInt(shaded ? 1 : 0);
        outs.writeInt(wraps ? 1 : 0);
        outs.writeInt(wrapt ? 1 : 0);
    }

    public PortableMaterial duplicate(String name) {
        PortableMaterial nmat = new PortableMaterial();
        nmat.name = name;
        nmat.color = color;
        nmat.ambient = ambient;
        nmat.specular = specular;
        nmat.shininess = shininess;
        //nmat.shininessstrengthpercent = shininessstrengthpercent;
        nmat.transparencypercent = transparencypercent;
        nmat.shaded = shaded;
        nmat.wraps = wraps;
        nmat.wrapt = wrapt;
        nmat.texture = texture;
        //19.1.18:emis
        nmat.emis = emis;
        return nmat;
    }

    /**
     * 27.12.17: public static, um allgemeingueltig aus einem LoadedMaterial ein Material zu machen. War frueher in LoadedFile.
     * 30.12.18: Um eine Textur zu laden, brauchts das Bundle, den path und den Namen. Auch einen absoluten Pfad im Texturename
     * in Form von "bundle:/xx/yy/zz.png" zulassen.
     * <p>
     * Returns mull, wenn bei Texturen Bundle oder Path fehlen. Aufrufer kann DummyMaterial verwenden oder auch keins (wird dann wireframe).
     *
     * @param mat
     * @param texturename
     * @param texturebasepath
     * @return
     */
    public /*10.4.17*/ static /*Native*/Material buildMaterial(Bundle bundle, PortableMaterial mat, String texturename, ResourcePath texturebasepath, boolean hasnormals) {
        NativeMaterial nmat;
        //SHADED ist der Defasult
        HashMap<NumericType, NumericValue> parameters = new HashMap<NumericType, NumericValue>();
        if (!mat.shaded) {
            parameters.put(NumericType.SHADING, new NumericValue(NumericValue.UNSHADED));
        } else {
            if (!hasnormals) {
                parameters.put(NumericType.SHADING, new NumericValue(NumericValue.FLAT));
            }
        }

        if (texturename != null) {
            /*21.12.16 nicht mehr noetig wegen ResourcePath if (texturebasepath == null) {
                texturebasepath = ".";
            }*/
            if (StringUtils.contains(texturename, ":")) {
                int index = StringUtils.indexOf(texturename, ":");
                bundle = BundleRegistry.getBundle(StringUtils.substring(texturename, 0, index));
                texturebasepath = null;
                texturename = StringUtils.substring(texturename, index + 1);
            } else {
                if (texturebasepath == null) {
                    logger.warn("no texturebasepath. Not building material.");
                    return null;
                }
            }
            Texture texture;
            HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();

            if (bundle != null) {
                BundleResource br = new BundleResource(texturebasepath, texturename);
                br.bundle = bundle;
                texture = Texture.buildBundleTexture(br, mat.wraps, mat.wrapt);
                if (texture.texture == null) {
                    //logger.error("texture i null");
                    texturename = texturename;
                }
                map.put("basetex", texture.texture);

            } else {
                // 26.4.17: Das bundle muss gesetzt sein.
                logger.error("bundle not set");
                NativeResource textureresource;
                //textureresource = new FileSystemResource(texturebasepath, texturename);
                //texture = new Texture(textureresource, mat.wraps, mat.wrapt);
            }
            //map.put("normalmap",normalmap.texture);
            //TODO die anderen Materialparameter
            nmat = Platform.getInstance().buildMaterial(null, null, map, parameters, null);
        } else {
            HashMap<ColorType, Color> color = new HashMap<ColorType, Color>();
            color.put(ColorType.MAIN, mat.color);
            //TODO die restlichen colors
            // 25.4.19 unshaded wird oben zwar schon eingetragen, aber nicht immer. "shaded" ist eh etwas unklar. Auf jeden Fall bleibt ein Material mit Color in JME sonst schwarz.
            //Darum erstmal immer setzen, bis klar ist, was mit Property "shaded" ist. 28.4.19: Das ist aber doof, nur weil JME die combination shaded/ambientLight schwarz darstellt.
            //Evtl. wegen Normale?
            //parameters.put(NumericType.UNSHADED, new NumericValue(1));
            nmat = Platform.getInstance().buildMaterial(null, color, null, parameters, null);
        }
        return new Material(nmat);
    }


}
