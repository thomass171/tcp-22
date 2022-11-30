package de.yard.threed.engine;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Keycodes from GWT which corresponds to Javascript Keycodes.
 * JME hat andere. JME hat die Tasten der Tastatuer quasi zeilenweise durchnummeriert (so wie Scancodes?).
 * <p/>
 * 19.1.16: Beginn Umstellung auf Unity Bezeichnungen. Unity hat auch eher sowas wie Scancodes.
 * 26.2.16: Scancodes sind wohl besser. Unterscheiden z.B. left/right shift
 * 30.11.22: JS Keycodes are deprecated meanwhile. Indeed these are somehow arbitrary, not really scancodes
 * and not really charcodes.  It highly depends on the
 * use case what is the better choice. Typically for gaming is using scan codes (probably thats why Unity
 * uses scancodes), where in an update loop is checked whether some key is pressed. This allows
 * keyboard independent WASD controls and left/right shift distingushing.
 * For writing a document however, charcodes are the only option to know, which character is on the key
 * the user pressed. charcode probably need event processing, because sometimes these result from pressing
 * multiple keys. Probing these in an update loop might be misleading.
 * <p/>
 * For our purposes we stay with the JS codes for now. Our use cases are
 * - press 'm' for opening a mneu
 * - press 'q' for quit (independent from keyboard layout)
 * - use cursor keys for moving
 * - enter a username
 * Maybe scancodes and charcodes can be combined. scancodes for probing and charcodes as event.
 *
 * Created by thomass on 12.06.15.
 */

public class KeyCode {
    static Log logger = Platform.getInstance().getLog(KeyCode.class);

    public static final int KEY_A = 65;
    public static final int KEY_B = 66;
    public static final int KEY_C = 67;
    public static final int KEY_D = 68;
    public static final int KEY_E = 69;
    public static final int KEY_F = 70;
    public static final int KEY_G = 71;
    public static final int KEY_H = 72;
    public static final int KEY_I = 73;
    public static final int KEY_J = 74;
    public static final int KEY_K = 75;
    public static final int KEY_L = 76;
    public static final int KEY_M = 77;
    public static final int KEY_N = 78;
    public static final int KEY_O = 79;
    public static final int KEY_P = 80;
    public static final int KEY_Q = 81;
    public static final int KEY_R = 82;
    public static final int KEY_S = 83;
    public static final int KEY_T = 84;
    public static final int KEY_U = 85;
    public static final int KEY_V = 86;
    public static final int KEY_W = 87;
    public static final int KEY_X = 88;
    public static final int KEY_Y = 89;
    public static final int KEY_Z = 90;
    public static final int KEY_ZERO = 48;
    public static final int KEY_ONE = 49;
    public static final int KEY_TWO = 50;
    public static final int KEY_THREE = 51;
    public static final int KEY_FOUR = 52;
    public static final int KEY_FIVE = 53;
    public static final int KEY_SIX = 54;
    public static final int KEY_SEVEN = 55;
    public static final int KEY_EIGHT = 56;
    public static final int KEY_NINE = 57;
    public static final int KEY_NUM_ZERO = 96;
    public static final int KEY_NUM_ONE = 97;
    public static final int KEY_NUM_TWO = 98;
    public static final int KEY_NUM_THREE = 99;
    public static final int KEY_NUM_FOUR = 100;
    public static final int KEY_NUM_FIVE = 101;
    public static final int KEY_NUM_SIX = 102;
    public static final int KEY_NUM_SEVEN = 103;
    public static final int KEY_NUM_EIGHT = 104;
    public static final int KEY_NUM_NINE = 105;
    public static final int KEY_NUM_MULTIPLY = 106;
    public static final int KEY_NUM_PLUS = 107;
    public static final int KEY_NUM_MINUS = 109;
    public static final int KEY_NUM_PERIOD = 110;
    public static final int KEY_NUM_DIVISION = 111;
    public static final int KEY_ALT = 18;
    public static final int KEY_BACKSPACE = 8;
    public static final int KEY_CTRL = 17;
    public static final int KEY_DELETE = 46;
    public static final int KEY_DOWN = 40;
    public static final int KEY_END = 35;
    public static final int KEY_ENTER = 13;
    public static final int KEY_ESCAPE = 27;
    public static final int KEY_HOME = 36;
    public static final int KEY_LEFT = 37;
    public static final int KEY_PAGEDOWN = 34;
    public static final int KEY_PAGEUP = 33;
    public static final int KEY_RIGHT = 39;
    public static final int KEY_SHIFT = 16;
    public static final int KEY_TAB = 9;
    public static final int KEY_UP = 38;
    public static final int KEY_F1 = 112;
    public static final int KEY_F2 = 113;
    public static final int KEY_F3 = 114;
    public static final int KEY_F4 = 115;
    public static final int KEY_F5 = 116;
    public static final int KEY_F6 = 117;
    public static final int KEY_F7 = 118;
    public static final int KEY_F8 = 119;
    public static final int KEY_F9 = 120;
    public static final int KEY_F10 = 121;
    public static final int KEY_F11 = 122;
    public static final int KEY_F12 = 123;
    public static final int KEY_WIN_KEY_FF_LINUX = 0;
    public static final int KEY_MAC_ENTER = 3;
    public static final int KEY_PAUSE = 19;
    public static final int KEY_CAPS_LOCK = 20;
    public static final int KEY_SPACE = 32;
    public static final int KEY_PRINT_SCREEN = 44;
    public static final int KEY_INSERT = 45;
    public static final int KEY_NUM_CENTER = 12;
    public static final int KEY_WIN_KEY = 224;
    public static final int KEY_WIN_KEY_LEFT_META = 91;
    public static final int KEY_WIN_KEY_RIGHT = 92;
    public static final int KEY_CONTEXT_MENU = 93;
    public static final int KEY_MAC_FF_META = 224;
    public static final int KEY_NUMLOCK = 144;
    public static final int KEY_SCROLL_LOCK = 145;
    public static final int KEY_FIRST_MEDIA_KEY = 166;
    public static final int KEY_LAST_MEDIA_KEY = 183;
    public static final int KEY_WIN_IME = 229;

    // Jetzt kommen die, die GWT offenbar nicht kennt
    // vielleicht wegen deutscher Tastatur

    public static final int KEY_PLUS = 187;
    public static final int KEY_DASH = 189;

    // und jetzt die Unity konformen
    public static final int LeftArrow = KEY_LEFT;
    public static final int RightArrow = KEY_RIGHT;
    public static final int UpArrow = KEY_UP;
    public static final int DownArrow = KEY_DOWN;
    public static final int Alpha0 = KEY_ZERO;
    public static final int Alpha1 = KEY_ONE;
    public static final int Alpha2 = KEY_TWO;
    public static final int Alpha3 = KEY_THREE;
    public static final int Alpha4 = KEY_FOUR;
    public static final int Alpha5 = KEY_FIVE;
    public static final int Alpha6 = KEY_SIX;
    public static final int Alpha7 = KEY_SEVEN;
    public static final int Alpha8 = KEY_EIGHT;
    public static final int Alpha9 = KEY_NINE;

    public static final int A = KEY_A;
    public static final int B = KEY_B;
    public static final int C = KEY_C;
    public static final int D = KEY_D;
    public static final int E = KEY_E;
    public static final int F = KEY_F;
    public static final int G = KEY_G;
    public static final int H = KEY_H;
    public static final int I = KEY_I;
    public static final int J = KEY_J;
    public static final int K = KEY_K;
    public static final int L = KEY_L;
    public static final int M = KEY_M;
    public static final int N = KEY_N;
    public static final int P = KEY_P;
    public static final int R = KEY_R;
    public static final int S = KEY_S;
    public static final int T = KEY_T;
    public static final int U = KEY_U;
    public static final int V = KEY_V;
    public static final int W = KEY_W;
    public static final int X = KEY_X;
    public static final int Y = KEY_Y;
    public static final int Z = KEY_Z;

    public static final int Space = KEY_SPACE;
    public static final int PageUp = KEY_PAGEUP;
    public static final int PageDown = KEY_PAGEDOWN;
    public static final int Plus = KEY_PLUS;
    public static final int Minus = KEY_DASH;
    public static final int Tab = KEY_TAB;
    //JS unterscheidet nicht, daher erstmal nur left
    //public static final int RightShift = KEY_SHIFT;
    //20.2.18: Unity unterscheidet Left/Right. Ich aber nicht wegen z.B. JS und überhaupt.
    // Vereinheitlicht auf schlicht "shift"
    public static final int /*Left*/Shift = KEY_SHIFT;
    public static final int Escape = KEY_ESCAPE;
    //analog left/Right wie shift
    public static final int Ctrl = KEY_CTRL;
    
    public static int[] unitykeys = new int[]{
            //Ziffern
            KeyCode.Alpha0, KeyCode.Alpha1, KeyCode.Alpha2, KeyCode.Alpha3, KeyCode.Alpha4, KeyCode.Alpha5, KeyCode.Alpha6, KeyCode.Alpha7, KeyCode.Alpha8, KeyCode.Alpha9,
            // Cursor
            KeyCode.LeftArrow, KeyCode.RightArrow, KeyCode.UpArrow, KeyCode.DownArrow, KeyCode.PageDown, KeyCode.PageUp,
            // Buchstaben
            KeyCode.A, KeyCode.B, KeyCode.C, KeyCode.D, KeyCode.E, KeyCode.F, KeyCode.G, KeyCode.H, KeyCode.I, KeyCode.J, KeyCode.K, KeyCode.L, KeyCode.M, KeyCode.N, KeyCode.P, KeyCode.R, KeyCode.S, KeyCode.T, KeyCode.U, KeyCode.V, KeyCode.W, KeyCode.X,KeyCode.Y,KeyCode.Z,
            // Sonderzeichen
            KeyCode.Space, KeyCode.Plus, KeyCode.Minus, KeyCode.Escape,
            // Modifier
            KeyCode.Shift, KeyCode.Ctrl, /*KeyCode.RightShift*/
    };

    /**
     * Das dürfte fuer LWJGL und JME geeignet sein.
     *
     * @param scancode
     * @return
     */
    public static int lwjgl2Js(int scancode) {
        switch (scancode) {
            case 1:
                return Escape;
            case 2:
                return Alpha1;
            case 3:
                return Alpha2;
            case 4:
                return Alpha3;
            case 5:
                return Alpha4;
            case 6:
                return Alpha5;
            case 7:
                return Alpha6;
            case 8:
                return Alpha7;
            case 9:
                return Alpha8;
            case 10:
                return Alpha9;
            case 11:
                return Alpha0;
            case 17:
                return W;
            case 18:
                return E;
            case 19:
                return R;
            case 20:
                return T;
            case 21:
                //Y/Z PRoblem
                return Y;
            case 22:
                return U;
            case 25:
                return P;
            case 27:
                return Plus;
            case 29:
                return Ctrl;
            case 30:
                return A;
            case 31:
                return S;
            case 32:
                return D;
            case 33:
                return F;
            case 34:
                return G;
            case 35:
                return H;
            case 38:
                return L;
            case 44:
                // /Y/Z Problem
                return Z;
            case 45:
                return X;
            case 46:
                return C;
            case 47:
                return V;
            case 48:
                return B;
            case 49:
                return N;
            case 50:
                return M;
            case 53:
                return Minus;
            case 54:
                // erstmal keine Unterscheidung links rechts wegen JS
                return Shift;
            case 200:
                return UpArrow;
            case 201:
                return PageUp;
            case 203:
                return LeftArrow;
            case 205:
                return RightArrow;
            case 208:
                return DownArrow;
            case 209:
                return PageDown;
        }
        logger.warn("unmapped keycode " + scancode);
        return 0;
    }
}
