package de.yard.threed.engine.loader;

import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.engine.geometry.ShapeGeometry;

import java.util.List;

/**
 * 06.03.21: Vor allem, weil man damit nicht nur GLTF speichern kann, sondern über PortableModelList
 *  auch ein 3D Model. Darum mal umbenannt: GltfFactory->PmlFactory
 * Created on 17.01.19.
 * 6.3.21: Das ist ein guter Weg, auch wegen "pmc" (ProceduralModelCreator).
 */
public class PmlFactory {
    public static PortableModelDefinition buildElement(ShapeGeometry geo, String materialname) {
        PortableModelDefinition pmd = new PortableModelDefinition();
        List<SimpleGeometry> geolist = GeometryHelper.buildSimpleGeometry(geo);
        //TODO und wenns mehrere gibt?
        return de.yard.threed.core.loader.PmlFactory.buildElement(geolist.get(0), materialname);
    }


}
