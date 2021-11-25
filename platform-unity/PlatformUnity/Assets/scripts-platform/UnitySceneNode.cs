using System;
using UnityEngine;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.engine.platform.common;
using de.yard.threed.core;

namespace de.yard.threed.platform.unity
{
    public class UnitySceneNode  :  /*16.9.16 UnityObject3D  , */ NativeSceneNode
    {
        Log logger = PlatformUnity.getInstance ().getLog (typeof(UnitySceneNode));

        //MA17
        UnityMesh mesh;
        private GameObject gameObject;
        UnityBase3D object3d;

        public UnitySceneNode (GameObject gameObject, bool existing) //15.9.16 : base (gameObject)
        {
            //TODO: hier wird das mesh nicht gesetzt

            this.gameObject = gameObject;
            if (gameObject == null) {
                logger.error ("gameobject is null");
            }
            object3d = new UnityBase3D (gameObject.transform, this, existing);//14.11.16 object3d;
        }

        /**
         * for existing
         */
        public UnitySceneNode (GameObject gameObject, UnityBase3D object3d)
        {
            //TODO: hier wird das mesh nicht gesetzt
            this.gameObject = gameObject;
            this.object3d = object3d;
            if (gameObject == null) {
                logger.error ("gameobject is null");
            }
            if (object3d == null) {
                logger.error ("object3d is null");
            }
        }

        public static UnitySceneNode build (String name)
        {
            GameObject emptygo = new GameObject ();
            UnitySceneNode n = new UnitySceneNode (emptygo, false);
            n.setName (name);
            return n;
        }

        /**
         * Ein Constructor wegen Convenience.
     *
     * @param mesh
     */
        /*6.10.17 public UnitySceneNode (UnityMesh mesh) //15.9.16 : base (null)
        {
            //add ((de.yard.threed.platform.NativeMesh)mesh);
            setMesh (mesh);
        }*/

        public void setMesh (NativeMesh pmesh)
        {
            if (mesh != null) {
                // bestehendes Mesh entfernen 
		// 11.11.19: Es gibt kein RemoveComponent, man soll Object.Destroy verwenden.
		// Der tatsaecxhliche Destroy erfolgt am Ende der update Loop. Also geht ein Replace wie hier gar nicht.
		// Strange! Also nicht destroyen, sondern weiterverwenden?
                //Mesh.Destroy(mesh.meshFilter);
                //UnityEngine.Mesh.Destroy(mesh.renderer);
		//UnityEngine.Mesh.Destroy(gameObject.GetComponent<MeshFilter>());
		//UnityEngine.Mesh.Destroy(gameObject.GetComponent<MeshRenderer>());
		    mesh = (UnityMesh)pmesh;
		    mesh.meshFilter = (MeshFilter)gameObject.GetComponent (typeof(MeshFilter));
		    mesh.renderer = (Renderer)gameObject.GetComponent<MeshRenderer> ();
            } else {
		    mesh = (UnityMesh)pmesh;
		  
		    //19.7.16 UnityMEsh ist jetzt kein eigenes GO mehr
		    //UnityObject3D.setParent (mesh.gameObject.transform, this.gameObject.transform);
		    //TODO alte Komponenten for add entfernen oder wiederverwenden
		    mesh.meshFilter = (MeshFilter)gameObject.AddComponent (typeof(MeshFilter));
		    mesh.renderer = (Renderer)gameObject.AddComponent<MeshRenderer> ();
	    }
            //Collider collider = (Collider)gameObject.GetComponent<Collider> ();
            //logger.debug ("collider is " + collider);
            //Es gibt wohl keine Default Collider. Darum erstmal immer einen Boxcollider anlegen, damit Picking geht
            //2.3.17: Der ist aber viel zu ungenau. Darum unten einen präziseren MeshCollider
            //23.3.18: Keinen BoxCollider mehr, auch wegen der MEldung BoxColliders does not support negative scale or size.
            /*BoxCollider boxCollider = (BoxCollider)gameObject.AddComponent<BoxCollider> ();
            //boxCollider.size = new UnityEngine.Vector3(5.0f, 5.0f, 0.000001f);
            if (mesh.boxcollidersize != null) {
                boxCollider.size = ((UnityVector3)mesh.boxcollidersize).v;
            }*/
            //UnityEngine.Mesh mesh = new UnityEngine.Mesh ();
            //setMeshBuffer (mesh, vertices, indexes, texCoord, normals);

            if (mesh.mat != null) {
                mesh.renderer.material = mesh.mat.mat;
            } else {
                // wireframe. Das kann Unity aber nicht pro Objekt, sondern nur global. Zumindest nicht so einfach, evtl. mit speziellem
                // Shader. Hmmm,erstmal weglassen.
            }
            mesh.meshFilter.mesh = mesh.geo.mesh;           

            //7.3.17 MeshCollider geht nicht in Android. 23.3.18: Das ist jetzt nachrangig. 
            //Das Mesh muss readable sein, darum darf kein 
            //UploadMeshData (true) gemacht werden.
            MeshCollider collider = (MeshCollider)gameObject.AddComponent<MeshCollider> ();
            collider.isTrigger = false;
            if (!collider.enabled) {
                //10.4.18 kommt aber nicht
                logger.warn ("collider not enabled");
            }
        }

        public NativeMesh getMesh ()
        {
            //MA17 return mesh;
        
            if (gameObject.GetComponent<MeshFilter> () == null) {
                // dann gibt es wohl kein Mesh
                return null;
            }
            UnityEngine.Mesh me = gameObject.GetComponent<MeshFilter> ().sharedMesh;
            UnityGeometry geo = new UnityGeometry (me);
            Renderer renderer = (Renderer)gameObject.GetComponent<MeshRenderer> ();
            //TODO name
            UnityMaterial mat = new UnityMaterial ("", renderer.material, false);
            UnityMesh m = new UnityMesh (geo, mat);
            m.renderer = renderer;
            return m;
        }

        /**
         * der Aufrufer macht einen mirrorZ
         */
        public void setLineMesh (de.yard.threed.core.Vector3 from, de.yard.threed.core.Vector3 to, de.yard.threed.core.Color color)
        {
            LineRenderer lineRenderer = (LineRenderer)gameObject.AddComponent<LineRenderer> ();
            lineRenderer.SetVertexCount (2);
            lineRenderer.SetColors (PlatformUnity.buildColor (color), PlatformUnity.buildColor (color)); 
            lineRenderer.SetPosition (0, UnityVector3.toUnity (from)); 
            lineRenderer.SetPosition (1, UnityVector3.toUnity (to)); 
            lineRenderer.useWorldSpace = true;
            float width = 0.1f;
            lineRenderer.SetWidth (width, width); 

        }

        /**
         * wegen des transform sollte das Gameobject (die SceneNode) exklusiv fuer das Light sein).
         */
        public void setLight (NativeLight plight)
        {
            UnityLight light = (UnityLight)plight;
            UnityEngine.Light lightComp = gameObject.AddComponent<UnityEngine.Light> ();
            lightComp.color = PlatformUnity.buildColor (light.color);
            // die -1 für z und bei der Rotation ist durch probieren entstanden (wegen mirror?). Mirror muss auf jeden Fall beachtet werden.
            // aber geschieht das nicht schon durch world?
            de.yard.threed.core.Quaternion rot = new core.Quaternion ();
            if (light.direction != null) {
                // 26.6.20: ambient has no direction
                rot = MathUtil2.buildQuaternion (new de.yard.threed.core.Vector3 (0, 0, -1),
                                                   new de.yard.threed.core.Vector3 (light.direction.getX (), light.direction.getY (), -light.direction.getZ ()));
                // 10.8.21 the default direction without identity rotation appears along z-axis.
                // 10.8.21: Mit nicht negierten z-Werten ist die Richtung zumindest in ReferenceScene korrekt
                rot = MathUtil2.buildQuaternion (new de.yard.threed.core.Vector3 (0, 0, 1),
                                                   new de.yard.threed.core.Vector3 (light.direction.getX (), light.direction.getY (), light.direction.getZ ()));
            }
            gameObject.transform.rotation = UnityQuaternion.toUnity (rot);
            lightComp.type = LightType.Directional;
            // Soft oder Hard? mal soft.
            lightComp.shadows = LightShadows.Soft;
        }

        public UnityEngine.Camera setCamera ()
        {
            UnityEngine.Camera camera = (UnityEngine.Camera)gameObject.AddComponent<UnityEngine.Camera> ();
            return camera;
        }

        public bool isDestroyed ()
        {
            return gameObject == null;
        }

        public NativeTransform/*Object3D*/ getTransform/*Object3D*/ ()
        {
            return object3d;
        }

        public int getUniqueId ()
        {
            int id = gameObject.GetInstanceID ();
            return id;
        }

        public void destroy ()
        {
            remove (this);
        }


        /**
         * 19.7.16: Soll laut Konvention wie destroy arbeiten
         **/
        private void remove (NativeSceneNode obj)
        {
            UnitySceneNode o = (UnitySceneNode)obj;
            UnityEngine.Object.Destroy (o.gameObject);
            o.gameObject = null;
        }

        virtual public void setName (string name)
        {
            gameObject.name = name;
        }

        virtual public string getName ()
        {
            return gameObject.name;
        }

        public bool isSame (UnitySceneNode obj)
        {
            return gameObject.GetInstanceID () == obj.gameObject.GetInstanceID ();
        }

        public NativeCamera getCamera ()
        {
            foreach (NativeCamera nc in (AbstractSceneRunner.getInstance()).getCameras()) {
                UnityCamera c = (UnityCamera)nc;
                if (c.carrier.isSame (this)) {
                    return c;
                }
            }
            return null;
        }
    }
}
