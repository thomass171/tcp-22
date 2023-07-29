package de.yard.threed.maze;

import de.yard.threed.engine.Material;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.apps.WoodenToyFactory;

/**
 *
 */
public class MazeDungeonModelFactory extends MazeModelFactory {

    public Texture wallDiffuse;
    public Texture wallNormal;
    public Material wallMaterial;

    public MazeDungeonModelFactory(MazeTheme settings, boolean art) {
        super(settings);
        loadTextures(art);
    }

    public Material getGroundmaterial() {
        return groundmaterial;
    }

    private void loadTextures(boolean art) {

        if (art) {
            wallDiffuse = Texture.buildBundleTexture("maze", "textures/wovado/stone_wall02-diffuse_map.jpg");
            wallNormal = Texture.buildBundleTexture("maze", "textures/wovado/stone_wall02-normal_map.jpg");
            //stoneWallMaterial = Material.buildPhongMaterialWithNormalMap(stoneWallDiffuse);
            // normalmap seems to have no visual effect.
            wallMaterial = Material.buildPhongMaterialWithNormalMap(wallDiffuse, wallNormal);
            groundmaterial = Material.buildPhongMaterialWithNormalMap(Texture.buildBundleTexture("maze", "textures/cethiel/Ground_02.jpg"),
                    Texture.buildBundleTexture("maze", "textures/cethiel/Ground_02_Nrm.jpg"));

        }else{
            wallDiffuse = buildTexture("textures/gimp/wood/BucheMedium.png");
            wallNormal = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
            wallMaterial = Material.buildPhongMaterialWithNormalMap(wallDiffuse, wallNormal);
            groundmaterial = Material.buildPhongMaterialWithNormalMap(buildTexture("textures/gimp/wood/BucheHell.png"),
                    Texture.buildNormalMap(MazeModelFactory.buildEdgeNormalmap().image));

        }
    }
}
