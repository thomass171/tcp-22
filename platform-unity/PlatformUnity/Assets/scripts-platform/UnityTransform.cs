using System;
using UnityEngine;
using java.lang;

using de.yard.threed.core;


namespace de.yard.threed.platform.unity
{

    /**
 * Position, Rotation und Scale Attribute als Superklasse fuer alles, was die verwendet.
 */
    public class UnityTransform
    {
        //  Log logger = Platform.getInstance ().getLog (typeof(UnityBase3D));

     
        static public /*Native*/de.yard.threed.core.Quaternion getRotation (UnityEngine.Transform transform)
        {
            /*Unity*/de.yard.threed.core.Quaternion q = UnityQuaternion.fromUnity (transform.localRotation);
            //14.4.
            //q = (UnityQuaternion)Platform.getInstance ().getWorld ().mirror (q);
            return q;
        }

        static public /*Native*/de.yard.threed.core.Vector3 getPosition (UnityEngine.Transform transform)
        {
            /*Unity*/de.yard.threed.core.Vector3 p = UnityVector3.fromUnity (transform.localPosition);
            //p =  Platform.getInstance ().getWorld ().mirrorZ (p);
            return p;
        }

        static public void setPosition (UnityEngine.Transform transform, /*Native*/de.yard.threed.core.Vector3 position)
        {
            //position = Platform.getInstance ().getWorld ().mirrorZ (position);
            transform.localPosition = UnityVector3.toUnity(position);
        }

        static public void setRotation (UnityEngine.Transform transform, /*Native*/de.yard.threed.core.Quaternion rotation)
        {
            UnityEngine.Quaternion q = UnityQuaternion.toUnity(rotation);
            //14.4.16: Hier nicht spiegeln, weil das indirekt über die world matrix geht.
            //q = (UnityQuaternion)Platform.getInstance ().getWorld ().mirror (q);
            transform.localRotation = q/*.q*/;
        }

        static public /*Native*/de.yard.threed.core.Vector3 getScale (UnityEngine.Transform transform)
        {
            return UnityVector3.fromUnity (transform.localScale);
        }

        /**
         * 15.11.16: Fuer die Camera passt der Unity Transform, wahrscheinlich weil sich Spiegeleffekt und Cameraorientrieung
         * gegenseitig wieder aufheben.
         */
        static public void translateOnAxisCamera (UnityEngine.Transform transform, /*Native*/de.yard.threed.core.Vector3 axis, double distance)
        {
            transform.Translate (UnityVector3.toUnity(axis) * (float)distance, Space.Self);
        }

        /**
         * 15.11.16: Fuer "normale" Objekte. Der Unity transform geht - offenbar wegen der Spiegelung - in die falsche Richtung.
         * Oder das mit dem Space.Self haut nicht hin. Siehe ReferencesScene whitebox FPC.
         */
        static public void translateOnAxis (UnityEngine.Transform transform, /*Native*/de.yard.threed.core.Vector3 axis, double distance)
        {
            //15.11.16: Spiegeln bringt nichts. Der translate arbeitet anders als in JME/ThreeJS
            //axis = Platform.getInstance ().getWorld ().mirrorZ (axis);
            //transform.Translate (((UnityVector3)axis).v * distance, Space.Self);
            UnityEngine.Vector3 v = transform.localRotation * (UnityVector3.toUnity(axis)) * (float)distance;
            transform.localPosition += v;
        }

        static public void setScale (UnityEngine.Transform transform, /*Native*/de.yard.threed.core.Vector3 scale)
        {
            transform.localScale = UnityVector3.toUnity(scale);
        }

        static public void rotateOnAxis (UnityEngine.Transform transform, /*Native*/de.yard.threed.core.Vector3 axis, double angle)
        {
          //  axis = ((PlatformUnity)Platform.getInstance ()).getWorld ().mirrorY (axis);
            double degree = MathUtil2.toDegrees (angle);
            //noch nicht gaz klar, ob die Verwenundung des axis vector so ok ist.
            transform.Rotate (UnityVector3.toUnity(axis)/*.v*/ * (float)degree);
        }

        /**
         * Hat Unity nicht so einfach verfuegbar. Aber ueber das rausrechnen des Parent aus der
         * Worldmatrix muesste es gehen.
         */
        static public /*Native*/Matrix4 getLocalModelMatrix (UnityEngine.Transform transform)
        {
            Matrix4x4 worldmatrix = transform.localToWorldMatrix;
            Matrix4x4 mir = new UnityMatrix4 (
                                1, 0, 0, 0,
                                0, 1, 0, 0,
                                0, 0, -1, 0,
                                0, 0, 0, 1).m;
            //   worldmatrix = mir * worldmatrix;
            if (transform.parent != null) {
                Matrix4x4 pi = transform.parent.localToWorldMatrix.inverse;
                //TODO pruefen?
                        return UnityMatrix4.fromUnity (pi * worldmatrix);
            }
                    return  UnityMatrix4.fromUnity (worldmatrix);
        }
           
      /*2.2.18  static public void LookAt (UnityEngine.Transform transform, NativeVector3 target)
        {
            //1.2.18 auch fuer mode0? Der Unity lookat erwartet world space. Ich stecke aber
            //local rein?
            target = Platform.getInstance ().getWorld ().mirrorZ (target);
            transform.LookAt (((UnityVector3)target).v);
        }

        static public void LookAt (UnityEngine.Transform transform, NativeVector3 target, NativeVector3 upVector)
        {
            target = Platform.getInstance ().getWorld ().mirrorZ (target);
            transform.LookAt (((UnityVector3)target).v,((UnityVector3)upVector).v);
        }*/
    }
}