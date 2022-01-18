package de.yard.threed.tools;

public class ToolsUtils {

    public static java.awt.Color toAwtColor(de.yard.threed.core.Color color) {
        return new java.awt.Color(color.getRasInt(), color.getGasInt(), color.getBasInt(), color.getAlphaasInt());
    }
}
