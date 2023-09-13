package de.yard.threed.core.loader;

import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.PortableMaterial;

/**
 * Helperklasse fuer prepocess des BTG. 14.12.17: Sofort auch schon deprecated. Kann weg, wenn SGMaterial hier ein Material ist.
 * Wegen landclass aber doch ganz praktisch.
 */
@Deprecated
public class GeoMat {
    public SimpleGeometry geo;
    public PortableMaterial/*27.12.17Material*/ mat;
    //27.12.17: textureindex gibt es nicht mehr?
    //31.12.17: Bei Nutzung von Landclasses doch
    int textureindex;
    public String landclass;

    public GeoMat(SimpleGeometry geo, PortableMaterial mat) {
        this.geo = geo;
        this.mat = mat;
        //this.textureindex = textureindex;
    }

    public GeoMat(SimpleGeometry geo, String mat, int textureindex) {
        this.geo = geo;
        this.landclass = mat;
        this.textureindex = textureindex;
    }
}
