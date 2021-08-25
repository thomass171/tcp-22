using System;
using java.lang;
using UnityEngine;
using de.yard.threed.core.platform;
using java.util;

namespace de.yard.threed.platform.unity
{
    public class UnityCollision  :  NativeCollision
    {
        Log logger = Platform.getInstance ().getLog (typeof(UnityCollision));
        RaycastHit hit;
        UnityVector3 point = null;

        public UnityCollision (RaycastHit hit)
        {
            this.hit = hit;
            this.point = new UnityVector3(hit.point);
        }

        public NativeSceneNode getSceneNode() {
            return new UnitySceneNode (hit.collider.gameObject,true);
        }

        public de.yard.threed.core.Vector3 getPoint() {
            return UnityVector3.fromUnity(point.v);
        } 
    }
}