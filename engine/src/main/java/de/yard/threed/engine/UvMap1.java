package de.yard.threed.engine;

import de.yard.threed.core.Vector2;

/**
 * Date: 02.09.14
 * 13.10.15: Nicht mehr als List<Uvmap1> sondern nur als map. Das ist doch viel logischer.
 */
public abstract class UvMap1 {
    // geht nicht als final wegen C#
    public static  UvMap1 leftRotatedTexture = buildRotatedTexture(ProportionalUvMap.ROTATION_LEFT);
    public static  UvMap1 rightRotatedTexture = buildRotatedTexture(ProportionalUvMap.ROTATION_RIGHT);
    public static  UvMap1 rightRotatedTextureAndYrotated = buildRotatedTexture(ProportionalUvMap.ROTATION_RIGHT_AND_ROTATE_Y);

    /**
     * Ermittelt aus dem nativen UV Mapping das wirklich zu verwendende. Im einfachsten Fall wird
     * das native einfach uebernommen.
     */
   public abstract Vector2 getUvFromNativeUv(Vector2 nativeuv);

    private static UvMap1 buildRotatedTexture(int rotation) {
        ProportionalUvMap map = new ProportionalUvMap(new Vector2(0, 0), new Vector2(1, 1), rotation);
       // List<UvMap1> uvmapping = new ArrayList<UvMap1>();
        //uvmapping.add(map);
        return map;
    }
}
