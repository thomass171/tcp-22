package de.yard.threed.engine.util;

/**
 * Created by thomass on 19.01.16.
 */
public class RandomIntProvider implements IntProvider {
    int low, high;
    int pos = 0;
    /*Random*/ LFSR rand;

    public RandomIntProvider() {
        // rand = new Random(System.currentTimeMillis());
        rand = new LFSR(450);
    }

    RandomIntProvider(int low, int high) {
        this.low = low;
        this.high = high;
        pos = 1111;//low * high;
    }

    public int nextInt() {
        if (rand != null) {
            return Math.abs(rand.nextShort());
        }
        /*int v = low + pos++;
        if (v >= high) {
            pos = 0;
        }
        return v;*/
        pos = (pos * pos) % 1000;
        return pos;
    }
}

