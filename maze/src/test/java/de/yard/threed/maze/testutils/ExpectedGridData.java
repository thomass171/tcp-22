package de.yard.threed.maze.testutils;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.GridTeam;
import de.yard.threed.maze.StartPosition;

import java.util.ArrayList;
import java.util.List;

public class ExpectedGridData {

    public GridTeam[] expectedTeams;
    public List<GridTeam> expectedLoginTeams = new ArrayList<>();
    public List<GridTeam> expectedMonsterTeams = new ArrayList<>();
    public Point[] expectedBoxesLocations, expectedItemsLocations;

    public ExpectedGridData(GridTeam[] expectedTeams, Point[] expectedBoxesLocations, Point[] expectedItemsLocations) {
        this.expectedTeams = expectedTeams;
        this.expectedBoxesLocations = expectedBoxesLocations;
        this.expectedItemsLocations = expectedItemsLocations;

        for (GridTeam gridTeam : expectedTeams) {
            if (gridTeam.isMonsterTeam) {
                expectedMonsterTeams.add(gridTeam);
            } else {
                expectedLoginTeams.add(gridTeam);
            }
        }
    }

    public int getStartPositionCount(boolean ignoreMonster) {
        int cnt = 0;

        for (GridTeam gridTeam : expectedTeams) {
            if (ignoreMonster == false || !gridTeam.isMonsterTeam) {
                cnt += gridTeam.positions.size();
            }
        }
        return cnt;
    }

    public List<StartPosition> getExpectedMonsterPositions() {
        List<StartPosition> result = new ArrayList<StartPosition>();

        for (GridTeam gridTeam : expectedMonsterTeams) {
            result.addAll(gridTeam.positions);
        }
        return result;
    }

    public List<StartPosition> getExpectedLoginPositions() {
        List<StartPosition> result = new ArrayList<StartPosition>();

        for (GridTeam gridTeam : expectedLoginTeams) {
            result.addAll(gridTeam.positions);
        }
        return result;
    }

    public int getInitialPlayerTeam() {

        for (int i = 0; i < expectedTeams.length; i++) {
            if (!expectedTeams[i].isMonsterTeam) {
                return i;
            }
        }
        return -1;
    }


    public static ExpectedGridData buildForD_80x25(String pteamSize) {

        GridTeam playerTeam = new GridTeam(new StartPosition[]{
                new StartPosition(1, 2, GridOrientation.N),
                new StartPosition(1, 3, GridOrientation.N),
                new StartPosition(1, 4, GridOrientation.E)}, false);
        if (pteamSize != null) {

            switch (Util.parseInt(pteamSize)) {
                case 1:
                    playerTeam = new GridTeam(new StartPosition[]{new StartPosition(1, 2, GridOrientation.N)}, false);
                    break;
                default:
                    throw new RuntimeException("not yet");
            }
        }
        return new ExpectedGridData(
                new GridTeam[]{
                        new GridTeam(new StartPosition[]{new StartPosition(26, 1, GridOrientation.N)}, true),
                        playerTeam,
                        new GridTeam(new StartPosition[]{new StartPosition(10, 8, GridOrientation.N)}, true),
                        new GridTeam(new StartPosition[]{
                                new StartPosition(43, 10, GridOrientation.N),
                                new StartPosition(43, 11, GridOrientation.N)}, true),
                        new GridTeam(new StartPosition[]{new StartPosition(22, 14, GridOrientation.N)}, true),
                        new GridTeam(new StartPosition[]{new StartPosition(10, 15, GridOrientation.N)}, true),
                        new GridTeam(new StartPosition[]{
                                new StartPosition(6, 22, GridOrientation.N),
                                new StartPosition(6, 23, GridOrientation.E)}, true),

                },
                new Point[]{new Point(55, 9)},
                new Point[]{new Point(78, 3), new Point(43, 5), new Point(6, 20)});
    }

    public static ExpectedGridData buildForM_30x20() {
        return new ExpectedGridData(
                new GridTeam[]{
                        new GridTeam(new StartPosition[]{new StartPosition(28, 3, GridOrientation.N)}, false),
                        new GridTeam(new StartPosition[]{new StartPosition(21, 11, GridOrientation.N), new StartPosition(22, 11, GridOrientation.N), new StartPosition(23, 11, GridOrientation.N)}, true)
                },
                new Point[]{},
                new Point[]{}
        );
    }

    public static ExpectedGridData buildForP_simple(boolean replaceMonsterWithPlayer) {
        return new ExpectedGridData(
                new GridTeam[]{
                        new GridTeam(new StartPosition[]{new StartPosition(5, 1, GridOrientation.N)}, false),
                        new GridTeam(new StartPosition[]{new StartPosition(4, 4, GridOrientation.fromDirection("E"))}, !replaceMonsterWithPlayer)
                },
                new Point[]{},
                new Point[]{new Point(7, 1), new Point(3, 3)}
        );
    }
}
