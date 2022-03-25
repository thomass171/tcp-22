package de.yard.threed.maze;

import java.util.ArrayList;
import java.util.List;

public class MoveResult {
    public GridMovement movement;
    List<Integer> collected = new ArrayList<Integer>();

    public MoveResult(GridMovement movement) {
        this.movement = movement;
    }

    public MoveResult(GridMovement movement, int itemId) {
        this.movement = movement;
        collected.add(new Integer(itemId));
    }
}
