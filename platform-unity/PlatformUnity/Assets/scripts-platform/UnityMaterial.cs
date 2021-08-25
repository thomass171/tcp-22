using System;
using java.lang;
using de.yard.threed.engine;
using de.yard.threed.engine.platform.common;

using de.yard.threed.core.platform;
using de.yard.threed.core.resource;
using de.yard.threed.core;
using java.util;
using UnityEngine;

namespace de.yard.threed.platform.unity
{
    /**
     */
    public class UnityMaterial  :  NativeMaterial
    {
        public UnityEngine.Material mat;
        bool transparent;
        //static Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityMaterial));
        string name;

        public UnityMaterial (string name, UnityEngine.Material mat, bool transparent)
        {
            this.name = name;
            this.mat = mat;//Resources.Load("MyMaterial/MyBasicMaterial", typeof(UnityEngine.Material)) as UnityEngine.Material;
            this.transparent = transparent;
        }

        public static NativeMaterial buildMaterial (MaterialDefinition definition/*string name, HashMap<ColorType, de.yard.threed.core.Color> colors, HashMap<String, NativeTexture> textures, HashMap<NumericType, NumericValue> parameter*/, Effect effect)
        {
            de.yard.threed.core.Color col = (definition.color == null) ? null : definition.color.get (ColorType.MAIN);
            UnityEngine.Material mat;

            Nullable<Single> transparency = NumericValue.transparency (definition.parameters);

            NativeTexture texture1 = (definition.texture == null) ? null : definition.texture.get ("basetex");
            if (effect != null) {
                // TODO Der Effect kann ja auch einen Shader vorgeben
                //mat = new UnityEngine.Material (Shader.Find ("Diffuse"));
                mat = new UnityEngine.Material (Shader.Find ("Standard"));
                if (col != null) {
                    mat.SetColor ("_Color", PlatformUnity.buildColor (col));
                }
                if (texture1 != null) {
                    mat.SetTexture ("_MainTex", ((UnityTexture)texture1).texture); 
                }

                if (transparency != null) {
                    setTransparent (mat, true);
                }

               
                int textureindex = 0;
                // 30.12.15 ist jetzt normale List
                /* foreach (Uniform uniform in effect.shader.uniforms) {
                    // Uniform uniform = effect.uniforms.get(key);

                    switch (uniform.type) {
                    case FLOAT_VEC4:
                        //TODOmat.setColor(uniform.name, ((JmeColor) col).color); // with Lighting.j3md
                        break;
                    case SAMPLER_2D:
                        JmeTexture texture2 = (JmeTexture)textures.get (uniform.name);
                        if (texture2 == null) {
                            throw new RuntimeException ("uniform not set:" + uniform.name);
                        }
                        mat.setTexture (uniform.name, ((JmeTexture)texture2).texture);

                        break;
                    case BOOL:
                        //TODO Die Zuordnung passt so nicht. Nur abhängig vom Typ ein uniform setzen?
                        if (NumericValue.unshaded (parameter)) {
                            mat.setBoolean ("isunshaded", true);
                        }
                        break;
                    default:
                        throw new RuntimeException ("unknown uniform type " + uniform.type);
                    //todo andere Fehlerbahdnlung
                    //   return null;
                    }
                }*/
                return new UnityMaterial (definition.name, mat, transparency != null);
            }

            // Moegliche Shader sind "Transparent/Diffuse","Diffuse","Standard" und viele mehr, siehe Unity Inspector.
            if (NumericValue.unshaded (definition.parameters)) {
                mat = new UnityEngine.Material (Shader.Find ("Standard"));
                // Die unlit Shader stellen keine Schatten dar.
                //5.10.17: unshaded kann trotzdem transparent sein. Und das koenne die normalen Unlit-Shader wohl nicht. Also mal transparent
                //shader versuchen. Aber nicht immer, das geht evtl. auf die Performance.
                //Beim 777 CDU gibt es Darstellungsfehler je nach Blickwinkel. Mal sieht man Buchstaben, mal nicht. Evtl.
                //passen da manche der Transparenzparameter nicht. 6.10.17:Doch immer transparent shader, damit nicht später ändern muss.
                //Mit Unlit/Color geht das aber nicht (RefSceneTower). Dann versuch ich doch mal "Standard". Der ist zwar dann nicht
                //unbedingt unshaded, aber vielleicht kann man das abschalten.
                if (col != null) {
                    /*if (transparency == null ) {
                        mat = new UnityEngine.Material (Shader.Find ("Unlit/Color"));
                    } else {
                        mat = new UnityEngine.Material (Shader.Find ("Unlit/Transparent"));
                    }*/
                    mat.SetColor ("_Color", PlatformUnity.buildColor (col));
                }
                if (texture1 != null) {
                    /*if (transparency == null && false) {
                        mat = new UnityEngine.Material (Shader.Find ("Unlit/Texture"));
                    } else {
                        mat = new UnityEngine.Material (Shader.Find ("Unlit/Transparent"));
                    }*/
                    mat.SetTexture ("_MainTex", ((UnityTexture)texture1).texture); 
                }


            } else {
                mat = new UnityEngine.Material (Shader.Find ("Standard"));
                if (col != null) {
                    mat.SetColor ("_Color", PlatformUnity.buildColor (col));
                }
                if (texture1 != null) {
                    //_MainTex ist quasi "Albedo"
                    mat.SetTexture ("_MainTex", ((UnityTexture)texture1).texture); 
                }
                NativeTexture normalmap = (definition.texture == null) ? null : definition.texture.get ("normalmap");
                if (normalmap != null) {
                    mat.SetTexture ("_BumpMap", ((UnityTexture)normalmap).texture);
                    mat.shaderKeywords = new string[1]{ "_NORMALMAP" };
                    //mat.SetTexture ("_LightMap", ((UnityTexture)normalmap).texture);
                    //mat.shaderKeywords = new string[1]{ "_LIGHTMAP" };
                }
            }
            //6.10.17tranparency immer klar ein/ausschalten? Gibt es z.Z. keinen Hinweis drauf.
            if (transparency != null) {
                setTransparent (mat, transparency != null);
            }

            return new UnityMaterial (definition.name, mat, transparency != null);
        }

        public void setWireframe (bool b)
        {
        }

        public bool isTransparent ()
        {
            return transparent;
        }

        public void setTransparency (bool transparency)
        {
            if (!transparency) {
                setTransparent (mat, false);
                transparent = false;
            } else {
                setTransparent (mat, true);
                transparent = true;
            }
        }

        /**
         * Ob das
         * Doc: https://docs.unity3d.com/Manual/SL-Blend.html und
         * http://forum.unity3d.com/threads/change-standard-shader-render-mode-in-runtime.318815/
         * 
         * Zur Transparenz auf Android siehe Wiki.
         * 
         * Das deaktivieren von transparancy scheint nicht zu gehen, wenn es mal enabled war.
         */
        private static void setTransparent (UnityEngine.Material mat, bool enabled)
        {
            // Das Setzen von _Mode duerfte totaler Quatsch sein. Das ist doch wohl aus der GUI.

            if (enabled) {
                //Der Standardshader rendered transparent auch mit Diffuse Reflexion, also Lichteinfluss.
                //Das scheint man nicht wegzubekommen.
                //mat.SetFloat ("_Mode", 3);//UnityEngine.BlendMode.Transparent);
                mat.SetInt ("_SrcBlend", (int)UnityEngine.Rendering.BlendMode.One);
                mat.SetInt ("_DstBlend", (int)UnityEngine.Rendering.BlendMode.OneMinusSrcAlpha);
                mat.SetInt ("_ZWrite", 0);
                mat.DisableKeyword ("_ALPHATEST_ON");
                mat.DisableKeyword ("_ALPHABLEND_ON");
                mat.EnableKeyword ("_ALPHAPREMULTIPLY_ON");
                mat.renderQueue = 3000;
            } else {
                // BlendMode.Opaque aus http://forum.unity3d.com/threads/change-standard-shader-render-mode-in-runtime.318815/
                //mat.SetFloat ("_Mode", 0);//
                mat.SetInt ("_SrcBlend", (int)UnityEngine.Rendering.BlendMode.One);
                mat.SetInt ("_DstBlend", (int)UnityEngine.Rendering.BlendMode.Zero);
                mat.SetInt ("_ZWrite", 1);
                mat.DisableKeyword ("_ALPHATEST_ON");
                mat.DisableKeyword ("_ALPHABLEND_ON");
                mat.DisableKeyword ("_ALPHAPREMULTIPLY_ON");
                mat.renderQueue = -1;
            }
        }

        public void setName (string n)
        {
            name = n;
        }

        public string getName ()
        {
            return name;
        }

        public NativeTexture[] getMaps ()
        {
            //TODO
            return new NativeTexture[]{ };
        }
    }
}