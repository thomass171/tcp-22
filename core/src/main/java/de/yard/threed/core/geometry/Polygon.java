package de.yard.threed.core.geometry;

import java.util.ArrayList;
import java.util.List;

public class Polygon<T> {
    public final List<T> points = new ArrayList();
    public boolean closed=false;

    public Polygon(){
    }

    public void addPoint(T point) {
        points.add(point);
    }

    public int getPointCount() {
        return points.size();
    }

    public T getPoint(int i) {
        return points.get(i);
    }
}
