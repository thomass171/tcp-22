package de.yard.threed.engine.test;


import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;

import de.yard.threed.core.testutil.Assert;

import java.util.List;


/**
 * Je nach dem, wie sich das entwickelt ("eigener" Intersectionalgorithmus), gehoeren die Tests ja nicht mehr zur Platfrom.
 * Created by thomass on 28.11.14.
 */
public class GeometryTest {
    static ShapeGeometry cuboidgeo = ShapeGeometry.buildBox(4, 3, 1, null);
    //SceneNode cuboid = new SceneNode(new Mesh(cuboidgeo, Material.buildBasicMaterial(Color.YELLOW)));

    
    

    /**
     * Ein Quader und Ray senkrecht von weit oben, aber daneben, der trifft nicht.
     */
    //@Test
    public void testIntersectionCenteredCuboid3() {
        Ray ray = buildVerticalRay(4f);
        //THREED TODO maze.getOrigin().setX(10);
        testIntersectionGeometry(ray, null);//new Vector3[]{});
    }

    /**
     * zeigt nach unten.
     * 
     * @param ypos
     * @return
     */
    private Ray buildVerticalRay(float ypos) {
        Vector3 origin = new Vector3(-0.25f, ypos, 0.25f);
        Vector3 direction = new Vector3(0, -1, 0);
        Ray ray = new Ray(origin, direction);
        return ray;
    }

    private void testIntersectionGeometry(Ray ray, SceneNode expectedintersection) {
        //List<Vector3> intersections = maze.getIntersection(cuboid);
        List<NativeCollision> intersections = ray.getIntersections();
        showIntersections(intersections);
        if (expectedintersection != null) {
            assertExpectedIntersection(intersections, null,expectedintersection);
        } else {
            Assert.assertEquals("Keine intersection expected", 0, intersections.size());
        }
    }
    
 /*   private void testIntersectionGeometry(Ray maze, Vector3[] expectedintersection) {
        //List<Vector3> intersections = maze.getIntersection(cuboid);
        List<NativeCollision> intersections = maze.getIntersections();
        showIntersections(intersections);
        if (expectedintersection != null && expectedintersection.length > 0) {
            assertExpectedIntersection(intersections, expectedintersection,cuboid);
        } else {
            Assert.assertEquals("Keine intersection expected", 0, intersections.size());
        }
    }*/

    public static void showIntersections(List<NativeCollision/*Vector3*/> intersections) {
        //System.out.println("intersectioncnt=" + intersections.size());
        for (/*Vector3*/NativeCollision intersection : intersections) {
            System.out.println("intersection=" + intersection);

        }
    }

    public static void assertExpectedIntersection(List<NativeCollision/*Vector3*/> intersections, Vector3[] expectedintersections,SceneNode expectednode) {
        //for (int i = 0; i < expectedintersections.length; i++) {
            boolean found = false;
            for (/*Vector3*/NativeCollision intersection : intersections) {
               // if (TestUtil.equals(intersection, expectedintersections[i]))
                if (intersection.getSceneNode()==expectednode)
                    found = true;

            }
            if (!found) {
                Assert.fail("Expected intersection not found: ");// + expectedintersections[i]);
            }
       // }
    }    

    /**
     * Ein verschobener Quader und Ray senkrecht von weit oben, aber jetzt daneben, der trifft nicht.
     */
    //@Test
    /*public void testIntersectionTranslatedCuboidNoHit() {
        Ray maze = buildVerticalRay(4f);
        //cuboid.translateX(10f);
        cuboid.t
        testIntersectionMesh(maze, new Vector3[]{});
    }*/

    /**
     * Ein verschobener Quader und verschobener Ray senkrecht von weit oben. Der trifft dann zweimal.
     */
    //@Test
    /*public void testIntersectionTranslatedCuboid() {
        Ray maze = buildVerticalRay(4f);
        maze.getOrigin().setX(maze.getOrigin().getX()+10);
        cuboid.translateX(10f);
        //System.out.println("cuboid.ModelMatrixWorld=" + cuboid.getModelMatrixWorld().dump("\n"));
        //System.out.println("cuboid.ModelMatrixWorld.inv=" + cuboid.getModelMatrixWorld().getInverse().dump("\n"));

        Vector3 origin = maze.getOrigin();
        Vector3 expectedintersection1 = new Vector3(origin.getX(), -1.5f, origin.getZ());
        Vector3 expectedintersection2 = new Vector3(origin.getX(), +1.5f, origin.getZ());
        testIntersectionMesh(maze, new Vector3[]{expectedintersection1, expectedintersection2});
    }*/

  

    /*private void testIntersectionMesh(Ray maze, Vector3[] expectedintersection) {
        List<Vector3> intersections = cuboid.getIntersection(maze);
        GeometryTest.showIntersections(intersections);
        if (expectedintersection != null && expectedintersection.length > 0) {
            GeometryTest.assertExpectedIntersection(intersections, expectedintersection);
        } else {
            assertEquals("Keine intersection expected", 0, intersections.size());
        }
    }*/


}
