package de.yard.threed.graph;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>
 * Created by thomass on 28.11.21.
 */
public class ExportImportTest {

    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine", "data"}, new SimpleHeadlessPlatformFactory());

    /**
     * Railing Beispiel 1
     */
    @Test
    public void testExportImportRailSample1() {
        float innerradius = RailingDimensions.innerarcradius;
        //float umfang = (float) (2 * Math.PI * innerradius);
        //float x = 50, y1 = 50, y2 = 150;

        Graph rails = RailingFactory.buildRailSample1(0);

        String xmlGraph = GraphExporter.exportToXML(rails, false, new ArrayList<>());
        System.out.println(xmlGraph);

        List<Long> tripNodes = new ArrayList<Long>();
        Graph imported = GraphFactory.buildfromXML(xmlGraph, tripNodes);

        assertEquals(rails.getNodeCount(), imported.getNodeCount());
        for (int i = 0; i < rails.getNodeCount(); i++) {
            GraphTestUtil.assertNode(rails.getNode(i).getName(),rails.getNode(i),imported.getNode(i));
        }
        assertEquals(rails.getEdgeCount(), imported.getEdgeCount());
        for (int i = 0; i < rails.getEdgeCount(); i++) {
            GraphTestUtil.assertEdge("",rails.getEdge(i),imported.getEdge(i));
        }
    }
}
