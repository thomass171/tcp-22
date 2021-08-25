using System;
using System.Globalization;

/**
 * Enthaelt TestKlassen aus org.junit
 */
namespace org.junit
{
    /**
     * als wrapper nicht geeignet. Das muss Single sein.
     */
    public class Floatxxx
    {
        public static float MAX_VALUE = float.MaxValue;
        public static float MIN_VALUE = float.MinValue;

        public static float parseFloat (String s)
        {
            return float.Parse (s);
        }

        public static bool isNaN(float f) {
            //TODO
            return false;
        }
    }
}
