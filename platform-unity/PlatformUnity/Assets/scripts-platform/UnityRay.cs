using System;
using java.lang;
using UnityEngine;
using de.yard.threed.engine;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using java.util;

namespace de.yard.threed.platform.unity
{
    /**
     * Ein Strahl bestimmter Laenge von einem Ausgangspunkt in eine Richtung.
     * Der Unity Ray ist gespiegelt!
     * 3.3.17: Es gibt keinen Grund anzunehmen, dass die driection normalisiert sein muss.
     * 10.4.18: FlightSceneTest liefert Elevation 315. Isr der Ray zu unpraezise?
     * <p/>
     * Created by thomass on 25.11.14.
     */
    public class UnityRay  :  NativeRay
    {
        Log logger = Platform.getInstance ().getLog (typeof(UnityRay));
        UnityEngine.Ray ray;
        //UnityVector3 origin, direction;
        //float length;
        static float maxDistance = Float.MAX_VALUE;

        public UnityRay (UnityEngine.Ray ray)
        {
            this.ray = ray;
            // this.ray.direction = new UnityEngine.Vector3 (-0.31426936f, -0.39283708f, 0.86424184f);
            UnityEngine.Vector3 dir = ray.direction;
           
            /*10.4.18 was soll das? float len = 1000 * 1000;
            dir.Scale (new UnityEngine.Vector3 (len, len, len));*/
            
            /*Debug.DrawRay (ray.origin, dir,
                UnityEngine.Color.white, 1000.0f, false);
            Debug.DrawRay (new UnityEngine.Vector3(0,0,0),new UnityEngine.Vector3(10,10,10),
                UnityEngine.Color.red, 1000.0f, false);
            */
        }

        /**
         * Die Parameter sind Righthanded
         */
        public UnityRay (de.yard.threed.core.Vector3  origin, de.yard.threed.core.Vector3  direction) :
        this (new UnityEngine.Ray (UnityVector3.toUnity(Scene.getWorld ().mirrorZ (origin)),
            UnityVector3.toUnity(Scene.getWorld ().mirrorZ (direction))))
        {
        }

        public override string ToString()
        {
            return "ray from " + getOrigin () + " with direction " + getDirection ();
        }

        virtual public de.yard.threed.core.Vector3 getDirection ()
        {
            return (UnityUtil.mirrorZ (UnityVector3.fromUnity (ray.direction)));
        }

        /**
         * Je nach Ermittlung eines Picking Ray bei Unity  "starting on the near plane of the camera", nicht die Camera position.
         * 2.3.17: Jetzt aber doch Camera.
         */
        virtual public de.yard.threed.core.Vector3 getOrigin ()
        {
            return (UnityUtil.mirrorZ (UnityVector3.fromUnity (ray.origin)));
        }

        /**
         * Die Suche nach einzelnen Objekten geht nur über eine "Layermask". Das lass ich aber erstmal. Darum alle Collisions suchen
         * und dann vergleichen. Das ist tricky, denn es kann ja auch ein Child gehitted worden sein. Das ist haeufig so, z.B.
         * beim GuiGrid.
         * 29.3.18: Das ist jetzt auch nicht mehr definiert.
         */
        virtual public List<NativeCollision> intersects (NativeSceneNode model)
        {
            List<NativeCollision> collisions = new ArrayList<NativeCollision> ();
            List<NativeCollision> allcollisions = getIntersections ();

            intersectsAnyChild (model, collisions, allcollisions);
            return collisions;
        }

        /**
         * Pruefen, ob eine der Collisions "model" trifft.
         */
        private void intersectsAnyChild (NativeSceneNode model, List<NativeCollision> collisions, List<NativeCollision> allcollisions)
        {
            for (int i = 0; i < allcollisions.size (); i++) {
                UnityCollision uc = (UnityCollision)allcollisions.get (i);
                UnitySceneNode hitnode = (UnitySceneNode)uc.getSceneNode ();
                logger.debug ("Hitnode=" + hitnode.getName ());
                if (hitnode.getUniqueId () == ((UnitySceneNode)model).getUniqueId ()) {
                    collisions.add (uc);
                }
                //Debug.Log ("You selected '" + hit.collider.gameObject.name+"'");
            }
            for (int i = 0; i < model.getTransform ().getChildCount (); i++) {
                UnitySceneNode child = (UnitySceneNode)model.getTransform ().getChild (i).getSceneNode ();
                intersectsAnyChild (child, collisions, allcollisions);
            }

        }

        /**
         * Alle Collisions.
         */
        public List<NativeCollision> getIntersections ()
        {
            List<NativeCollision> collisions = new ArrayList<NativeCollision> ();
            RaycastHit[] hits = Physics.RaycastAll (ray/*, maxDistance*/);
            for (int i = 0; i < hits.Length; i++) {
                collisions.add (new UnityCollision (hits [i]));
                //Debug.Log ("You selected '" + hit.collider.gameObject.name+"'"); 
            }
            logger.debug ("" + collisions.size()+" collisions found total with ray "+ToString()+" and distance "+maxDistance);

            return collisions;
        }
    }
}