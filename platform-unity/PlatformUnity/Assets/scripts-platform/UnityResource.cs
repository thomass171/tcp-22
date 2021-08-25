using System;
using System.IO;
using UnityEngine;
using java.lang;

using de.yard.threed.core;
using de.yard.threed.core.resource;

namespace de.yard.threed.platform.unity
{
    /**
     * 27.4.16: TODO Das ist noch nicht so ganz rund.
     */
    public class UnityResource : NativeResource
    {
        //public File file;
        string name;
        ResourcePath path;

        public UnityResource (string name) {
            if (StringUtils.contains(name, "\\")) {
                // obscure windows path separator.
                name = StringUtils.replaceAll(name, "\\", "/");
            }
            this.name = name;
        }

        public UnityResource(ResourcePath path, String name) :
            this(name)
        {
            this.path = path;
        }

        public bool exists ()
        {
            return File.Exists (getFullName());
        }

        public string getParent ()
        {
            return null;//file.getParent ();
        }

        public ResourcePath getPath ()
        {
            return path;
        }

        public string getName ()
        {
            return name;
        }

        public bool isBundled ()
        {
            return false;
        }

        /**
     * Hat keinen bundlepath
     * @return
     */
        public ResourcePath getBundlePath() {
            return null;
        }

        public String getFullName() {
            if (path != null) {
                return path.path + "/" + name;
            }
            return name;
        }

        /*   public List<NativeResource> listFiles(){
                File[] files = file.listFiles();
                List<NativeResource> list = new ArrayList<NativeResource>();
                for (File f:files){
                    list.add(new JAFile(f.getAbsolutePath()));
                }
                return list;
            }*/

        public string getExtension() {
            int index = StringUtils.lastIndexOf(name, ".");
            if (index == -1) {
                return "";
            }
            // Der "." koennte auch irgendwo im Pfad sein, aber name ist ohne Pfad
            return StringUtils.substring(name, index + 1);
        }

        public static NativeResource buildFile (String path)
        {
            // das mit dem Pfad ist erstmal ein Provisoruim, bis Resourcenumgang geklärt ist
            // 19.3.16: Vorlaeufig geklaert.
            //16.3.16: jetzt nach resources verschoben
            /*19.3.16: FileReader ist jetzt auch classpath faehig if (!path.startsWith("/")){
            //path = "src/main/webapp/"+path;
            path = "src/main/resources/"+path;
        }*/
            return new UnityResource (path);

        }
    }
}

