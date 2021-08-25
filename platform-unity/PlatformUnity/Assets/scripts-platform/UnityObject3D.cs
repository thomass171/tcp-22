using System;
using UnityEngine;
using java.lang;
using java.util;

namespace de.yard.threed.platform.unity
{

    /**
     * 7.4.16: Mal nicht abstract wegen getParent.
     */
    /*26.1.17: public class UnityObject3D  :  UnityBase3D /*Object3D* /
    {
        static Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityObject3D));
        // Die SceneNode, deren Komponente dies Object3D ist
     
        //private int id;
        //static private int uniqueidnumber = 1;

        /*public UnityObject3D() :
        this(createId()) {
    }*/

       


        /* public static UnityMatrix4 getWorldModelMatrix (UnityMatrix4 local, UnityMatrix4 parentworld)
        {
            // 19.8.14: ThreeJS multipliziert aber umgekehrt (three-62.js:7653)
            // 26.8.14: Die Mulitplikation ist M = Parent * local;
            // Das ist so, wie ThreeJS es auch macht.

            UnityMatrix4 transformation = (UnityMatrix4)parentworld.multiply (local);
            //Matrix4 transformation = parenttransformation.multiply(buildModelMatrix());
            return transformation;
        }*/

        /*virtual public int getUniqueId ()
        {
            return id;
        }*/
        /*15.9.16 public int getUniqueId ()
        {
            int id = gameObject.GetInstanceID ();
            return id;
        }*/

        /*7.9.16  private static string createId ()
        {
            return "generatedid-" + uniqueidnumber++;
        }*/

       
  
    
}