package de.yard.threed.engine;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import org.junit.jupiter.api.Test;


/**
 * Tests fuer Konvertierung Left- und Righthanded.
 * <p/>
 * Date: 12.04.16
 */
public class HandednessTest {
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());


    // Die RH world Matrix der movingbox (der echte tower ist ohne Rotation.
    Matrix4 mboxworldrh = new Matrix4(
            0.25f,0,0,4,
            0,0.25f,0,1.125f,
            0,0,0.25f,-3,
            0,0,0,1);

    Matrix4 conv = new Matrix4(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, -1, 0,
            0, 0, 0, 1);

    /**
     *
     */
    @Test
    public void test1() {
        
        Matrix4 mboxworldlh = conv.multiply(mboxworldrh);
        System.out.println("mboxworldlh=" + mboxworldlh.dump("\n"));
       // TestUtil.assertMatrix4(inverseexpected, mat2.getInverse(), delta);
        // Die Gegenprobe muss wieder die Einheitsmatrix geben (in beide Richtungen)
        Matrix4 m = conv.multiply(mboxworldlh);
        System.out.println("gegenprobe mboxworldrh=" + m.dump("\n"));

    }

    @Test
    public void testMirrorQuaternion() {
        World world = new World(true);
        Quaternion q = new Quaternion(-0.212f, 0, 0, 0.977f);
q = (world.mirror(q));
        System.out.println("qmirror=" + q.dump("\n"));
       // System.out.println("gegenprobe mboxworldrh=" + m.dump("\n"));

    }
/*
    @Test
    public void testDeterminant() {
        TestUtil.assertEquals("mat1", 0f, MathUtil2.getDeterminant(mat1.matrix4));
        TestUtil.assertEquals("mat2", 649f, MathUtil2.getDeterminant(mat2.matrix4));
    }

    @Test
    public void testTransformVector() {
        // Eine Verschiebung auf der x-Achse 10 nach rechts
        Matrix4 tm = Matrix4.buildTranslationMatrix(new Vector3(10, 0, 0));

        Vector3 direction = new Vector3(0, -1, 0);
        direction = tm.transform(direction);
        System.out.println("transformed direction=" + direction);
        TestUtil.assertVector3(new Vector3(10, -1, 0), direction);
    }

    @Test
    public void testRotation() {
        Matrix4 tm = Matrix4.buildRotationZMatrix(new Degree(90));

        Vector3 v = new Vector3(0, 2, 0);
        v = tm.transform(v);
        System.out.println("transformed direction=" + v);
        TestUtil.assertVector3(new Vector3(-2, 0, 0), v);
    }

   */

}