package de.yard.threed.core.geometry;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;

import java.util.List;

/**
 * Eine (Teil)obefl�che einer Geometrie.
 * <p/>
 * Hat nichts mit Face(34) zu tun.
 * <p/>
 * Ein wichtiges Kriterium einer Surface ist, dass sie keine Kanten im Sinne von Knicken haben,
 * an denen mehrere UV Mappings hinterlegt werden muesste.
 * Ein W�rfel hat z.B. 6, eine Kugel 1.
 * <p/>
 * Wichtig fuer UV Mapping, das immer fuer eine einzelne Surface stattfindet.
 *
 * 18.07.15: Duerfte sich auch gut fuer die Abbilung einer Surface aus einer AC Datei eignen,
 * wenn hier (bzw. den enthaltenen Faces) die UVs mit aufgenommen werden. Siehe Kommentar Face3.
 *
 * <p/>
 * Date: 02.09.14
 */
public abstract class Surface {
    Log logger = Platform.getInstance().getLog(Surface.class);
    // Zeilenweise aufgebaut, nicht mehr nach Tapes
    //Face[][] faces;
    //public List<Face> faces = new ArrayList<Face>();
    //15.6.16: Auch auf Facelist umgestellt wegen Einheitlichkeit.
FaceList facelist = new FaceList(true);
    // Die Koordinaten jedes Vertex relativ in seiner UV Mapping Area.
    //6.11.15 HashMap<Integer, Vector2> vloc = new HashMap<Integer, Vector2>();





    public List<Face> getFaces() {
        return facelist.faces;
    }
    
    public FaceList getFacelist(){
        return facelist;
    }
}
