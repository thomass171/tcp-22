using System;
using System.IO;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.core.platform;
using de.yard.threed.core.resource;
using de.yard.threed.core.buffer;
using de.yard.threed.core;

using de.yard.threed.outofbrowser;

namespace de.yard.threed.platform.unity
{
    /**
     * 
     */
    public class UnityResourceManager  : NativeResourceReader/*ResourceManager*/
    {
        Log logger = Platform.getInstance ().getLog (typeof(UnityResourceManager));
        //TODO
        public const string RESOURCEPREFIX = "/Users/thomas/Projekte/Granada/data/src/main/resources/";
        protected static UnityResourceManager instance;

        public UnityResourceManager ()
        {
            //zum Test
            /*listFiles ("/");
            listFiles ("/storage");
            listFiles ("/storage/external_SD");
            listFiles ("/storage/external_SD/MyAircraft");
                 listFiles ("/mnt");
            listFiles ("/mnt/sdcard");
             listFiles ("/sdcard");*/
            //listFiles ("/sdcard/Download");
            //   listFiles ("/sdcard/MyAircraft");
        }

        /*9.8.21 public static UnityResourceManager getInstance ()
        {
            if (instance == null) {
                instance = new UnityResourceManager ();
            }
            return instance;
        }*/

        /*16.10.18 public void loadRessource (NativeResource ressource, ResourceLoadingListener loadlistener)
        {
            logger.info ("loadRessource:" + ressource.getFullName ());

            byte[] bytebuf;// = new byte[0];
            try {
                bytebuf = loadResourceSync (ressource).inputStream.readFully ();
                loadlistener.onLoad (new BundleData (StringUtils.buildString (bytebuf)));
            } catch (ResourceNotFoundException e) {
                loadlistener.onError (22/*TODO e* /);
            }
        }*/

        /**
         * Das geht jetzt mal nur mit den von Unity unterstützten Formaten. Wie praktikabel das ist, muss sich noch zeigen.
         * 3DS Dateien sind base64 codiert gespeichert mit Endungen ...3ds.b64.txt
         */
        public UnityLoadedResource loadResourceSync (NativeResource ressource) /*throws RessourceNotFoundException*/
        {
            byte[] bytebuf = null;
            if (ressource.isBundled ()) {
                //xxResources.Load("MyMaterial/MyBasicMaterial", typeof(UnityEngine.Material)) as UnityEngine.Material;
                string path = ressource.getFullName ();
                // Der Suffix muss weg, aber nur bei regulären Dateien und nicht den verkappten Textdateien. Da muss vielmehr noch einer dran.
                if (path.EndsWith (".ac")) {
                    // Nothing to do.
                } else {
                    if (path.EndsWith (".3ds")) {
                        path += ".b64";
                    } else {
                        int index = StringUtils.lastIndexOf (path, ".");
                        if (index != -1) {
                            path = StringUtils.substring (path, 0, index);
                        }
                    }
                }
                UnityEngine.Object loadedObject = Resources.Load (path); 
                if (loadedObject == null) {
                    throw new ResourceNotFoundException (ressource.getFullName ());
                } 
                if (loadedObject is TextAsset) {
                    TextAsset textAsset = (TextAsset)loadedObject;
                    // "b64" wurde oben drangehangen.
                    if (path.EndsWith (".3ds.b64")) {
                        byte[] decodedByteArray = Convert.FromBase64String (textAsset.text);
                        return new UnityLoadedResource (new ByteArrayInputStream (new SimpleByteBuffer (decodedByteArray)));
                    }
                    return new UnityLoadedResource (new ByteArrayInputStream (new SimpleByteBuffer (textAsset.bytes)));
                }
                if (loadedObject is Texture2D) {
                    // Imagedateien wie png und jpgs
                    Texture2D texture = (Texture2D)loadedObject;                                  
                    return new UnityLoadedResource (UnityTexture.buildFromImage (texture));
                }
                throw new ResourceNotFoundException ("unknown " + (loadedObject.GetType ()) + ":" + ressource.getFullName ());
            } else {
                //Der Unity Resources.load kann nicht mit System paths arbeiten
                try {
                    return new UnityLoadedResource (new ByteArrayInputStream (new SimpleByteBuffer (UnityFileReader.getFileStream (ressource.getFullName ()))));
                } catch (System.Exception e) {
                    throw new ResourceNotFoundException (ressource.getFullName () + ":" + e.Message);
                }
            }
            //return UnityFileReader.getFileStream(((UnityFile)ressource).path);
          
        }
    
       /*7.10.19  override public NativeOutputStream saveResourceSync (NativeResource resource)
        {
            // fname zum pruefen nehmen, aber fullname zum speichern
            string fname = resource.getName ();
            // Secuirtycheck um nicht versehentlich wichtige Dateien zu ueberschreiben.
            if (!fname.Contains ("SL") && !fname.StartsWith ("GRCH")) {
                logger.error ("saving resource security check failed");
                de.yard.threed.core.Util.notyet ();
            }
            string fullname = resource.getFullName ();
            if (!fullname.Contains ("SL") && !fullname.Contains ("GRCH")) {
                logger.error ("saving resource security check failed");
                de.yard.threed.core.Util.notyet ();
            }
            try {
                return new UnityOutputStream (fullname);
            } catch (System.Exception e) {

                throw new ResourceSaveException ("saveResourceSync failed: " + e.Message);
            }
        }*/

        /**
         * zum testen
         */
        private void listFiles (String path)
        {
            if (File.Exists (path)) {
                // This path is a file
                ProcessFile (path); 
            } else if (Directory.Exists (path)) {
                // This path is a directory
                ProcessDirectory (path);
            } else {
                logger.error (path + " is not a valid file or directory.");
            }   
        }

        // Process all files in the directory passed in, recurse on any directories
        // that are found, and process the files they contain.
        public  void ProcessDirectory (string targetDirectory)
        {
            // Process the list of files found in the directory.
            string[] fileEntries = Directory.GetFiles (targetDirectory);
            foreach (string fileName in fileEntries)
                ProcessFile (fileName);

            // Recurse into subdirectories of this directory.
            string[] subdirectoryEntries = Directory.GetDirectories (targetDirectory);
            foreach (string subdirectory in subdirectoryEntries)
                /*ProcessDirectory(subdirectory);*/
                logger.debug ("processed dir " + subdirectory);
        }

        // Insert logic for processing found files here.
        public  void ProcessFile (string path)
        {
            logger.debug ("processed file " + path);       
        }

        /*9.8.21 override
        public void completeBundle (BundleResource file)
        {
            BundleLoaderExceptGwt.completeBundle (file);
        }*/

        /**
     * Nicht relevant, weil sync geladen wird.
     * @param file
     * @return
     */
       /*9.8.21  override        public bool isLoading (BundleResource file)
        {
            return false;
        }*/

        /*9.8.21 override public void loadBundle (string bundlename, bool delayed)
        {
            BundleLoaderExceptGwt.loadBundle (bundlename, delayed);
        }*/

        override    public  string loadTextFile (string resource)
        {
            byte[] bytebuf = null;
            string contents = new UnityStringHelper ().buildString (loadBinaryFile (resource));
            return contents;
          
        }

        /**
     * Not for textures.
     * 
     * @param resource
     * @return
     * @throws ResourceNotFoundException
     */
        override     public  byte[] loadBinaryFile (string resource)
        {
            byte[] bytebuf = null;
            //Der Unity Resources.load kann nicht mit System paths arbeiten
            try {
                return  (UnityFileReader.getFileStream (resource)/*16.10.18 .readFully ()*/);
            } catch (System.Exception e) {
                throw new ResourceNotFoundException (resource + ":" + e.Message);
            }
        }

        
        override public bool exists (string resource)
        {
            if (File.Exists (resource)) {
                return true;
            }
            if (File.Exists (resource + ".gz")) {
                return true;
            }
            return false;
        }
    }
}
