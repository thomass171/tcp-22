package de.yard.threed.engine;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.NativeCamera;

/**
 * Hier wird eine passende perspective viewer Matrix bereitgestellt.
 * <p/>
 * Es gibt keine Grund, warum die Klasse abstract sein sollte.
 * Doch, es gibt einen: Eine PerspectiveCamera kann alleine keine View Matrix erstellen,
 * weil die Blickrichtung undefiniert ist.
 * 27.08.14: Jetzt ist sie ueber die Rotation der Superklassen aber definiert und damit auch nicht mehr abstract.
 * (siehe Kommentar Superklasse).
 * 2.3.17: Die Klasse wird doch so gar nicht (mehr) verwendet.
 * <p/>
 * <p/>
 * Date: 14.02.14
 * Time: 13:21
 */
public class PerspectiveCamera extends Camera {
    //float fov = 45;
    //float aspect;
    //ThreeJs Beispiele haben haefig 0.1 als near Wert
    //JME hat 1 als Default
    //4.10.18 float near = 1;
    //4.10.18 float far = 1000;

    public PerspectiveCamera(/*int width, int height,*/ double fov, double aspect, double near, double far) {
        //super(width, height);
        super(Platform.getInstance().buildPerspectiveCamera(fov, aspect, near, far));
        //this.fov = fov;
        //this.aspect = ((float) width) / (float) height;
        //4.10.18 this.near = near;
        //4.10.18 this.far = far;
        logger = Platform.getInstance().getLog(PerspectiveCamera.class);

    }

    /**
     * Constructor fuer eine Defaultcamera.
     */
    public PerspectiveCamera(NativeCamera camera) {
        //super(20,30);
        super(camera);
        logger = Platform.getInstance().getLog(PerspectiveCamera.class);
    }

    /*26.11.18 public PerspectiveCamera(Dimension dim) {
        this(dim.width, dim.height, 45f, 0.1f, 10000);
    }*/

    /**
     * @return the field of viewer
     */
    public double getFoV() {
        return nativecamera.getFov();
    }

    /**
     * @return the aspect ratio
     */
    public double getAspect() {
        return nativecamera.getAspect();
    }

    /**
     * @return the distance of near plane
     */
    /*public float getNearPlane() {
        return near;
    }*/

    /**
     * @return the distance of far plane
     */
    /*public float getFarPlane() {
        return far;
    }*/
    
  /*3.3.17  @Override
    public Vector3 getScale() {
        return new Vector3();
    }

    @Override
    public void setScale(Vector3 scale) {
        //ignored
    }*/
}
