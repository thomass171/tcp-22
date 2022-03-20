package de.yard.threed.maze;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Timestamp;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Koennte zur Abbildung des Lösungsbaums auch gut einen Graph brauchen. Bilde ich aber erstmal nach. Auch wenn es gemiensame kompleye Algorithmen wie kuerzesten Pfad gibt.
 * Liefert z.Z. nicht den kuerzesten Weg und tendentiell eine "Left" lastigen.
 * <p>
 * <p>
 * Created by thomass on 14.02.17.
 */
public class SokobanAutosolver {
    Log logger = Platform.getInstance().getLog(SokobanAutosolver.class);
    //MazeLayout layout;
    Grid grid;
    //MA32GridState gridstate;
    //HashMAp wegen schnellerer Suche
    //List<SolutionNode> solutions = new ArrayList<SolutionNode>();
    Map<GridState,SolutionNode> checkedstates = new HashMap<GridState,SolutionNode>();
    
    SolutionNode solutionNode = null;
    
    /*public static void main(String[] arg) {
        try {
            Grid grid = new Grid(Platform.getInstance().getRessourceManager().loadResourceSync(new BundleResource("skbn/SokobanWikipedia.txt")).inputStream);
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    SokobanAutosolver(Grid grid/*MazeLayout layout/*MA32,GridState gridstate*/) {
        this.grid=grid;
        //MA32this.gridstate = gridstate;
    }

    public void solve() {
        Timestamp timestamp=new Timestamp();

        GridMover player = MazeFactory.buildMover(grid.getMazeLayout().getNextLaunchPosition(null));
        List<GridMover> boxes = MazeFactory.buildMovers(grid.getBoxes());

        //no items here for now.
        if (grid.getDiamonds().size()>0){
            throw new RuntimeException("no items in autosolve");
        }
        GridState gridstate=new GridState(Util.buildList(player), boxes, new ArrayList<GridItem>());

        solutionNode = solve(null, gridstate);
        logger.debug("Checked "+checkedstates.size()+" states. "+timestamp.getTookLogString("Solving "));
    }

    public String dumpSolution() {
        if (solutionNode == null) {
            solve();
        }
        String s = "";
        s += dumpSolutionNode(0, solutionNode);
        return s;
    }

    /**
     * Liefert nur einen der möglichen Lösungswege. Das muss nicht der kürzeste sein.
     *
     * @return
     */
    public List<GridMovement> getSolution() {
        if (solutionNode == null) {
            solve();
        }
        List<GridMovement> list = new ArrayList<GridMovement>();
        addSolutionStep(solutionNode, list);
        return list;
    }

    public String getSolutionAsString() {
        String s = "";
        for (GridMovement m : getSolution()) {
            s += ((StringUtils.length(s) > 0) ? "-" : "") + m;
        }
        return s;
    }

    private void addSolutionStep(SolutionNode sn, List<GridMovement> list) {
        for (GridMovement m : sn.movements.keySet()) {

            SolutionNode successor = sn.movements.get(m);
            list.add(m);
            if (successor == null) {
                //irritiert s += "Solved";

            } else {

                addSolutionStep(successor, list);
            }
        }
    }

    private String dumpSolutionNode(int level, SolutionNode sn) {
        String s = "";
        for (GridMovement m : sn.movements.keySet()) {
            for (int i = 0; i < level; i++) {
                s += "  ";
            }
            s += "" + m + "\n";
            SolutionNode successor = sn.movements.get(m);
            if (successor == null) {
                //irritiert s += "Solved";
            } else {
                s += dumpSolutionNode(level + 1, successor);
            }
        }
        return s;
    }

    /**
     * Interner rekursiver Abstieg, darum private.
     * <p>
     * Liefert den Schritt, der zur Lösung geführt hat. Das könnten theoretisch auch mehrere sein.
     * Liefert in der Node alle Schritte, die zur Lösung geführt haben.
     *
     * 15.4.21: Tuts nicht mehr
     * @param state
     * @return
     */
    private SolutionNode solve(SolutionNode predecessor, GridState state) {
        // Vor dem rekursivem Abstieg muss diese Node gespeichert werden, damit erkennbar ist,
        // dass sie schon behandelt wird.
        SolutionNode node = new SolutionNode(predecessor, state);
        checkedstates.put(state,node);
        //logger.debug("known states now:" + solutions.size());
        for (GridMovement m : GridMovement.regularpossiblemoves) {
            //GridState nextstate = state.execute(m,grid.getLayout());
            GridMover player = state.players.get(0);
            GridMovement rm = player.move(m,player.getOrientation(),state,grid.getMazeLayout());
            GridState nextstate =null;
            if (rm != null){
                nextstate=new GridState(state.players, state.boxes,state.items);
            }
            if (nextstate != null) {
                if (GridState.isSolved(state.boxes,grid.getMazeLayout())) {
                    logger.debug("Solved");
                    node.addMovement(m, null);
                    // Wenn ich aus meinem State einen Schritt in Richtung Lösung gefunden habe, versuche ich keinen anderen mehr.
                    // Wie soll es da denn noch andere geben koennen? Es waere vielleicht denkbar, dass Player "noch ne Runde dreht" und dann löst?
                    // brechen wir mal nicht ab, um alle Lösungen zu finden. Wirklich alle?
                    // Abbrechen muss aber sein, denn wenn nextstate hier geloest ist, werde ich bei weiterem Abstieg einfache Drehungen finden
                    // die auch Lösungen sind, weil der uebergebene State ja gelöst ist.
                    return node;
                }
                SolutionNode sn;
                if ((sn = knownstate(nextstate)) != null) {
                    // Wenn ich hier schon mal war und es von hier eine Lösung gab, dann den Zug auch vermerken. Denn
                    // vielleicht bin ich jetzt schneller nach hier gekommen als der vorherige Weg. Hier entsteht aber ein Zirkelschluss
                    // und ich kann im Kreis laufen. Daher prüfen, welcher der beiden Wege in den "nextstate" der kürzeste ist. Wenn der neue
                    // kürzer ist, den letzten Schritt aus dem alten entfernen und übernehmen.
                    // Einfach erstmal ignorieren. Dann bleiben halt Umwege.
                    if (sn.movements.size() > 0) {
                        // suchen, wo sich die Wege getrennt haben.
                        //SolutionNode commonorigin
                        int existingdistance = sn.getDistanceToOrigin();
                        int newdistance = node.getDistanceToOrigin();
                        // Die newdistance ist für den Vergleich eins niedriger als die exisitng, weil ich ja den Vorgaenger des nextstate betrachte
                        if (newdistance + 1 < existingdistance) {
                            //kuerzer. Den Weg aufnehmen. 
                            node.addMovement(m, sn);
                            // und den längeren alten entfernen
                            sn.replacePredecessor(node);

                        }
                    }
                } else {
                    // bisher nicht betrachtet. Runtersteigen.
                    sn = solve(node, nextstate);
                    if (sn.movements.size() > 0) {
                        node.addMovement(m, sn);
                    }
                }
            }

        }
        return node;
    }

    /**
     * Fuer Map.get() brauchts einen guten Hashcode.
     * 
     * @param state
     * @return
     */
    private SolutionNode knownstate(GridState state) {
        for (GridState n : checkedstates.keySet()) {
            if (n.equals(state)) {
                return checkedstates.get(n);
            }
        }
        return null;
        //return checkedstates.get(state);
    }


}

class SolutionNode {
    GridState state;
    // Enthält alle Schritte, die von diesem State zur Lösung führen.
    HashMap<GridMovement, SolutionNode> movements = new HashMap<GridMovement, SolutionNode>();
    //List<GridMovement> movements = new ArrayList<GridMovement>();
    // null bei startnode. oder wenn ein Umweg entfernt wurde?
    SolutionNode predecessor = null;

    SolutionNode(SolutionNode predecessor, GridState state) {
        this.state = state;
        this.predecessor = predecessor;
    }

    public void addMovement(GridMovement m, SolutionNode sn) {
        movements.put(m, sn);
    }

    public int getDistanceToOrigin() {
        if (predecessor == null) {
            return 0;
        }
        return predecessor.getDistanceToOrigin() + 1;
    }

    /**
     * Es wurde ein kuerzerer Weg zu dieser Node gefunden. Den bestehenden ersetzen.
     */
    public void replacePredecessor(SolutionNode newpredecessor) {
        if (predecessor != null) {
            predecessor.removeWayToNode(this);
        }
        predecessor = newpredecessor;
    }

    private void removeWayToNode(SolutionNode node) {
        List<GridMovement> toberemoved = new ArrayList<GridMovement>();
        for (GridMovement m : movements.keySet()) {
            if (movements.get(m).state.equals(node.state)) {
                toberemoved.add(m);
            }
        }
        for (GridMovement m : toberemoved) {
            movements.remove(m);
        }
        if (movements.size() == 0 && predecessor != null) {
            // wenn von hier kein Weg mehr weiterführt, auch im Vorgaeger den Weg hier hin löschen
            predecessor.removeWayToNode(this);
        }
    }
}