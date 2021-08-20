package de.yard.threed.maze;


import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Geometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.PerspectiveCamera;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;


/**
 * Ein "Ray" Avatar.
 * 
 * Created by thomass on 30.03.15.
 */
public class MazeRay extends SceneNode /*implements Entity/*16.9.16 , Movable*/ {
    Log logger = Platform.getInstance().getLog(MazeRay.class);
    SceneNode body, axis, head, leftwheel, rightwheel;
    PerspectiveCamera camera;
    //GridPosition position;
    MoverComponent mover;

    public MazeRay(){
        this(Color.ORANGE);
        //TODO 12.4.21: initial pos+orinetation
        Util.notyet();
        mover = new MoverComponent(getTransform()/*this*/,true,null,null);
    }

    public MazeRay(Color color) {
        setName("Ray");
        /*body = buildBody();
        add(body);
        head = buildHead();
        head.translateY(Sizes.BODYHEIGHT);
        add(head);
        leftwheel = buildWheel();
        add(leftwheel);
        rightwheel = buildWheel();
        add(rightwheel);
        axis = buildAxis();
        add(axis);*/
        body = buildSimpleBody(MazeSettings.getSettings().simplerayheight, MazeSettings.getSettings().simpleraydiameter, color);

        attach(body);

        //kommt spaeter addCamera();

    }

    public MoverComponent getMover(){
        return mover;
    }

    /**
     * Liefert die logische (x,y) Position im Grid. Wird gerundet aus den echten 3D Kooridnaten
     * @return
     */
    /*3.3.17 public static Point getGridPosition(Vector3 pos){
        return MazeDimensions.getCoordinatesOfElement(pos);
    }*/

    /*private Model buildBody() {
        ShapeGeometry geometry = new ShapeGeometry(buildBodyShape(), 1, 1);
        Mesh bodymesh = new Mesh(geometry, new MeshPhongMaterial(Color.BLUE));
        bodymesh = new Mesh(geometry, new MeshBasicMaterial(Color.BLUE));
        Model m = new Model();
        m.add(bodymesh);
        return m;
    }

    private Shape buildBodyShape() {
        float width = Sizes.BODYWIDTH;
        float height = Sizes.BODYHEIGHT;
        Shape shape = new Shape(true);
        //links beginnen und unten rum die Unterschale nach rechts
        shape.addPoint(-width / 2, 0);
        //shape.addPoint(new Vector2(-width / 2, height / 2));
        shape.addArc(new Vector2(0, 0), new Degree(10), 18);
        // und jetzt oben rum eine Eiform (oder besser Ellipse)
        shape.addArc(new Vector2(0, 0), new Degree(10), 18, 1, 2);
        Model.addShape("body",shape);
        return shape;
    }

    private Model buildHead() {
        ShapeGeometry geometry = new ShapeGeometry(buildHeadShape(), 1, 1);
        Mesh bodymesh = new Mesh(geometry, new MeshPhongMaterial(Color.BLUE));
        bodymesh = new Mesh(geometry, new MeshBasicMaterial(Color.GREEN));
        Model m = new Model();
        m.add(bodymesh);
        return m;
    }

    private Shape buildHeadShape() {
        float width = Sizes.HEADWIDTH;
        float height = Sizes.HEADHEIGHT;
        Shape shape = new Shape(true);
        //links beginnen und unten rum die Unterschale nach rechts
        shape.addPoint(-width / 2, 0);
        shape.addPoint(new Vector2(-width / 2, height / 2));
        shape.addArc(new Vector2(0, 0), new Degree(10), 18);
        // und jetzt oben rum eine Eiform (oder besser Ellipse)
        shape.addArc(new Vector2(0, 0), new Degree(10), 18, 1, 2);

        return shape;
    }

    

    private Model buildAxis() {
        ShapeGeometry geometry = new ShapeGeometry(buildAxisShape(), 1, 1);
        Mesh mesh = new Mesh(geometry, new MeshPhongMaterial(Color.BLUE));
        mesh = new Mesh(geometry, new MeshBasicMaterial(Color.YELLOW));
        Model m = new Model();
        m.add(mesh);
        return m;
    }

    private Shape buildAxisShape() {
        float height = Sizes.AXISRADIUS;
        Shape shape = new Shape(true);
        shape.addPoint(0, -height / 2);
        shape.addArc(new Vector2(0, 0), new Degree(10), 36);
        return shape;
    }*/

    private void addCamera() {
        float VIEW_ANGLE = 45,
                ASPECT = 800 / 600,
                NEAR = 0.1f,
                FAR = 10000;

        /*16.6. camera = new PerspectiveCamera(800, 600);
        camera.setPosition(new Vector3(0, 1, 1.5f));
        camera.lookAt(new Vector3(0, 0, -1));
        // Die Camera kommt einfach so als Objekt da dran
        addCamera(camera);*/
    }

    private SceneNode buildSimpleBody(float height, float diameter, Color color) {
        Geometry cuboid = Geometry.buildCube(diameter, height, diameter);
        Mesh m = new Mesh(cuboid,Material.buildBasicMaterial(color));
        return new SceneNode(m);
    }

   /*erstmal nicht  public void attemptFire() {
        if (!hasBalls()) {
            logger.debug("cannot throw. no balls");
            return;
        }
        Vector3 rot = Matrix4.buildRotationMatrix(object3d.getRotation()).getColumn(2);
        logger.debug("fire rot=" + object3d.getRotation().dump(""));
        logger.debug("fire rot=" + rot.dump(""));
        Controller.getInstance().fire(object3d.getPosition(), rot);
    }*/


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
