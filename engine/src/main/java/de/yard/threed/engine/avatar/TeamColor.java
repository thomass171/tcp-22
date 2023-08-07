package de.yard.threed.engine.avatar;

/**
 * Based on a well defined color for which a face texture exists.
 *
 * We have four (login)user colors. Monster have their own.
 */
public class TeamColor {

    public static TeamColor TEAMCOLOR_DARKGREEN = new TeamColor("darkgreen");
    public static TeamColor TEAMCOLOR_RED = new TeamColor("red");
    public static TeamColor TEAMCOLOR_BLUE = new TeamColor("blue");
    public static TeamColor TEAMCOLOR_GREEN = new TeamColor("green");

    public static TeamColor[] teamColors=new TeamColor[]{TEAMCOLOR_DARKGREEN, TEAMCOLOR_RED,TEAMCOLOR_BLUE,TEAMCOLOR_GREEN};

    private String color;

    public TeamColor(String color) {
        this.color=color;
    }

    public String getColor(){
        return color;
    }

    public static TeamColor getByIndex(int index){
        //TODO check index
        return teamColors[index];
    }
}
