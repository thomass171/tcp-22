package de.yard.threed.engine;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.avatar.VehiclePmlFactory;
import de.yard.threed.engine.loader.PortableMaterial;
import de.yard.threed.engine.loader.PortableModelList;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;

import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.jupiter.api.Test;

/**
 * Nur fuer Model building, das keine echt platform braucht. Sonst in opengl.
 *
 * Created by thomass on 02.06.16.
 */
public class SimpleModelFactoryTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    /**
     * Test vor allem wegen Texturen.
     * 3.1.19
     */
    @Test
    public void testLocomotive() {
        PortableModelList locomotive = VehiclePmlFactory.buildLocomotive();
        Bundle data = BundleRegistry.getBundle("data");
        int oldcnt =  EngineHelper.getStatistics().texturefailures;
        SceneNode node = locomotive.buildModel(data);
        TestUtil.assertFalse("dummymaterialused",locomotive.dummymaterialused);
        TestUtil.assertEquals("texturefailures",oldcnt, EngineHelper.getStatistics().texturefailures);
        for (PortableMaterial m : locomotive.materials) {
            TestUtil.assertTrue("shaded", m.shaded);
        }
    }
}
