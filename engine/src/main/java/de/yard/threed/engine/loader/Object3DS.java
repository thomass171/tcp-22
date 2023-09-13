package de.yard.threed.engine.loader;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.geometry.Face;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.loader.LoadedObject;

import java.util.ArrayList;
import java.util.List;

public
class Object3DS extends LoadedObject {
    // texcoords gibt es nicht immer
    public List<Vector2> texcoords;
    public List<Face> tmpfaces = new ArrayList<Face>();

    public Face3 addTmpFace(int a, int b, int c) {
        Face3 face = new Face3(a, b, c);
        tmpfaces.add(face);
        return face;
    }
}
