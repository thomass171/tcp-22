package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Color;

/**
 * n moeglichst disjunkte (gut unterscheidbare Farben bereitstellen)
 * kann man noch optimieren
 * Created by thomass on 27.08.16.
 */
public class ColorPalette {
    Color[] col;
    
    public ColorPalette(int size){
        col = new Color[size];
        //int ranges = (size / 6) + 1;
        //int rangesize
        for (int i=0;i<size;i++){
            int r = i * 256 / size;
            int g = 0;
            int b = 0;
            col[i] = new Color(r,g,b);
        }
    }
    
    public Color getColor(int i){
        return col[i%col.length];
    }
}
