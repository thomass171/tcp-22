package de.yard.threed.graph;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.graph.Visualizer;

/**
 * Das Aussehen eines Graph kann sehr unterschiedlich sein, z.B. Strassen, Gleise, einfache Lnien, Tubes.
 * Obwohl die Methoden hier schon Terrainlasstig sind, oder?
 * 12.11.18: Die Methoden bekommen alle den Graph uebergeben, um ihn nicht selber vorhalten zu muessen. Evtl.
 * ändert er sich ja auch mal. Oder es gibt mehrere. Ja, richtig. Aber der Graph ist dem Aufrufer gar nicht
 * immer bekannt, z.B. bei PATH_COMPLETED. Darum mal doch nicht immer mitgegen.
 * <p>
 * Created by thomass on 24.11.16.
 */
public interface GraphVisualizer extends Visualizer<Graph> {

    /**
     * 18.11.20: destinationnode added
     * Manual CS remove of annotation needed?
     */
    @Override
    void visualize/*Graph*/(Graph graph, SceneNode destinationnode);

    // Ein Offset passend zur jeweiligen Visualisierung
    // 12.5.17: deprecated. Das soll doch das Model regeln, oder? Das kann es vielleicht nicht?
    // 1.3.18: Ich versuch es jetzt aber ohne. Immer den visualizer mitschleppen ist doof.
    // Railing hat das verwndet um anheben. Ist jetzt in EngineNode.
    Vector3 getPositionOffset();

    /**
     * Hervorhebung der Edges eines Path.
     * Ein visualizePath() kann nicht durch ein visualizeLayer() ersetzt werden,
     * weil ein Pfad ja nicht nur aus einem einzigen Layer bestehen muss.
     * Der Visiualize erfolgt auf der Graphdarstellung mit z.B. anderer Farbe.
     * Lifert die Nodes, damit der Auf
     *
     * @param path
     * @return
     */
    void/*List<SceneNode>*/ visualizePath(Graph graph, GraphPath path, SceneNode destinationnode);

    /**
     * einen hervorgehobenenen Path wieder entfernen.
     *
     * @param path
     */
    void removeVisualizedPath(Graph graph, GraphPath path);

    /**
     * Hervorhebung einer Edge. Der Visiualize erfolgt auf der Graphdarstellung mit z.B. anderer Farbe.
     *
     * @param edge
     * @return
     */
    SceneNode visualizeEdge(Graph graph, GraphEdge edge, SceneNode destinationnode);

    /**
     * Darstellung so wie auch der Grundgraph.
     *
     * @param g
     * @param layer
     */
    void addLayer(Graph g, int layer, SceneNode destinationnode);

    void removeLayer(Graph g, int layer);

    /**
     * 20.11.20: Die beiden gehoeren hier doch auch rein, oder?
     * 9.3.21: oder muessen die nur "visualize" statt "build" heissen, weil
     * sie keinen zusaetzlichen Edge bauen, sondern nur eine Node als Visual bauen.
     */

    SceneNode buildNode(Graph graph, GraphNode n);

    SceneNode buildEdge(Graph graph, GraphEdge edge);


}
