using System;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using de.yard.threed.platform;
using java.util;

namespace de.yard.threed.platform.unity
{
    /**
 * Unity scheint kein Sceneobjekt zur Verfügung zu stellen.
 * 
 * Created by thomass on 22.01.16.
 */
    public class UnityScene  :  NativeScene
    {
        private List<UnityLight> lights = new ArrayList<UnityLight> ();
        bool enableModelCameracalled;
        private string uniqueName;
        private static int uniqueid = 1;
        private static UnityScene instance = null;

        private UnityScene ()
        {
        }

        public static void init ()
        {
            instance = new UnityScene ();
        }

        public static UnityScene getInstance ()
        {
            return instance;
        }

        /**
         * Eine3n add gibt es Unity nicht. Das GameObject ist nach dem Anlegen sofort da.
         */
        virtual public void add (NativeSceneNode objtoadd)
        {
            //addToTree((UnityObject3D) objtoadd);
        }

        /*19.7.16 virtual public void add (NativeMesh objtoadd)
        {
            // addToTree((UnityObject3D) objtoadd);
        }*/

        /*virtual     public void remove (NativeMesh objtoremove)
        {
            //removeFromTree((UnityObject3D)objtoremove);
        }*/

        /*virtual     public void remove (NativeSceneNode objtoremove)
        {
            // das sieht falsch aus, mit dem remove auf dem zu removendem Objekt
            ((UnitySceneNode)objtoremove).remove (objtoremove);
        }*/

        virtual public void add (NativeLight light)
        {
            //lights.add((UnityLight) light);
        }

        /**     */
        virtual      public void enableModelCamera (NativeSceneNode model, NativeCamera camera, de.yard.threed.core.Vector3 position, de.yard.threed.core.Vector3 lookat)
        {
            Util.notyet ();
        }


        public Dimension getDimension ()
        {
            UnityEngine.Resolution resolution = UnityEngine.Screen.currentResolution;
            return new Dimension (UnityEngine.Screen.width, UnityEngine.Screen.height);
        }
    }
}