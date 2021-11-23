package de.yard.threed.engine.ecs;

import de.yard.threed.engine.ViewPoint;

import java.util.ArrayList;
import java.util.List;

public class ViewPointProvider implements DataProvider {
    List<ViewPoint> vps = new ArrayList<ViewPoint>();

    public ViewPointProvider(List<ViewPoint> vps) {
        this.vps = vps;
    }

    public static ViewPointProvider fromList(List<ViewPoint> vps) {
        return new ViewPointProvider(vps);
    }

    @Override
    public Object getData(Object[] parameter) {
        return vps;
    }
}
