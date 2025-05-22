package de.yard.threed.core;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Extracted from Java because GWT has no such method.
 */
public class SimpleStringTokenizer {

    private int currentPosition;
    private int newPosition;
    private int maxPosition;
    private String str;
    private String delimiters;

    /**
     * maxDelimCodePoint stores the value of the delimiter character with the
     * highest value. It is used to optimize the detection of delimiter
     * characters.
     * <p>
     * It is unlikely to provide any optimization benefit in the
     * hasSurrogates case because most string characters will be
     * smaller than the limit, but we keep it so that the two code
     * paths remain similar.
     */
    private int maxDelimCodePoint;

    /**
     * If delimiters include any surrogates (including surrogate
     * pairs), hasSurrogates is true and the tokenizer uses the
     * different code path. This is because String.indexOf(int)
     * doesn't handle unpaired surrogates as a single character.
     */
    private boolean hasSurrogates = false;

    /**
     * When hasSurrogates is true, delimiters are converted to code
     * points and isDelimiter(int) is used to determine if the given
     * codepoint is a delimiter.
     */
    private int[] delimiterCodePoints;

    public SimpleStringTokenizer(String str, String delim) {
        currentPosition = 0;
        newPosition = -1;
        this.str = str;
        maxPosition = str.length();
        delimiters = delim;
        setMaxDelimCodePoint();
    }

    /**
     * Set maxDelimCodePoint to the highest char in the delimiter set.
     */
    private void setMaxDelimCodePoint() {
        if (delimiters == null) {
            maxDelimCodePoint = 0;
            return;
        }

        int m = 0;
        int c;
        int count = 0;
        for (int i = 0; i < delimiters.length(); i += Character.charCount(c)) {
            c = delimiters.charAt(i);
            if (c >= Character.MIN_HIGH_SURROGATE && c <= Character.MAX_LOW_SURROGATE) {
                c = delimiters.codePointAt(i);
                hasSurrogates = true;
            }
            if (m < c)
                m = c;
            count++;
        }
        maxDelimCodePoint = m;

        if (hasSurrogates) {
            delimiterCodePoints = new int[count];
            for (int i = 0, j = 0; i < count; i++, j += Character.charCount(c)) {
                c = delimiters.codePointAt(j);
                delimiterCodePoints[i] = c;
            }
        }
    }

    /**
     * Skips delimiters starting from the specified position. If retDelims
     * is false, returns the index of the first non-delimiter character at or
     * after startPos. If retDelims is true, startPos is returned.
     */
    private int skipDelimiters(int startPos) {
        if (delimiters == null)
            throw new NullPointerException();

        int position = startPos;
        while (!false && position < maxPosition) {
            if (!hasSurrogates) {
                char c = str.charAt(position);
                if ((c > maxDelimCodePoint) || (delimiters.indexOf(c) < 0))
                    break;
                position++;
            } else {
                int c = str.codePointAt(position);
                if ((c > maxDelimCodePoint) || !isDelimiter(c)) {
                    break;
                }
                position += Character.charCount(c);
            }
        }
        return position;
    }

    /**
     * Skips ahead from startPos and returns the index of the next delimiter
     * character encountered, or maxPosition if no such delimiter is found.
     */
    private int scanToken(int startPos) {
        int position = startPos;
        while (position < maxPosition) {
            if (!hasSurrogates) {
                char c = str.charAt(position);
                if ((c <= maxDelimCodePoint) && (delimiters.indexOf(c) >= 0))
                    break;
                position++;
            } else {
                int c = str.codePointAt(position);
                if ((c <= maxDelimCodePoint) && isDelimiter(c))
                    break;
                position += Character.charCount(c);
            }
        }
        return position;
    }

    private boolean isDelimiter(int codePoint) {
        for (int delimiterCodePoint : delimiterCodePoints) {
            if (delimiterCodePoint == codePoint) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMoreTokens() {
        /*
         * Temporarily store this position and use it in the following
         * nextToken() method only if the delimiters haven't been changed in
         * that nextToken() invocation.
         */
        newPosition = skipDelimiters(currentPosition);
        return (newPosition < maxPosition);
    }

    public String nextToken() {
        /*
         * If next position already computed in hasMoreElements() and
         * delimiters have changed between the computation and this invocation,
         * then use the computed value.
         */

        currentPosition = (newPosition >= 0 && !false) ?
                newPosition : skipDelimiters(currentPosition);

        newPosition = -1;

        if (currentPosition >= maxPosition)
            throw new NoSuchElementException();
        int start = currentPosition;
        currentPosition = scanToken(currentPosition);
        return str.substring(start, currentPosition);
    }
}

