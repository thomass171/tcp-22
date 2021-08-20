package de.yard.threed.core;

/**
 * Created by thomass on 25.04.15.
 */
public class Dimension {
    public int width,height;

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString(){
        return "width="+width+", height="+height;
    }
}
