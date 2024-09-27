package de.yard.threed.core.geometry;

import de.yard.threed.core.Vector2;

public class UsedIndex {
    public int index;
    public Vector2 uv;
    public int duplicateOf;

    public UsedIndex(int index, Vector2 uv) {
        this.index = index;
        this.uv = uv;
        this.duplicateOf = -1;
    }

    public UsedIndex(int index, Vector2 uv, int duplicateOf) {
        this.index = index;
        this.uv = uv;
        this.duplicateOf = duplicateOf;
    }
}
