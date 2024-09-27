package de.yard.threed.core.loader;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.platform.NativeMaterial;

import java.util.ArrayList;
import java.util.List;

public class PreparedObject {
    // never null or empty
    public String name;
    public Vector3 position;
    public Quaternion rotation;
    public NativeMaterial material;
    public NativeGeometry geometry;

    public PreparedObject(String name) {
        if (StringUtils.empty(name)) {
            name = "<no name>";
        }
        this.name = name;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public void setMaterial(NativeMaterial material) {
        this.material = material;
    }

    public void setGeometry(NativeGeometry geometry) {
        this.geometry = geometry;
    }

    public String getName(){
        return name;
    }
}
