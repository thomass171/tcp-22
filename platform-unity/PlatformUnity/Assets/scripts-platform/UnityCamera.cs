using System;
using java.lang;
using java.util;
using UnityEngine;
using UnityEngine.XR;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using de.yard.threed.engine;
using de.yard.threed.core.resource;
using de.yard.threed.engine.platform.common;

namespace de.yard.threed.platform.unity
{
    /**
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
  * 27.08.2014: Jetzt mal ableiten von Base3D. Denn Position und Rotation hat eine Camera ja doch auch. Und
 * ueber die Rotation ist dann doch auch Yaw/Pitch entbehrlich. Das ist doch einafch y-rot und x-rot. OK, scale
 * wird nicht gebraucht und einfach ignoriert.
 * 28.08.2014: Damit enthaelt die Camera jetzt die Rotationen als w�re es ein echten 3D-Objekt, und nicht mehr
 * quasi die umgekerhten fuer die ViewMatrix.
 * 26.11.2014: Die Camera hat die Properties der Leinwand (width,height). Das ist zunaechst mal unabhaengig von der
 * eigentlichen Projektion. width,height sind ja keine Attribute der Scene. Und sie sind fuer alle Cameras gleich.
 * 
 * Zur Camera in Unity: http://stackoverflow.com/questions/14874153/how-to-access-camera-from-another-object-in-unity-3d
 * Ableiten von UnityBase3D oder nicht? Camera und GameObject haben keine gemeinsame Superklasse. Das spricht gegen ableiten.
 * 7.4.16: Jetzt ohne Ableitung.
 * 
 * Aus http://docs.unity3d.com/ScriptReference/Camera-worldToCameraMatrix.html:
 *  Note that camera space matches OpenGL convention: camera's forward is the negative Z axis. This is different from Unity's convention, where forward is the positive Z axis.
 * Heistt das, das Unity die Camera ähnlich verdreht wie JME? Und das könnte erklären, warum Rotation hier noch gespiegelt werden muss.
 * Das kann aber auch daran liegen, dass die Viewmatrix anders verläuft. Denn das rausrechnen der Handedness erfolgt umgekehrt wie bei der
 * Modelmatrix.
 * Eine attached Camera hat immer eine gegenläufige Rotation. Darum wird die attached camera jetzt nachgebildet.
 * 6.9.16: Das heisst, die Camera ist nicht wirklich Child des Models. Und der update wird ständig aufgerufen, um die Camera
 * synchron um model zu halten.
 * 26.1.17: Implementiert NativeTransform wegen der merkwürdigen Camera bzw. des mirroring.
 * 3.12.18: Ungenutze modes entfernt. Stehen noch in "Kopie".
 * 
 */
    public class UnityCamera  :  /*UnityBase3D  ,*/  NativeCamera/*, NativeTransform/*Object3D*/
    {
        private UnityVector3 up = new UnityVector3 (0, 1, 0);
        // Der Abstand zwischen Position und dem lookat Punkt
        protected float lookatdistance = 1;
        // Muss die ableitende Klasse setzen.
        protected Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityCamera));
        // Groesse der Leinwand (siehe Kommentar oben)
        protected int width, height;
        public UnityEngine.Camera camera;
        private bool attached = false;
        //31.1.18: Wegen mode 0 brauch ich den parent nicht mehr (Camera ist immer attached)
        UnityBase3D /*15.9.16SceneNode*/ parent = null;
        /*Unity*/
        de.yard.threed.core.Vector3 position = new de.yard.threed.core.Vector3 ();
        /*Unity*/
        de.yard.threed.core.Quaternion rotation = new de.yard.threed.core.Quaternion ();
        //mode 0 ist mit echtem parent. Hat aber immer die gegenläufige Rotation.
        //mode 2 nachgebildeter attach nicht in Mirrorworld und setzen von camera.worldToCameraMatrix. 20.4. Der ist jetzt der beste. Da passt alles, auch die
        //Endrotation der moving box und die Höhe auf der Movingbox. Scheinbar perfekt, kein Manko. Ausser das Cardboard-attached damit nicht geht.
        //mode 5 ist camera überhaupt nicht in Mirrorworld. 19.4. Bis jetzt der Beste, auch für Cardboard. 21.4. Mit Matrix (und Extraktion)
        //ist der Mode genauso gut wie Mode 2. Und Cardboard geht damit auch. Also ist es jetzt dieser.
        //31.1.18: Man kann die VR Camera nicht positionieren/rotieren, wenn sie nicht attached ist, weil sie ihre Position/Rotation vom Headset bekommt.
        //Also muss sie immer attached sein. Darum jetzt mal zurueck zu mode 0. Allerdings ist die camera IMMER attached. Wenn nich an eine model, dann
        //zumindext an "World". Das läuft eigentlich sehr gut.

        //Fuer VR brauch ich noch einen Adapter (carrier), an dem der VR Input wieder gespiegelt wird. Den carrier gibts immer, nur das Spiegeln
        //fallweise.
        World world;
        UnityBase3D /*14.11.16 SceneNode*/ cameramodel;
        public UnitySceneNode carrier;
	int layer=0;

        public UnityCamera (UnityEngine.Camera camera, UnitySceneNode pcarrier)
        {
            this.camera = camera;
            world = Scene/*((EnginePlatform)Platform.getInstance ())*/.getWorld ();

            // 21.5.16: Ob das stimmt, ist unklar, weil z.Z. mode 5 verwendet wird.
            cameramodel = (UnityBase3D)new SceneNode ().getTransform ().transform/*object3d*/;
            //als default immer an world.
            if (pcarrier == null) {
                carrier = UnitySceneNode.build ("Main Camera Carrier");
            } else {
                carrier = pcarrier;
            }
            if (inputvr ()) {
                carrier.getTransform ().setScale ((new de.yard.threed.core.Vector3 (1, 1, -1)));
            }
            UnityBase3D.setParent (camera.transform, getCarrierTransform ());

            attach (world.getTransform ().transform);


            UnityBase3D.setParent (cameramodel/*.gameObject*/.getTransform (), camera.transform);
            AbstractSceneRunner.getInstance () /*((EnginePlatform)Platform.getInstance ())*/.addCamera (this);
        }

        public static UnityCamera buildPerspectiveCamera (double fov, double aspect, double near, double  far, de.yard.threed.core.Color backgroundColor)
        {
            UnitySceneNode carrier = UnitySceneNode.build ("");
            UnityEngine.Camera camera = carrier.setCamera ();
            // Das ist hier ganz merkwürdig mitdem vermeintlichen Carrier. Eiegtnlich ist er das
            // gar nicht, sondern die Camera. Darum wird er auch nicht uebergeben. Strange. Aber so läuft es
            // wie erwartet.
            return new UnityCamera (camera, null);
        }

        private UnityEngine.Transform getCarrierTransform ()
        {
            return ((UnityBase3D)carrier.getTransform ()).getTransform ();
        }

        private bool inputvr ()
        {
            string model = XRDevice.model;
            logger.info ("found VR model: " + model);
            return (model != null && model.Length > 0);
        }

        /**
         * Liefert die Matrix neutral von einer evtl. Handedness Konvertierung.
         * Siehe Header wegen Unitycamera.
         */
        public /*Native*/Matrix4 getViewMatrix ()
        {
            if (attached) {
                // das ginge eigentlich immer, nicht nur bei attached
                return MathUtil2.getInverse (getWorldModelMatrix ());
            }
            // ob das richtig ist? Ich glaub nicht. Laut
            // http://stackoverflow.com/questions/24165915/advanced-info-on-unity3ds-camera-matrix
            // aber schon.
            Matrix4x4 wtoc = (camera.worldToCameraMatrix);
            return (Scene/*((EnginePlatform)Platform.getInstance ())*/.getWorld ().mirror2 (UnityMatrix4.fromUnity (wtoc)));
        }

        /**
         * Liefert die Matrix neutral von einer evtl. Handedness Konvertierung.
         * @return the projection matrix
         * Siehe Header wegen Unitycamera.
         */
        public /*Native*/Matrix4 getProjectionMatrix ()
        {
            Matrix4x4 pm = camera.projectionMatrix;
            //2.3.17: Der Picking Ray Test zeigt, dass nicht gespiegelt werden darf. Hmm.
            //return (UnityMatrix4)Platform.getInstance ().getWorld ().mirror2 (new UnityMatrix4 (pm));
            return UnityMatrix4.fromUnity (pm);
        }

        /**
         *  Siehe Header wegen Unitycamera.
         */
        public /*Native*/Matrix4 getWorldModelMatrix ()
        {
            if (attached) {
                
            }
            /*Unity*/
            Matrix4 worldmatrix = UnityMatrix4.fromUnity (camera.transform.localToWorldMatrix);
            worldmatrix = Scene/*((EnginePlatform)Platform.getInstance ())*/.getWorld ().mirror (worldmatrix);
            return worldmatrix;

        }

        public void detach ()
        {
            // Dann muss die Camera wieder in die World wegen handedness
            //14.11.16 geht nicht mehr! UnityObject3D.setParent (camera.transform, ((UnityObject3D)PlatformUnity.getInstance ().world.object3d).gameObject.transform);
            //als default immer an world.
            attach (world.getTransform ().transform);
            attached = false;
            parent = null;
           
        }

        /**
         * nmodel als parent des carrier setzen.
         */
        public void attach (NativeTransform/*Object3D/*SceneNode*/ nmodel)
        {
            UnityBase3D /*15.9.16SceneNode*/ model = (UnityBase3D)nmodel;

            UnityBase3D.setParent (((UnityBase3D)carrier.getTransform ()).getTransform (), model.getTransform ());
            parent = model;
            attached = true;
        }

        public de.yard.threed.core.Quaternion getRotation ()
        {
            return UnityTransform.getRotation (getCarrierTransform ());
               
           
        }

        public de.yard.threed.core.Vector3 getPosition ()
        {
            return UnityTransform.getPosition (getCarrierTransform ());
        }

        virtual public void setPosition (de.yard.threed.core.Vector3 position)
        {
            UnityTransform.setPosition (getCarrierTransform (), position);
           
            
        }

        virtual public void setRotation (de.yard.threed.core.Quaternion rotation)
        {
            UnityTransform.setRotation (getCarrierTransform (), rotation);
        }

        public de.yard.threed.core.Vector3 getVrPosition ()
        {
            UnityEngine.Transform ct = camera.transform;
            return UnityTransform.getPosition (ct);
            return position;
        }

        virtual public void translateOnAxis (de.yard.threed.core.Vector3 axis, double distance)
        {
            UnityTransform.translateOnAxisCamera (getCarrierTransform (), axis, distance);
            return;
        }

        public NativeRay buildPickingRay (/*de.yard.threed.engine.*/NativeTransform real, Point mouselocation/*, Dimension screendimensions*/)
        {
            //1.3.17: Die Berechnung in Unity mit ScreenPointToRay liefert etwas andere Ergebnisse als mein RayHelper. Evtl. ist origin doch
            //nicht in der Camera? Oder es liegt an der Handedness Konvertierung?
            //origin ist bei Unity  "starting on the near plane of the camera", nicht die Camera position.
            bool useunity = false;
            if (useunity) {
                UnityEngine.Vector3 mousePosition = new UnityEngine.Vector3 (mouselocation.getX (), mouselocation.getY (), 0);
                //logger.debug ("mousePosition=" + mousePosition);
                UnityEngine.Ray ray = camera.ScreenPointToRay (mousePosition); 
                UnityEngine.Vector3 d = ray.direction;
                UnityEngine.Ray mray = new UnityEngine.Ray (ray.origin, new UnityEngine.Vector3 (d.x, d.y, d.z));
                return new UnityRay (mray);
            }
            Dimension screendimensions = new Dimension (Screen.width, Screen.height);//RunnerHelper.getInstance().dimension;

            NativeRay r = new RayHelper (this).buildPickingRay (new de.yard.threed.engine.Transform (real),mouselocation.getX (), mouselocation.getY (), screendimensions);
            return (UnityRay)r;
        }

        public void add (NativeTransform/*Object3D/*15.9.16SceneNode*/ model)
        {
            cameramodel.add ((UnityBase3D/*SceneNode*/)model);
        }

        public double getNear ()
        {
            return camera.nearClipPlane;
        }

        public double getFar ()
        {
            return camera.farClipPlane;
        }

        public void setFar(double far) {
            camera.farClipPlane = (float)far;
        }

        public double getAspect ()
        {
            float aspect = camera.aspect;
            return aspect;
        }

        public double getFov ()
        {
            float fov = camera.fieldOfView;
            return fov;
        }

        public NativeSceneNode getCarrier ()
        {
            //28.11.18: den carrier liefern. Der ist jetzt "offiziell".
            return carrier;//new UnitySceneNode (camera.gameObject, true);
        }

        public void rotateOnAxis (de.yard.threed.core.Vector3 axis, double angle)
        {
            UnityTransform.rotateOnAxis (camera.transform, axis, angle);
           
        }

        public de.yard.threed.core.Matrix4 getLocalModelMatrix ()
        {
            return (Matrix4)Util.notyet ();
        }

	/**
	 * 13.11.19: cullingMask ist wirklich eine Bitmask, d.h. eine Camera kann mehrere Layer rendern.
	 * "User layer" starten bei Bit 8. (https://docs.unity3d.com/Manual/Layers.html)
	 */
        public void setLayer (int layer)
        {
            camera.cullingMask = 1 << (8 + layer);
	    this.layer=layer;
        }

	/**
	 * 12.11.19: Exakt gesetzten Layer zurueckliefern.
	 */
        public int getLayer ()
        {
            return layer;//camera.cullingMask;
        }

        public void setName (String name)
        {
            //logger.debug("setting name "+name);
            carrier.setName (name + " Carrier");
            camera.name = name;
        }

        public String getName ()
        {
            return camera.name;
        }

        /**
         * Muesste das nicht eine Bitmask sein?
         */
        public void setClearDepth (bool clearDepth)
        {
            camera.clearFlags = CameraClearFlags.Depth;
        }

        public void setClearBackground (bool clearBackground)
        {
            camera.clearFlags = CameraClearFlags.SolidColor;
        }

	public void setEnabled (bool b)
        {
            camera.enabled=b;
        }

    }
}
