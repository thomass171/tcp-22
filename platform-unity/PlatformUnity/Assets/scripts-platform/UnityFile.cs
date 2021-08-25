using System;
using System.IO;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.core.platform;
using de.yard.threed.core.resource;

namespace de.yard.threed.platform.unity
{
   
    public class UnityFile
    {
        static Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityFile));
        public string path;

        public UnityFile (NativeResource res)
        {
            //7.4.17 this.path = res.getName ();
            this.path = res.getFullName ();
           }

        public bool exists ()
        {
            //C# needs special check for directories
            return File.Exists (path) || Directory.Exists (path);
        }

        public String getParent ()
        {
            return null;//TODO file.getParent();
        }

        public String getPath ()
        {
            return   null;//TODOfile.getPath();
        }

        public String getName ()
        {
            return   null;//TODOfile.getName();
        }

        public Boolean isBundled ()
        {
            return false;
        }

        public List<NativeResource> listFiles ()
        {
            return null;//TODO
            /*     File[] files = file.listFiles();
            List<NativeFile> list = new ArrayList<NativeFile>();
            for (File f:files){
                list.add(new JAFile(f.getAbsolutePath()));
            }
            return list;*/
        }

        // public static NativeResource buildFile (String path)
        // {
        // das mit dem Pfad ist erstmal ein Provisoruim, bis Resourcenumgang geklärt ist
        // 19.3.16: Vorlaeufig geklaert.
        //16.3.16: jetzt nach resources verschoben
        /*19.3.16: FileReader ist jetzt auch classpath faehig if (!path.startsWith("/")){
            //path = "src/main/webapp/"+path;
            path = "src/main/resources/"+path;
        }*/
        //    return new UnityFile (path);
        // }
    }
}