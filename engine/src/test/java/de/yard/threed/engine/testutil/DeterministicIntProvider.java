package de.yard.threed.engine.testutil;

import de.yard.threed.engine.util.IntProvider;

/**
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
        return values[pos++];
    }
}
