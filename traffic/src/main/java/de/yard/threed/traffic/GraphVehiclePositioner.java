package de.yard.threed.traffic;

import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphPosition;

public class GraphVehiclePositioner implements VehiclePositioner {
    TrafficGraph graph;
    GraphPosition position;

    public GraphVehiclePositioner(TrafficGraph graph, GraphPosition position) {
        this.graph = graph;
        this.position = position;
    }

    /**
     * // Code taken from former buildVehicleOnGraph() method
     *
     * @param vehicle
     */
    @Override
    public void positionVehicle(EcsEntity vehicle) {
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(vehicle);
        //MA31: navigator has no graph
        // projection is no longer needed here because Graph might be an extension of ProjectedGraph

                        /*no longer used?? if (projection != null && !(graph.getBaseGraph() instanceof ProjectedGraph)) {
                            throw new RuntimeException("should use ProjectedGraph");
                        }*/
        gmc.setGraph((graph == null) ? null : graph.getBaseGraph(), position, null/*projection*/);

    }

    @Override
    public SceneNode getDestinationNode() {
        // has always been attached to world without intermediate node, but on two different ways. Now use sphere node.
        //return Scene.getCurrent().getWorld();
        return SphereSystem.getSphereNode();
    }
}
