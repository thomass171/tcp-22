package de.yard.threed.maze;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.StringReader;

import java.util.List;

/**
 * Provide the current maze grid to anybody who is interested.
 * No need to have a singleton because its statelesse. But a singleton makes it more clear..
 */
public class MazeDataProvider implements DataProvider {

    private Log logger = Platform.getInstance().getLog(MazeDataProvider.class);

    public static String PROVIDER_NAME = "grid";
    private static MazeDataProvider instance = null;
    //private String initialMaze;
    Grid grid;
    String gridName;

    private MazeDataProvider(String initialMaze) {

        logger.debug("Building for initialMaze " + initialMaze);
        // only for system init
        if (initialMaze == null) {
            initialMaze = "skbn/SokobanWikipedia.txt";
        }

        String fileContent;
        String title;
        if (StringUtils.startsWith(initialMaze, "##")) {
            // directly grid definition
            fileContent = initialMaze;
            title = "on-the-fly";
        } else {
            String name = StringUtils.substringBeforeLast(initialMaze, ".");
            name = StringUtils.substringAfterLast(name, "/");
            String filename = StringUtils.substringBeforeLast(initialMaze, ":");
            fileContent = MazeUtils.readMazefile(filename/*, name*/);

            title = StringUtils.substringAfterLast(initialMaze, ":");
        }
        //loadLevel(fileContent, title);
        loadGrids(fileContent, title);
        gridName = initialMaze;
    }

    public static void init() {
        Configuration configuration = Platform.getInstance().getConfiguration();
        String initialMaze = configuration.getString("initialMaze");
        init(initialMaze);
    }

    public static void init(String initialMaze) {
        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        instance = new MazeDataProvider(initialMaze);


        SystemManager.putDataProvider(PROVIDER_NAME, instance);
    }

    public static MazeDataProvider getInstance() {
        return instance;
    }

    /**
     * Needed for testing.
     */
    public static void reset() {
        instance = null;
    }

    public static Grid getGrid() {
        DataProvider dataProvider = SystemManager.getDataProvider(MazeDataProvider.PROVIDER_NAME);
        return (Grid) dataProvider.getData(new Object[]{"grid"});
    }

    public static String getGridName() {
        DataProvider dataProvider = SystemManager.getDataProvider(MazeDataProvider.PROVIDER_NAME);
        return (String) dataProvider.getData(new Object[]{"gridname"});
    }

    @Override
    public Object getData(Object[] parameter) {
        if (((String) parameter[0]).equals("grid")) {
            return grid;
        }
        if (((String) parameter[0]).equals("gridname")) {
            return gridName;
        }
        return null;
    }

    /**
     * The file(content) might contain more than one grid.
     */
    void loadGrids(String fileContent, String gridName) {


        try {
            List<Grid> grids = Grid.loadByReader(new StringReader(fileContent));
            if (grids.size() > 1 && gridName != null) {
                grid = Grid.findByTitle(grids, gridName);
            } else {
                grid = grids.get(0);
            }

        } catch (InvalidMazeException e) {
            logger.error("load error: InvalidMazeException:" + e.getMessage());
            return;
        }
    }
}
