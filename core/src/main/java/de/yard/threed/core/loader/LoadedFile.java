package de.yard.threed.core.loader;

import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.resource.ResourcePath;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 23.9.24: What is this finally? Just a container for a root object together with materials.
 * <p/>
 * Created by thomass on 10.06.16.
 */
public class LoadedFile {
    // 12.8.24: Better a MaterialCandidate, but that too much effort now
    public List<MaterialCandidate> materials = new ArrayList<MaterialCandidate>();
    // 23.9.24: For simplification only have one root object any more. If we have more, these should be children.
    //public List<LoadedObject> objects = new ArrayList<LoadedObject>();
    public LoadedObject object;
    // Die Texturen werden dort erwartet, also in dem ResourcePAth, wo auch das Model liegt.
    // wird nicht mit serialisiert. Ist NativeResoure um zu erkennen, ob bundled. Auch nicht schoen.
    public ResourcePath texturebasepath;

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
        /*TODO 12.8.24 for (PortableMaterial mat : materials) {
            s += " name=" + mat.name + separator;
            s += " color/diffuse=" + mat.color + separator;
            s += " ambient=" + mat.ambient + separator;
            s += " emis=" + mat.emis + separator;
            s += " spec=" + mat.specular + separator;
            s += " shi=" + mat.getShininess() + separator;
            s += " trans=" + mat.getTransparency() + separator;
        }*/
        return s;
    }

    public MaterialCandidate findMaterialCandidateByName(String name) {
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).name.equals(name)) {
                return materials.get(i);
            }
        }
        return null;
    }
}
