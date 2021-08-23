package de.yard.threed.tools;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class Decoration {
    public final Color color;
    public List<GeneralPath> paths = new ArrayList();
    public List<Text> texts = new ArrayList();

    Decoration(Color color) {
        this.color = color;
    }

    void add(GeneralPath path) {
        paths.add(path);
    }

    public void addText(Text text) {
        texts.add(text);
    }

    static class Text {
        public double x, y;
        public String text;

        public Text(double x, double y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }
    }
}
