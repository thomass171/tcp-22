using System;
using java.lang;

using de.yard.threed.core;
using de.yard.threed.core.platform;
using java.util;
using de.yard.threed.engine.platform.common;

namespace de.yard.threed.platform.unity
{ 
    /**
     * Im Unity Jargon ist das ein Mesh.
     * 
     * Created by thomass on 29.01.16.
     */
    public class UnityGeometry  :  NativeGeometry
    {
        Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityGeometry));

        public UnityEngine.Mesh mesh;

        public UnityGeometry (UnityEngine.Mesh mesh)
        {
            this.mesh = mesh;
            //mesh.UploadMeshData (true);
            // ob die Buffer noch da sind? Sind sie nach dem obigem upload nicht mehr. Speicherverbrauch sinkt aber trotzdem nicht merklich.
            //logger.debug("mesh.vertices count="+mesh.vertices.Length);
            if (!mesh.isReadable) {
                logger.warn ("mesh is not readable");
            }
        }

        public static UnityGeometry buildGeometry (SimpleGeometry simpleGeometry)
        {
            //List<Face3List> flist = new ArrayList<Face3List>();
            //flist.add(simpleGeometry.faces);
            return buildGeometry (simpleGeometry.getVertices (), simpleGeometry.getIndices(),simpleGeometry.getUvs(), simpleGeometry.getNormals ());
        }

        public static UnityGeometry buildGeometry (UnityEngine.Vector3[] vertices, int[] indexes, UnityEngine.Vector2[] texCoord, UnityEngine.Vector3[] normals)
        {          
            UnityEngine.Mesh mesh = new UnityEngine.Mesh ();
            setMeshBuffer (mesh, vertices, indexes, texCoord, normals);
            return new UnityGeometry (mesh);
        }

        public static UnityGeometry buildGeometry (Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals)
        {
            /* GameObject capsule = GameObject.CreatePrimitive(PrimitiveType.Capsule);
            Renderer renderer = capsule.GetComponent<Renderer>();
            Debug.Log ("renderer=" + renderer);
            Debug.Log ("renderer.material=" + renderer.material);
            capsule.transform.position = new UnityEngine.Vector3(2, 3, 0);
            MeshFilter meshFilter = (MeshFilter)capsule.GetComponent(typeof(MeshFilter));
           // meshFilter.mesh = mesh;
            UnityEngine.Material newMat1 = Resources.Load("MyMaterial/MyBasicMaterial", typeof(UnityEngine.Material)) as UnityEngine.Material;
            renderer.material = newMat1;*/

            //UnityVBO vbo = moveToNative (vertices, faces, normals/*, , uvs*/);

            //return buildGeometry (vbo.vertices.list.ToArray (), vbo.getIndexes (), vbo.uvs.list.ToArray (), vbo.normals.list.ToArray ());
            return buildGeometry(buildVector3Array(vertices), indices, buildVector2Array(uvs),(normals==null)?null:buildVector3Array(normals));

            //Material newMat = new Material(Shader.Find("Standard"));
            //newMat.CopyPropertiesFromMaterial(dummyObjMat); 
            //newMat.EnableKeyword("_NORMALMAP"); //etc.
            //renderer.material = new Material (Shader.Find(" Diffuse"));

        }

        private static UnityEngine.Vector3[] buildVector3Array(Vector3Array v) {
            UnityEngine.Vector3[] arr = new UnityEngine.Vector3[v.size()];
            for (int i = 0; i < v.size(); i++) {
                arr[i] = UnityVector3.toUnity(v.getElement(i));
            }
            return arr;
        }

        private static UnityEngine.Vector2[] buildVector2Array(Vector2Array v) {
            UnityEngine.Vector2[] arr = new  UnityEngine.Vector2[v.size()];
            for (int i = 0; i < v.size(); i++) {
                Vector2 v2 = v.getElement(i);
                arr[i] = new UnityEngine.Vector2((float)v2.getX(),(float)v2.getY());
            }
            return arr;
        }

        /*18.7.16: Passt konzeptionell nicht public static UnityGeometry buildCubeGeometry(float width, float height, float depth){
            GameObject cube = GameObject.CreatePrimitive (PrimitiveType.Cube);
            // Groesse setzen geht bei Unity ueber scale
            cube.transform.localScale = Vector3 (width,height,depth);
            //Renderer renderer = capsule.GetComponent<Renderer> ();
            // Debug.Log ("renderer=" + renderer);
            // Debug.Log ("renderer.material=" + renderer.material);
            // capsule.transform.position = new UnityEngine.Vector3(2, 3, 0);
            MeshFilter meshFilter = (MeshFilter)cube.GetComponent (typeof(MeshFilter));
            // meshFilter.mesh = mesh;
            return new UnityGeometry (meshFilter.mesh);
        }*/

        virtual public string getId ()
        {
            return "";
        }

        public static void setMeshBuffer (UnityEngine.Mesh  mesh, UnityEngine.Vector3[] vertices, int[] indexes, UnityEngine.Vector2[] texCoord, UnityEngine.Vector3[] normals)
        {
            mesh.vertices = vertices;
            mesh.triangles = indexes;
            mesh.uv = texCoord;
            if (normals != null) {
                mesh.normals = normals;
            } else {
                mesh.RecalculateNormals ();
            }
            mesh.RecalculateBounds ();
            //14.12.17: no longer supported mesh.Optimize ();   

        }

       /* private static UnityVBO moveToNative (NativeVector3Array vertices, Face3List faces, NativeVector3Array normals)
        {
            UnityVBO vbo = new UnityVBO ();
            GeometryHelper.buildVBOandTriangles (vertices, faces, vbo, normals);
            return vbo;           
        }*/


    }
}
