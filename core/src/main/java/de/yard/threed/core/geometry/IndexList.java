package de.yard.threed.core.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 07.06.18.
 */
public class IndexList {
    List<Integer> indexes = new ArrayList<Integer>();

    public int[] getIndices() {
        int[] ni = new int[indexes.size()];
        for (int i = 0; i < ni.length; i++) {
            ni[i] = (int) indexes.get(i);
        }
        return ni;
    }

    public void add(int a, int b, int c) {
        indexes.add(a);
        indexes.add(b);
        indexes.add(c);

    }
}
