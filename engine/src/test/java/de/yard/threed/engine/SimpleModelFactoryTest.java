package de.yard.threed.engine;


import de.yard.threed.core.PortableModelTest;
import de.yard.threed.core.Util;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PreparedModel;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.avatar.VehiclePmlFactory;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;

import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.resource.Bundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nur fuer Model building, das keine echt platform braucht. Sonst in opengl.
 * <p>
 * Created by thomass on 02.06.16.
 */
public class SimpleModelFactoryTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "data"}, new PlatformFactoryHeadless());

    /**
     * Test especially for textures.
     * 3.1.19
     */
    @Test
    public void testLocomotive() {
        PortableModel locomotive = VehiclePmlFactory.buildLocomotive();
        PortableModelTest.assertLocomotive(locomotive, false);

        Bundle data = BundleRegistry.getBundle("data");
        assertNotNull(data);
        int oldcnt = EngineHelper.getStatistics().texturefailures;
        PreparedModel pmb = PortableModelBuilder.prepareModel(locomotive, null);
        assertEquals(1, pmb.getRoot().findNode(n -> "Chimney".equals(n.getElement().getName())).size());
        SceneNode node = PortableModelBuilder.buildModel(pmb);
        Assertions.assertEquals(0, PortableModelBuilder.dummyMaterialReasons.size(), "dummymaterialused");
        Assertions.assertEquals(oldcnt, EngineHelper.getStatistics().texturefailures, "texturefailures");
        for (int i = 0; i < locomotive.getMaterialCount(); i++) {
            PortableMaterial m = locomotive.getMaterialByIndex(i);
            Assertions.assertTrue(m.isShaded(), "shaded");
        }
        assertTrue(Texture.hasTexture("BucheHell.png"), "BucheHell.png");
    }
}
