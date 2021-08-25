using System;
using System.IO;
using java.lang;
using de.yard.threed.core;


namespace de.yard.threed.platform.unity
{
    using de.yard.threed.core.buffer;
    


// import de.yard.threed.platform.common.Color;

    /**
     * 26.7.2016: Ob das mit dem Schriben so ideal ist, aber fuer den Cache halt ganz praktisch
     */
    public class UnityOutputStream : NativeOutputStream
    {
        BinaryWriter sw;

        public UnityOutputStream (string filename)
        {
            sw = new BinaryWriter (File.Open (filename, FileMode.Create));
        }

        public void writeInt (int i)
        {
            try {
                sw.Write (i);
                /*ByteBuffer bb = ByteBuffer.allocate(1 * 4);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                IntBuffer fb = bb.asIntBuffer();
                fb.put(i);
                os.write(bb.array(),0,4);*/
            } catch (IOException e) {
                //TODO e.printStackTrace();
            }

        }

        public void writeFloat (float f)
        {
            try {
                sw.Write (f);
                //os.writeFloat(f);
                /*ByteBuffer bb = ByteBuffer.allocate(1 * 4);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                FloatBuffer fb = bb.asFloatBuffer();
                fb.put(f);
                os.write(bb.array(),0,4);*/
            } catch (IOException e) {
                //TODO e.printStackTrace();
            }

        }

        public void writeByte (byte s)
        {
            try {
                sw.Write (s);
            } catch (IOException e) {
                //TODO e.printStackTrace();
            }
        }

        /**
        * Mit 0-terminating byte damit ueber Simplebuffer gelesen werden kann.
        * 
        * @param s
        */
        public void writeString (String s)
        {
            try {
                if (s == null) {
                    // bloede Kruecke zur Abbildung von nulls.
                    s = ByteArrayInputStream.NULLPHRASE;
                }
                //os.writeInt(s.length());
                sw.Write (StringUtils.getBytes (s));
                sw.Write (new byte[]{ 0 });
            } catch (IOException e) {
                //TODO  e.printStackTrace();
            }

        }

        //@Override
        public void close ()
        {
            try {
                sw.Close ();
            } catch (IOException e) {
                //TODO e.printStackTrace();
            }

        }
    }
}

