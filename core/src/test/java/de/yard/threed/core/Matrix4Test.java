package de.yard.threed.core;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.jupiter.api.Test;


/**
 * Das sind die Tests fuer plattformunabhängige Methoden (z.B. in Mathutil2).
 * Es gibt noch einen weiteren Matrix4Test!
 * <p/>
 * Date: 26.08.15
 */
public class Matrix4Test {
    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    Matrix4 mat1 = new Matrix4(
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16);

    Matrix4 mat2 = new Matrix4(
            5, 4, 4, 2,
            5, 6, 7, 8,
            9, 3, 6, 1,
            8, 2, 3, 9);

    // Das ist die currentmodel matrix aus der Referencescene Animation.
    Matrix4 currentmodelmatrix = new Matrix4(0.5f, 0, 0, 0,
            0, 0.5f, 0, 0.75f,
            0, 0, 0.5f, 0,
            0, 0, 0, 1);
    /**
     *
     */
    @Test
    public void testInverse() {
        // Die Referenzwerte stammen aus Javascript
        Matrix4 inverseexpected = new Matrix4(0.165f, -0.151f, 0.018f, 0.096f,
                0.653f, -0.122f, -0.291f, -0.005f,
                -0.556f, 0.276f, 0.293f, -0.154f,
                -0.106f, 0.069f, -0.049f, 0.079f);
        System.out.println("mat2.inverse=" + mat2.getInverse().dump("\n"));
        TestUtil.assertMatrix4(inverseexpected, mat2.getInverse());
        // Die Gegenprobe muss wieder die Einheitsmatrix geben (in beide Richtungen)
        TestUtil.assertMatrix4(new Matrix4(), mat2.multiply(mat2.getInverse()));
        TestUtil.assertMatrix4(new Matrix4(), mat2.getInverse().multiply(mat2));
    }

    @Test
    public void testDeterminant() {
        TestUtil.assertEquals("mat1", 0f, MathUtil2.getDeterminant(mat1));
        TestUtil.assertEquals("mat2", 649f, MathUtil2.getDeterminant(mat2));
    }

    @Test
    public void testDeterminant2() {
        TestUtil.assertEquals("mat1 scaled", 0f, MathUtil2.getDeterminant(mat1.multiply(Matrix4.buildScaleMatrix(new Vector3(8, 9, 10)))));        
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

    /**
     *
     */
    @Test
    public void testTransformationVector1() {
        // Die Referenzwerte stammen aus der ThreeJs Referenz Pyramide mit den Winkeln -90 /90
        Matrix4 refmat = new Matrix4(
                0, -1, 0, 1.5f,
                0, 0, 1, 0,
                -1, 0, 0, 3,
                0, 0, 0, 1);

        Vector3 translation = new Vector3(1.5f, 0, 3);
        Quaternion rotation = new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f);
        Matrix4 tm = Matrix4.buildTransformationMatrix(translation, rotation);
        TestUtil.assertMatrix4(refmat, tm);

        // Die Referenzwerte stammen aus der ThreeJs Referenz Pyramide mit den Winkeln -70 /25
        refmat = new Matrix4(
                0.906f, -0.423f, 0, 1.5f,
                0.145f, 0.31f, 0.94f, 0,
                -0.397f, -0.852f, 0.342f, 3,
                0, 0, 0, 1
        );

        translation = new Vector3(1.5f, 0, 3);
        rotation = new Quaternion(-0.56f, 0.124f, 0.177f, 0.8f);

        tm = Matrix4.buildTransformationMatrix(translation, rotation);
        // Hier koennten sich Rundungsfehler sammeln, darum ist die Toleranz etwas groesser
        TestUtil.assertMatrix4("",refmat, tm, 0.002f);
    }

    /**
     * Den Punkt (-0.5,0,0) 90 Grad nach liks um die y-Achse drehem
     */
    @Test
    public void testSimpleRotation() {
        Vector3 v = new Vector3(-0.5f, 0, 0);
        Matrix4 tm = Matrix4.buildRotationMatrix(new Quaternion(0, 1, 0, 1));
        //die expected ist falsch assertMatrix4(expectedrotmatrix, tm);

        v = tm.transform(v);
        System.out.println("transformed v=" + v);
        TestUtil.assertVector3(new Vector3(0, 0, 0.5f), v);

    }

    @Test
    public void testInverseMatrix3() {
        // Die Referenzwerte stammen aus ThreeJS
        Matrix3 inverseexpected = new Matrix3(0.294f, -0.235f, 0.078f,
                0.647f, -0.118f, -0.294f,
                -0.765f, 0.412f, 0.196f);
        Matrix3 inv = MathUtil2.getInverseAsMatrix3(mat2);
        System.out.println("mat2.inverse=" + inv.dump("\n"));
        TestUtil.assertMatrix3("", inverseexpected, inv);
        // Die Gegenprobe duerfte bei dieser merkwürdigen Operation nicht gehen.

    }

    /**
     * Eine TransformationMatrix bauen und dann wieder die Bestandteile extrahieren.
     */
    @Test
    public void testExtractsUniformScale() {
        Vector3 pos = new Vector3(1, 2, 3);
        Quaternion rot = new Quaternion(4, 5, 6, 7);
        Quaternion normrot = rot.normalize();

        Vector3 scale = new Vector3(8, 8, 8);
        Matrix4 trmatrix = Matrix4.buildTransformationMatrix(pos, rot, scale);
        System.out.println("trmatrix=" + trmatrix.dump("\n"));
        Vector3 expos = trmatrix.extractPosition();
        Quaternion exrot = trmatrix.extractQuaternion();
        Vector3 exscale = trmatrix.extractScale();

        TestUtil.assertVector3("extracted pos",pos, expos);
        System.out.println("extracted rot=" + exrot.dump(" "));
        System.out.println("normrot=" + normrot.dump(" "));
        //seit MA16 ist exrot normalisiert
        TestUtil.assertQuaternion("extracted rotation",rot.normalize(),exrot);
        TestUtil.assertVector3("extracted scale",scale, exscale);
    }

    /**
     * Eine TransformationMatrix bauen und dann wieder die Bestandteile extrahieren.
     *
     * 20.4.16: Es koennte sein, dass diese Art Test überhaupt nicht zulässig ist. Die Rotation
     * muss nach dem extrahieren ja nicht zwangsläufig so aussehen wie vorher, sondern inhaltlich
     * nur dasselbe (Rotation) repraesentieren.
     */
    @Test
    public void testExtractsNonUniformScale() {
        Vector3 pos = new Vector3(1, 2, 3);
        Quaternion rot = new Quaternion(4, 5, 6, 7);
        Quaternion normrot = rot.normalize();

        Vector3 scale = new Vector3(8, 9, 10);
        Matrix4 trmatrix = Matrix4.buildTransformationMatrix(pos, rot, scale);
        System.out.println("trmatrix=" + trmatrix.dump("\n"));
        Vector3 expos = trmatrix.extractPosition();
        Quaternion exrot = trmatrix.extractQuaternion();
        Vector3 exscale = trmatrix.extractScale();

        TestUtil.assertVector3("extracted pos",pos, expos);
        System.out.println("extracted rot=" + exrot.dump(" "));
        System.out.println("normrot=" + normrot.dump(" "));
        //siehe Kommentar oben TestUtil.assertQuaternion("extracted rotation",rot,exrot);
        TestUtil.assertVector3("extracted scale",scale, exscale);
    }

    /**
     * 20.4.16: Es koennte sein, dass diese Art Test überhaupt nicht zulässig ist. Die Matrix
     * muss nach dem extrahieren ja nicht zwangsläufig so aussehen wie vorher, sondern inhaltlich
     * nur dasselbe (Rotation) repraesentieren.
     */
    //@Test
    public void testRemoveScale() {        
        Vector3 scale = new Vector3(8, 9, 10);
        Matrix4 trmatrix = mat1.multiply(Matrix4.buildScaleMatrix(scale));
        System.out.println("trmatrix=" + trmatrix.dump("\n"));
        Matrix4 trmatrixohnescale = (MathUtil2.removeScale(trmatrix));
        System.out.println("trmatrixohnescale=" + trmatrixohnescale.dump("\n"));
        
        TestUtil.assertMatrix4("trmatrixohnescale",mat1, trmatrixohnescale);
        
    }



    /**
     * Das Ergebnis der Multiplikation per Onlinerechner und auch ThreeJS ist:
     * <p/>
     * 74,33,48,57,
     * 182,93,128,137,
     * 290,153,208,217,
     * 398,213,288,297
     */
    public void testMultiplyMat1TimesMat2() {
        Matrix4 res = mat1.multiply(mat2);
        // 1*5+2*5+3*9+4*8=5+10+27+32=74
        TestUtil.assertEquals("00 ", 74, res.getElement(0, 0));
        TestUtil.assertEquals("10 ", 182, res.getElement(1, 0));
        TestUtil.assertEquals("20 ", 290, res.getElement(2, 0));
        TestUtil.assertEquals("30 ", 398, res.getElement(3, 0));
        TestUtil.assertEquals("01 ", 33, res.getElement(0, 1));
        TestUtil.assertEquals("12 ", 93, res.getElement(1, 1));
        TestUtil.assertEquals("21 ", 153, res.getElement(2, 1));
        TestUtil.assertEquals("31 ", 213, res.getElement(3, 1));
        TestUtil.assertEquals("02 ", 48, res.getElement(0, 2));
        TestUtil.assertEquals("12 ", 128, res.getElement(1, 2));
        TestUtil.assertEquals("22 ", 208, res.getElement(2, 2));
        TestUtil.assertEquals("32 ", 288, res.getElement(3, 2));
        TestUtil.assertEquals("03 ", 57, res.getElement(0, 3));
        TestUtil.assertEquals("13 ", 137, res.getElement(1, 3));
        TestUtil.assertEquals("23 ", 217, res.getElement(2, 3));
        TestUtil.assertEquals("33 ", 297, res.getElement(3, 3));
        //TODO assertEquals(0, res.getDeterminant(), 0.1f);
    }

    public void testMultiply() {
        Matrix4 m1 = new Matrix4(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, -2,
                0, 0, 0, 1);
        Matrix4 m2 = new Matrix4(0, 0, 1, 0,
                0, 1, 0, 0,
                -1, 0, 0, 0,
                0, 0, 0, 1);
        Matrix4 result = m1.multiply(m2);
        //Referenz aus Internetrechner
        Matrix4 expected = new Matrix4(0, 0, 1, 0,
                0, 1, 0, 0,
                -1, 0, 0, -2,
                0, 0, 0, 1);
        //System.out.println("result=" + result.dump("\n"));
        TestUtil.assertMatrix4(expected, result);
    }

    /**
     * der koennte auch plattformunabhaengig sein.
     */
    public void testbuildRotationMatrix() {
        Quaternion pathrotation = new Quaternion(0, 1, 0, 1);
        Matrix4 m = Matrix4.buildRotationMatrix(pathrotation);
        // Die Referenz entstand halb aus nachrechnen und halb aus probieren.
        // Der Quaternion muss aber normalisiert sein. Darum sind keine
        // 2en mehr in der Matrix.
        Matrix4 expected = new Matrix4(0, 0, 1, 0,
                0, 1, 0, 0,
                -1, 0, 0, 0,
                0, 0, 0, 1);
        //System.out.println("rotationmatrix=" + m.dump("\n"));
        TestUtil.assertMatrix4(expected, m);

        Quaternion rotation = new Quaternion(0, 0, 0, 1);
        m = Matrix4.buildRotationMatrix(rotation);
        expected = currentmodelmatrix;
        // System.out.println("rotationmatrix=" + m.dump("\n"));
        //TODO falsch assertMatrix4(expected, m);

    }

    /**
     * Dieser Test ist eigentlich plattformunabhaengig. Aber der innenliegende multiply ist es nicht.
     * Da lassen wir den ganzen Test mal hier.
     */
    public void testbuildTransformationMatrix() {
        Vector3 pathposition = new Vector3(0, 0, -2);
        Quaternion pathrotation = new Quaternion(0, 1, 0, 1);
        Matrix4 transformmatrix = Matrix4.buildTransformationMatrix(pathposition, pathrotation);
        Matrix4 expected = new Matrix4(0, 0, 1, 0,
                0, 1, 0, 0,
                -1, 0, 0, -2,
                0, 0, 0, 1);
        //System.out.println("transformmatrix=" + transformmatrix.dump("\n"));
        TestUtil.assertMatrix4(expected, transformmatrix);
    }

    public void testExtractQuaternion() {
        // Das ist die Viewmatrix aus der Referencescene.
        Matrix4 m = new Matrix4(1, 0.0f, 0.0f, 0.0f,
                0.0f, 0.91036636f, -0.41380283f, -4.7683716E-7f,
                0.0f, 0.41380286f, 0.9103664f, -12.083045f,
                0.0f, 0.0f, 0.0f, 1.0f);

        Quaternion q = m.extractQuaternion();
        TestUtil.assertEquals("x1", 0.212f, q.getX());
        TestUtil.assertEquals("y1", 0, q.getY());
        TestUtil.assertEquals("z1", 0, q.getZ());
        TestUtil.assertEquals("w1", 0.977f, q.getW());

        q = currentmodelmatrix.extractQuaternion();
        TestUtil.assertEquals("x1", 0f, q.getX());
        TestUtil.assertEquals("y1", 0, q.getY());
        TestUtil.assertEquals("z1", 0, q.getZ());
        //TODO falsch? assertEquals("w1", 1.0f, q.getW(), TestUtil.floattesttolerance);

    }

    public void testRotationMatrix() {
        // und wieder die Rotation extrahieren
        Matrix3 rot = null;//tm.extractRotation();
        Matrix3 rotationexpected = new Matrix3(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1);
        TestUtil.assertMatrix3("extracted rotation", rotationexpected, rot);

    }

    /**
     * Aus der Pyramide im ReferenceTest.
     * der koennte auch plattformunabhaengig sein.
     */
    public void testbuildRotationMatrix2() {
        float xangle = (float) -Math.PI / 2;
        float yangle = (float) Math.PI / 2;
        Quaternion q =  Quaternion.buildFromAngles(xangle, yangle, 0);
        Matrix4 m = Matrix4.buildRotationMatrix(q);

        Matrix4 expected = new Matrix4(0, -1, 0, 0,
                0, 0, 1, 0,
                -1, 0, 0, 0,
                0, 0, 0, 1);
        System.out.println("testbuildRotationMatrix2.rotationmatrix=" + m.dump("\n"));
        TestUtil.assertMatrix4(expected, m);

        // Das muss die gleiche Matrix geben wie bei einer einzelnen z-Rotation. 14.4.16: Nein, das stimmt nicht! Siehe Lage Flag bei Pyramide.

        /*float zangle = (float) Math.PI / 2;
        q = new Quaternion(MathUtil2.buildQuaternionFromAngles(0, 0, zangle));
        m = Matrix4.buildRotationMatrix(q);
        System.out.println("testbuildRotationMatrix2.rotationmatrix(ueber z)=" + m.dump("\n"));
        TestUtil.assertMatrix4(expected, m);*/

    }

}