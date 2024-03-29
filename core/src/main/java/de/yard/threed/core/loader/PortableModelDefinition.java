package de.yard.threed.core.loader;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.SimpleGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * 06.03.21: A universal intermediate format of a model specification.
 *
 * Created by thomass on 12.02.16.
 * 7.6.18: PreprocessedLoadedObject->PortableModelDefinition
 */
public class PortableModelDefinition /*extends CustomGeometry */{
    Log logger = Platform.getInstance().getLog(PortableModelDefinition.class);
    public String name;
    public List<PortableModelDefinition> kids = new ArrayList<PortableModelDefinition>();
    // Die Materiallist darf nicht LoadedMaterial enthalten, weil nicht jedes Modell Material enthält (z.B. BTG und OBJ). Evtl.
    // stimmen die Namen auch nicht (3DS Shuttle). Darum hier im Loader nur den Materialnamen speichern. Der Aufrufer muss dann
    // sehen, wo er das Material herbekommt. Der Materialname in der Liste passt vom Index her zur geolist.
    // 28.12.17: Das Arbeiten mit Materialnamen kann riskant sein, wenn sie nicht eindeutig sind. Gabs schon mal bei Merhfachnutzung in BTGs. Ein eindeutiger Index
    // waere besser.
    public List<String> geolistmaterial = new ArrayList<String>();
    // Auf AC zugeschnitten, denn nur die(?) haben die Textur am Object. 22.12.17: Das ist ja Kappes. Jetzt auch in LoadedMaterial
    // 3.5.19: Wird das Material bei AC dann dupliziert? Ja, offebar. siehe testAC_777_200. Aus 12 wird 37. Darum ganz raus.
    //@Deprecated
    //public String texture;
    public Vector3 translation = null;//3.5.19 new Vector3(0, 0, 0);
    public Quaternion rotation=null;
    public Vector3 scale;
    public List<SimpleGeometry> geolist;

    public PortableModelDefinition() {
        geolist = new ArrayList<SimpleGeometry>();
    }

    public void addGeoMat(SimpleGeometry geo, String material) {
        geolist.add(geo);
        geolistmaterial.add(material);
    }

    public void setPosition(Vector3 position) {
        this.translation = position;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public void attach(PortableModelDefinition pmd) {
        kids.add(pmd);
    }

    public void setName(String name) {
        this.name = name;
    }
}
