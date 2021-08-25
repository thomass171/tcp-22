using System;
using System.Globalization;

/**
 * Enthaelt Klassen, die es in Java implizit immer gibt (z.B. aus java.lang)
 */
namespace java.lang
{
    /**
	 * als wrapper nicht geeignet. Das muss Single sein.
	 */
    public class Float
    {
        public static float MAX_VALUE = float.MaxValue;
        public static float MIN_VALUE = float.MinValue;

        public static float parseFloat (String s)
        {
            int i;
            if ((i = s.IndexOf ("f")) != -1) {
                // Fuer Java ist ein trailing f ok
                s = s.Remove (i);
            }
            return float.Parse (s);
        }

        public static bool isNaN (float f)
        {
            //TODO
            return false;
        }
    }

    public class Double
    {
        public static double MAX_VALUE = double.MaxValue;
        public static double MIN_VALUE = double.MinValue;

        public static double parseDouble (String s)
        {
            int i;
            if ((i = s.IndexOf ("f")) != -1) {
                // Fuer Java ist ein trailing f ok
                s = s.Remove (i);
            }
            double d;
            try {
                d = double.Parse (s);
            } catch (FormatException e) {
                throw new java.lang.Exception (e.Message);
            }
            return d;
        }

        public static bool isNaN (double f)
        {
            //TODO
            return false;
        }
    }

    /**
	 * als wrapper nicht geeignet. Das muss Int32 sein.
	 */
    public class Integer
    {
        public static int MAX_VALUE = int.MaxValue;
        public static int MIN_VALUE = int.MinValue;

        public static int parseInt (String s)
        {
            return int.Parse (s);
        }

        public static int parseInt (String s, int basis)
        {
            // basis wird ignoriert. TODO irgendwie verbessern
            int result;
            int.TryParse (s, NumberStyles.HexNumber, null, out result);
            return result;
        }

        public static string toHexString (int i)
        {
            return "todo";
        }
    }

    /**
     * als wrapper nicht geeignet?.
     */
    public class Long
    {
        public static long MAX_VALUE = long.MaxValue;
        public static long MIN_VALUE = long.MinValue;

        public static long parseLong (String s)
        {
            return long.Parse (s);
        }

      
    }

    public class StringBuffer
    {
        string s;

        public StringBuffer ()
        {
            s = "";
        }

        public StringBuffer (string s)
        {
            this.s = s;
        }

        public void append (char c)
        {
            s += c;
        }

        public void append (string ps)
        {
            s += ps;
        }

        public int length ()
        {
            return s.Length;
        }

        public void delete (int start, int end)
        {
            // Bei Java darf end laenger sein.
            while (end > s.Length) {
                end--;
            }
            s = s.Substring (0, start) + s.Substring (end);
        }

        public override string ToString ()
        {
            return s;
        }
    }

    public class RuntimeException : Exception
    {
        public RuntimeException (string msg) :
            base (msg)
        { 
        }

        public RuntimeException (Exception e) :
            base ("", e)
        { 
        }

        public RuntimeException (string msg, Exception e) :
            base (msg, e)
        { 
        }
    }

    /**
     * 23.3.18: Exception verwende ich im catch, um Nullpointer, ArrayIndex und aehnliche abzufangen. Das wird aber nicht gehen
     * wenn ich hier ableite, weil das keine Hierarchie ist. Das ist witzlos. Da muss der Converter dran.
     * Die Ableitung hier brauche ich aber trotzdem, wenn ich eigene Exceptions ableite, z.B. MazeException.
     */
    public class Exception : System.Exception
    {
        
        public Exception (string msg) :
            base (msg)
        { 
        }

        public Exception (string msg, System.Exception e) :
            base (msg, e)
        { 
        }

        public Exception (System.Exception e) :
            base ("", e)
        { 
        }

        public void printStackTrace ()
        {
            Console.WriteLine (this.ToString ());
        }

        public string getStackTrace ()
        {
            return this.ToString ();
        }

        public string getMessage ()
        {
            return base.Message;
        }

        public Exception getCause ()
        {
            return null;
        }
    }

    public class ExceptionWrapper
    {
        System.Exception e;

        public ExceptionWrapper (System.Exception e) 
        { 
        }

       

        public void printStackTrace ()
        {
            Console.WriteLine (this.ToString ());
        }

        public string getStackTrace ()
        {
            return this.ToString ();
        }

        public string getMessage ()
        {
            return e.Message;
        }

        public Exception getCause ()
        {
            return null;
        }
    }
}

