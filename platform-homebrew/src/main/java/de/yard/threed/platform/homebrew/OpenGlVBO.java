package de.yard.threed.platform.homebrew;

/**
 * Date: 11.04.14
 */
public abstract class OpenGlVBO {
    // The amount of bytes an element has
    public static final int elementBytes = 4;

    // Elements per parameter
    public static final int positionElementCount = 4;
    public static final int colorElementCount = 4;
    public static final int textureElementCount = 2;

    // Bytes per parameter
    public static final int positionBytesCount = positionElementCount * elementBytes;
    public static final int colorByteCount = colorElementCount * elementBytes;
    public static final int textureByteCount = textureElementCount * elementBytes;

    // Byte offsets per parameter
    public static final int positionByteOffset = 0;
    public static final int colorByteOffset = positionByteOffset + positionBytesCount;
    public static final int textureByteOffset = colorByteOffset + colorByteCount;

    // The amount of elements that a vertex has
    public static final int elementCount = positionElementCount +
            colorElementCount + textureElementCount;
    // The size of a vertex in bytes, like in C/C++: sizeof(Vertex)
    public static final int stride = positionBytesCount + colorByteCount +
            textureByteCount;


}
