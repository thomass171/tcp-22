package de.yard.threed.traffic;

import de.yard.threed.graph.*;
import de.yard.threed.traffic.config.VehicleConfig;


import java.util.ArrayList;
import java.util.List;

/**
 * Um Groundnet und Traffic besser zu trennen.
 * 28.3.20 Das ist aber eher fuer Roads geeignet, nicht so fue Railing.
 * 8.5.19
 */
public class TrafficGraph /*MA31 extends Graph */ {
    //SMOOTHINGRADIUS entspricht quasi dem Wendekreisradius aller Fahrzeuge.
    // Es eine kritische Groesse, von der wegen der daraus resultierenden Minimallänge viel abhängt. Und auch wegen Tests.
    // 26.4.18 Trotzdem muesste das irgendwie, zumindest optional, Vehicleabhängig sein.
    public static double SMOOTHINGRADIUS = 10;
    //26.5.17: considered to be a good value. Das haengt natürlich vom Winkel zwischen den Edges ab. Aber eine smartere Lösung gibts noch nicht.
    //28.7.17: Eben, und ein 20m Segment mit stumpfen Winkeln ist relativ lang. Vor allem wenns eng wird, z.B. am Servicepoint. Darum mal nur noch, sagen wir mal 8. Aber nur bei strict.
    public static double MINIMUMPATHSEGMENTLEN = 2 * SMOOTHINGRADIUS;
    //13.3.19: multilane kann man quasi jederzeit aktivieren und deaktivieren
    public boolean multilaneenabled = false;
    private int layerid = 0;
    //ein bischen ne Kruecke. Ist das nicht zu Groundnet? Eigentlich nicht. Naja, mal sehn.
    private GraphNode home;
    //16.5.19: Erstmal nur so
    public List<Long> tripnodes = new ArrayList<Long>();
    //28.3.20 Mal versuchen, diese Info hier unterzubringen
    public String icao;
    //10.3.21: Decoupling
    private Graph baseGraph;
    public static String RAILWAY="railway";
    public static String ROAD="road";

    public TrafficGraph(GraphValidator validator, GraphOrientation buildForZ0) {
        //super(validator,buildForZ0);
        baseGraph = new Graph(validator, buildForZ0);
    }

    public TrafficGraph(GraphOrientation buildForZ0) {
        //super(buildForZ0);
        baseGraph = new Graph(buildForZ0);
    }

    /**
     * 9.3.21: Wegen Trennung von Graph. Ist aber reichlich daemlich, weil es leichtfertig verwendet werden kann und dann z.B. icao verloren geht. Naja,
     * wird sich finden.
     *
     * @param graph
     */
    public TrafficGraph(Graph graph) {
        baseGraph = graph;
    }

    public TrafficGraph() {
        baseGraph = new Graph();
    }

    public GraphPath createPathFromGraphPosition(GraphPosition from, GraphNode to, GraphWeightProvider graphWeightProvider, VehicleConfig vehicleConfig) {
        int layer = newLayer();
        return createPathFromGraphPosition(from, to, graphWeightProvider, true, layer, vehicleConfig);
    }

    public GraphPath createPathFromGraphPosition(GraphPosition from, GraphNode to, GraphWeightProvider graphWeightProvider, boolean withsmooth, VehicleConfig vehicleConfig) {
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(MINIMUMPATHSEGMENTLEN, (vehicleConfig != null) ? vehicleConfig.getTurnRadius() : SMOOTHINGRADIUS);
        return GraphUtils.createPathFromGraphPosition(baseGraph, from, to, graphWeightProvider, graphPathConstraintProvider, newLayer(), withsmooth, false, getLaneInfo(vehicleConfig));
    }

    public GraphPath createPathFromGraphPosition(GraphPosition from, GraphNode to, GraphWeightProvider graphWeightProvider, boolean withsmooth, boolean allowrelocation, VehicleConfig vehicleConfig) {
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(MINIMUMPATHSEGMENTLEN, (vehicleConfig != null) ? vehicleConfig.getTurnRadius() : SMOOTHINGRADIUS);
        return GraphUtils.createPathFromGraphPosition(baseGraph, from, to, graphWeightProvider, graphPathConstraintProvider, newLayer(), withsmooth, allowrelocation, getLaneInfo(vehicleConfig));
    }

    public GraphPath createPathFromGraphPosition(GraphPosition from, GraphNode to, GraphWeightProvider graphWeightProvider, boolean withsmooth, int layer, VehicleConfig vehicleConfig) {
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(MINIMUMPATHSEGMENTLEN, (vehicleConfig != null) ? vehicleConfig.getTurnRadius() : SMOOTHINGRADIUS);
        return GraphUtils.createPathFromGraphPosition(baseGraph, from, to, graphWeightProvider, graphPathConstraintProvider, layer, withsmooth, false, getLaneInfo(vehicleConfig));
    }

    public GraphPath createBackPathFromGraphPosition(GraphNode startnode, GraphEdge startedge, TurnExtension backturn, GraphNode to, GraphWeightProvider graphWeightProvider, boolean withsmooth, VehicleConfig vehicleConfig) {
        int layer = newLayer();
        //TurnExtension backturn = GraphUtils.createBack(groundnetgraph, startnode, startedge, successor, layer);
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(MINIMUMPATHSEGMENTLEN, SMOOTHINGRADIUS);
        return GraphUtils.createBackPathFromGraphPosition(baseGraph, backturn, to, graphWeightProvider, graphPathConstraintProvider, layer, withsmooth, false, getLaneInfo(vehicleConfig));
    }

    public TurnExtension addTearDropTurn(GraphNode node, GraphEdge edge, boolean left) {
        return addTearDropTurn(node, edge, left, newLayer());
    }

    public TurnExtension addTearDropTurn(GraphNode node, GraphEdge edge, boolean left, int layer) {
        return GraphUtils.addTearDropTurn(baseGraph, node, edge, left, TrafficGraph.SMOOTHINGRADIUS, layer, false);
    }

    public int newLayer() {
        layerid++;
        return layerid;
    }

    public void setHome(GraphNode home) {
        this.home = home;
    }


    public GraphNode getHome() {
        return home;
    }


    /**
     * aircraft still move on the center line.
     *
     * @param vehicleConfig
     * @return
     */
    private GraphLane getLaneInfo(VehicleConfig vehicleConfig) {
        GraphLane laneinfo = null;
        if (multilaneenabled && (vehicleConfig == null || !vehicleConfig.getType().equals(VehicleComponent.VEHICLE_AIRCRAFT))) {
            //7 statt 17, denn je groesser der Abstand umso wahrscheinlicher Problem mit shorties smoothing
            // 10.6.18: doch besser 12, sonst ist der Abstand zu den Wings optisch zu dicht.
            laneinfo = new GraphLane(12);
        }
        return laneinfo;
    }


    public Graph getBaseGraph() {
        return baseGraph;
    }
}
