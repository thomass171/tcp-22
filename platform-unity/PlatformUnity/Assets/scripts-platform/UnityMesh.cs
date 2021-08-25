using System;
using java.lang;
using UnityEngine;
using de.yard.threed.engine;
using de.yard.threed.core;
using de.yard.threed.core.platform;

using java.util;

namespace de.yard.threed.platform.unity
{
    /**
     * Dies ist noch keine GameObject. Das entsteht erst in SceneNode.
     * 
     * Der eigentliche Unity Mesh liegt in UnityGeometry.
     */
    public class UnityMesh  :  NativeMesh/*9.8.21 gibts nicht mehr? , NativeSceneNodeComponent*/
    {
        static Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityMesh));
        bool istransparent;
        public  UnityGeometry geo;
        public UnityMaterial mat;
        public MeshFilter meshFilter;
        public Renderer renderer;
        public de.yard.threed.core.Vector3 boxcollidersize = null;

        /**
         * siehe Platform Interface.
         * 6.10.17: Auch fuer existierende.
         */
        public UnityMesh (UnityGeometry geo, UnityMaterial mat)
        {
            this.geo = geo;
            this.mat = mat;
           
            if (Main.gcpermesh) {
                Resources.UnloadUnusedAssets ();
                System.GC.Collect ();
            }
        }

        virtual public void updateMesh (UnityGeometry geo, UnityMaterial mat)
        {            
            if (geo != null/*vertices != null && faces != null*/) {
                meshFilter.mesh = geo.mesh;
            }
            if (mat != null) {
                istransparent = mat.isTransparent ();
                renderer.material = mat.mat;
            }
        }

        public static UnityMesh buildMesh (UnityGeometry geo, UnityMaterial mat, bool castShadow, bool receiveShadow)
        {
            

            /* Text str;
            str = gameobject.AddComponent<Text> ();
            str.text = "Hello World";
        str.font = Font.*/

            return new UnityMesh (/*gameobject*/geo, mat);
            /*  Geometry geom = new Geometry(JmeScene.getInstance().getUniqueName(), mesh);
            geom.setMaterial(mat.material);
            if (castShadow) {
                if (receiveShadow) {
                    geom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                } else {
                    geom.setShadowMode(RenderQueue.ShadowMode.Cast);
                }
            } else {
                if (receiveShadow) {
                    geom.setShadowMode(RenderQueue.ShadowMode.Receive);
                } else {
                    geom.setShadowMode(RenderQueue.ShadowMode.Off);
                }
            }
            if (mat.isTransparent()) {
                geom.setQueueBucket(RenderQueue.Bucket.Transparent);
            }
            return new JmeMesh(geom);*/
        }

        public NativeMaterial getMaterial ()
        {
            return mat;
        }

        public void setBoxColliderSizeHint (de.yard.threed.core.Vector3 boxcollidersize)
        {
            this.boxcollidersize = boxcollidersize;
        }
            
        public NativeSceneNode getSceneNode() {
            Util.notyet ();
            return null;
        }
    }
}