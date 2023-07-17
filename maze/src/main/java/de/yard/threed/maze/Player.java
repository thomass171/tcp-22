package de.yard.threed.maze;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;

/**
 * Ein Pseudo Avatar, der regulär überhaupt nicht sichtbar ist. 
 * Created by thomass on 30.03.15.
 *
 * 15.8.19: Ersetzt durch Avatar. Diese Klasse kann weg.
 */
@Deprecated
public class Player extends SceneNode /*implements Entity/*16.9.16 , Movable*/ {
    Log logger = Platform.getInstance().getLog(Player.class);
    SceneNode body;
    PerspectiveCamera camera;
    GridPosition position;
    //Mover mover;

    public Player(){
        this(Color.ORANGE);
       // mover = new Mover(object3d/*this*/);
    }

    public Player(Color color) {
        setName("Ray");
        body = buildSimpleBody(MazeTheme.getSettings().simplerayheight, MazeTheme.getSettings().simpleraydiameter, color);
        attach(body);

        //kommt spaeter addCamera();

    }

    /*public Mover getMover(){
        return mover;
    }*/

    /**
     * Liefert die logische (x,y) Position im Grid. Wird gerundet aus den echten 3D Kooridnaten
     * @return
     */
    /*3.3.17 public static Point getGridPosition(Vector3 pos){
        return MazeDimensions.getCoordinatesOfElement(pos);
    }*/
    
     SceneNode buildSimpleBody(float height, float diameter, Color color) {
        Geometry cuboid = Geometry.buildCube(diameter, height, diameter);
        Mesh m = new Mesh(cuboid,Material.buildBasicMaterial(color));
        return new SceneNode(m);
    }



    private boolean hasBalls() {
        return true;
    }

    /**
     * Nicht das ganze Model rotieren, denn das enthält die Position. Nur Ray rotieren.
     * @param q
     */
    public void rotateXX(Quaternion q) {
        body.getTransform().setRotation(q);
    }


}
