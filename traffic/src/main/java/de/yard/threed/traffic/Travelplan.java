package de.yard.threed.traffic;

import java.util.ArrayList;
import java.util.List;

/**
 * Eigentlich ein FlightPlan, aber letztendlich auch für z.B. Schiffe, Raumschiffe.
 * <p>
 * Es kann intermediate intermediateDestinations geben (analog Waypoints), was aber auch Holdings (Orbit) sein koennen.
 * Und die koennen auch unterwegs noch dazukommen.
 * <p>
 * Aber braucht man das wirklich? Die können doch immer on-the-fly berechnet werden.
 * Naja, es muss aber zumindest die naechste Destination irgendwo abgelegt werden.
 * Und man will ja auch wissen, wo das Vehicle dann gerade ist.
 * So gesehen kann ein Travelplan immer weiter fortgeschrieben werden.
 * <p>
 * See also {@link Destination}
 * <p>
 * 8.3.2020
 */
public class Travelplan {
    public Destination travelDestination;
    public List<Destination> intermediateDestinations = new ArrayList<Destination>();

    public Travelplan(Destination travelDestination) {
        this.travelDestination = travelDestination;
    }

    public void addDestination(Destination destination) {
        intermediateDestinations.add(destination);
    }

    public Destination getCurrentDestination() {
        return intermediateDestinations.get(intermediateDestinations.size() - 1);
    }
}
