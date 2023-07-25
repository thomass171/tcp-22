package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.SimpleGeometry;
import de.yard.threed.engine.platform.common.SimpleGeometryBuilder;

/**
 * 3D visualization of a maze. This is a dungeon grid where
 * - A wall completely occupies a field.
 * <p>
 * Also see GridState and MazeLayout.
 * <p>
 * <p/>
 * Created by thomass on 17.07.23.
 */
public class MazeDungeonTerrain extends AbstractMazeTerrain {

    MazeDungeonModelFactory mazeModelFactory;
    SimpleGeometryBuilder wallGeometryBuilder = null;
    Vector3 southNormal = new Vector3(0, 0, 1);
    Vector3 northNormal = new Vector3(0, 0, -1);
    Vector3 westNormal = new Vector3(-1, 0, 0);
    Vector3 eastNormal = new Vector3(1, 0, 0);
    double gsz2 = MazeModelFactory.gsz2;

    public MazeDungeonTerrain(MazeLayout layout, MazeDungeonModelFactory mazeModelFactory) {
        super(layout);
        this.mazeModelFactory = mazeModelFactory;
    }

    @Override
    Material getGroundmaterial() {
        return mazeModelFactory.getGroundmaterial();
    }

    @Override
    SceneNode buildGroundElement() {
        return mazeModelFactory.buildGroundElement();
    }

    @Override
    void handleWall(Point p) {

        if (wallGeometryBuilder == null) {
            wallGeometryBuilder = new SimpleGeometryBuilder();
        }

        int x = p.getX();
        int y = p.getY();

        if (layout.isWall(p)) {
            if (!layout.isWall(p.addY(-1))) {
                addSouthWallElement(x, y);
            }
            if (!layout.isWall(p.addY(1))) {
                addNorthWallElement(x, y);
            }
            if (!layout.isWall(p.addX(-1))) {
                addWestWallElement(x, y);
            }
            if (!layout.isWall(p.addX(1))) {
                addEastWallElement(x, y);
            }
        }
    }

    @Override
    protected void buildCeiling() {

        SimpleGeometryBuilder ceilingGeometryBuilder = new SimpleGeometryBuilder();
        Vector3 ceilingNormal = new Vector3(0, -1, 0);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                addCeilingElement(ceilingGeometryBuilder, x, y, ceilingNormal);
            }
        }
        SimpleGeometry sg = ceilingGeometryBuilder.getGeometry();
        SceneNode n = new SceneNode(new Mesh(sg, mazeModelFactory.pillarmaterial));
        node.attach(n);
    }

    private void addSouthWallElement(int x, int y) {
        addWallElement(wallGeometryBuilder, x, y, new Vector3(-gsz2, 0, gsz2), new Vector3(gsz2, 0, gsz2), southNormal);
    }

    private void addNorthWallElement(int x, int y) {
        addWallElement(wallGeometryBuilder, x, y, new Vector3(gsz2, 0, -gsz2), new Vector3(-gsz2, 0, -gsz2), northNormal);
    }

    private void addWestWallElement(int x, int y) {
        addWallElement(wallGeometryBuilder, x, y, new Vector3(-gsz2, 0, -gsz2), new Vector3(-gsz2, 0, gsz2), westNormal);
    }

    private void addEastWallElement(int x, int y) {
        addWallElement(wallGeometryBuilder, x, y, new Vector3(gsz2, 0, gsz2), new Vector3(gsz2, 0, -gsz2), eastNormal);
    }

    private void addWallElement(SimpleGeometryBuilder gb, int x, int y, Vector3 offset0, Vector3 offset1, Vector3 normal) {


        Vector3 center = getTerrainElementCoordinates(x, y);
        Vector3 h = new Vector3(0, MazeModelFactory.PILLARHEIGHT, 0);
        // 3 - 2
        // 0 - 1
        int index = gb.addVertex(center.add(offset0), normal, new Vector2(0, 0));
        gb.addVertex(center.add(offset1), normal, new Vector2(1, 0));
        gb.addVertex(center.add(offset1).add(h), normal, new Vector2(1, 1));
        gb.addVertex(center.add(offset0).add(h), normal, new Vector2(0, 1));

        gb.addFace(index + 0, index + 1, index + 2);
        gb.addFace(index + 2, index + 3, index + 0);
    }

    private void addCeilingElement(SimpleGeometryBuilder gb, int x, int y, Vector3 normal) {

        Vector3 center = getTerrainElementCoordinates(x, y);
        // 3 - 2
        // 0 - 1
        int index = gb.addVertex(center.add(new Vector3(-gsz2, MazeModelFactory.PILLARHEIGHT, -gsz2)), normal, new Vector2(0, 0));
        gb.addVertex(center.add(new Vector3(gsz2, MazeModelFactory.PILLARHEIGHT, -gsz2)), normal, new Vector2(1, 0));
        gb.addVertex(center.add(new Vector3(gsz2, MazeModelFactory.PILLARHEIGHT, gsz2)), normal, new Vector2(1, 1));
        gb.addVertex(center.add(new Vector3(-gsz2, MazeModelFactory.PILLARHEIGHT, gsz2)), normal, new Vector2(0, 1));

        gb.addFace(index + 0, index + 1, index + 2);
        gb.addFace(index + 2, index + 3, index + 0);
    }

    @Override
    protected void finalizeGrid() {
        SimpleGeometry sg = wallGeometryBuilder.getGeometry();
        //sg = Primitives.buildBox(0.5f, 0.5f, 0.5f);

        //SceneNode n = new SceneNode(new Mesh(sg, mazeModelFactory.pillarmaterial));
        SceneNode n = new SceneNode(new Mesh(sg, mazeModelFactory.stoneWallMaterial));
        node.attach(n);
    }

    /**
     * @return top/right/center array
     */
    public SceneNode[] getPillar(Point p) {
        throw new RuntimeException("not usable?");
        //return new SceneNode[]{topPillars.get(p), rightPillars.get(p), centerPillars.get(p)};
    }
}
