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
 * 22.12.17: Derived from AC, but meanwhile also for covering GLTF material, so quite general (was
 * once LoadedMaterial, MA19).
 * <p>
 * See also https://github.com/KhronosGroup/glTF/tree/master/extensions/1.0/Khronos/KHR_materials_common.
 * <p>
 * 29.07.24: For keeping it simple either base color or texture are set.
 * <p>
 * Created by thomass on 12.02.16.
 */
public class PortableMaterial {
    static Log logger = Platform.getInstance().getLog(PortableMaterial.class);

    private String name;
    // color is base color. Only without texture
    private Color color, ambient, specular;
    //22.1.18 not percent but 0-1. 12.8.24: And no default.
    private FloatHolder shininess = null;
    // 4.8.24: Also no percent and no default. 1 = full transparent. Should somehow correspond to color/emis alpha channel if one of these is set.
    private FloatHolder transparency = null;

    // What is difference to color? Well, its just an additional property,
    // which probably can be a property for both base color and texture
    // The ASI needle e.g. has color(1,1,1) and emis (0.15,0.15,0.15)
    private Color emis;
    //3.1.19 shaded default true ist schon vernuenftig. Aber ob es so eine Property ueberhaupt geben sollte, stammt ja aus AC. Siehe Header
    //2.5.19: Doch, die kann es geben. Es gibt die GLTF Extension "KHR_materials_unlit". Unlit, d.h. ohne Einfluss durch Beleuchtung.
    private boolean shaded = true;
    // AC hat default texrep 1,1 und viele Modelle verlassen sich darauf. D.h. als default kein wrap.
    // TODO 8.6.18: wrap sollte ein Attribut mit Werten Repeaet, clamp, ... sein?
    private boolean wraps, wrapt;
    // Only without (base)color
    private String texture;

    private PortableMaterial() {

    }

    public PortableMaterial(String name, String texture, boolean wraps, boolean wrapt) {
        this.name = name;
        this.texture = texture;
        this.wraps = wraps;
        this.wrapt = wrapt;
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
        transparency = new FloatHolder(ins.readFloat());
        shaded = ins.readInt() != 0;
        wraps = ins.readInt() != 0;
        wrapt = ins.readInt() != 0;
    }


    public FloatHolder getShininess() {
        if (shininess != null) {
            return shininess;
        }
        return null;
    }

    /*public float getTransparencypercent() {
        if (transparencypercent != null) {
            return transparencypercent.value;
        }
        return 0;
    }*/

    public void serialize(NativeOutputStream outs) {
        outs.writeString(name);
        //  die drei Farben sind nie null
        color.serialize(outs);
        ambient.serialize(outs);
        specular.serialize(outs);
        outs.writeFloat(shininess.value);
        outs.writeFloat(0/*shininessstrengthpercent.value*/);
        outs.writeFloat(transparency.value);
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
        nmat.transparency = transparency;
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

    /**
     * Returns transparency in range 0..1 or null for non transparent.
     * AC3D has transparency 0 for opaque, while Unity/OpenGL have 1 for opaque alpha channel
     * and alpha = 0 means fully transparent.
     * Threejs uses property 'opacity" where value of 0.0 indicates fully transparent, 1.0 is fully opaque.
     * But we stay with phrase 'transparency' because it better fits to value 'null'
     * for no transparency. And 1 is full transparent.
     */
    public Float getTransparency() {
        if (transparency != null) {
            return Float.valueOf(transparency.value);
        }
        return null;
    }

    public void setShaded(boolean shaded) {
        this.shaded = shaded;
    }

    public void setShininess(FloatHolder shininess) {
        this.shininess = shininess;
    }

    public void setTransparency(FloatHolder transparency) {
        this.transparency = transparency;
    }

    public void setEmis(Color emis) {
        this.emis = emis;
    }

    public String getTexture() {
        return texture;
    }

    /**
     * still needed for AC
     *
     * @param texture
     */
    @Deprecated
    public void setTexture(String texture) {
        this.texture = texture;
    }

    public boolean isShaded() {
        return shaded;
    }

    public boolean getWraps() {
        return wraps;
    }

    public boolean getWrapt() {
        return wrapt;
    }

    public Color getColor() {
        return color;
    }

    public Color getEmis() {
        return emis;
    }


}
