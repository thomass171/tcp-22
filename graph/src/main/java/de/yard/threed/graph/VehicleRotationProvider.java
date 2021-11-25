package de.yard.threed.graph;

import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.graph.RotationProvider;

public class VehicleRotationProvider implements RotationProvider {
    GraphMovingComponent gmc;

    public VehicleRotationProvider(GraphMovingComponent gmc){
        this.gmc=gmc;
    }
    @Override
    public Quaternion get3DRotation() {
        GraphPosition cp = gmc.getCurrentposition();

return         (gmc.graph.orientation.get3DRotation(cp.reverseorientation, cp.currentedge.getEffectiveDirection(cp.getAbsolutePosition()), cp.currentedge));
    }
}
