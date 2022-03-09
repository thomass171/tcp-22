package de.yard.threed.core;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.jupiter.api.Test;


/**
 * Date: 26.04.14
 */
public class Matrix3Test {
    Matrix3 mat1 = new Matrix3(
            1, 2,3,
            5, 6,7,
            8,9,0);

    Matrix3 mat2 = new Matrix3(
            5, 4,2,
            3, 7,1,
            8,0,1);


    /**
     * Das Ergebnis der Multiplikation per Onlinerechner ist:
     * <p/>
     * 35 18 7
     * 99 62 23
     * 67 95 25
     */
    @Test
    public void testMultiplyMat1TimesMat2() {
        Matrix3 res = mat1.multiply(mat2);
        Assert.assertEquals("00 ", 35, res.e11, 0.1f);
        Assert.assertEquals("10 ", 99, res.e21, 0.1f);
        Assert.assertEquals("20 ", 67, res.e31, 0.1f);
        Assert.assertEquals("01 ", 18, res.e12, 0.1f);
        Assert.assertEquals("12 ", 62, res.e22, 0.1f);
        Assert.assertEquals("21 ", 95, res.e32, 0.1f);
        Assert.assertEquals("02 ", 7, res.e13, 0.1f);
        Assert.assertEquals("12 ", 23, res.e23, 0.1f);
        Assert.assertEquals("22 ", 25, res.e33, 0.1f);

    }

    @Test
    public void testTransformVector() {
        // Eine Verschiebung auf der x-Achse 10 nach rechts
        Matrix3 tm = Matrix3.buildTranslationMatrix(new Vector2(10, 0));

        Vector2 direction = new Vector2(0, -1);
        direction = tm.transform(direction);
        //System.out.println("transformed direction=" + direction);
        TestUtil.assertVector2("", new Vector2(10, -1), direction);
    }

    @Test
    public void testRotation() {
        Matrix3 tm = Matrix3.buildRotationMatrix((float) (Math.PI/2)/*new Degree(90)*/);

        Vector2 v = new Vector2(0, 2);
        v = tm.transform(v);
        //System.out.println("transformed direction=" + v);
        TestUtil.assertVector2("", new Vector2(-2, 0), v);
    }


}