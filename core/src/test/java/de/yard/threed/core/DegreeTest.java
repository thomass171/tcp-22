package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;

/**
 *
 * <p>
 * Created by thomass on 07.3.17.
 */
public class DegreeTest {
    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @Test
    public void test1() {
        Degree d = new Degree(6.580982);
        TestUtil.assertEquals("", "6.580982", d.toString());

        d = Degree.parseDegree("N50 52.011847");
        TestUtil.assertFloat("", 50.866864f, d.getDegree());

        d = Degree.parseDegree("N50 51.219783");
        TestUtil.assertFloat("", 50.853663f, d.getDegree());

        d = Degree.parseDegree("E7 9.861116");
        TestUtil.assertFloat("", 7.164352f, d.getDegree());

        d = Degree.parseDegree("E7 9.861116");
        TestUtil.assertFloat("", 7.164352f, d.getDegree());
        
    }


}
