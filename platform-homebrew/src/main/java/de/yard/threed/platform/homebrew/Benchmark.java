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

    public void runLoop2() {
        long starttime = System.currentTimeMillis();
        for (int j = 0; j < dim*20; j++) {
            DspJava.applyLowpass();
        }
        System.out.println("loop2 took " + (System.currentTimeMillis() - starttime) + " ms");

    }

    public static void main(String[] arg) {
        Benchmark bm = new Benchmark();
        for (int i = 0; i < 3; i++) {
            bm.runLoop1();
        }
        for (int i = 0; i < 3; i++) {
            bm.runLoop2();
        }
    }
}

/**
 * TODO Copyright? brauch ich den überhaupt?
 */
class DspJava {
    
    public static class FilterState {
        static final int size = 16;

        final double[] input = new double[size];
        final double[] output = new double[size];

        int current;
        FilterState(){
            for (int i=0;i<size;i++){
                input[i] = i + 55;
            }
        }
    }

    static short clamp(short input) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, input));
    }

    static int getOffset(FilterState filterState, int relativeOffset) {
        return ((filterState.current + relativeOffset) % FilterState.size + FilterState.size) % FilterState.size;
    }

    static void pushSample(FilterState filterState, short sample) {
        filterState.input[getOffset(filterState, 0)] = sample;
        ++filterState.current;
    }

    static short getOutputSample(FilterState filterState) {
        return clamp((short) filterState.output[getOffset(filterState, 0)]);
    }

    static void applyLowpass(FilterState filterState) {
        final double[] x = filterState.input;
        final double[] y = filterState.output;

        y[getOffset(filterState, 0)] =
                (1.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState, -10)] + x[getOffset(filterState, -0)]))
                        + (10.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState, -9)] + x[getOffset(filterState, -1)]))
                        + (45.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState, -8)] + x[getOffset(filterState, -2)]))
                        + (120.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState, -7)] + x[getOffset(filterState, -3)]))
                        + (210.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState, -6)] + x[getOffset(filterState, -4)]))
                        + (252.0 * (1.0 / 6.928330802e+06) * x[getOffset(filterState, -5)])

                        + (-0.4441854896 * y[getOffset(filterState, -10)])
                        + (4.2144719035 * y[getOffset(filterState, -9)])
                        + (-18.5365677633 * y[getOffset(filterState, -8)])
                        + (49.7394321983 * y[getOffset(filterState, -7)])
                        + (-90.1491003509 * y[getOffset(filterState, -6)])
                        + (115.3235358151 * y[getOffset(filterState, -5)])
                        + (-105.4969191433 * y[getOffset(filterState, -4)])
                        + (68.1964705422 * y[getOffset(filterState, -3)])
                        + (-29.8484881821 * y[getOffset(filterState, -2)])
                        + (8.0012026712 * y[getOffset(filterState, -1)]);
    }

    public static void applyLowpass() {
        int length = 256;
        FilterState filterState = new FilterState();
        for (int i = 0; i < length ;        ++i){
            pushSample(filterState, (short) i);
            applyLowpass(filterState);
            getOutputSample(filterState);
        }
    }
}