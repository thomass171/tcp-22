package de.yard.threed.engine.avatar;


import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.core.Color;

/**
 * Einfach nur eine SceneNode/Entity, um damit die Camera analog anderer Objekte versetzen zu können und um eine
 * Abstraktion für eine Person zu haben, die Cockpit/View Plätze wechselt. Für VR ist diese Abstraktion auch
 * ganz intuitiv, weil die Controller dran sind und XYZ-Justierung geht.
 * <p>
 * Optional mit oder ohne ECS nutzbar.
 * <p>
 * Der Avatar hat als Defaultblickrichtung den OpenGL Default, d.h. nach -z mit y up. Wenn man das anders braucht, gibt es
 * die Builder.
 * <p>
 * Die VR Controller sind hier, weil man bei VR besser einen Referenzpunkt hat, an den man die Controller haengt.
 * Dafuer muss der Avatar gar nicht sichtbar sein. Ohne ginge es wohl auch, ist aber halt praktisch.
 * <p>
 * Wenn der Avatar sichtbar sein soll, kann man ja ein Mesh dranhaengen. Optional hier ein kleiner grüner Würfel.
 * <p>
 * 27.11.20 Smmothly move to AvatarSystem/AvatarComponent.
 * 11.5.21: Avatar is nothing more than the visual representation of a loggedin/joined player. Everything view related is in {@link Observer}. (MA35)
 * Könnte auch direkt SceneNode sein?
 * VR (vrdown,vrOffsetPosition) is no longer an avatar issue but an {@link Observer} issue.
 */
public class Avatar {
    Log logger = Platform.getInstance().getLog(Avatar.class);

    private SceneNode mainNode;
    private boolean hasMesh = false;

    public Avatar(EcsEntity entity) {

        mainNode = new SceneNode();
        mainNode.setName("Avatar");

        entity.scenenode=mainNode;
        entity.addComponent(new TeleportComponent(mainNode));
        entity.addComponent(new AvatarComponent());
    }

    /**
     * MA35: Constructor without camera and in generel more simple.
     *
     * @return
     */
    public Avatar() {
        mainNode = new SceneNode();
        mainNode.setName("Avatar");
    }

    /**
     * Die Position des Viewpoint setzen. Das ist ausserhalb VR identisch zum Setzen der Camera. In VR greift noch ein Offset,
     * um die Floorerhöhung durch VR wieder auszugleichen.
     * 9.10.19: vrposition nicht mehr reingeben sondern nur intern setzen. Dann kann man Teleport besser verwenden.
     * wie steht denn setPosition zu Teleport? z.B in CockpitScene? Naja, ist halt die Position der mainNode.
     */
    public void setPosition(Vector3 position/*, Vector3 vrposition*/) {
        //7.4.21 wird z.Z. eh nicht verwendet this.ac.position = position;
        //this.vrposition = vrposition;
        mainNode.getTransform().setPosition(position);
    }

    /**
     * 15.5.21: Das ist fragwürdig, denn kippen dürfte/könnte merkwürdige Effekte haben.
     * @param rotation
     */
    public void setRotation(Quaternion rotation) {
        mainNode.getTransform().setRotation(rotation);
    }

    public SceneNode getSceneNode() {
        return mainNode;
    }

    /**
     * Simpler Body zum Testen. Normal ist er grün, bei vr low ist er gelb.
     */
    public void enableBody() {
        hasMesh = true;
        setMesh();
    }



    private void setMesh() {
        if (hasMesh) {
           /* if (vrdown) {
                mainNode.setMesh(new Mesh(Geometry.buildCube(0.1f, 0.1f, 0.1f), Material.buildBasicMaterial(Color.YELLOW)));
            } else */{
                mainNode.setMesh(new Mesh(Geometry.buildCube(0.1f, 0.1f, 0.1f), Material.buildBasicMaterial(Color.GREEN)));
            }
        }
    }

    public SceneNode getNode() {
        return mainNode;
    }
}
