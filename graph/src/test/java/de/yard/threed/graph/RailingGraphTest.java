package de.yard.threed.graph;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.RailingBranchSelector;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.graph.RailingDimensions;
import de.yard.threed.graph.RailingFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.core.testutil.TestUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests, die wirklich Railing spezifisch sind und nicht generisch fuer Graphen.
 * <p>
 * Created by thomass on 28.11.16.
 */
public class RailingGraphTest {
    //static Platform platform = TestFactory.initPlatformForTest(false,false,null,true);
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine","data"}, new SimpleHeadlessPlatformFactory());

    /**
     * Railing Beispiel 1 mit Extensions
     */
    @Test
    public void testRailSample1_0() {
        float innerradius = RailingDimensions.innerarcradius;
        float umfang = (float) (2 * Math.PI * innerradius);
        float x = 50, y1 = 50, y2 = 150;
        
        Graph rails = RailingFactory.buildRailSample1(0);
        TestUtil.assertEquals("Anzahl edges", 12 + 7 + 4, rails.getEdgeCount());
        TestUtil.assertEquals("Anzahl nodes", 12 + 6 + 3, rails.getNodeCount());
        GraphNode outerlinksoben = rails.findNodeByName("o2");
        TestUtil.assertEquals("edges an inner links oben node", 2, outerlinksoben.edges.size());
        GraphNode innerlinksoben = rails.findNodeByName("i2");
        TestUtil.assertEquals("edges an inner links oben node", 2, innerlinksoben.edges.size());

        RailingBranchSelector railingselector = new RailingBranchSelector();
         GraphPosition railingpos = new GraphPosition(rails.getEdge(0));
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(rails,railingpos,null);
        gmc.setSelector(railingselector);
        TestUtil.assertVector3("", new Vector3(x, 0, -y1 + innerradius), railingpos.get3DPosition());
        gmc.moveForward(umfang/4+(y2-y1));
        // fuehrt ans Ende der ersten Gerade (wenn die Weiche auf Gerade steht)
        TestUtil.assertVector3("", new Vector3(x - innerradius, 0, -y2), railingpos.get3DPosition());
        gmc.moveForward(umfang/4);
        // ganz oben
        TestUtil.assertVector3("", new Vector3(x, 0, -y2 - innerradius), railingpos.get3DPosition());
    }

    @Test
    public void testBranch() {
        Graph graph = RailingFactory.buildRailSample2();
        TestUtil.assertEquals("Anzahl edges", 1, graph.getEdgeCount());
        GraphEdge branch = RailingFactory.addBranch(graph, graph.getEdge(0), 6, false, false, null);
        TestUtil.assertEquals("Anzahl edges", 3, graph.getEdgeCount());
        TestUtil.assertVector3("branch from", new Vector3(50, 0, -56), branch.from.getLocation());
        // Werte durch ausprobieren
        TestUtil.assertVector3("branch to", new Vector3(52, 0, -58.23607f), branch.to.getLocation());

        branch = RailingFactory.addBranch(graph, graph.getEdge(0), 3, true, false, null);
        TestUtil.assertEquals("Anzahl edges", 5, graph.getEdgeCount());
        TestUtil.assertVector3("branch from", new Vector3(50, 0, -53), branch.from.getLocation());
        // Werte durch ausprobieren
        TestUtil.assertVector3("branch to", new Vector3(48, 0, -55.23607f), branch.to.getLocation());

    }

}
