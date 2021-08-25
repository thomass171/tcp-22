using System;
using java.lang;
using UnityEngine;

namespace de.yard.threed.platform
{
    using de.yard.threed.engine;
    using de.yard.threed.core.platform;
    using de.yard.threed.core.resource;
    using de.yard.threed.platform.unity;
    using de.yard.threed.core;
    /**
     *
     */
    public class UnityPdfDocument
    {
        static Log logger = Platform.getInstance ().getLog (typeof(UnityPdfDocument));
        // private final PDDocumentInformation docinfo;
        String name;
        // PDDocument doc;
        //bool cacheenabled = false;
        TestPdfDoc docid;

        /**
         * Name muss mit Suffix pdf sein.
         *
         * @param name
         */
        public UnityPdfDocument (String name, TestPdfDoc docid)
        {
            this.name = name;
            this.docid = docid;
            if (!name.EndsWith (".pdf"))
                throw new RuntimeException ("suffix must be .pdf");
            /*try {
                doc = PDDocument.load(new File(name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            docinfo = doc.getDocumentInformation();*/
        }

        public int getNumberOfPages ()
        {
            return 137;//TODO doc.getNumberOfPages ();
        }

        public ImageData getPage (int page)
        {
            //try {
            // if (docid == TestPdfDoc.IPADUSERGUIDE) {
            String filename = "/Users/thomas/Projekte/Granada/cache/iPad_User_Guide " + page + ".jpeg";
            //byte[] buf = UnityFileReader.getFileStream (filename).readFully ();
            UnityResource imgfile = (UnityResource)UnityResource.buildFile(filename);
            Util.nomore ();
            LoadedResource loadedresource = null;//16.10.18; UnityResourceManager.getInstance ().loadResourceSync (imgfile);
            //16.10.18: Geht ohne inputstream jetzt nicht mehr
            Util.nomore();
            byte[] buf = null;//loadedresource.inputStream.readFully ();
            return new ImageData(buf);//UnityTexture.buildFromImage (buf);

          
        }

        /* 
        private String getCacheName() {
            String cname = name.replaceAll("/", ".");
            cname = cname.substring(0, cname.length() - 4);
            if (cname.startsWith("."))
                cname = cname.substring(1);
            return "../cache/" + cname;
        }

        private String getPageCacheName(int page) {
            return getCacheName() + "-" + page;
        }

        private BufferedImage getPageFromCache(int page) throws IOException {
            String filename = getPageCacheName(page)+".jpeg";
            //  filename = "cache/iPad_User_Guide " + page + ".jpeg";
            return getImage(filename);
        }
*/

    }
}
