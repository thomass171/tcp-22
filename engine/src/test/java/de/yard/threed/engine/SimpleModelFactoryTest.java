package de.yard.threed.engine;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.avatar.VehiclePmlFactory;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;

import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.resource.Bundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Nur fuer Model building, das keine echt platform braucht. Sonst in opengl.
 * <p>
 * Created by thomass on 02.06.16.
 */
public class SimpleModelFactoryTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    /**
     * Test vor allem wegen Texturen.
     * 3.1.19
     */
    @Test
    public void testLocomotive() {
        PortableModelList locomotive = VehiclePmlFactory.buildLocomotive();
        Bundle data = BundleRegistry.getBundle("data");
        int oldcnt = EngineHelper.getStatistics().texturefailures;
        PortableModelBuilder pmb = new PortableModelBuilder(locomotive);
        SceneNode node = pmb.buildModel(data);
        Assertions.assertFalse(pmb.dummymaterialused, "dummymaterialused");
        Assertions.assertEquals(oldcnt, EngineHelper.getStatistics().texturefailures, "texturefailures");
        for (PortableMaterial m : locomotive.materials) {
            Assertions.assertTrue(m.shaded, "shaded");
        }
    }
}
