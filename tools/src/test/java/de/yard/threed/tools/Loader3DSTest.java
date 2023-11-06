package de.yard.threed.tools;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.loader.Object3DS;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;


/**
 * Created by thomass on 08.02.16.
 */
public class Loader3DSTest {
    static Platform platform = ToolsPlatform.init();


    @Test
    public void test3DS() throws Exception {
        Loader3DS shuttle;
        String srcfile = "tools/src/test/resources/shut.3ds";

        InputStream inputStream = new FileInputStream(TestUtils.locatedTestFile(srcfile));
        shuttle = new Loader3DS(new ByteArrayInputStream(new SimpleByteBuffer(IOUtils.toByteArray(inputStream))));

        System.out.println(shuttle.loadedfile.dumpMaterial("\n"));
        Assertions.assertEquals(1, shuttle.loadedfile.objects.size());
        Object3DS shutlayer = (Object3DS) shuttle.loadedfile.objects.get(0);
        Assertions.assertEquals(9876, shutlayer.vertices.size(), "vertices");
        Assertions.assertEquals(9876, shutlayer.texcoords.size(), "texcoords");
        Assertions.assertEquals(42, shutlayer.getFaceLists().size(), "face lists");
        // List<Face> facelist = shutlayer.getFaceLists().get(0);
        // assertEquals("faces", 19408, facelist.size());

    }

    @Test
    public void testShuttle() {
        try {
            String srcfile = "tools/src/test/resources/shut.3ds";

            InputStream inputStream = new FileInputStream(TestUtils.locatedTestFile(srcfile));
            Loader3DS shuttle = new Loader3DS(new ByteArrayInputStream(new SimpleByteBuffer(IOUtils.toByteArray(inputStream))));
            System.out.println(shuttle.loadedfile.dumpMaterial("\n"));
            Assertions.assertEquals(1, shuttle.loadedfile.objects.size());
            LoadedObject shutlayer = shuttle.loadedfile.objects.get(0);
            Assertions.assertEquals(9876, shutlayer.vertices.size(), "vertices");
            Assertions.assertEquals(42, shutlayer.getFaceLists().size(), "face lists");
            List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(shutlayer.vertices, shutlayer.getFaceLists(), null, true, null, false, null);
            Assertions.assertEquals(42, geolist.size(), "geolist size");
            Assertions.assertEquals(2072 * 3, geolist.get(0).getIndices().length, "geolist 0 faces size");
            Assertions.assertEquals(572, geolist.get(0).getVertices().size(), "geolist 0 vertices size");
            Assertions.assertEquals(1926 * 3, geolist.get(2).getIndices().length, "geolist 2 faces size");
            Assertions.assertEquals(572, geolist.get(2).getVertices().size(), "geolist 2 vertices size");

            //Einfache Plausipruefung, ob alle Indizes passen
            for (SimpleGeometry g : geolist) {
               /*TODO for (Face gface : g.getFaces().faces) {
                    Face3 face = (Face3) gface;
                    g.getVertices().get(face.index0);
                    g.getVertices().get(face.index1);
                    g.getVertices().get(face.index2);
                }*/
            }

        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading 3ds", e);
        }
    }




}
