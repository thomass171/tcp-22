package de.yard.threed.engine.avatar;

import de.yard.threed.core.Vector3;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.core.platform.Log;

/**
 * <p>
 * Created by thomass on 27.11.20.
 */
public class AvatarComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(AvatarComponent.class);


    public static String TAG = "AvatarComponent";

    public boolean vrdown = false;

    //7.5.21 public Camera camera;
    //15.8.19: vrposition sollte auf den vrcarrier wirken. Ob diese Positions nicht komplett durch XYZ Justierung obsolet sind?
    //7.4.21 wird z.Z. eh nicht verwendet public Vector3 position = null;
    //5.5.21: Das ist jetzt die Korrektur, um in VR die von aussen gesetzte Avatar Position nach unten auszugleichen. Kann nur derjenige setzen, der
    //auch den Avatar positioniert hat. Per fine tuning kann diese Korrektur noch weiter angepasst werden.
    private Vector3 vrOffsetPosition = null;
    // die Node für die XYZ Justierung muss vom Avatar entkoppelt sein, weil der Avatar
    // selber ja auch bewegt werden kann, z.B. bei Maze.
    //11.5.21public SceneNode vrcarrier;

    //bei VR lower muss es eine Node in Camera Hoehe geben, vor der z.B. ein Menu angezeigt werden kann. Sonst ist das nachher zu niedrig, und wenns zu nah ist (<3.0-5.0) ist es
    //gar nicht sichtbar. Wohl wichtig für Browser, die auch bei inaktivem VR die Position zu hoch haben und gelowerred werden müssen.
    //5.5.21: Wichtig fuer crosshair bzw. alle Rays vom Viewpoint. Aber kann man dafuer nicht den rotator nehmen?
    //11.5.21private SceneNode faceNode;

    //11.5.21public SceneNode rotator;
    //11.5.21 boolean vrEnabled;

    public AvatarComponent(/*boolean vrEnabled*/) {
        //this.vrEnabled = vrEnabled;
    }

    @Override
    public String getTag() {
        return "AvatarComponent";
    }






    public void setVrOffsetPosition(Vector3 offset) {
        vrOffsetPosition = offset;
    }

    /**
     * 5.5.21: Geht nicht der Rotator?
     *
     * @return
     */
    /*11.5.21public SceneNode getFaceNode() {
        //return faceNode;
        return rotator;
    }*/


    public static AvatarComponent getAvatarComponent(EcsEntity e) {
        AvatarComponent gmc = (AvatarComponent) e.getComponent(AvatarComponent.TAG);
        return gmc;
    }

    /*11.5.21 public void buildFaceNode() {
        faceNode = new SceneNode();
        faceNode.setName("Face");
        vrcarrier.attach(faceNode);
    }*/
}
