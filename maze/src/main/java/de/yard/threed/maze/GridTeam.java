package de.yard.threed.maze;

import de.yard.threed.core.Point;

import java.util.ArrayList;
import java.util.List;

public class GridTeam {

    public boolean isMonsterTeam;
    public List<StartPosition> positions = new ArrayList<StartPosition>();

    public GridTeam(StartPosition startPosition, boolean isMonsterTeam) {
        positions.add(startPosition);
        this.isMonsterTeam = isMonsterTeam;
    }

    public GridTeam(StartPosition[] startPositions, boolean isMonsterTeam) {
        for (StartPosition p:startPositions){
            this.positions.add((p));
        }
        this.isMonsterTeam = isMonsterTeam;
    }

    public boolean canExtend(Point p) {
        for (StartPosition point : positions) {
            if (Point.getDistance(point.p, p) == 1) {
                return true;
            }
        }
        return false;
    }

    public void add(StartPosition startPosition) {
        positions.add(startPosition);
    }
}
