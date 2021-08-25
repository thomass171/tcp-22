using System;
using System.IO;
using UnityEngine;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using de.yard.threed.core.resource;

namespace de.yard.threed.platform.unity
{
   
    public class UnityTexture  :  NativeTexture
    {
        static Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityTexture));
        public Texture2D texture;

        public UnityTexture (Texture2D texture)
        {
            this.texture = texture;
        }

        /**
         * liefert null bei Fehler, aber keine Exception wegen difizilem Exceptionhandling..
         */
        public static UnityTexture loadFromFile (NativeResource filename)
        {
            long starttime = Platform.getInstance ().currentTimeMillis ();
            //TODOif (!filename.startsWith("/")){
            //TODO  auch hier noch klären
            //19.3.16 koennte ueber CP jetzt gehen filename = JmeRessourceManager.RESOURCEPREFIX+"/"+filename;
            //}
            //13.11.15: zu langsam: return buildFromImage(JmeImageUtil.buildImageData(JmeImageUtil.loadImageFromFile(new File(filename))));
            //14.6.16:Je nach Herkunft der Texture kann es schon als Texture2D oder nur als ByteStream vorliegen.
            //24.4.17: Der Unity Resources.load kann nicht mit System paths arbeiten. Er mag aber effizienter sein.
            //Darum erst damit versuche, ansonsten normal ueber Bundle.
            //    LoadedResource lr = UnityResourceManager.getInstance ().loadResourceSync (filename);

            UnityEngine.Object loadedObject = Resources.Load (filename.getFullName ()); 
            if (loadedObject != null) {
                /*if (loadedObject is TextAsset) {
                    TextAsset textAsset = (TextAsset)loadedObject;
                    // "b64" wurde oben drangehangen.
                    if (path.EndsWith (".3ds.b64")) {
                        byte[] decodedByteArray = Convert.FromBase64String (textAsset.text);
                        return new LoadedResource (new ByteArrayInputStream (decodedByteArray));
                    }
                    return new LoadedResource (new ByteArrayInputStream (textAsset.bytes));
                }*/
                if (loadedObject is Texture2D) {
                    // Imagedateien wie png und jpgs
                    Texture2D texture = (Texture2D)loadedObject;                                  
                    return new UnityTexture (texture);
                }
            }

            /* UnityTexture tex;
            if (lr.texture != null) {
                tex = (UnityTexture)lr.texture;
            } else {
                tex = buildFromImage (lr.inputStream.readFully ());
            }*/
            // 24.10.17: Keine ResourceNotfoundexception rausgeben.
            byte[] bytebuf;
            try {
                bytebuf = new UnityResourceManager ().loadBinaryFile (filename.getFullName ());
            } catch (System.Exception e) {
                logger.error ("IO Exception" + e.Message);
                return null;
            }
            UnityTexture tex = buildFromImage (bytebuf);
            logger.debug ("loadFromFile took " + ((Platform.getInstance ()).currentTimeMillis () - starttime) + " ms");
           
            return tex;
            //return new UnityTexture (loadImageFromFile (/*filename*/"/Users/thomas/Projekte/ThreeJs/images/Dangast.jpg"));
        }

        public static UnityTexture buildFromImage (ImageData imagedata, bool fornormalmap)
        {
            //ARGB32 (32 bit with alpha) and no mipmaps
            Texture2D tex = new Texture2D (imagedata.width, imagedata.height, TextureFormat.ARGB32, false);
            UnityEngine.Color[] pixels = new UnityEngine.Color[imagedata.width * imagedata.height];
            for (int y = 0; y < imagedata.height; y++) {
                for (int x = 0; x < imagedata.width; x++) {
                    int i = y * imagedata.width + x;
                    int pi = (imagedata.height - y - 1) * imagedata.width + x;
                    de.yard.threed.core.Color col = new de.yard.threed.core.Color (imagedata.pixel [i]);
                    if (fornormalmap) {
                        //fuer normal maps. Die werden anders gespeichert. Evtl. nicht ganz stimmig. Optisch aber erstmal ganz ok.
                        //pixels [pi] = new UnityEngine.Color (0/*col.getG ()*/, col.getG (), 0/*col.getG ()*/, col.getR ());
                        pixels [pi] = new UnityEngine.Color (col.getG (), col.getG (), col.getG (), col.getR ());
                    } else {
                        pixels [pi] = new UnityEngine.Color (col.getR (), col.getG (), col.getB (), col.getAlpha ());
                    }

                }
            }
            // Op miplevel sein mss und wofuer ist unklar.
            int miplevel = 0;
            tex.SetPixels (pixels, miplevel);
            if (fornormalmap) {
                // fuer NormalMap führt bi/tri Filtering leicht zu Entartungen, weil die Normalen empfindlich sind.
                tex.filterMode = FilterMode.Point;
                // Zumindest bei der Wall sieht bilin doch etwas besser aus.
                tex.filterMode = FilterMode.Bilinear;
            } else {
                // Ob Point gut ist, muss sich noch zeigen. Evtl. muss das ganze ein Parameter werden.
                tex.filterMode = FilterMode.Point;
            }
            tex.Apply ();
            return new UnityTexture (tex);
        }

        /**
         * Der Texturtyp muss dafuer auf "advanced" gesetzt werden und read/write aktiviert werden
         */
        public static ImageData buildImageData (Texture2D texture)
        {
            int width = texture.width;
            int height = texture.height;
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = y * width + x;
                    UnityEngine.Color col = texture.GetPixel (x, height - y - 1);
                    pixels [i] = new  de.yard.threed.core.Color (col.r, col.g, col.b, col.a).getARGB ();                 
                    /* if (pixels[i] != 0) {
                        logger.debug("pixel!=0 at "+i+",x="+x+",y="+y);
                       
                    }*/
                }
            }
            ImageData img = new ImageData (width, height, pixels);
            return img;
        }


        public static UnityTexture buildFromImage (byte[] image)
        {
            Texture2D tex = null;
            tex = new Texture2D (2, 2);
            tex.LoadImage (image); //..this will auto-resize the texture dimensions.
            //Mipmaps neu erzeugen und Speicher freigeben.
            //Mipsmaps weglassen wegen Resourcenbedarf. Hat aber alles offenbar keinen Effekt.
            tex.Apply (false, true);
            return new UnityTexture (tex);
        }

        public static UnityTexture buildFromImage (Texture2D tex)
        {
            return new UnityTexture (tex);//new com.jme3.texture.Texture2D(JmeImageUtil.buildJmeImage(image)));

        }

        public String getName ()
        {
            return "notyet";
        }
    }
}
