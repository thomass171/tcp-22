package de.yard.threed.core.loader;

import de.yard.threed.core.FloatHolder;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;

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



    public String getName() {
        return name;
    }
}
