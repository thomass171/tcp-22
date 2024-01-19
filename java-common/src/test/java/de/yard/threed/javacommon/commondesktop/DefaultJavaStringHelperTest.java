package de.yard.threed.javacommon.commondesktop;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.javacommon.DefaultJavaStringHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultJavaStringHelperTest {

    @Test
    void testUnknownCharset() throws Exception {

        DefaultJavaStringHelper stringHelper = new DefaultJavaStringHelper();

        assertEquals("ab", stringHelper.buildString(new byte[]{'a', 'b'}));
        assertEquals("ab", stringHelper.buildString(new byte[]{97, 98}));

        // 0xF6 is german 'ö' in ISO 8859-1
        assertThrows(CharsetException.class, () -> stringHelper.buildString(new byte[]{97, 98, (byte) 0xF6}));
    }
}
