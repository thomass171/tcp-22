package de.yard.threed.traffic.flight;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.graph.*;
import de.yard.threed.traffic.FgVehicleSpace;

public class AircraftVehicleRotation implements GraphVehicleRotation {
    Log logger = Platform.getInstance().getLog(AircraftVehicleRotation.class);

    Degree fullRollOnArc;
    double rampupDistance;

    public AircraftVehicleRotation(Degree fullRollOnArc, double rampupDistance) {
        this.fullRollOnArc = fullRollOnArc;
        this.rampupDistance = rampupDistance;
    }

    @Override
    public Quaternion getVehicleOrientation(GraphPosition graphPosition, Graph graph) {
        Quaternion basicRotation = FgVehicleSpace.getFgVehicleForwardRotation();

        // FG: "the x-axis runs lengthwise, towards the back"
        Degree roll = new Degree(0);
        if (edgeWithRolling(graphPosition.currentedge)) {
            roll = getRollForPosition(graphPosition, graph);
        }
        return basicRotation.multiply(Quaternion.buildRotationX(roll));
    }

    public Degree getRollForPosition(GraphPosition graphPosition, Graph graph) {
        Degree roll = getRollForPosition(graphPosition.getAbsolutePosition(), graphPosition.currentedge.getLength(),
                graphPosition.getEdge().arcDirectionLeft(true, graph.getGraphOrientation()), graphPosition.getEdge().getArc().getRadius());
        logger.debug("roll=" + roll + ",arcLen=" + graphPosition.getEdge().getLength() + ",pos=" + graphPosition.edgeposition + ",arcRadius=" + graphPosition.getEdge().getArc().getRadius());
        return roll;
    }

    public Degree getRollForPosition(double pos, double edgeLength, Boolean arcToLeft, double arcRadius) {
        double fraction = 1.0;
        double effectiveRampupDistance = rampupDistance;
        if (rampupDistance > edgeLength / 2.0){
           // no full roll possible
           effectiveRampupDistance = edgeLength / 2.0;
           fraction = effectiveRampupDistance / rampupDistance;
        }
        // Does not work with 'reverseOrientation'? and subsequent arcs are problematic.
        if (pos < effectiveRampupDistance) {
            // even with shorter rampupDistance use rampupDistance to avoid reaching full roll in this case
            fraction = pos / rampupDistance;
        } else {
            if (pos > edgeLength - effectiveRampupDistance) {
                fraction = (edgeLength - pos) / rampupDistance;
            }
        }
        Degree roll = new Degree(fraction * fullRollOnArc.getDegree() * getRollFactor(arcRadius));

        if (arcToLeft == null) {
            logger.warn("Rolling edge defined for non arc?");
            roll = new Degree(0);
        } else {
            if (!arcToLeft) {
                roll = roll.negate();
            }
        }

        return roll;
    }

    private double getRollFactor(double arcRadius) {
        // assume fzll roll with radius <= 200, but lower the higher the radius
        double factor = 200.0 / arcRadius;
        if (factor > 1.0) {
            factor = 1.0;
        }
        return factor;
    }

    public boolean edgeWithRolling(GraphEdge edge) {
        if (edge.needsRolling) {
            //logger.debug("Rolling in arc with beta " + edge.arcParameter.beta);
            return true;
        }
        return false;
    }
}
