package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class RotationProviderTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "data"}, new SimpleHeadlessPlatformFactory());

    /**
     *
     */
    @Test
    public void testEdgeBased() {
        Vector3 upVector = new Vector3(0,1,0);
        Quaternion rotation = DefaultEdgeBasedRotationProvider.get3DRotation(false,new Vector3(1,0,0),upVector);
        // no idea why it this value, but thats like it always was
        TestUtils.assertQuaternion( Quaternion.buildFromAngles(new Degree(0),new Degree(-90),new Degree(0)), rotation);
    }


}
