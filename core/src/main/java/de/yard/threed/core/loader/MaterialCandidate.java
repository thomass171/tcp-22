package de.yard.threed.core.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.FloatHolder;
import de.yard.threed.core.Util;

/**
 * Helper for collecting material properties for loader that load step by step.
 */
public class MaterialCandidate {

    public String name;
    public Color color, ambient, specular;
    public FloatHolder shininess = null;
    public FloatHolder transparency = null;
    public Color emis;
    public boolean shaded = true;
    public boolean wraps, wrapt;
    public String texture;

    public MaterialCandidate duplicate(String name) {
        MaterialCandidate nmat = new MaterialCandidate();
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

    public PortableMaterial buildPortableMaterial(String finalMaterialName, String textureName) {
        // keep it simple and only decide between color and texture based
        // 14.8.24 what if material already has a texture??
        if (this.texture != null && textureName != null) {
            // inconsistent
            Util.notyet();
        }
        PortableMaterial pm;
        if (textureName == null && texture == null) {
            pm = new PortableMaterial(finalMaterialName, color);
        } else {
            pm = new PortableMaterial(finalMaterialName, textureName, wraps, wrapt);
        }
        // In terms of 'portable' assume values 0.0 for shininess or transparency as 'no'.
        if (shininess != null && shininess.value != 0.0) {
            pm.setShininess(new FloatHolder(shininess.value));
        }
        if (transparency != null && transparency.value != 0.0) {
            pm.setTransparency(new FloatHolder(transparency.value));
        }
        if (emis != null) {
            // emis probably can be a property for both base color and texture
            pm.setEmis(emis);
        }
        pm.setShaded(shaded);
        return pm;
    }
}
