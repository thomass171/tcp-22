package de.yard.threed.engine.ecs;


import de.yard.threed.engine.avatar.Avatar;

/**
 * A "client context".
 * 11.10.19
 */
public class Player {
    private static Player instance = null;
    //1.4.21 public Avatar avatar;

    /*1.4.21 private Player(Avatar avatar) {
        this.avatar = avatar;
    }

    public static void init(Avatar avatar) {
        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        instance = new Player(avatar);
    }*/

    /*1.4.21 public static Player getInstance() {
        if (instance == null) {
            throw new RuntimeException("not inited");
        }
        return instance;
    }*/

    /*1.4.21 public Avatar getAvatar() {
        return avatar;
    }*/
}
