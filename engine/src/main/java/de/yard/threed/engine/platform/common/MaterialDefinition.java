package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.platform.NativeTexture;

import java.util.HashMap;

/**
 * Container zum einfachen Transport von Material Definitionen.
 * Effect ist eine Abstraktionsebene höher und har hier nicht mehr zu suchen.
 * Der name hat keine funktionale Bedeutung. Er dient nur der Wiedererkennbarkeit.
 * Wird im Speicher vorgehalten. Sollte daher klein bleiben.
 */
public class MaterialDefinition {
    public  String name;
    public HashMap<ColorType, Color> color;
    public HashMap<String, NativeTexture> texture;
    public HashMap<NumericType, NumericValue> parameters;

    public MaterialDefinition(String name, HashMap<ColorType, Color> color, HashMap</*TextureType*/String, NativeTexture> texture,
                  HashMap<NumericType, NumericValue> parameters){
        this.name = name;
        this.color = color;
        this.texture = texture;
        this.parameters=parameters;
    }
}
