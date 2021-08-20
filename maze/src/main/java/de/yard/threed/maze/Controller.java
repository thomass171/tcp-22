package de.yard.threed.maze;

import de.yard.threed.core.SceneUpdater;

/**
 * Der Controller, der alles verbindet.
 * 6.3.17: Da er nur fuer Balls ist, erstmal weglassen. 
 * <p/>
 * Created by thomass on 06.08.15.
 */
public class Controller implements SceneUpdater {
    public static Controller instance;
    MazeScene mazeScene;
    Player ray;
   //erstmal nicht List<Ball> balls = new ArrayList<Ball>();

    public Controller(MazeScene mazeScene, Player ray) {
        this.mazeScene = mazeScene;
        this.ray = ray;
    }

    public static Controller initController(MazeScene mazeScene, Player ray) {
        instance = new Controller(mazeScene, ray);
        return instance;
    }

    public static Controller getInstance() {
        return instance;
    }

    /*erstmal nicht public void fire(Vector3 location, Vector3 rotation) {
        Ball ball = new Ball(location, rotation);
        mazeScene.add(ball);
        balls.add(ball);
    }*/

    @Override
    public void update() {
        /*erstmal nichtfor (Ball ball:balls){
            ball.moveForward(mazeScene.getDeltaTime());
        }*/
      
    }
}
