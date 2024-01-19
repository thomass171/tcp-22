package de.yard.threed.maze;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.JsonHelper;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.core.loader.StringReader;

import java.util.List;

/**
 * Provide the current maze grid to anybody who is interested.
 * No need to have a singleton because its stateless. But a singleton makes it more clear.
 */
public class MazeDataProvider implements DataProvider {

    public static String PROVIDER_NAME = "grid";
    private static MazeDataProvider instance = null;
    Grid grid;
    // The grid name might be a filename, but not necessarily. So its just a name.
    String gridName;

    private MazeDataProvider(String gridName, Grid grid) {

        getLogger().debug("Building for maze " + gridName);

        this.gridName = gridName;
        this.grid = grid;
    }

    public static void init() {
        Configuration configuration = Platform.getInstance().getConfiguration();
        String initialMaze = configuration.getString("initialMaze");
        String teamSize = configuration.getString("teamSize");
        init(initialMaze, teamSize);
    }

    /**
     * Might be async/deferred in case of remote grids.
     */
    public static void init(String initialMaze, String teamSize) {

        getLogger().debug("init for " + initialMaze);

        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        // only for system init
        if (initialMaze == null) {
            initialMaze = "skbn/SokobanWikipedia.txt";
        }

        if (StringUtils.startsWith(initialMaze, "http")) {

            Platform.getInstance().httpGet(initialMaze, null, null, response -> {
                getLogger().debug("Got http response " + response);
                if (response.getStatus() == 200) {
                    NativeJsonValue json;
                    try {
                        json = Platform.getInstance().parseJson(response.getContentAsString());
                    } catch (CharsetException e) {
                        // TODO improved eror handling
                        throw new RuntimeException(e);
                    }
                    NativeJsonObject mazeObject = json.isObject();
                    String rawGrid = JsonHelper.getString(mazeObject, "grid");
                    Grid grid = GridReader.readWithModificatorFromRaw(rawGrid, teamSize);
                    instance = new MazeDataProvider(JsonHelper.getString(mazeObject, "name"), grid);
                    SystemManager.putDataProvider(PROVIDER_NAME, instance);
                } else {
                    getLogger().error("Unexpected response " + response);
                    //TODO inform request?
                }
            });
        } else {
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
            Grid grid = loadGrids(fileContent, title, teamSize);

            instance = new MazeDataProvider(initialMaze, grid);
            SystemManager.putDataProvider(PROVIDER_NAME, instance);
        }
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
        if (dataProvider == null) {
            return null;
        }
        return (Grid) dataProvider.getData(new Object[]{"grid"});
    }

    public static String getGridName() {
        DataProvider dataProvider = SystemManager.getDataProvider(MazeDataProvider.PROVIDER_NAME);
        if (dataProvider == null) {
            return null;
        }
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
     * If 'gridName' is null, the first is returned.
     */
    private static Grid loadGrids(String fileContent, String gridName, String teamSize) {

        Grid grid;
        try {
            List<Grid> grids = GridReader.readWithModificator(new StringReader(fileContent), teamSize);
            if (grids.size() > 1 && gridName != null) {
                grid = Grid.findByTitle(grids, gridName);
            } else {
                grid = grids.get(0);
            }

        } catch (InvalidMazeException e) {
            getLogger().error("load error: InvalidMazeException:" + e.getMessage());
            return null;
        }
        return grid;
    }

    private static Log getLogger() {
        return Platform.getInstance().getLog(MazeDataProvider.class);
    }
}
