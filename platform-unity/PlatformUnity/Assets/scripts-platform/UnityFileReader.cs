using System;
using System.IO;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.core;
using de.yard.threed.core.resource;

namespace de.yard.threed.platform.unity
{
    /**
     * 
     */
    public class UnityFileReader
    {
        /*public static InputStream getFileStream (String file)
        {
            return getFileStream (new File (file));
        }*/

        /**
        * 11.4.16: Nicht fuer Zugriff aus Resources. (19.4.16 Häh?)
         */
        public static byte[]/*System.IO.Stream*/ getFileStream (string filePath) /*throws IOException*/
        {
            //TextAsset textAsset = (TextAsset) Resources.Load("MyXMLFile");  
            // 6.4.16: Ob das die richtige Stelle fuer den PREFIX ist?
            // 26.4.16: jetzt ohne Prefix, z.B. für cahced PDF images mit absolutem Pfad
            //filePath = UnityResourceManager.RESOURCEPREFIX + filePath;
            byte[] fileData;

            if (!File.Exists (filePath)) {
                throw new ResourceNotFoundException (filePath);
            }
            // try {
            //File file = new File(filename);
            bool iscompressed = filePath.EndsWith (".gz");
            //knownsize = System.IO.File.(int) file.length();
            if (iscompressed) {
                System.IO.Stream ins;
                //    Nullable<Int32> knownsize = null;
                Util.nomore();
                ins = System.IO.File.OpenRead (filePath);

                return null;//new CSInputStream(new System.IO.Compression.GZipStream(ins, System.IO.Compression.CompressionMode.Decompress));
            }
            fileData = File.ReadAllBytes (filePath);

            return /*16.10.18 new ByteArrayInputStream*/ (fileData);


            /*} catch (IOException e) {
                throw e;
            }*/
        }

        /*public static byte[] readFully(System.IO.Stream ins){
            if (ins==null){
                return null;
            }
            try {
                ins.rreturn IOUtils.toByteArray(ins);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }*/
    }

/*    public class CSInputStream : InputStream
    {
        System.IO.Stream ins;

        public CSInputStream (System.IO.Stream ins)
        {
            this.ins = ins;
        }

        public int read ()
        {
            // try {
            return ins.ReadByte ();
            /* } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }* /
        }

        public void read (byte[] buf, int size)
        {
           // try {
                int offset = 0;
                // Der io.InputStream liest nicht unbedingt so viel wie ich moechte.
                //TODO notaus
                do {
                    int read = ins.Read (buf, offset, size);
                    size -= read;
                    offset += read;
                } while (size > 0);
          /*  } catch (IOException e) {
                throw new RuntimeException ("io", e);
            }* /

        }

        public byte[] readFully ()
        {
           /* MemoryStream memoryStream = new MemoryStream ();
                            ins.CopyTo(memoryStream);
                return memoryStream.ToArray();
            return Stream.ins.CStreamHelper.ReadToEnd (ins);* /
            return (byte[])Util.notyet ();
        }
    }*/
  
}