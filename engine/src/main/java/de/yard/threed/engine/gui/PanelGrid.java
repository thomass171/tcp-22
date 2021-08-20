package de.yard.threed.engine.gui;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.DimensionF;

public class PanelGrid {

    public double[] midx;
    // counts from bottom!
    public double[] midy;

    public PanelGrid(double width, double rowHeight, int rows, double[] colWidth) {
        DimensionF rowsize = new DimensionF(width, rowHeight);
        double midElementWidth4 = width / 4;
        double m4_2 = midElementWidth4 / 2;
        double h = rowHeight * rows;
        double h2 = rowHeight * rows / 2;
        double rh = rowHeight;
        double rh2 = rowHeight / 2;
        midx = new double[colWidth.length];
        midy = new double[rows];

        for (int i = 0; i < colWidth.length; i++) {
            midx[i] = colWidth[i] / 2 - width / 2;
            for (int j = 0; j < i; j++) {
                midx[i] += colWidth[j];
            }
        }
        for (int i = 0; i < rows; i++) {
            midy[i] = rh2 - h2;
            for (int j = 0; j < i; j++) {
                midy[i] += rowHeight;
            }
        }
    }

    public Vector2 getPosition(int x, int y) {
        return new Vector2(midx[x],midy[y]);
    }
}
