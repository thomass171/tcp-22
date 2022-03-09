package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.jupiter.api.Test;

public class GridTeleporterTest {

    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    /**
     * Skizze 65
     */
    @Test
    public void testMoveDestinationMarker() {

        SceneNode directionMarker = new SceneNode();
        SceneNode localMarker = new SceneNode();

        GridTeleporter gridTeleporter = new GridTeleporter(localMarker, directionMarker);

        Vector2 intersectionInNegativeUpperArea = new Vector2(4.1, -6.3);
        gridTeleporter.moveDestinationMarker(intersectionInNegativeUpperArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.VISIBLE_GROUNDMARKER_Y, -6.375), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.HIDDEN_GROUNDMARKER_Y, -6 - 0.25 - 0.125), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(0)), directionMarker.getTransform().getRotation());

        Vector2 intersectionInNegativeLowerArea = new Vector2(4.1, -5.7);
        gridTeleporter.moveDestinationMarker(intersectionInNegativeLowerArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.VISIBLE_GROUNDMARKER_Y, -5.625), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.HIDDEN_GROUNDMARKER_Y, -6 + 0.25 + 0.125), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(180)), directionMarker.getTransform().getRotation());

        Vector2 intersectionInPositiveUpperArea = new Vector2(4.1, 5.7);
        gridTeleporter.moveDestinationMarker(intersectionInPositiveUpperArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.VISIBLE_GROUNDMARKER_Y, 5.625), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.HIDDEN_GROUNDMARKER_Y, 6 - 0.25 - 0.125), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(0)), directionMarker.getTransform().getRotation());

        Vector2 intersectionInPositiveLowerArea = new Vector2(4.1, 6.3);
        gridTeleporter.moveDestinationMarker(intersectionInPositiveLowerArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.VISIBLE_GROUNDMARKER_Y, 6.375), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(4, GridTeleporter.HIDDEN_GROUNDMARKER_Y, 6 + 0.25 + 0.125), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(180)), directionMarker.getTransform().getRotation());

        Vector2 intersectionInPositiveRightArea = new Vector2(4.3, 6.1);
        gridTeleporter.moveDestinationMarker(intersectionInPositiveRightArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(4.375, GridTeleporter.VISIBLE_GROUNDMARKER_Y, 6), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(4 + 0.25 + 0.125, GridTeleporter.HIDDEN_GROUNDMARKER_Y, 6), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(-90)), directionMarker.getTransform().getRotation());

        Vector2 intersectionInPositiveLeftArea = new Vector2(3.7, 6.1);
        gridTeleporter.moveDestinationMarker(intersectionInPositiveLeftArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(3.625, GridTeleporter.VISIBLE_GROUNDMARKER_Y, 6), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(4 - 0.25 - 0.125, GridTeleporter.HIDDEN_GROUNDMARKER_Y, 6), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(90)), directionMarker.getTransform().getRotation());

        Vector2 intersectionInNegativeRightArea = new Vector2(-3.7, 6.1);
        gridTeleporter.moveDestinationMarker(intersectionInNegativeRightArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(-3.625, GridTeleporter.VISIBLE_GROUNDMARKER_Y, 6), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(-4 + 0.25 + 0.125, GridTeleporter.HIDDEN_GROUNDMARKER_Y, 6), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(-90)), directionMarker.getTransform().getRotation());

        Vector2 intersectionInNegativeLeftArea = new Vector2(-4.3, 6.1);
        gridTeleporter.moveDestinationMarker(intersectionInNegativeLeftArea, localMarker, directionMarker, 1);
        TestUtil.assertVector3(new Vector3(-4.375, GridTeleporter.VISIBLE_GROUNDMARKER_Y, 6), directionMarker.getTransform().getPosition());
        TestUtil.assertVector3(new Vector3(-4 - 0.25 - 0.125, GridTeleporter.HIDDEN_GROUNDMARKER_Y, 6), localMarker.getTransform().getPosition());
        TestUtil.assertQuaternion("", Quaternion.buildRotationY(new Degree(90)), directionMarker.getTransform().getRotation());
    }

    @Test
    public void testMoveDestinationMarkerSize15() {

        SceneNode directionMarker = new SceneNode();
        SceneNode localMarker = new SceneNode();

        GridTeleporter gridTeleporter = new GridTeleporter(localMarker, directionMarker);

        Vector2 intersectionInNegativeCenterArea = new Vector2(0.2, -1.5);
        gridTeleporter.moveDestinationMarker(intersectionInNegativeCenterArea, localMarker, directionMarker, 1.5);
        TestUtil.assertVector3(new Vector3(0, GridTeleporter.VISIBLE_GROUNDMARKER_Y, -1.5), directionMarker.getTransform().getPosition());

        intersectionInNegativeCenterArea = new Vector2(0.2, -3);
        gridTeleporter.moveDestinationMarker(intersectionInNegativeCenterArea, localMarker, directionMarker, 1.5);
        TestUtil.assertVector3(new Vector3(0, GridTeleporter.VISIBLE_GROUNDMARKER_Y, -3), directionMarker.getTransform().getPosition());

        Vector2 intersectionInNegativeUpperArea = new Vector2(1.51, -2.1);
        gridTeleporter.moveDestinationMarker(intersectionInNegativeUpperArea, localMarker, directionMarker, 1.5);
        TestUtil.assertVector3(new Vector3(1.5, GridTeleporter.VISIBLE_GROUNDMARKER_Y, -1.5 - (1.5 / 2) + 1.5 / 8), directionMarker.getTransform().getPosition());

    }

    @Test
    public void testOffset() {
        TestUtil.assertFloat("", 3 * 1.5 / 8, GridTeleporter.getCenterOffset(1.5));
    }
}
