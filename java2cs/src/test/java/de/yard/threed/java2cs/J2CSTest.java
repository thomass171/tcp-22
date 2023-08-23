package de.yard.threed.java2cs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Anders als der Name sagt sind das keine Swift Tests, sondern die ganz normalen.
 * <p>
 * Created by thomass on 29.02.16.
 */

public class J2CSTest {
    private static final String TESTFILE1 = "testfiles/Test1.java";
    private static final String TESTFILE2 = "testfiles/Test2.java";
    private static final String TESTFILE1o = "testfiles/Test1.cs";
    private static final String TESTFILE3 = "testfiles/PixelHandler.java";
    private static final String TESTFILE3o = "testfiles/PixelHandler.cs";
    private static final String TESTFILE4 = "testfiles/AsyncJobDelegate.java";
    private static final String TESTFILE4o = "testfiles/AsyncJobDelegate.cs";

    @Test
    public void testConvert() {
        String classpath = System.getProperty("java.class.path");
        System.out.println("classpath=" + classpath);

        callConverter(TESTFILE1);
        assertFile(TESTFILE1o, "output/" + TESTFILE1o);
    }

    @Test
    public void testConvertPixelHandler() {
        callConverter(TESTFILE3);
        assertFile(TESTFILE3o, "output/" + TESTFILE3o);
    }

    @Test
    public void testAsyncJobDelegate() {
        callConverter(TESTFILE4);
        assertFile(TESTFILE4o, "output/" + TESTFILE4o);
    }

    /**
     * Die 5 error sind: 2xref, assert, in, is
     */
    @Test
    public void testError() {
        J2CS j2s = callConverter(TESTFILE2);
        assertEquals(5, j2s.errorcnt, "errorcnt");
        // assertFile(TESTFILE1o, "output/" + TESTFILE1o);
    }

    private J2CS callConverter(String filename) {
        try {
            // Its sometimes confusing that test knowninterfaces/knowngenerics differs from real life, but using those from main add additional dependencies to test files.
            return J2CS.main1(new String[]{"-src", "src/test/java/de/yard/threed/java2cs", "-target", "output", "-f", filename}, buildKnownInterfaces(), buildKnownGenericMethods());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertFile(String expected, String actual) {
        try {
            String expectedcontent = new String(Files.readAllBytes(Paths.get("src/test/resources").resolve(expected)));
            String actualcontent = new String(Files.readAllBytes(Paths.get(actual)));
            assertEquals(expectedcontent, actualcontent, "The files differ:" + actual);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private KnownInterfaces buildKnownInterfaces() {
        KnownInterfaces knownInterfaces = new KnownInterfaces();
        knownInterfaces.add("MyInterface", new String[]{"run"});
        knownInterfaces.add("Gee", new String[]{"run1", "addFuture"});
        //21.6.20: Handle FunctionalInterface
        knownInterfaces.add("PixelHandler", "handlePixel");
        knownInterfaces.add("AsyncJobDelegate", "completed");
        return knownInterfaces;
    }

    private KnownGenericMethods buildKnownGenericMethods() {
        KnownGenericMethods knowngenericmethods = new KnownGenericMethods();
        //23.6.20: asyncContentLoad is only generic in test
        knowngenericmethods.add("asyncContentLoad", new String[]{"PlatformAsyncCallback"});
        knowngenericmethods.add("completed", new String[]{"AsyncJobDelegate"});
        knowngenericmethods.add("addFuture", new String[]{"??"});
        //knowngenericmethods.add("modelToJson", new String[]{"??"});
        return knowngenericmethods;
    }
}
