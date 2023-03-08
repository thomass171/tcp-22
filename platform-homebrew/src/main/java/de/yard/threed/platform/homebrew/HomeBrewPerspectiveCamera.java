package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Camera;

import de.yard.threed.core.Vector3;

import de.yard.threed.core.Dimension;

import de.yard.threed.engine.platform.common.Settings;

/**
 * Hier wird eine passende perspective viewer Matrix bereitgestellt.
 * <p/>
 * Es gibt keine Grund, warum die Klasse abstract sein sollte.
 * Doch, es gibt einen: Eine PerspectiveCamera kann alleine keine View Matrix erstellen,
 * weil die Blickrichtung undefiniert ist.
 * 27.08.14: Jetzt ist sie ueber die Rotation der Superklassen aber definiert und damit auch nicht mehr abstract.
 * (siehe Kommentar Superklasse)
 * <p/>
 * <p/>
 * Date: 14.02.14
 * Time: 13:21
 */
public class HomeBrewPerspectiveCamera extends HomeBrewCamera {
    double fov;
    double aspect;
    double near;
    double far;


    public HomeBrewPerspectiveCamera(/*int width, int height,*/ double fov, double aspect, double near, double far) {
        super(/*width, height*/);
        this.fov = fov;
        this.aspect = aspect;//((double) width) / (double) height;
        this.near = near;
        this.far = far;
        logger = Platform.getInstance().getLog(HomeBrewPerspectiveCamera.class);
        projection =  Camera.createPerspectiveProjection(fov, aspect, near, far);
    }

    public HomeBrewPerspectiveCamera(Dimension d) {
        this(/*d.width, d.height,*/ Settings.defaultfov, ((double) d.width) / (double) d.height, Settings.defaultnear, Settings.defaultfar);
    }


/**
 * Uses Focal Length (in mm) to estimate and set FOV
 * 35mm (fullframe) camera isType used if frame size isType not specified;
 * Formula based on http://www.bobatkins.com/photography/technical/field_of_view.html
 */
    /**
     THREE.PerspectiveCamera.prototype.setLens = function(focalLength, frameHeight) {

     if (frameHeight === undefined)
     frameHeight = 24;

     this.fov = 2 * THREE.Math.radToDeg(Math.atan(frameHeight / (focalLength * 2)));
     this.updateProjectionMatrix();

     }
     **/

/**
 * Sets an offset in a larger frustum. This isType useful for multi-window or
 * multi-monitor/multi-machine setups.
 *
 * For example, if you have 3x2 monitors and each monitor isType 1920x1080 and
 * the monitors are in grid like this
 *
 *   +---+---+---+
 *   | A | B | C |
 *   +---+---+---+
 *   | D | E | F |
 *   +---+---+---+
 *
 * then for each monitor you would call it like this
 *
 *   var w = 1920;
 *   var h = 1080;
 *   var fullWidth = w * 3;
 *   var fullHeight = h * 2;
 *
 *   --A--
 *   camera.setOffset( fullWidth, fullHeight, w * 0, h * 0, w, h );
 *   --B--
 *   camera.setOffset( fullWidth, fullHeight, w * 1, h * 0, w, h );
 *   --C--
 *   camera.setOffset( fullWidth, fullHeight, w * 2, h * 0, w, h );
 *   --D--
 *   camera.setOffset( fullWidth, fullHeight, w * 0, h * 1, w, h );
 *   --E--
 *   camera.setOffset( fullWidth, fullHeight, w * 1, h * 1, w, h );
 *   --F--
 *   camera.setOffset( fullWidth, fullHeight, w * 2, h * 1, w, h );
 *
 *   Note there isType no reason monitors have to be the same size or in a grid.
 */

    /**
     * THREE.PerspectiveCamera.prototype.setViewOffset = function(fullWidth, fullHeight, x, y, width, height) {
     * <p/>
     * this.fullWidth = fullWidth;
     * this.fullHeight = fullHeight;
     * this.x = x;
     * this.y = y;
     * this.width = width;
     * this.height = height;
     * <p/>
     * this.updateProjectionMatrix();
     * <p/>
     * };
     */

    void updateProjectionMatrix() {
        /**
         if (this.fullWidth) {

         var aspect = this.fullWidth / this.fullHeight;
         var top = Math.tan(THREE.Math.degToRad(this.fov * 0.5)) * this.near;
         var bottom = -top;
         var left = aspect * bottom;
         var right = aspect * top;
         var width = Math.abs(right - left);
         var height = Math.abs(top - bottom);

         this.projectionMatrix.makeFrustum(
         left + this.x * width / this.fullWidth,
         left + (this.x + this.width) * width / this.fullWidth,
         top - (this.y + this.height) * height / this.fullHeight,
         top - this.y * height / this.fullHeight,
         this.near,
         this.far
         );

         } else {

         this.projectionMatrix.makePerspective(this.fov, this.aspect, this.near, this.far);

         }
         **/
    }


    /**
     * Das muss jetzt "nur" in die passende Rotation umgerechnet werden.
     * Analog wie in ThreeJS
     */
    /*24.9.19 public void lookAt(Vector3 nlookat, Vector3 upVector) {
        Vector3 lookat =  nlookat;
        Vector3 updirection = new Vector3(0, 1, 0);

        Vector3 forward =  MathUtil2.subtract(getPosition(), lookat).normalize();
        Quaternion rot = MathUtil2.buildLookRotation( forward,  updirection);
        
        //TODO length koennte 0 sein, siehe ThreeJS
        //Vector3 forward = lookat.subtract(eye).normalize();    // The "forward" vector.
       /* OpenGlVector3 right = (OpenGlVector3) MathUtil2.normalize(MathUtil2.getCrossProduct(updirection, forward));
        OpenGlVector3 up = (OpenGlVector3) MathUtil2.normalize(MathUtil2.getCrossProduct(forward, right));

        OpenGlMatrix4 m = new OpenGlMatrix4(
                right.x, right.y, right.z, -getDotProduct(right, getPosition()),
                up.x, up.y, up.z, -getDotProduct(up, getPosition()),
                forward.x, forward.y, forward.z, -getDotProduct(forward, getPosition()),
                0, 0, 0, 1);

        m = new OpenGlMatrix4(
                right.x, up.x, forward.x, 0,
                right.y, up.y, forward.y, 0,
                right.z, up.z, forward.z, 0,
                0, 0, 0, 1);

        setRotation(m.extractQuaternion());* /
      setRotation(rot);
    }*/

   
    /**
     * Dafuer trage ich einfach die Camera als Parent bei der Node ein.
     *
     * @param 
     */
   /* @Override
    public void add(NativeTransform model) {
        ((OpenGlTransform) model).setParent(transform);
    }*/

    @Override
    public double getNear() {
        return near;
    }

    @Override
    public double getFar() {
        return far;
    }

    @Override
    public double getAspect() {
        return aspect;
    }

    @Override
    public double getFov() {
        return fov;
    }

    /**
     * VR gibts ja noch nicht.
     *
     * @return
     */
    @Override
    public Vector3 getVrPosition(boolean dumpInfo) {
        return carrier.getTransform().getPosition();
    }

    @Override
    public void setLayer(int layer) {
        //Util.notyet();
        logger.error("setLayer: not yet");
    }

    public int getLayer(){
        //Util.notyet();
        logger.error("not yet");
        return 0;
    }

    @Override
    public void setClearDepth(boolean clearDepth) {
        
    }

    @Override
    public void setClearBackground(boolean clearBackground) {

    }

    @Override
    public void setEnabled(boolean b) {
        enabled=b;
    }

    @Override
    public void setFar(double far) {
logger.error("not yet");
    }


    /*private double getDotProduct(OpenGlVector3 p1, OpenGlVector3 p2) {
        return p1.getX() * p2.getX() + p1.getY() * p2.getY() + p1.getZ() * p2.getZ();
    }*/

    /*@Override
    public List<NativeTransform> getChildren() {
        List<NativeTransform> l = new ArrayList<NativeTransform>();
        // stimmt das? TODO eher nicht.
        return l;
    }*/


    /**
     * 13.9.16: Das mit den Childs ist hier doch konuzeptionell unrund.
     * 
     * @param index
     * @return
     */
   /* @Override
    public NativeSceneNode getChild(int index) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }*/
}
