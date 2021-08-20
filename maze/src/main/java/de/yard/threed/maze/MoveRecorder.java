package de.yard.threed.maze;



import java.util.ArrayList;
import java.util.List;

/**
 * Die Zuege werden als Graph abgebildet. Ob eine Abstraktion und Reuse des 3D Graph sinnvoll ist, ist doch sehr fraglich.
 * Erstmal als Map versuchen. Eh Quatsch, das ist doch eine einfache Liste; naja, zwei.
 * 
 * Created by thomass on 14.02.17.
 */
public class MoveRecorder {
    private static MoveRecorder instance = null;
    //MA32 List<GridState> statelist = new ArrayList<GridState>();
    // moves enthaelt ein Element weniger als statelist
    List<GridMovement> moves = new ArrayList<GridMovement>();
    
    private MoveRecorder(){
        //MA32 statelist = new ArrayList<GridState>();
        moves = new ArrayList<GridMovement>();
    }

    /**
     * Kann oefters bei jedem neuen Level geschehen.
     * 
     *
     * @return
     */
    public static MoveRecorder init(/*GridState startstate*/){
        instance = new MoveRecorder();
        //MA32 instance.statelist.add(startstate);
        return instance;
    }
    
    public static MoveRecorder getInstance(){
        if (instance==null){
            throw new RuntimeException("not inited");
        }
        return instance;
    }
    
    public void addMove(GridMovement move/*MA32 , GridState state*/){
        //MA32 statelist.add(state);
        moves.add(move);
    }

    public void reset() {
        /*MA32 while (statelist.size()>1){
            statelist.remove(1);
        }*/
        moves.clear();
    }

    public GridMovement removeLastMove() {
        int index = moves.size()-1;
        if (index < 0){
            return null;
        }
        GridMovement m = moves.get(index);
        moves.remove(index);
        //MA32 statelist.remove(index+1);
        return m;
    }
}
