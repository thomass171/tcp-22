package de.yard.threed.engine.test;


import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.testutil.RuntimeTestUtil;


/**
 * Created by thomass on 21.12.15.
 */
public class Base3DTest {
    Log logger = Platform.getInstance().getLog(Base3DTest.class);

    public void testTranslationAndRotation() {
        Matrix4 refrotation = new Matrix4(0.4330f, -0.7500f, 0.50f, 0,
                0.89628077f, 0.41721186f, -0.15038368f, 0f,
                -0.09581819f, 0.51325846f, 0.85286856f, 0,
                0, 0, 0, 1);
        TestBase3D model = new TestBase3D();

        checkLocalModelMatrix(model, new Matrix4(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1));
        model.rotateX(new Degree(10));
        model.rotateY(new Degree(30));
        model.rotateZ(new Degree(60));
        checkLocalModelMatrix(model, refrotation);

        model.translateX(2.7f);
        model.translateY(5.7f);
        model.translateZ(-12.7f);
        //Hier wurde erst rotiert und dnan translated. Damit entspricht die Position nicht mehr der Translation!
         checkLocalModelMatrix(model, new Matrix4(0.433f, -0.75f, 0.5f, -9.456f,
                0.896f, 0.417f, -0.1504f, 6.708f,
                -0.096f, 0.513f, 0.852f, -8.164f,
                0, 0, 0, 1));

        // Die Rotationreferenzweret stammen aus JME, sind aber identisch zu ThreeJS.
        // Dann sind Quaternions doch nicht plattformabhängig.
        Quaternion rotation = model.getRotation();
        RuntimeTestUtil.assertEquals("x1", 0.20182428f, rotation.getX());
        RuntimeTestUtil.assertEquals("y1", 0.18119794f, rotation.getY() );
        RuntimeTestUtil.assertEquals("z1", 0.50066054f, rotation.getZ());
        RuntimeTestUtil.assertEquals("w1", 0.82205427f, rotation.getW());

        Vector3 v = new Vector3(1, 2, 3).rotate(rotation);
        RuntimeTestUtil.assertVector3("rotated", new Vector3(0.43301272f, 1.2795533f, 3.489304f), v);

        // Durch setzen der eigenen Rotation sollte alles beim alten bleiben
        model.setRotation(rotation);
        checkLocalModelMatrix(model, new Matrix4(0.4330126941204071f, -0.75f, 0.5f, -9.455865859985352f,
                0.8962805867195129f, 0.41721200942993164f, -0.15038372576236725f, 6.707939147949219f,
                -0.09581820666790009f, 0.5132583379745483f, 0.8528685569763184f, -8.164566993713379f,
                0, 0, 0, 1));

    }

    public void testTranslationAndRotation2() {
        Matrix4 refrotation = new Matrix4(0.4330f, -0.7500f, 0.50f, 0,
                0.89628077f, 0.41721186f, -0.15038368f, 0f,
                -0.09581819f, 0.51325846f, 0.85286856f, 0,
                0, 0, 0, 1);
        TestBase3D model = new TestBase3D();

        checkLocalModelMatrix(model, new Matrix4(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1));
        model.rotateX(new Degree(10));
        model.rotateY(new Degree(30));
        model.rotateZ(new Degree(60));
        checkLocalModelMatrix(model, refrotation);

        Vector3 position = new Vector3(2.7f, 5.7f, -12.7f);
        model.setPosition(position);
        checkLocalModelMatrix(model, Matrix4.buildTranslationMatrix(position).multiply(refrotation));

        Vector3 scale = new Vector3(2, 3, 4);
        model.setScale(scale);
        logger.debug("scalematrix="+Matrix4.buildScaleMatrix(scale).dump("\n"));
        checkLocalModelMatrix(model, (Matrix4.buildTranslationMatrix(position).multiply(refrotation)).multiply(Matrix4.buildScaleMatrix(scale)));

        // Die Rotationreferenzweret stammen aus JME, sind aber identisch zu ThreeJS.
        // Dann sind Quaternions doch nicht plattformabhängig.
        Quaternion rotation = model.getRotation();
        RuntimeTestUtil.assertEquals("x1", 0.20182428f, rotation.getX());
        RuntimeTestUtil.assertEquals("y1", 0.18119794f, rotation.getY());
        RuntimeTestUtil.assertEquals("z1", 0.50066054f, rotation.getZ());
        RuntimeTestUtil.assertEquals("w1", 0.82205427f, rotation.getW());

        Vector3 v = new Vector3(1, 2, 3).rotate(rotation);
        RuntimeTestUtil.assertVector3("rotated", new Vector3(0.43301272f, 1.2795533f, 3.489304f), v);

        // Durch setzen der eigenen Rotation sollte alles beim alten bleiben
        model.setRotation(rotation);
        //2.1.16: Hmm wirklich? Scale wurde doch für die Ermittlung der Rotation rausgerechnet.
        // checkLocalModelMatrix(model,  Matrix4.buildTranslationMatrix(position).multiply(refrotation));
    }

    private void checkLocalModelMatrix(Transform model, Matrix4 expected) {
        Matrix4 m = model.getLocalModelMatrix();

        logger.debug("modelmatrix=" + m.dump("\n"));
        RuntimeTestUtil.assertMatrix4(expected, m);

    }
}

class TestBase3D extends Transform {
   public TestBase3D() {
        super( Platform.getInstance().buildModel("name").getTransform());
    }

}
