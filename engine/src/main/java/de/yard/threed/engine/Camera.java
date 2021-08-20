package de.yard.threed.engine;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.DimensionF;

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
 * ueber die Rotation ist dann doch auch Yaw/Pitch entbehrlich. Das ist doch einafch y-rot und x-rot. OK, scale
 * wird nicht gebraucht und einfach ignoriert.
 * 28.08.2014: Damit enthaelt die Camera jetzt die Rotationen als w�re es ein echten 3D-Objekt, und nicht mehr
 * quasi die umgekerhten fuer die ViewMatrix.
 * 26.11.2014: Die Camera hat die Properties der Leinwand (width,height). Das ist zunaechst mal unabhaengig von der
 * eigentlichen Projektion. width,height sind ja keine Attribute der Scene. Und sie sind fuer alle Cameras gleich.
 * 16.05.2015: Die WebGl Instanz der Camera ist auch object3d
 * 12.06.2015: Doch NativeCamera, wegen Defaultcamerakonzept und weil JME die Camera auch nicht von Spatial ableitet.
 * 15.09.2016: Und wieder anderrum. Unity hat in der Camera auch ein transform
 * wie in allen anderen Objekten. Und eine Camera kann childs und parents haben (auch wenn ich das wegen der Komplikationen nicht so nutze.
 * Aber trotzdem, eine Camera sollte genauso behandelbar sein wie ein Object (z.B. von Move- und StepControllern). Darum doch wieder
 * ein Base3D. Nicht Object3D, weil ich Child/PArent ja nicht so nutzte möchte. Die NativeCamera will ich aber behalten, denn View/Worldmatrix
 * können für die Camera ja anders sein. Da lass ich lieber erstmal die Finger von. Ah, doch nicht. In Unity ist der setRotation/Position der Camera sehr speziell,
 * in JME glaub ich auch. Darum verbietet sich diese Ableitung. Aber um Controller gemeinsam nutzen zu können, gibt es ein gemainsam implementiertes Interface.
 * 26.1.17: Etwas bloed mit Transform wegen scale, parent, child. Camera braucht sowas aber wegen z.B. FPC. Analog SceneNode nicht ableiten sondern component.
 * MA29: Component wie Mesh.
 * 28.11.18: Nicht mehr abstract, um ueber nativecamera angelegt werden zu können.
 * 31.12.18: Es gibt jetzt immer einen Carrier (in der Platform). Der kapselt die Camera. Darum gehen getsetposition/attach auf den carrier.
 * Die Camera hat keinen eigenen transform.
 */
public /*abstract*/ class Camera /*implements NativeTransform*/ {
    private Vector3 defaultup = new Vector3(0, 1, 0);
    // Der Abstand zwischen Position und dem lookat Punkt
    //28.4.21 asbach? protected double lookatdistance = 1;
    // Muss die ableitende Klasse setzen.
    protected Log logger;
    // Groesse der Leinwand (siehe Kommentar oben)
    //29.11.15 protected int width, height;
    protected NativeCamera nativecamera;

    public Camera(NativeCamera camera) {

        nativecamera = camera;

    }

    /**
     * Convenience.
     *
     * @return
     */
    public Vector3 getCarrierPosition() {
        return getCarrier().getTransform().getPosition();
    }

    /**
     * Convenience.
     *
     * @return
     */
    public Transform getCarrierTransform() {
        return getCarrier().getTransform();
    }

    /**
     * @return the projection matrix
     */
    public Matrix4 getProjectionMatrix() {
        return (this.nativecamera.getProjectionMatrix());
    }

    /**
     * @return the viewer matrix
     */
    public Matrix4 getViewMatrix() {
        return (this.nativecamera.getViewMatrix());
    }

    public Matrix4 getWorldModelMatrix() {
        //return (transform.getWorldModelMatrix());
        return getCarrier().getTransform().getWorldModelMatrix();
    }

    public NativeCamera getNativeCamera() {
        return (NativeCamera) nativecamera;
    }

    /**
     * // Ob wirklich jede Camera eine lookat" Funktion hat, ist unklar.
     * 2.3.16: Bei einer attached Camera dürfte das ziemlich witzlos sein, weil es wohl world coordinates sind.
     * 1.2.18: Bei ThreeJS und Unity ist lookat im world space. ThreeJS schränkt noch ein, dass der parent keinen Transform haben darf.
     * Meine Nutzung ist anscheinend uneinheitlich. Vielleicht besser nicht mehr ueber die Platform? Alternativ mal selbst berechnen.
     * 6.2.18: Die ersten Erfahrungen damit sind ganz gut.
     */
    public void lookAt(Vector3 lookat, Vector3 upVector) {
        //getNativeCamera().lookAt(lookat.vector3,(upVector!=null)?upVector.vector3:null);
        //lookat ist worldpos, darum auch die position im world space nehmen
        Vector3 wpos = getWorldModelMatrix().extractPosition();
        Vector3 forward = lookat.subtract(wpos).negate();
        Quaternion rotation = (MathUtil2.buildLookRotation(forward, (upVector != null) ? upVector : defaultup));
        getCarrier().getTransform().setRotation(rotation);
    }

    public void lookAt(Vector3 lookat) {
        lookAt(lookat, null);
    }

    public double getNear() {
        return nativecamera.getNear();
    }

    public double getFar() {
        return nativecamera.getFar();
    }

    public double getAspect() {
        return nativecamera.getAspect();
    }

    public double getFov() {
        return nativecamera.getFov();
    }

    /**
     * detach camera from model
     */
    public void detachFromModel() {
        getCarrier().getTransform().setParent(null);
    }

    /**
     * attach a camera to a model!
     * Convenience.
     * 28.4.21: Und deswegen nicht mehr deprecated.
     *
     * @param model
     */
    public void attachToModel(Transform model) {
        getCarrier().getTransform().setParent(model);
    }

    /**
     * Translation im Object space. Beruecksichtig die aktuelle Rotation.
     */
    public void translateOnAxis(Vector3 vector3, double distance) {
        getCarrierTransform().translateOnAxis(vector3, distance);
    }

    public void rotateOnAxis(Vector3 axis, Degree angle) {
        Quaternion q = Quaternion.buildQuaternionFromAngleAxis((double) angle.toRad(), axis);
        Quaternion rotation = MathUtil2.multiply(getCarrier().getTransform().getRotation(), q);
        getCarrier().getTransform().setRotation((rotation));
    }

    /**
     * mouselocation hat (0,0) links unten.
     * 11.5.21: Was ist denn eine "realViewPosition"? Anders als die der Camera? Kam wohl mal durch VR.
     * @param mouselocation
     * @return
     */
    public Ray buildPickingRay(Transform realViewPosition, Point mouselocation) {
        return new Ray(getNativeCamera().buildPickingRay(realViewPosition.transform, mouselocation));
    }

    /**
     * Liefert die Groesse der Plane in WorldCoordinate Einheiten
     *
     * @return
     */
    public DimensionF getNearplaneSize() {
        return getPlaneSize(getNear());
    }

    /**
     * Liefert die Groesse der Plane in WorldCoordinate Einheiten
     *
     * @return
     */
    public DimensionF getFarplaneSize() {
        return getPlaneSize(getFar());
    }

    /**
     * Liefert die Groesse der Plane in WorldCoordinate Einheiten
     *
     * @return
     */
    public DimensionF getPlaneSize(double z) {
        double aspect = getAspect();
        double fov = getFov();
        double h = (double) (2 * Math.tan(new Degree(fov).toRad() / 2) * z);
        return new DimensionF(h * aspect, h);
    }

    public Vector3 getVrPosition() {
        return (nativecamera.getVrPosition());
    }

    /**
     * Generates a Perspective Projection Matrix.
     * 2.3.17: Das macht eigentlich die Platform. Die Methode ist hier nur zu Test und Dokuzwecken (und für Platform OpenGL).
     *
     * @param fov    The vertical field of viewer
     * @param aspect The aspect ratio
     * @param zNear  The near plane
     * @param zFar   The far plane
     * @return
     * @author Sri Harsha Chilakapati
     */
    public static Matrix4 createPerspectiveProjection(double fov, double aspect, double zNear, double zFar) {
        double yScale = 1f / (double) Math.tan(Util.toRadians(fov / 2f));
        double xScale = yScale / aspect;
        double frustumLength = zFar - zNear;

        /*
        mat.e11 = xScale;
        mat.e22 = yScale;
        mat.e33 = -((zFar + zNear) / frustumLength);
        mat.e43 = -1;
        mat.e34 = -((2 * zFar * zNear) / frustumLength);
        mat.e44 = 0;
*/
        Matrix4 mat = new Matrix4(
                xScale, 0, 0, 0,
                0, yScale, 0, 0,
                0, 0, -((zFar + zNear) / frustumLength), -((2 * zFar * zNear) / frustumLength),
                0, 0, -1, 0);

        // logger.debug("createPerspectiveProjection:" + new Matrix4(mat).dump("\n"));

        return mat;
    }

    public void setLayer(int layer) {
        nativecamera.setLayer(layer);
    }

    public void setName(String name) {
        nativecamera.setName(name);
    }

    public String getName() {
        return nativecamera.getName();
    }

    public SceneNode getCarrier() {
        return new SceneNode(nativecamera.getCarrier());
    }

    public void setClearBackground(boolean clearBackground) {
        nativecamera.setClearBackground(clearBackground);
    }

    public void setClearDepth(boolean clearDepth) {
        nativecamera.setClearDepth(clearDepth);
    }

    public int getLayer() {
        return nativecamera.getLayer();
    }

    /**
     * Camera for deferred rendering at any near distance with the same FOV of an existing camera
     * and attached to that camera for syncing transforms.
     * Der attach ist nur für sowas wie HUD sinnvoll, aber nicht für interior.
     */
    public static PerspectiveCamera createAttachedDeferredCamera(Camera camera, int layer, double near, double far) {
        PerspectiveCamera deferredcamera = new PerspectiveCamera(camera.getFov(), camera.getAspect(), near, far);
        deferredcamera.setLayer(layer);
        deferredcamera.getCarrier().getTransform().setParent(camera.getCarrier().getTransform());
        deferredcamera.setClearBackground(false);
        deferredcamera.setClearDepth(true);
        return deferredcamera;
    }

    public static PerspectiveCamera createAttachedDeferredCamera(Camera camera, int layer) {
        return createAttachedDeferredCamera(camera, layer, camera.getNear(), camera.getFar());
    }

    /**
     * Das ganze ohne attach.
     */
    public static PerspectiveCamera createFreeDeferredCamera(Camera camera, int layer, double near, double far) {
        PerspectiveCamera deferredcamera = new PerspectiveCamera(camera.getFov(), camera.getAspect(), near, far);
        deferredcamera.setLayer(layer);
        deferredcamera.setClearBackground(false);
        deferredcamera.setClearDepth(true);
        return deferredcamera;
    }

    public void setEnabled(boolean b) {
        nativecamera.setEnabled(b);
    }

    public void setFar(double far) {
        nativecamera.setFar(far);
    }
}

