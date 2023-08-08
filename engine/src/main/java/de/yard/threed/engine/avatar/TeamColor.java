package de.yard.threed.engine.avatar;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Based on a well defined color for which a face texture exists.
 * <p>
 * We have four (login)user colors. Monster have their own.
 */
public class TeamColor {

    public static TeamColor TEAMCOLOR_DARKGREEN = new TeamColor("darkgreen");
    public static TeamColor TEAMCOLOR_RED = new TeamColor("red");
    public static TeamColor TEAMCOLOR_BLUE = new TeamColor("blue");
    public static TeamColor TEAMCOLOR_GREEN = new TeamColor("green");

    public static TeamColor[] teamColors = new TeamColor[]{TEAMCOLOR_DARKGREEN, TEAMCOLOR_RED, TEAMCOLOR_BLUE, TEAMCOLOR_GREEN};

    private String color;

    public TeamColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public static TeamColor getByIndex(int index) {
        //TODO check index
        return teamColors[index];
    }

    public static TeamColor fromName(String name) {
        if (name.equals("darkgreen")) return TEAMCOLOR_DARKGREEN;
        if (name.equals("red")) return TEAMCOLOR_RED;
        if (name.equals("blue")) return TEAMCOLOR_BLUE;
        if (name.equals("green")) return TEAMCOLOR_GREEN;
        getLogger().warn("team color not found: " + name);
        return null;
    }

    private static Log getLogger() {
        return Platform.getInstance().getLog(TeamColor.class);
    }
}
