package de.yard.threed.core;

/**
 * Created by thomass on 25.04.15.
 */
public class DimensionF {
    public double width,height;

    public DimensionF(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString(){
        return "width="+width+",height="+height;
    }

    @Override
    public boolean equals(Object o){
        DimensionF of = (DimensionF)o;
        return Math.abs(of.width-width) < 0.0001 && Math.abs(of.height-height) < 0.0001;
    }
}
