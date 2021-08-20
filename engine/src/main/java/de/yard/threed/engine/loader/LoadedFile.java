package de.yard.threed.engine.loader;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.buffer.NativeOutputStream;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.engine.platform.common.FaceList;
import de.yard.threed.core.resource.ResourcePath;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialisierbar zum Cachen.
 * MA19: Das wird jetzt die Hauptklasse zur Anbindung von GLTF (statt serialisieren).
 * Koennte deswegen vielleicht umbenannt werden. Ach, fuer GLTF gibts einen eigenen Loader. Das passt besser.
 * 11.12.17: Das preprocess Resultat ausgelagert nach PreprocessObject/File. Kann man später nochmal umbenennen.
 * <p/>
 * Created by thomass on 10.06.16.
 */
public class LoadedFile {
    Log logger = Platform.getInstance().getLog(LoadedFile.class);
    int MAGIC = 38;
    int VERSION = 1;
    public List<PortableMaterial> materials = new ArrayList<PortableMaterial>();
    public List<LoadedObject> objects = new ArrayList<LoadedObject>();
    // Material, das verwendet wird, wenn das eigentlich definierte nicht bekannt ist     
    private NativeMaterial dummyMaterial;
    //TODO besser double??
    public /*SGVec3d*/ Vector3 gbs_center;
    // Die Texturen werden dort erwartet, also in dem ResourcePAth, wo auch das Model liegt.
    // wird nicht mit serialisiert. Ist NativeResoure um zu erkennen, ob bundled. Auch nicht schoen.
    public ResourcePath texturebasepath;
    public Bundle bundle;
    ResourcePath rpath;

    public LoadedFile() {

    }
    

    public String dumpObject(String offset, LoadedObject obj, String separator) {
        String s = offset + "OBJECT:";
        s += offset + "name=" + obj.name + separator;
        if (obj instanceof AcObject) {
            s += offset + "type=" + ((AcObject) obj).type + separator;
            s += offset + "location=" + ((AcObject) obj).location + separator;
            s += offset + "texture=" + ((AcObject) obj).texture + separator;
            s += offset + "kids=" + ((AcObject) obj).kids.size() + separator;

        }
        s += offset + "vertices (" + obj.vertices.size() + "):";
        for (int i = 0; obj.vertices != null && i < obj.vertices.size(); i++) {
            //zu viel Output
            // s +=n(offset + obj.vertices[i]);
        }
        s += separator;
        s += offset + "face lists (" + obj.faces.size() + "):" + separator;
        int index = 0;
        if (obj.faces != null) {
            for (FaceList surface : obj.getFaceLists()) {
                s += offset + "faces (" + surface.faces.size() + " with material " + obj.facelistmaterial + "):" + separator;
                //zu viel Output
                // s +=(offset + "type=" + surface.type);
                // s +=(offset + "mat=" + surface.mat);
                //s +=(offset + "refs:");
                //for (int i = 0; i < surface.uv.length; i++) {
                //  s +=(offset + "  " + surface.vref[i] + "," + surface.uv[i]);
                //}
            }
            index++;
        }
        for (int i = 0; i < obj.kids.size(); i++) {
            s += dumpObject("  " + offset, obj.kids.get(i), separator);
        }
        return s;
    }

    public String dumpMaterial(String separator) {
        String s = "MATERIAL:" + separator;
        for (PortableMaterial mat : materials) {
            s += " name=" + mat.name + separator;
            s += " color/diffuse=" + mat.color + separator;
            s += " ambient=" + mat.ambient + separator;
            s += " emis=" + mat.emis + separator;
            s += " spec=" + mat.specular + separator;
            s += " shi=" + mat.getShininess() + separator;
            s += " trans=" + mat.getTransparencypercent() + separator;
        }
        return s;
    }
    
    /**
     * Serialisieren geht nur mit den vorgefertigten Geolists, damit es den maximalen Nutzen hat.
     *
     * @param outs
     */
    public void serialize(NativeOutputStream outs) {
        outs.writeInt(MAGIC);
        outs.writeInt(VERSION);
        outs.writeInt(materials.size());
        for (PortableMaterial m : materials) {
            m.serialize(outs);
        }
        outs.writeInt(objects.size());
        for (LoadedObject o : objects) {
            o.serialize(outs);
        }
    }

}
