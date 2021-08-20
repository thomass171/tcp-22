package de.yard.threed.core;

import de.yard.threed.core.platform.NativeSceneNode;




/**
 * 
 * 26.1.17: Ausgebaut fuer Animationen und auf Basis SceneNode.
 * Enthaelt die fertige Node, aber kein Loadedfile.
 * 9.1.18: Muss immer eine Node enthalten. Bei async Oprationen wird dort das Model eingehangen. Eine message hier
 * ist damit eigentlich Unsinn. Wenn ein Fehler auftrtitt, wird der anderswo gelogged.
 * <p/>
 * Gabs mal als ModelBuildResult, jetzt aber wieder gemerged:
 *  * 21.12.17: Hergeleitet aus BuildResult, aber reduziert auf das, was der platform buildmodel liefern kann. Z.B.
 * keine Animationen.
 *  Naja, wenn die mal aus GLTF gelesen werden, vielleicht doch.
 * Aber das lasse ich erstmal. Animationen gibts nur aus FG.
 *
 * 10.1.18: Deswegen nach platform common
 * 4.4.18: Wird seit Animation im modeldelaget auch nicht mehr gebraucht.
 * 
 * 29.12.18: TODO: Was ist denn jetzt deprecated? ReadResult oder BuildResult?  Wahrscheinlich BuildResult.
 *
 * 5.7.21: SceneNode->NativeSceneNode
 * Created by thomass on 07.12.15.
 */
public class BuildResult {
    //30.6.21 public static BuildResult FILE_NOT_FOUND = new BuildResult("filenotfound");
    //9.1.18 public static BuildResult FILE_NOT_HANDLED = new BuildResult("filenothandled");
    //30.6.21 public static BuildResult ERROR_IN_READING_FILE = new BuildResult("errorinreadingfile");
    //list of all failures, also deeper. Just for analyzing.
    //30.9.19 sowas geht nicht wegen async
    //public List<String> failures;

    private NativeSceneNode rootnode = null;
    private String msg;
    //FG DIFF
   // public List<SGAnimation> animationList;

    public BuildResult(String msg) {
        this.msg = msg;
    }

    public BuildResult(NativeSceneNode result) {
        rootnode = result;
        msg = "";
    }

    /*public BuildResult(SceneNode result, List<SGAnimation> animationList) {
        this(result);
        this.animationList=animationList;
    }*/

    
    public String message() {
        return msg;
    }


    public NativeSceneNode getNode() {
        return rootnode;
    }
}
