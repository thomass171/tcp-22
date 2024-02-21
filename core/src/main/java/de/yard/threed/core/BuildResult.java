package de.yard.threed.core;

import de.yard.threed.core.platform.NativeSceneNode;

/**
 * Result of an async (GLTF) model built. Relates to the complete file, not each sub model.
 *
 * For now does not contain animations. Maybe in the future when derived from GLTF.
 * FG animations are handled in FG model loading.
 * 9.1.18: Muss immer eine Node enthalten. Bei async Oprationen wird dort das Model eingehangen. Eine message hier
 * ist damit eigentlich Unsinn. Wenn ein Fehler auftrtitt, wird der anderswo gelogged.
 * 5.7.21: SceneNode->NativeSceneNode
 * 14.2.24: root node is longer set in case of a general parse error.
 * Created by thomass on 07.12.15.
 */
public class BuildResult {
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
    
    public String message() {
        return msg;
    }


    public NativeSceneNode getNode() {
        return rootnode;
    }
}
