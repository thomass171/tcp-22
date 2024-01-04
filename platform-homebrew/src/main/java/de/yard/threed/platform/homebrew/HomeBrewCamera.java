package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.Point;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.Transform;


import de.yard.threed.core.Dimension;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.engine.platform.common.RayHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

/**
 * Date: 14.02.14
 * Time: 13:15
 * <p/>
 * Hier gibt es nur eine Standard (Identity) perspective viewer Matrix. Mit unveraendertem Yaw/Pitch
 * ist auch die View Matrix dann eine Identity (wirklich?).
 * Damit steht eine
 * Camera dann an 0,0,0 und "blickt" nach Sueden.
 * <p/>
 * Ob die Ableitung von Object3D guenstig/sauber ist, ist offen. Zumindest
 * bekommt die Camera da jetzt ihre Position her. Schliesslich sind alle Object3D dazu
 * gedacht, gerendered zu werden, ausser halt die Camera. Nee, gerendered wird ja das Mesh (30.6.14: Aber ein Mesh ist ja ein Object3D).
 * <p/>
 * 30.6.14: Eigentlich gibt es doch keine Camera, das ist doch ein Hilfsmittel zur Vorstellung. Es gibt
 * doch nur eine View auf die Szene. Die "Position" einer Camera  ist daher was anderes als die Position normaler
 * Objekte. Wenn die Camra auf der z-Ache positiv verschoben wird, heist dass eigentlich, dass die View negativ verschoben wird.
 * Macht von der Darstellung keinen Unterschied. Nur ob der z-Offset jetzt positiv oder negativ ist, f�hrt
 * zur Verwirrung. Daher jetzt doch nicht als Ableitung von Object3D. Auch ein Argument gegen die Ableitung sind die abstrakten
 * Methoden zum Rendern, die hier sonst implementiert werden muessten.
 * <p/>
 * 22.08.2014: Eine Camera macht jetzt keine Controls mehr. Es ist einfach sauberer, das zu entkoppeln.
 * 27.08.2014: Jetzt mal ableiten von Base3D. Denn Position und Rotation hat eine Camera ja doch auch. Und
 * ueber die Rotation ist dann doch auch Yaw/Pitch entbehrlich. Das ist doch einfach y-rot und x-rot. OK, scale
 * wird nicht gebraucht und einfach ignoriert.
 * 28.08.2014: Damit enthaelt die Camera jetzt die Rotationen als wäre es ein echten 3D-Objekt, und nicht mehr
 * quasi die umgekehrten fuer die ViewMatrix.
 * 26.11.2014: Die Camera hat die Properties der Leinwand (width,height). Das ist zunaechst mal unabhaengig von der
 * eigentlichen Projektion. width,height sind ja keine Attribute der Scene. Und sie sind fuer alle Cameras gleich.
 * 02.08.2016: Jetzt doch wieder von Object3D statt OpenGlBase3D ableiten, weil
 * - es günstig ist um Objekte an die Camera zu hängen (mit Camerra als parent)
 * - Argumente geben die Ableitung wie Implementierung von Rendermethoden weggefallen sind. Gerendfered werden ja nur Meshes, aber nicht Object3ds
 * <p>
 * 26.1.17: Muesste eigentlich nicht NativeTransform implementieren, macht es aber um einheitlich zu anderen Platformen zu sein, die das machen muessen.
 * 23.9.19: Nicht NativeTransform wegen Component.
 */
public abstract class HomeBrewCamera /*extends OpenGlObject3D */ implements NativeCamera/*, NativeTransform/*Object3D*/ {
   // private OpenGlVector3 up = new OpenGlVector3(0, 1, 0);
    // Der Abstand zwischen Position und dem lookat Punkt
    protected float lookatdistance = 1;
    // Muss die ableitende Klasse setzen.
    protected Log logger;
    // Projection als default immer Identity
    protected Matrix4 projection;
    // Groesse der Leinwand (siehe Kommentar oben)
    //protected int width, height;
    HomeBrewTransform parent = null;
    protected RayHelper rayHelper;
    HomeBrewTransform transform = new HomeBrewTransform(null);
    HomeBrewSceneNode carrier;
    private String name;
    public boolean enabled;

    public HomeBrewCamera(/*int width, int height*/) {
        // this.width = width;
        // this.height = height;
        // Camera als Default etwas "vorziehen" nach Sueden?
        // 3.3.16: NeeNee, sowas wird nicht gemacht
        //position = new OpenGlVector3(0, 0, -1);

        //this.updateProjectionMatrix();
        projection = new Matrix4();
        rayHelper = new RayHelper(this);

        //carrier name isType set later
        carrier = new HomeBrewSceneNode("");
        carrier.getTransform().setParent(Scene.getCurrent().getWorld().getTransform().transform);
        //Platform.getInstance().addCamera(this);
        AbstractSceneRunner.getInstance().addCamera(this);
    }

    /**
     * Die View Matrix ist die Inverse der Camera Transformation Matrix (siehe doc).
     * Camera local model (oder world?). World, bei attached. World ist
     * immer richtig.
     * 23.9.19: Umdenken weil Camera Component einer SceneNode ist? Woher kommt der transform bzw. die Node? Aus dem Carrier.
     */
    @Override
    public Matrix4 getViewMatrix() {
        Matrix4 worldmatrix = carrier.getTransform().getWorldModelMatrix();
        /*if (parentscenenode != null) {
            return (OpenGlMatrix4) MathUtil2.getInverse(OpenGlObject3D.getWorldModelMatrix(local,(OpenGlMatrix4) parentscenenode.getWorldModelMatrix()));
        }*/
        Matrix4 viewMatrix = MathUtil2.getInverse(worldmatrix);
        //System.out.println((new Matrix4(viewMatrix)).dump("\n"));

        return viewMatrix;
    }

    /**
     * @return the projection matrix
     */
    @Override
    public Matrix4 getProjectionMatrix() {
        return projection;
    }

   /*23.9.19 wegen Component @Override
    public Matrix4 getWorldModelMatrix() {
        Matrix4 parentworld = new Matrix4();
        if (parent != null) {
            parentworld =  parent.getWorldModelMatrix();
        }
        return OpenGlTransform.getWorldModelMatrix( getLocalModelMatrix(), parentworld);
    }*/

    /**
     * Das ist "einfach" das Gegenteil der Projektion.
     * @param vector
     * @return
     */
   /*OGL  OpenGlVector3 unprojectVector  ( OpenGlVector3 vector) {

       /* camera.projectionMatrixInverse.getInverse( camera.projectionMatrix );

        _viewProjectionMatrix.multiplyMatrices( camera.matrixWorld, camera.projectionMatrixInverse );
* /
        OpenGlMatrix4 unprojectionmatrix = getViewMatrix().getInverse().multiply(getProjectionMatrix().getInverse());
        //logger.debug("unprojectionmatrix=\n"+unprojectionmatrix.dump("\n"));

        return vector.project( unprojectionmatrix );

    };*/

    /**
     * Ermittelt aus den relativen Mauskoordinaten den mouseVector.
     * y wird als von unten nach oben zaehlend betrachtet! Anders als z.B. ThreeJS.
     * 26.11.14: So ganz exakt schint mir die Rechnung nicht zu stimmen. Fuer 0 schon, aber der
     * Vollausschlag muss ja WIDTH/HEIGHT -1 sein, und damit kommt man nicht genau auf -1 oder 1.
     */
   /*OGL  public OpenGlVector3 buildMouseVector(int x,int y) {
        // Das ist gut erklaert in http://stackoverflow.com/questions/11036106/three-js-projector-and-ray-objects
        // Lwjgl zaehlt y aber von unten, anders als ThreeJS. Darum y umkehren bzw. nicht umkehren.
        // ThreeJs verwendet -1 und 1 (??). Aber laut stackoverflow ist 0.5 ganz gut.
        // JME (Camera.java:1349) berücksichtigt noch den ViewPort und bekommt die 0.5 flexibel über eine projectionZPos.
        OpenGlVector3 mouseVector = new OpenGlVector3( ( (float)x / width ) * 2 - 1,
                ((float) y  / height ) * 2 - 1,
                0.5f );
        //logger.debug("mouseVector="+mouseVector);
        return mouseVector;
    }*/

    /**
     * Ermittelt aus den relativen Mauskoordinaten den Picking Ray.
     * Der Origin des Ray ist immer in der Camera.
     */
    /*OGL public Ray buildPickingRay(int x,int y) {
         OpenGlVector3 mouseVector = buildMouseVector(x,y);

        // Code aus http://stackoverflow.com/questions/11036106/three-js-projector-and-ray-objects nachgebaut.
        // Das ist auch irgendwie eingängiger.
        //var raycaster = projector.pickingRay( mouseVector.clone(), camera );

        //logger.debug("mouseVector=\n"+mouseVector.dump(" ")+", width="+width+",x="+x);
        mouseVector = this.unprojectVector( mouseVector );
        //logger.debug("unprojectedmouseVector="+mouseVector);

        // find direction from vector to end
        OpenGlVector3 direction = mouseVector.subtract( getPosition() );
        direction =  direction.normalize();
        //logger.debug("direction="+direction);

        Ray pickingray = new Ray( getPosition(), direction );
        //logger.debug("built pickingray="+pickingray);

        return pickingray;
    }*/
    /*3.3.16 public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }*/
    /*@Override
    public void detach() {
        parent = null;
    }

    @Override
    public void attach(NativeTransform nmodel) {
        OpenGlTransform model = (OpenGlTransform) nmodel;
        parent = model/*.object3d* /;
    }*/
    @Override
    public NativeRay buildPickingRay(NativeTransform realViewPosition, Point mouselocation/*,Dimension screendimensions*/) {
        Dimension screendimensions = AbstractSceneRunner.getInstance().dimension;
        NativeRay ray = rayHelper.buildPickingRay(new Transform(realViewPosition),mouselocation.getX(), mouselocation.getY(), screendimensions);
        return ray;
    }

    @Override
    public NativeSceneNode getCarrier() {
        return carrier;
    }

    /**
     * Analog JME
     */
    @Override
    public void setName(String name) {
        this.name=name;
        carrier.setName(name+ " Carrier");
    }

    @Override
    public String getName() {
        return name;
    }


   /* @Override
    public void destroy(){
        //TODO
    }*/

    /*@Override
    public Vector3 getPosition() {
        return transform.getPosition();
    }

    @Override
    public void setPosition(Vector3 vector3) {
        transform.setPosition(vector3);
    }

    @Override
    public Quaternion getRotation() {
        return transform.getRotation();
    }

    @Override
    public void setRotation(Quaternion quaternion) {
        transform.setRotation(quaternion);
    }*/

    /*@Override
    public void translateOnAxis(Vector3 axis, double distance) {
        transform.translateOnAxis(axis, distance);
    }*/

    /*@Override
    public NativeTransform getTransform() {
        return this;
    }*/

    /*@Override
    public NativeTransform getChild(int i) {
        return (NativeTransform) Util.notyet();
    }

    @Override
    public NativeTransform getParent() {
        return (NativeTransform) Util.notyet();
    }


    @Override
    public void setParent(NativeTransform parent) {
        Util.notyet();
    }*/

   /* @Override
    public Vector3 getScale() {
        return (Vector3) Util.notyet();
    }

    @Override
    public void setScale(Vector3 scale) {
        Util.notyet();
    }*/

    /*@Override
    public NativeSceneNode getSceneNode() {
        return (NativeSceneNode) Util.notyet();
    }

    @Override
    public void rotateOnAxis(Vector3 axis, double angle) {
        transform.rotateOnAxis(axis,angle);
    }*/

   /* @Override
    public Matrix4 getLocalModelMatrix() {
        return transform.getLocalModelMatrix();
    }

    @Override
    public int getChildCount() {
         Util.notyet();;
        return 0;
    }*/
}