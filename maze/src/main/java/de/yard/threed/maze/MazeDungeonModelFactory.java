package de.yard.threed.maze;

import de.yard.threed.engine.Material;
import de.yard.threed.engine.Texture;

/**
 *
 */
public class MazeDungeonModelFactory extends MazeModelFactory {

    public Texture stoneWallDiffuse;
    public Texture stoneWallNormal;
    public Material stoneWallMaterial;

    public MazeDungeonModelFactory(MazeTheme settings) {
        super(settings);
        loadTextures();
    }

    public Material getGroundmaterial() {
        return groundmaterial;
    }

    private void loadTextures() {

        stoneWallDiffuse = Texture.buildBundleTexture("maze", "textures/wovado/stone_wall02-diffuse_map.jpg");
        stoneWallNormal = Texture.buildBundleTexture("maze", "textures/wovado/stone_wall02-normal_map.jpg");
        //stoneWallMaterial = Material.buildPhongMaterialWithNormalMap(stoneWallDiffuse);
        // normalmap seems to have no visual effect.
        stoneWallMaterial = Material.buildPhongMaterialWithNormalMap(stoneWallDiffuse, stoneWallNormal);
    }
}
