package de.yard.threed.maze.testutils;

import de.yard.threed.engine.avatar.TeamColor;
import de.yard.threed.maze.GridTeam;
import de.yard.threed.maze.StartPosition;

public class ExpectedGridTeam extends GridTeam {

    public TeamColor expectedTeamColor;

    public ExpectedGridTeam(StartPosition[] startPosition, boolean isMonsterTeam, TeamColor expectedTeamColor) {
        super(startPosition, isMonsterTeam);
        this.expectedTeamColor = expectedTeamColor;
    }
}
