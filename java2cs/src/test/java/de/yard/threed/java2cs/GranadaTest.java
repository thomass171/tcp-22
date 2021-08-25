package de.yard.threed.java2cs;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomass on 29.02.16.
 *
 * Nur noch, bis das ganze ins Skript verschoben ist.
 */

public class GranadaTest {
    /**
     * Das ist z.Z. der eigentliche Konverter und kein Test
     */
    @Test
    public void testGranada() {
        try {
            /*J2Swift j2s = J2Swift.main1(new String[]{"-src", "/Users/thomass/Projekte/Granada/desktop/src/main/java",
                    "-target", "/Users/thomass/Projekte/Unity/Granada/Assets/Granada-Generated",
                });
            assertEquals("errorcnt",0,j2s.errorcnt);*/
            //14.12.17: was ist denn mit dem CP zu log4j.properties? Ist das ein Intellij Bug? Seit wann? Geloest mit -Dlog4j.configuration
            String classpath = System.getProperty("java.class.path" );
            //System.out.println("classpath="+classpath);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream ins = cl.getResourceAsStream("log4j.properties");
             ins = cl.getResourceAsStream("testfiles/Test1.cs");
            //assertNotNull(ins);
/**   29.3.21


            
            //14.11.18: Module sandbox
            j2s = J2Swift.main1(new String[]{"-src", "/Users/thomas/Projekte/Granada/sandbox/src/main/java",
                    "-exclude", "",
                    "-target", "/Users/thomas/Projekte/Unity/Granada/Assets/Granada-Generated",
                    "de/yard/threed/apps/tube",
                    //My737 lass ich erstmal weg wegen uninteressant
                    "de/yard/threed/apps",
                    "de/yard/threed/apps/voffice",
                    "de/yard/threed/apps/voffice/g3d",
                    "de/yard/threed/apps/voffice/model",
                    "de/yard/threed/apps/voffice/util",
                    "de/yard/threed/apps/ShowroomScene.java",
                    "de/yard/threed/client/ModelViewScene.java",
            },buildKnownInterfaces(), buildKnownGenericMethods());
            assertEquals("errorcnt",0,j2s.errorcnt);

            //14.11.18: Module railing
            j2s = J2Swift.main1(new String[]{"-src", "/Users/thomas/Projekte/Granada/railing/src/main/java",
                    "-exclude", "de/yard/threed/apps/railing/Cp.java",
                    "-target", "/Users/thomas/Projekte/Unity/Granada/Assets/Granada-Generated",
                    "de/yard/threed/apps/railing",
            },buildKnownInterfaces(), buildKnownGenericMethods());
            assertEquals("errorcnt",0,j2s.errorcnt);

            //22.06.20: Module traffic-core
            j2s = J2Swift.main1(new String[]{"-src", "/Users/thomas/Projekte/Granada/traffic-core/src/main/java",
                    "-exclude", "",
                    "-target", "/Users/thomas/Projekte/Unity/Granada/Assets/Granada-Generated",
                    "de/yard/threed/traffic/model",
            },buildKnownInterfaces(), buildKnownGenericMethods());
            assertEquals("errorcnt",0,j2s.errorcnt);
 **/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Konvertieren der Tests.
     * 
     * 5.12.18: Ich weiss nicht mehr wofür das ist. Die laufen doch nicht in Unity, oder nicht mehr.
     * Und sind auch lange nicht mehr gebaut worden. Und lassen sich auch nicht mehr unbedingt in C# compilieren.
     * Ich lösch die Unity mal komplett raus.
     * 29.6.20: Wegen Maintest.java?
     */
    @Test
    public void testGranadaTest() {
        try {
           /**29.3.21  J2Swift j2s = J2Swift.main1(new String[]{"-src", "/Users/thomas/Projekte/Granada/core/src/test/java",
                    "-target", "/Users/thomas/Projekte/Unity/Granada/Assets/Granada-Generated-Test",
                    //"de/yard/threed",
                    "de/yard/threed/PlatformTest.java",
                    "de/yard/threed/apps",
                    // platform enthaelt nur Klassen für Java(z.B. die TestFactory)
                    //"de/yard/threed/platform",
                    "de/yard/threed/engine",
                    //"de/yard/threed/loader",
                    "de/yard/threed/util",

            },buildKnownInterfaces(), buildKnownGenericMethods());
            assertEquals("errorcnt",0,j2s.errorcnt);
**/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
