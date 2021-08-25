using System;
using System.Globalization;
using System.Text;
using de.yard.threed.core.platform;

/**
 * 
 */
namespace de.yard.threed.platform.unity
{
    public class UnityStringHelper : NativeStringHelper
    {
        public string trim (string s)
        {
            return s.Trim ();
        }

        public int length (string s)
        {
            return s.Length;
        }

        public string substring (string s, int index)
        {
            return s.Substring (index);
        }

        public string substring (string s, int index, int end)
        {
            int l = end - index;
            return s.Substring (index, l);
        }

        public char charAt (string s, int i)
        {
            return s [i];
        }

        public string[] split (string str, string s)
        {
            string[] a = str.Split (new string[]{ s }, StringSplitOptions.None);
            // wenn der string mit dem trennzeichen endete, gibt es hier anders als bei Java zum Schluss noch einen Leerstring.
            // den entfernen
            int size = a.Length;
            if (str.EndsWith (s) && a[size-1].Length == 0) {
                Array.Resize<string>(ref a,size - 1);
            }
            return a;
        }

        public string[] splitByWhitespace(string str)
        {            
            return str.Split(new char[0], StringSplitOptions.RemoveEmptyEntries);
        }

        public int indexOf (string s, char c)
        {
            return s.IndexOf (c);
        }

        public int indexOf (string s, string c)
        {
            return s.IndexOf (c);
        }

        public int lastIndexOf (string s, string sub)
        {
            return s.LastIndexOf (sub);
        }

        public bool equalsIgnoreCase (string s1, string s2)
        {
            return s1.Equals (s2, StringComparison.InvariantCultureIgnoreCase);
        }



        public string buildString (byte[] buf)
        {
            // welches Encoding? irgendwie noch nicht ganz klar.
            // Analog zu getBytes()
            //string s = Encoding.Unicode.GetString (buf);
            string s = Encoding.ASCII.GetString (buf);
            return s;
        }

        public byte[] getBytes (string s)
        {
            // welches Encoding? irgendwie noch nicht ganz klar.
            // 11.7.16: Unicode ist aber nicht gut, da gibts immer zwei Bytes. Eigentlich soll es doch nur fuer Ascii genutzt werden.
            //return Encoding.Unicode.GetBytes (s);
            return Encoding.ASCII.GetBytes (s);
        }

        public String toLowerCase(string s) {
            return s.ToLower();
        }

        public String toUpperCase(string s) {
            return s.ToUpper();
        }
    }
}
