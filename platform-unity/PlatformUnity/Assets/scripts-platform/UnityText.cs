using System;
using java.lang;
using UnityEngine;

using de.yard.threed.core.platform;

using java.util;

namespace de.yard.threed.platform.unity
{
    /**
     *
     */
    public class UnityText : UnitySceneNode//15.9.16 :  UnityObject3D
    {
        static Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityText));

        UnityText (GameObject gameObject) : base (gameObject,false)
        {
            logger.debug ("Building UnityText");
        }

        public static UnityText buildText (String text)
        {
            GameObject gameobject = new GameObject ();

            Renderer renderer = (Renderer)gameobject.AddComponent<MeshRenderer> ();
           
           
           // renderer.material = mat.mat;
            //meshFilter.mesh = geo.mesh;

            TextMesh textMesh = (TextMesh)gameobject.AddComponent<TextMesh> ();
            // textMesh.font = font;
            textMesh.text = "Hello World!";

            return new UnityText (gameobject);
        }            
    }
}
