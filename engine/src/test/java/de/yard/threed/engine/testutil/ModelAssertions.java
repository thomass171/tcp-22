package de.yard.threed.engine.testutil;

import de.yard.threed.core.PortableModelTest;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PortableModelDefinition;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAssertions {

    public static void assertAlphaBlendModeTest(PortableModel portableModel) {
        PortableModelDefinition root = portableModel.getRoot();

        PortableModelTest.assertLevel(root, LoaderGLTF.GLTF_ROOT, new String[]{
                "Bed",
                "DecalBlend",
                "DecalOpaque",
                "GreenArrows",
                "TestBlend",
                "TestCutoff25",
                "TestCutoff75",
                "TestCutoffDefault",
                "TestOpaque",
        });
        assertEquals("data/gltf-sample-assets/AlphaBlendModeTest/AlphaBlendModeTest.gltf", portableModel.getName());

        PortableMaterial matBlend = portableModel.findMaterial("MatBlend");
        assertNotNull(matBlend);
        // what causes shaded?
        assertTrue(matBlend.isShaded());
        assertEquals("AlphaBlendLabels.png", matBlend.getTexture());
        assertNotNull(matBlend.getTransparency());
        // 0.6 currently hardcoded for textures
        assertEquals(0.6, matBlend.getTransparency().floatValue(), 0.0001);
    }
}
