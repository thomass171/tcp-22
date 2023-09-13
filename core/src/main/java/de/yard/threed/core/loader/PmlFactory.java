package de.yard.threed.core.loader;

import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.SimpleGeometry;

import java.util.List;

/**
 * 06.03.21: Vor allem, weil man damit nicht nur GLTF speichern kann, sondern über PortableModelList
 *  auch ein 3D Model. Darum mal umbenannt: GltfFactory->PmlFactory
 * Created on 17.01.19.
 * 6.3.21: Das ist ein guter Weg, auch wegen "pmc" (ProceduralModelCreator).
 */
public class PmlFactory {
    /*11.9.23 in engine public static PortableModelDefinition buildElement(ShapeGeometry geo, String materialname) {
        PortableModelDefinition pmd = new PortableModelDefinition();
        List<SimpleGeometry> geolist = GeometryHelper.buildSimpleGeometry(geo);
        //TODO und wenns mehrere gibt?
        return buildElement(geolist.get(0), materialname);
    }*/

    public static PortableModelDefinition buildElement(SimpleGeometry geo, String materialname) {
        PortableModelDefinition pmd = new PortableModelDefinition();
        pmd.addGeoMat(geo, materialname);
        return pmd;
    }

    public static PortableModelList buildPortableModelList(PortableModelDefinition[] pmd, PortableMaterial[] material) {

        PortableModelList pml = new PortableModelList(null);
        for (PortableModelDefinition m : pmd) {
            pml.addModel(m);
        }
        for (PortableMaterial mat : material) {
            pml.addMaterial(mat);
        }
        return pml;
    }
}
