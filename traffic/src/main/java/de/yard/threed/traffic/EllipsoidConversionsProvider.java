package de.yard.threed.traffic;

import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.trafficcore.EllipsoidCalculations;

public class EllipsoidConversionsProvider implements DataProvider {
    EllipsoidCalculations ellipsoidCalculations;

    public EllipsoidConversionsProvider(EllipsoidCalculations ellipsoidCalculations) {
        this.ellipsoidCalculations = ellipsoidCalculations;
    }

    @Override
    public Object getData(Object[] parameter) {
        return ellipsoidCalculations;
    }
}
