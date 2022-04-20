/**
 * Classes for maze handling
 */


/**
 * y=0 at bottom
 */
class Location {

    constructor(x,y) {
        this.x = x;
        this.y = y;
    }

    isOutside(width, height) {
        if (this.x < 0 || this.x >= width) {
            return 1;
        }
        if (this.y < 0 || this.y >= height) {
            return 1;
        }
        return 0;
    }

    toString() {
        return "" + this.x + "," + this.y;
    }
}

/**
 *
 */
class MazeCell {

    constructor(code) {
        if (code.length != 1) {
            console.log("invalid code ",code);
        }
        this.code = code;
    }

    getStyle() {
        switch (this.code) {
            case ' ':
                return "cellempty";
            case '#':
                return "cellwall";
            case 'P':
            case '@':
                return "cellstart";
            case 'T':
            case '.':
                return "celldestination";
            case '$':
            case 'B':
                return "cellbox";
            case 'M':
                return "cellmonster";
            case 'D':
                return "celldiamond";
        }
        console.log("unknown code ", this.code);
        return "";
    }

    getContent() {
        var content = "";
        if (["M", "D", "P", "B", "T"].includes(this.code)) {
            content += this.code;
        }
        return content;
    }

    static buildWall() {
        return new MazeCell('#');
    }

    static buildEmpty() {
        return new MazeCell(' ');
    }

    /**
     * Build a maze cell object (class MazeCell) from a grid definition string char.
     */
    static buildMazeCellByCode(code) {
        switch (code) {
            case ' ':
                return new MazeCell(' ');
            case '#':
                return new MazeCell('#');
            case 'P':
            case '@':
                return new MazeCell('P');
            case 'T':
            case '.':
                return new MazeCell('T');
            case '$':
            case 'B':
                return new MazeCell('B');
            case 'M':
                return new MazeCell('M');
            case 'D':
                return new MazeCell('D');
        }
        console.log("unknown code ", code);
        return ' ';
    }
}

/**
 * row0 is the bottom row
 */
class Maze {

    constructor() {
        // Location->MazeCell
        this.elements = new Map();
        this.rows = 0;
        this.cols = 0;
        this.selfHref = null;
        this.dirty = false;
    }

    addCell(location, cell) {
        //console.log("addcell at ",location);
        this.elements.set(location.toString(), cell);
        if (location.x >= this.cols) {
            this.cols = location.x + 1;
        }
        if (location.y >= this.rows) {
            this.rows = location.y + 1;
        }
    }

    /**
     * if element does not exist (non rectangular grids), it is created
     */
    getCell(location) {
        if (location.isOutside(this.cols, this.rows)) {
            console.error("location outside");
            return null;
        }
        var cell = this.elements.get(location.toString());
        if (cell == null) {
            //console.log("delayed cell built at ", location, " maze=", this.toString());
            var cell = MazeCell.buildMazeCellByCode(' ');
            this.addCell(location, cell);
        }
        return cell;
    }

    getGrid() {
        var grid = "";
        for (let y = this.rows - 1; y >= 0; y--) {
            for (let x = 0; x < this.cols; x++) {
                var location = new Location(x,y);
                var mazeCell = this.getCell(location);
                grid += mazeCell.code;
            }
            grid += "\n";
        }
        return grid;
    }

    toString() {
        return "" + this.name + ": " + this.cols + "x" + this.rows;
    }

    static buildEmpty(width, height) {
        return new Maze();
    }

    /**
     * Build a maze object (class Maze) from a grid definition string.
     */
    static buildFromGrid(grid) {

        // split by real newline or 'n'
        var lines = grid.split(/[\nn]/g);
        // ignore empty row after last line break
        if (lines[lines.length-1].length == 0) {
            lines = lines.slice(0, lines.length - 1);
        }

        let maze = new Maze();
        var row = lines.length - 1;
        for (let line of lines) {
            var i;
            for (i = 0; i < line.length; i++) {
                var cell = MazeCell.buildMazeCellByCode(line.charAt(i));
                maze.addCell(new Location(i,row), cell);
            }
            row--;
        }
        maze.grid = grid;
        return maze;
    }
}