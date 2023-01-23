package de.yard.threed.platform.homebrew;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Matrix4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by thomass on 24.03.16.
 */
public class Benchmark {
    private static final int dim = 1000;

    public void runLoop1() {
        long starttime = System.currentTimeMillis();
        List<Matrix4> mlist = new ArrayList<Matrix4>();
        for (int i = 0; i < dim; i++) {
            mlist.add(new Matrix4(1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16));
        }

        Matrix4 res = new Matrix4();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                res = res.multiply(mlist.get(i).multiply(mlist.get(j)));
            }
        }
        // damit alles gebraucht wird und nicht wegoptimiert werden kann.
        float det = (float) MathUtil2.getDeterminant(res);
        new Random((long) det);
        System.out.println("loop1 took " + (System.currentTimeMillis() - starttime) + " ms");
    }

    public static void main(String[] arg) {
        Benchmark bm = new Benchmark();
        for (int i = 0; i < 3; i++) {
            bm.runLoop1();
        }

    }
}

