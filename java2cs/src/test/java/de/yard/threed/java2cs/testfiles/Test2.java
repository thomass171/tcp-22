package de.yard.threed.java2cs.testfiles;

import java.io.InputStream;

/**
 * Zum Testen von Fehlern
 * 
 * Created by thomass on 01.03.16.
 */
public class Test2 {
    // in ist keyword
    InputStream in;
    // ref ist CS keyword
    static float mult( int a, float ref ) // static func two args
    {
        assert 2 > 1;
        
        return 42.0f * ref;
    }

    // is ist auch keyword
    private void is() {
        
    }
}
