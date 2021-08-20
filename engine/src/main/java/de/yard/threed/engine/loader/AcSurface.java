package de.yard.threed.engine.loader;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.Face;
import de.yard.threed.engine.platform.common.Face3;
import de.yard.threed.engine.platform.common.FaceList;
import de.yard.threed.engine.platform.common.FaceN;


/**
 * Ersetrzt durhc SurfaceBin
 * Created by thomass on 04.04.16.
 */


class AcSurface {
    static Log logger = Platform.getInstance().getLog(AcSurface.class);

    int type;
    Vector2[] uv = null;
    int[] vref = null;
    private int rcount = 0;
    public int mat;

    public AcSurface(int type) throws InvalidDataException {
        this.type = type;
    }

    public void init(int size) {
        uv = new Vector2[size];
        vref = new int[size];
    }

    /**
     * Liefert true, when Face isType complete.
     *
     * @param token
     * @return
     * @throws InvalidDataException
     */
    public boolean addRef(AcToken[] token) throws InvalidDataException {
        if (token.length != 3) {
            throw new InvalidDataException("ref line has " + token.length + " token, expected 3");
        }

        uv[rcount] = new Vector2(token[1].getValueAsFloat(), token[2].getValueAsFloat());
        vref[rcount] = LoaderAC.getIntValue(token[0]);
        rcount++;
        if (rcount == vref.length) {
            // Face ist komplett
            return true;
        }
        return false;
    }

    /**
     * Add face of this surface to the Facelist of the object. For twosided Faces, an additional opposite Face isType added.
     *
     * @param faceList
     * @throws InvalidDataException
     */
    public void buildFace(AcObject obj, FaceList faceList) throws InvalidDataException {
        //31.3.17: Sonderfrickellösung. Wenn die erste SURF nicht shaded ist, nehme ich das für die ganze und unterdrücke damit später das crease-smoothing.(z.B. Douglas.ac)
        if (faceList.faces.size() == 0) {
            if (!isShaded()) {
                faceList.unshaded = true;
            }
        }
        Face face, backface = null;
        if (rcount == 2) {
            //in marker.ac gibts auch faces mit nur zwei Vertices! TODO das sind wohl lines
            face = new Face3(vref[0], vref[1], vref[1], uv[0], uv[1], uv[1]);
            faceList.faces.add(face);
        } else {
            if (rcount == 3) {
                face = new Face3(vref[0], vref[1], vref[2], uv[0], uv[1], uv[2]);
                faceList.faces.add(face);
                if (isTwoSided()) {
                    backface = new Face3(vref[2], vref[1], vref[0], uv[2], uv[1], uv[0]);
                    faceList.faces.add(backface);
                }
            } else {
                if (rcount > 3) {
                    face = new FaceN(vref, uv);
                    faceList.faces.add(face);
                    if (isTwoSided()) {
                        backface = new FaceN(vref, uv);
                        ((FaceN) backface).revert();
                        faceList.faces.add(backface);
                    }
                } else {

                    logger.warn("unknown face size " + rcount + " ignoring");
                    face = new Face3(vref[0], vref[0], vref[0], uv[0], uv[0], uv[0]);
                    faceList.faces.add(face);
                }
            }
        }

    }

    public boolean isTwoSided() {
        return (type & LoaderAC.flagSurfaceTwoSided) > 0;
    }

    public boolean isShaded() {
        return (type & LoaderAC.flagSurfaceShaded) > 0;
    }
}


