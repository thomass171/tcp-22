package de.yard.threed.core.geometry;



import de.yard.threed.core.loader.PmlFactory;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;

/**
 * Used by reflection! Als unit tests in tools.
 * <p>
 * Created on 06.03.21.
 */
public class PrimitiveCreator implements ProceduralModelCreator {

    Log logger = Platform.getInstance().getLog(PrimitiveCreator.class);
    String[] args;

    public PrimitiveCreator(String[] args) {
        this.args = args;
    }

    @Override
    public PortableModelList createModel() throws ModelCreateException {
        String type = args[0];
        String colorName = args[1];
        logger.debug("type=" + type + ",color=" + colorName);

        Color color = Color.parseString(colorName);

        PortableModelDefinition pmd;

        if (type.equals("plane")) {

            SimpleGeometry planeGeo = Primitives.buildPlaneGeometry(1, 1, 1, 1);
            PortableMaterial planeMat = new PortableMaterial("planeMat", color);
            pmd = PmlFactory.buildElement(planeGeo, planeMat.name);
            pmd.setName("Plane");

            return PmlFactory.buildPortableModelList(new PortableModelDefinition[]{pmd}, new PortableMaterial[]{planeMat});

        }
        if (type.equals("sphere")) {

            SimpleGeometry sphereGeo = Primitives.buildSphereGeometry(1, 32,32);
            PortableMaterial sphereMat = new PortableMaterial("sphereMat", color);
            pmd = PmlFactory.buildElement(sphereGeo, sphereMat.name);
            pmd.setName("Sphere");

            return PmlFactory.buildPortableModelList(new PortableModelDefinition[]{pmd}, new PortableMaterial[]{sphereMat});

        }
        if (type.equals("cube")) {

            SimpleGeometry cubeGeo = Primitives.buildBox(1, 1,1);
            PortableMaterial cubeMat = new PortableMaterial("cubeMat", color);
            pmd = PmlFactory.buildElement(cubeGeo, cubeMat.name);
            pmd.setName("Cube");

            return PmlFactory.buildPortableModelList(new PortableModelDefinition[]{pmd}, new PortableMaterial[]{cubeMat});

        }
        throw new ModelCreateException("unknown type:" + type);
    }
}
