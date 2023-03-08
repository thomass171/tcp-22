package de.yard.threed.platform.homebrew;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.NativeTransform;

import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Position, Rotation und Scale Attribute als Superklasse fuer alles, was die verwendet.
 * <p/>
 * <p/>
 * Diskrete Abbildung statt "versteckt" in einer Matrix.
 * Quaternion und keine Winkel.
 * <p>
 * <p/>
 * Date: 23.08.14
 */
public class HomeBrewTransform implements NativeTransform/*Base3D*/ {
    List<HomeBrewTransform> children = new ArrayList<HomeBrewTransform>();
    Log logger = Platform.getInstance().getLog(HomeBrewTransform.class);
    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 scale = new Vector3(1, 1, 1);
    private HomeBrewTransform parent;
    // 16.9.16 SceneNode, an dem dieses Object3D hängt.
    private HomeBrewSceneNode parentscenenode;
    private String id;
    static private int uniqueidnumber = 1;
    public boolean isRoot = false;
    //precalculated
    private Matrix4 worldModelMatrix = null;

    public HomeBrewTransform() {
    }

    public HomeBrewTransform(Vector3 position, Quaternion rotation, Vector3 scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    /**
     * parentscenenode kann wegen Camera nicht uebergeben werden. Doch, bei Camera ist es halt null; oder muss auch irgendwie uebergeben werden.
     */
    public HomeBrewTransform(HomeBrewSceneNode parentscenenode) {
        this(createId(), parentscenenode);
    }

    public HomeBrewTransform(String id, HomeBrewSceneNode parentscenenode) {
        this.id = id;
        this.parentscenenode = parentscenenode;
    }

    @Override
    public Quaternion getRotation() {
        //ist nie null
        return rotation;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        needsUpdate();
    }

    @Override
    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
        needsUpdate();
    }

    public Vector3 getScale() {
        return scale;
    }

    @Override
    public void translateOnAxis(Vector3 axis, double distance) {
        axis = MathUtil2.multiply(rotation, axis);
        Vector3 v = axis.multiply(distance);
        position = MathUtil2.add(position, v);
        needsUpdate();
       /*9.3.16 ginge wahrscheinlich auch so:
        Matrix4 m = MathUtil2.buildRotationMatrix(rotation);
        v = MathUtil2.multiply(m.transform(v),distance);
        position = (OpenGlVector3) MathUtil2.add(position, (OpenGlVector3) v);*/
        //logger.debug("position="+new Vector3(position).dump("")+"axis="+new Vector3(axis).dump("")+" distance="+distance);
    }

    public void setScale(Vector3 scale) {
        this.scale = scale;
        needsUpdate();
    }

    @Override
    public void rotateOnAxis(Vector3 axis, double angle) {
        Quaternion q = Quaternion.buildQuaternionFromAngleAxis(angle, axis);
        rotation = MathUtil2.multiply(rotation, q);
        needsUpdate();
    }

    @Override
    public Matrix4 getLocalModelMatrix() {
        return buildMatrix();
    }

    public void translate(Vector3 v) {
        position = MathUtil2.add(position, v);
        needsUpdate();
    }

    public void translateX(float t) {
        position = position.add(new Vector3(t, 0, 0));
        needsUpdate();
    }

    public void translateY(float t) {
        position = position.add(new Vector3(0, t, 0));
        needsUpdate();
    }

    public void translateZ(float t) {
        position = position.add(new Vector3(0, 0, t));
        needsUpdate();
    }

    /**
     * Der Sinn dieser Methode ist fraglich
     *
     * @param v
     */
    public void scale(Vector3 v) {
        //ich mach das mal genauso wie setScale
        //scale = scale.add(v);
        setScale(v);
    }


    /**
     * M = T x R x S
     * <p>
     * Winkel werden ueberhaupt nicht mehr verwendet, nur Quaternions.
     *
     * @return
     */
    private Matrix4 buildMatrix() {
        Matrix4 translationMatrix = Matrix4.buildTranslationMatrix(position);
        Matrix4 rotationMatrix;
        rotationMatrix = MathUtil2.buildRotationMatrix(rotation);
        Matrix4 scaleMatrix = Matrix4.buildScaleMatrix(scale);
        return MathUtil2.buildMatrix(translationMatrix, rotationMatrix, scaleMatrix);
    }

    /**
     * Precalculated world matrix improves about 25%.
     *
     * @return
     */
    @Override
    public Matrix4 getWorldModelMatrix() {
        if (worldModelMatrix == null) {
            Matrix4 local = buildMatrix();
            if (parent != null) {
                Matrix4 transformation = getWorldModelMatrix(local, parent.getWorldModelMatrix());
                worldModelMatrix = transformation;
            } else
                worldModelMatrix = local;
        }
        return worldModelMatrix;
    }

    public static Matrix4 getWorldModelMatrix(Matrix4 local, Matrix4 parentworld) {
        // 19.8.14: ThreeJS multipliziert aber umgekehrt (three-62.js:7653)
        // 26.8.14: Die Mulitplikation ist M = Parent * local;
        // Das ist so, wie ThreeJS es auch macht.

        Matrix4 transformation = parentworld.multiply(local);
        //Matrix4 transformation = parenttransformation.multiply(buildModelMatrix());
        return transformation;
    }

    @Override
    public NativeTransform getParent() {
        if (parent == null || parent.isRoot) {
            // den nicht liefern
            return null;
        }
        return parent;
    }

    @Override
    public void setParent(NativeTransform parent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = (HomeBrewTransform) parent;
        if (this.parent != null) {
            this.parent.children.add(this);
        }
        needsUpdate();
    }

    @Override
    public NativeTransform getChild(int index) {
        return children.get(index);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public List<NativeTransform> getChildren() {
        List<NativeTransform> l = new ArrayList<NativeTransform>();
        for (HomeBrewTransform t : children) {
            l.add(t);
        }
        return l;
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return parentscenenode;
    }
    
      /*16.9.16  Es gibt keinen add, das geht ueber setParent. @Override
    public void add(NativeObject3D obj) {
        OpenGlObject3D o = (OpenGlObject3D) obj;
        children.add(o);
        o.setParent(this);
        /*15.6.16 if (o instanceof OpenGlMesh) {
            //TODO kann hier was doppelt reinkommen?
         //   OpenGlScene.renderables.add((OpenGlMesh) o);
        }* /
    }*/

    //21.7.16 @Override
    public void remove(HomeBrewTransform obj) {
        HomeBrewTransform o = (HomeBrewTransform) obj;
        children.remove(o);
        o.setParent(null);
        /*15.6.16 if (o instanceof OpenGlMesh) {
       //     OpenGlScene.renderables.remove((OpenGlMesh) o);
        }*/

    }

    public String getId() {
        return id;
    }

    private static String createId() {
        return "generatedid-" + uniqueidnumber++;
    }

    /**
     * 08.02.2015: Konzeptionell ist das etwas unrund.
     *
     * @param wireframe
     */
    public void setWireframe(boolean wireframe) {
        if (true) throw new RuntimeException("check ob Meshumbau wirklich geht");
        for (HomeBrewTransform c : children) {
           /*15.6.16  if (c instanceof OpenGlMesh) {
                ((OpenGlMesh) c).setWireframe(wireframe);
            } else {*/
            c.setWireframe(wireframe);
            //}
        }
    }

    @FunctionalInterface
    interface ChildVisitor {
        void handleChild(HomeBrewTransform child);
    }

    void traverse(ChildVisitor childVisitor) {
        for (HomeBrewTransform child : children) {
            childVisitor.handleChild(child);
            child.traverse(childVisitor);
        }
    }

    private void needsUpdate() {
        worldModelMatrix = null;
        for (HomeBrewTransform child : children) {
            child.needsUpdate();
        }
    }

    @Override
    public void setLayer(int layer) {
        //Util.notyet();
        logger.error("setLayer: not yet");
    }

    @Override
    public int getLayer() {
        Util.notyet();
        return 0;
    }

    /** nicht mehr, seit es eine globale Liste von Objekten gibt
     * @param renderables
     */
    /*public void collectRenderables(Renderables renderables) {
        if (true) throw new RuntimeException("check ob Meshumbau wirklich geht");
        /*15.6.16 if (this instanceof OpenGlMesh){
            OpenGlMesh mesh = (OpenGlMesh) this;
            if (mesh.istransparent){
                renderables.transparent.add(mesh);
            }else {
                renderables.renderables.add(mesh);
            }
        }* /
        for (OpenGlObject3D c : children) {
            c.collectRenderables(renderables);
        }
    }*/
}
