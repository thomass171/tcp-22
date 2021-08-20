package de.yard.threed.engine.test;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.platform.common.RayHelper;
import de.yard.threed.core.testutil.TestUtil;


/**
 * Date: 26.11.15
 */
public class ProjectionTest {

    /**
     * Aus ThreeJS ReferenceSceneTest:
     * Pickingray aus der Default Camera Position (0,5,11) genau in die Mitte der Scene (mousevector =0,0,0.5)
     */
    //@Test
    public void testRay() {
        // Die unprojectmatrix ist aus ThreeJS ermittelt.
        Matrix4 unprojectmatrix = new Matrix4(
                0.552f,0,0,0,
                0,0.377f,-25,24.586f,
                0,-0.171f,-54.999f,54.09f,
                0,0,-5,5);

        Vector3 mousevector = new Vector3(0,0,0.5f);
        Vector3 unprojectedmouseVector = (RayHelper.project(mousevector,unprojectmatrix));
        TestUtil.assertVector3("unprojectedmouseVector", new Vector3(0, 4.8344f, 10.6362f), unprojectedmouseVector);

        /*Ray raycaster = buildPickingRay(400,3002,camera);
        logger.debug("maze direction=" + dumpVector3(raycaster.maze.direction));
        unprojectedmouseVector of x=1198,y=310 =x=0.44, y=4.83, z=10.64
        06:02:23 DEBUG - direction of x=1198,y=310 =x=0.74, y=-0.29, z=-0.61
        06:02:23 DEBUG - picking maze from x=0, y=5, z=11 direction=x=0.74, y=-0.29, z=-0.61*/

    }

}