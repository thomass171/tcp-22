package de.yard.threed.maze.testutils;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.GridTeleporter;
import de.yard.threed.engine.Ray;
import de.yard.threed.maze.*;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestHelper;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {


    public static void rotatePlayer(GridMover mc, boolean left, Point expected) {
        mc.rotate(left);
        TestUtil.assertPoint("current location", expected, mc.getLocation());
    }

   /* public static void rotatePlayer(GridMover mc, boolean left){

        GridMovement gm = mc.rotate(left);
        TestUtil.assertNotNull("GridMovement", gm);
        TestUtil.assertEquals("Orientation","West",mc.getOrientation().getOrientation().toString());
        // terminate movement
        waitForNotMoving(mc);
        TestUtil.assertFalse("isMoving", mc.isMoving());

    }*/

    public static GridMovement walkPlayer(GridMover player, List<GridMover> boxes, GridMovement gridMovement, MazeLayout layout, Point expected) {
        GridMovement gm = player.walk(gridMovement, player.getOrientation(), new GridState(player, boxes, new ArrayList<GridItem>()), layout);
        TestUtil.assertPoint("new player location", expected, player.getLocation());
        return gm;
    }

    public static GridMovement walkPlayer(GridMover player, List<GridMover> boxes, GridMovement gridMovement, MazeLayout layout, Point expected, GridMovement expectedGridMovement) {
        GridMovement gm = walkPlayer(player, boxes, gridMovement, layout, expected);
        if (expectedGridMovement != null) {
            TestUtil.assertNotNull("returned GridMovement", gm);
            TestUtil.assertEquals("returned GridMovement", expectedGridMovement.toString(), gm.toString());
        } else {
            TestUtil.assertNull("returned GridMovement", gm);
        }
        return gm;
    }

    public static void walkPlayer(GridMover mc, GridMovement gridMovement, GridState gridState, MazeLayout layout, Point expected) {
        mc.walk(gridMovement, mc.getOrientation(), gridState, layout);
        TestUtil.assertPoint("new player location", expected, mc.getLocation());
    }

    /*public static void walkPlayerByPush(GridMover player, GridMover box, GridState gridState, MazeLayout layout, Point expected) {
        Point boxLocation = box.getLocation();
        MazeUtils.combinedMove(gridState, player, layout);
        TestUtil.assertPoint("new player location", expected, player.getLocation());
        TestUtil.assertPoint("new box location", expected.add(player.getOrientation().getDirectionForMovement(GridMovement.Forward).getPoint()), box.getLocation());
    }*/

   /* public static void movePlayer(GridMover mc, GridMovement gridMovement, GridState, MazeLayout layout){

        GridMovement gm = mc.walk(gridMovement,layout);
        TestUtil.assertNotNull("GridMovement", gm);
        // terminate movement
        waitForNotMoving(mc);
        TestUtil.assertFalse("isMoving", mc.isMoving());

    }*/

    public static String loadGrid(String name) {
        return TestHelper.getDataBundleString("maze", name);
    }

    public static Ray getHittingRayForTeleport(Point destinationField, char direction){
        double offset = GridTeleporter.getCenterOffset(MazeDimensions.GRIDSEGMENTSIZE);
        Vector3 hitPoint=MazeUtils.point2Vector3(destinationField);
        switch (direction){
            case 'N':
                hitPoint= hitPoint.add(new Vector3(0,0,-offset));
                break;
            case 'E':
                hitPoint= hitPoint.add(new Vector3(0,0,-offset));
                break;
            case 'S':
                hitPoint= hitPoint.add(new Vector3(0,0,-offset));
                break;
            case 'W':
                hitPoint= hitPoint.add(new Vector3(0,0,-offset));
                break;
        }
        Vector3 origin=new Vector3(hitPoint.getX(),100,hitPoint.getZ());
        return new Ray(origin,hitPoint.subtract(origin));
    }
}
