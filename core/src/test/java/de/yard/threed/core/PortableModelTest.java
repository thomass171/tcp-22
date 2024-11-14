package de.yard.threed.core;

import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.FaceN;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Also for PortableModelDefinition
 * <p>
 */
public class PortableModelTest {
    Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void dummy() {
    }

    public static void assertLevel(PortableModelDefinition modelDefinition, String expectedName, String[] expectedChildren) {

        if (expectedName == null) {
            assertNull(modelDefinition.getName());
        } else {
            assertEquals(expectedName, modelDefinition.getName());
        }

        assertEquals(expectedChildren.length, modelDefinition.kids.size());
        for (int i = 0; i < expectedChildren.length; i++) {
            if (expectedChildren[i] == null) {
                assertNull(modelDefinition.kids.get(i).getName());
            } else {
                assertEquals(expectedChildren[i], modelDefinition.kids.get(i).getName());
            }
        }
    }

    /**
     * Needed in different modules. So its just in core.
     */
    public static void assertLocomotive(PortableModel model, boolean loadedFromGltf) {

        assertNotNull(model);
        assertEquals(2, model.getMaterialCount());
        assertEquals("BucheHell", model.getMaterialByIndex(0).getName());
        assertEquals("wheelred", model.getMaterialByIndex(1).getName());

        PortableModelDefinition root = model.getRoot();

        if (loadedFromGltf) {
            // has an intermediate node
            assertEquals(1, root.kids.size());
            PortableModelTest.assertLevel(root, LoaderGLTF.GLTF_ROOT, new String[]{"Locomotive"});
            root = root.kids.get(0);
        }

        PortableModelTest.assertLevel(root, "Locomotive", new String[]{"baseblock"});
        PortableModelDefinition baseblock = root.kids.get(0);
        // all without name currently
        PortableModelTest.assertLevel(baseblock, "baseblock", new String[]{
                "boiler",
                "wheel",
                "wheel",
                "wheel",
                "wheel",
                "wheel",
                "wheel",
                "back",
        });
        PortableModelTest.assertLevel(baseblock.getChild(0), "boiler", new String[]{"Chimney"});
        PortableModelTest.assertLevel(baseblock.getChild(7), "back", new String[]{
                "pole",
                "pole",
                "pole",
                "pole",
                "roof"
        });

    }

    /**
     * Why is only one child important? Because we only use it in that context for easier handling. Of course
     * a root element might have multiple children in general. Otherwise we have assertLevel().
     * //ac-world is no single root!
     */
    public static PortableModelDefinition validateSingleRoot(PortableModelDefinition singleRootCandidate, String expectedRootName){

        if (expectedRootName!=null) {
            assertEquals(expectedRootName, singleRootCandidate.getName());
            if (singleRootCandidate.kids.size() != 1) {
                fail("not only one child");
            }
            singleRootCandidate = singleRootCandidate.getChild(0);
        }
        return singleRootCandidate;
    }

}
