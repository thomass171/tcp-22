package de.yard.threed.core.platform;

public interface NativeElementArray {
    /**
     * 11.3.16: Für jede Face3List wird eine eigene Indexliste erstellt, die separat gedrawed wird. Damit können dann auch multiple materials verwendet werden.
     */
    void addTriangle(int indexlistindex, int[] index/*0, int index1, int index2*/);
}
