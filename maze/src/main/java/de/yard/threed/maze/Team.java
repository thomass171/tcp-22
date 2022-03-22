package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.List;

public class Team {
    public int id;
    public List<Point> homeFields;

    public Team(int id, List<Point> homeFields) {
        this.id = id;
        this.homeFields = homeFields;
    }
}
