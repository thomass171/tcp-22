package de.yard.threed.engine.util;

/**
 * Pseudozufallszahlen Generator.
 *
 * Created by thomass on 19.01.16.
 * <p/>
 * From stackoverflow.com. Author unknown.
 */
public class LFSR {
    private static final int M = 15;

    // hard-coded for 15-bits
    private static int[] TAPS = {14, 15};

    private boolean[] bits = new boolean[M + 1];

    public LFSR(int seed) {
        for (int i = 0; i < M; i++) {
            //31.3.16: Wegen C# aus ">>>" ein ">>" gemacht. 
            // ">>>" ist bitweises Logikshift (d.h. links wird immer mit 0 gefuellt), ">>" ist mathematisch, das linke Bit bleibt erhalten.
            // C# hat nur ">>" und macht die Funktionsweise vom Typ (int/uint) abhaengig.
            //24.11.16 Durch den long muesste das Problem entschaerft sein. Darum kein Abbruch mehr. TODO Aber noch testen.
            //Util.notyet();
            long shiftedeins = 1L << i;
            long lseed = seed;
           // bits[i] = (((1 << i) & seed) >> i) == 1;
            bits[i] = (((shiftedeins) & lseed) >> i) == 1;
        }
    }

    /* generate a random int uniformly on the interval [-2^31 + 1, 2^31 - 1] */
    public short nextShort() {
        //printBits();

        // calculate the integer value from the registers
        short next = 0;
        for (int i = 0; i < M; i++) {
            // der cats auf den short ist auch neu->auch testen
            next |= (short)((bits[i] ? 1 : 0) << i);
        }

        // allow for zero without allowing for -2^31
        if (next < 0) next++;

        // calculate the last register from all the preceding
        bits[M] = false;
        for (int i = 0; i < TAPS.length; i++) {
            bits[M] ^= bits[M - TAPS[i]];
        }

        // shift all the registers
        for (int i = 0; i < M; i++) {
            bits[i] = bits[i + 1];
        }

        return next;
    }
}
