package de.yard.threed.tools;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.loader.StringReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * 9.3.21: From engine due to "genie*" (licence?), etc. .
 *
 * Created by thomass on 08.02.16.
 */
public class LoaderOBJTest {
    static Platform platform = ToolsPlatform.init();

    //geht noch nicht wegen face3 @Test
    public void testOBJ() {
        try {
            //InputStream isType = FileReader.getFileStream("src/main/resources/osm/Essen.obj");
            //InputStream ins = FileReader.getFileStream(new BundleResource("model/genielamp/genie lamp 05.obj"));
            //LoaderOBJ obj = new LoaderOBJ(ins);
//            assertEquals("",1,obj.objects.size());
            //          assertEquals("",1,obj.objects.get(0).kids.size());
            //        assertEquals("facelists",1,obj.objects.get(0).kids.get(0).getFaceLists().size());
            //      assertEquals("face4",1,obj.objects.get(0).kids.get(0).getFaceLists().get(0).size());

        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading obj", e);
        }
    }

    @Test
    public void testOBJsample() {
        try {
            //ByteArrayInputStream ins = new ByteArrayInputStream(LoaderOBJ.sampleobj.getBytes());
            LoaderOBJ obj = new LoaderOBJ(new StringReader(LoaderOBJ.sampleobj));
            Assertions.assertEquals( 1, obj.loadedfile.objects.size());
            Assertions.assertEquals( 0, obj.loadedfile.objects.get(0).kids.size());
            Assertions.assertEquals( 1, obj.loadedfile.objects.get(0).getFaceLists().size(),"facelists");
            Assertions.assertEquals( 12, obj.loadedfile.objects.get(0).getFaceLists().get(0).faces.size(),"face3");
            LoadedObject o = obj.loadedfile.objects.get(0);
            // vorletztes pruefen (       "f 3//5 7//5 4//5\n" )
            Face3 face3 = (Face3) obj.loadedfile.objects.get(0).getFaceLists().get(0).faces.get(10);
            TestUtils.assertVector3(new Vector3(-1, -1, 1), o.vertices.get(face3.index0));
            TestUtils.assertVector3(new Vector3(-1, 1, 1), o.vertices.get(face3.index1));
            TestUtils.assertVector3(new Vector3(-1, -1, -1), o.vertices.get(face3.index2));
           /*14.7.16 Gibts nicht mehr bzw wird nicht in Face uebernommen  TestUtil.assertVector3(new Vector3(-1, 0, 0), face3.normal/*s.get(0));
            TestUtil.assertVector3(new Vector3(-1, 0, 0), face3.normals.get(1));
            TestUtil.assertVector3(new Vector3(-1, 0, 0), face3.normals.get(2));*/
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading obj", e);
        }
    }






}
