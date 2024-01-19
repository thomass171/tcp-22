package de.yard.threed.javacommon;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.JavaStringHelper;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Not for GWT.
 * <p>
 * Created by thomass on 16.01.24.
 */
public class DefaultJavaStringHelper extends JavaStringHelper {

    public String buildString(byte[] buf) throws CharsetException {
        // The behavior of the Java string constructor when the given bytes are not valid in the default charset is unspecified.
        // So use dedicated decoder.
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            return String.valueOf(decoder.onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(buf)));
        } catch (CharacterCodingException e) {
            throw new CharsetException();
        }
    }
}
