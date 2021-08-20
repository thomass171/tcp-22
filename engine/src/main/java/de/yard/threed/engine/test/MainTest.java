package de.yard.threed.engine.test;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Wrapper fuer Testaufrufe, die auch in den jeweiligen Platformen wie im Browser laufen sollen. Wird aber auch per JUnit aufgerufen, um die Tests für JME zu machen. 
 * 
 * 22.12.16: Es gibt auch eine MainTestScene, um die Tests als Scene in der Platform laufen zu lassen.
 * 23.1.18: In ReferenceScene aufgenommen. Damit ist die MainTestScene wohl obsolet.
 * <p/>
 * Created by thomass on 29.05.15.
 */
public class MainTest {
    public static void runTest(String testname) {
        Log logger = Platform.getInstance().getLog(MainTest.class);

        try {
            //logger.info("Running PathTest().testSegmentedPath");
            //new PathTest().testSegmentedPath();

            QuaternionTest quaternionTest = new QuaternionTest();
            logger.info("Running QuaternionTest.testNormalize()");
            quaternionTest.testNormalize();
            logger.info("Running QuaternionTest.testAngles()");
            quaternionTest.testAngles();
            logger.info("Running QuaternionTest.testVectorRotation()");
            quaternionTest.testVectorRotation();
            logger.info("Running QuaternionTest.testVectorRotationForExtrusion()");
            quaternionTest.testVectorRotationForExtrusion();

            XmlTest xmltest = new XmlTest();
            logger.info("Running XmlTest.testSampleXml()");
            xmltest.testSampleXml();

            ProjectionTest raytest = new ProjectionTest();
            logger.info("Running RayTest.testRay()");
            raytest.testRay();

            SceneTest scenetest = new SceneTest();
            logger.info("Running SceneTest.testTree()");
            scenetest.testTree();

            Base3DTest base3DTest = new Base3DTest();
            logger.info("Running Base3DTest.testTranslationAndRotation()");
            base3DTest.testTranslationAndRotation();
            logger.info("Running Base3DTest.testTranslationAndRotation2()");
            base3DTest.testTranslationAndRotation2();

            ViewTest viewtest = new ViewTest();
            logger.info("Running ViewTest.testCameraRotation()");
            viewtest.testCameraRotation();

            JavaUtilTest javautiltest = new JavaUtilTest();
            logger.info("Running JavaUtilTest.testList()");
            javautiltest.testList();
            logger.info("Running JavaUtilTest.testMap()");
            javautiltest.testMap();
            logger.info("Running JavaUtilTest.testMap2()");
            javautiltest.testMap2();
            logger.info("Running JavaUtilTest.testStringBuffer()");
            javautiltest.testStringBuffer();
            logger.info("Running JavaUtilTest.testSet()");
            javautiltest.testSet();
            logger.info("Running JavaUtilTest.testParse()");
            javautiltest.testParse();
            logger.info("Running ColorTest.testColor()");
            ColorTest colortest = new ColorTest();
            colortest.testColor();
            logger.info("Running StringUtilsTest.test1()");
            StringUtilsTest stringUtilsTest = new StringUtilsTest();
            stringUtilsTest.test1();
            logger.info("Running UtilTest.testFormat()");
            UtilTest utilTest = new UtilTest();
            utilTest.testFormat();

            GeometryTest geometryTest = new GeometryTest();
            //25.1.18 ist in ReferenceScenelogger.info("Running GeometryTest.testSimpleIntersection()");
            //geometryTest.testSimpleIntersection();
            //logger.info("Running GeometryTest.testIntersectionCenteredCuboid1()");
            //schwierig wegen alten nodes TODO geometryTest.testIntersectionCenteredCuboid1();
            /*moved to traffic to avoid dependency. simple one needed here JsonTest jsonTest = new JsonTest();
            logger.info("Running JsonTest.testSampleJson()");
            jsonTest.testSampleJson();*/

        } catch (RuntimeException e) {
            logger.error("Exception:" + e.getMessage() + e.toString());
            e.printStackTrace();
            throw e;
        }
    }
}
