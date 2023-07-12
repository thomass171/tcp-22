package de.yard.threed.engine.util;

import de.yard.threed.engine.util.IntProvider;

/**
 * Most common use case is testing. But also useful for pseudo random.
 *
 * Created by thomass on 07.12.16.
 */
public class DeterministicIntProvider implements IntProvider {
    private int[] values;
    int pos=0;

    public DeterministicIntProvider(int[] values){
        this.values = values;
    }

    @Override
    public int nextInt() {
        if (pos == values.length) {
            pos = 0;
        }
        return values[pos++];
    }
}
