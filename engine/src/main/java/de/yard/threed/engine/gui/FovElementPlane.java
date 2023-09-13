package de.yard.threed.engine.gui;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.geometry.UvMap1;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.geometry.SimpleGeometry;


/**
 * Eine SceneNode, die nicht in der world sondern immer an einer bestimmten Position im FOV ist.
 * Die Orientierung wird an die Cameraposition anpasst und damit steht die SceneNode immer
 * an der selben Stelle im FOV.
 * <p/>
 * ist nearplanesize die richtrige Groesse? Das ist doch nicht die Fenstergroesse, oder?
 * 3.3.17: Wie dem auch sei. Stand jetzt funktioniert es auf allen drei Platformen ganz ordentlich.
 * 4.10.18: Ich denke, nearplane ist schon richtig, denn darauf soll das FovElement ja abgebildet werden.
 * 2.10.19: Ohne Verzerrung oder Artefakte funktioniert es aber nur mit deferredCamera, weil die nearplan sonst zu nah sein muss.
 * 3.10.19 Umbenannt FovElement->FovElementPlane
 * 5.10.19: Und das ist jetzt weithegend unabhängig von Camera, nearplane oder deferred Camera. Einfach eine
 * Plane in x/y Ausdehnung an einer z Position, so dass sie in die Sichtachse einer Camera eingebunden werden kann.
 * Ach, doof, weil alles auf nearplane mit seinen kleinen Dimensionen ausgelegt ist. Das dürfte aber auch anders gehen.
 * 7.10.19: Jetzt more generic eine Plane senkrecht zur z-Achse. Camera/Layer spielen dafuer hier keine Rolle mehr.
 * 20.4.21: Independent from camera. An die Camera kommt es erst durch den attach(). (statt an world)
 * 28.4.21: Mal deprecated mit Inventory als more straightforward Alternativentwurf (der auch VR abdeckt)?
 * NeeNee, das hier ist fuer ausserhalb VR ganz ok, aber doch irgendwie umstaendlich. Also doch deprecaten? Oder mehr static?
 * 20.12.22: Back to deprecated.
 * <p/>
 * Created by thomass on 15.12.15.
 */
@Deprecated
public abstract class FovElementPlane extends SceneNode {
    //das eigentlich sichtbare Element
    public SceneNode element;
    public DimensionF nearplaneSize;
    //Depending on the mode of usage (eg. button) the effective plane size might differ from the available total size.
    public DimensionF planeSize;
    protected int level = 0;
    double zpos;

    public FovElementPlane(DimensionF nearplaneSize, double zpos) {
        this.zpos = zpos;
        this.nearplaneSize = nearplaneSize;
    }

    /**
     * Alles im Constructor ist unpraktisch wegen der Callbacks.
     * Die colliderboxsize ist wohl eine Kruecke fuer Unity. (28.4.21: Wegen zu naher near plane?)
     */
    public void buildFovElement(Vector3 colliderboxsize) {

        planeSize = getSize(nearplaneSize);
        // logger.debug("Building FovElement. nearplaneSize="+nearplaneSize+",elementsize="+elementsize+",zpos="+zpos);
        //28.4.21 Nutzung von Primitive macht unteres Icon in ReferenceScene kaputt (oder die -90 Rotation unten)
        //3.5.21: Jetzt wird aber umgestellt und nicht mehr rotiert
        //ShapeGeometry geo = ShapeGeometry.buildPlane(planeSize.width, planeSize.height, 1, 1, getUvMap());
        SimpleGeometry geo = Primitives.buildSimpleXYPlaneGeometry(planeSize.width, planeSize.height,getUvMap());
        Material mat = buildMat();
        Mesh mesh = new Mesh(geo, mat);
        if (colliderboxsize != null) {
            mesh.nativemesh.setBoxColliderSizeHint(colliderboxsize);
        }
        element = new SceneNode(mesh);
        // aufrichten, damit sie senkrecht zur Camera steht.
        //3.5.21 element.getTransform().rotateX(new Degree(90));
        // 28.4.21 zusaetlich rotieren, weil keine ShapeGeometry mehr verwendet wird,
        //element.getTransform().rotateY(new Degree(-90));
        element.setName("FovElement.Plane");
        setName("FovElementPlane");
        attach(element);

        // verschieben, wenn es nicht zentral liegen soll.
        Vector2 xytranslation = getXyTranslation(nearplaneSize);

        // unmittelbar hinter die near plane positionieren. Damit ist es im sichtbaren Bereich und
        // trotzdem fast die Groesse der near plane.
        // 7.3.16: Die nearplane liegt als default bei 0.1. Werte von 0.0001 auf 0.001f pro Ebene führen schon bei JME zu Artefakten.
        // Aber das ist jetzt eh Sache des Aufrufers (zpos).
        getTransform().setPosition(new Vector3(xytranslation.getX(), xytranslation.getY(), zpos));
    }

    /**
     * 28.4.21 Refactored zur Entkopplung.
     * @param colliderboxsize
     */
    public static SceneNode buildFovElementPlane(Vector3 colliderboxsize, DimensionF planeSize, Material mat) {

        // logger.debug("Building FovElement. nearplaneSize="+nearplaneSize+",elementsize="+elementsize+",zpos="+zpos);
        SimpleGeometry geo = Primitives.buildPlaneGeometry(planeSize.width, planeSize.height, 1, 1);

        Mesh mesh = new Mesh(geo, mat);
        if (colliderboxsize != null) {
            mesh.nativemesh.setBoxColliderSizeHint(colliderboxsize);
        }
        SceneNode element = new SceneNode(mesh);
        // aufrichten, damit sie senkrecht zur Camera steht.
        element.getTransform().rotateX(new Degree(90));
        // 28.4.21 zusaetlich rotieren, weil keine ShapeGeometry mehr verwendet wird.
        // Aber das ist doch Quatsch hier? Die Plane ist wie sie ist?? Und nur so sind width/height intuitiv
        //element.getTransform().rotateY(new Degree(-90));
        element.setName("FovElement.Plane");
        return element;
    }

    public DimensionF getElementsize() {
        return planeSize;
    }

    public abstract Material buildMat();

    /**
     * Die Groesse bzw. Position in Abhanegigkeit von der Groesse der nearplane ermitteln. Typischerweise sollte die Groesse kleiner
     * sein.
     */
    public abstract DimensionF getSize(DimensionF nearplaneSize);

    public abstract Vector2 getXyTranslation(DimensionF nearplaneSize);

    public abstract UvMap1 getUvMap();
}
