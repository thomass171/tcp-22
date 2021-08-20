
/**
 * y=0 at bottom
 */
class Location {

    constructor(x,y) {
        this.x = x;
        this.y = y;
    }

    toString() {
        return "" + this.x + "," + this.y;
    }
}

/**
 *
 */
class MazeCell {

    constructor(code, style) {
        if (code.length != 1) {
            console.log("invalid code ",code);
        }
        this.code = code;
        this.style = style;
    }

    getStyle() {
        return this.style;
    }

    static buildWall() {
        return new MazeCell('#',"cellwall");
    }

    static buildEmpty() {
        return new MazeCell(' ',"cellempty");
    }

    static buildMazeCellByCode(code) {
        switch (code) {
            case ' ':
                return EMPTY;
            case '#':
                return WALL;
            case '@':
                return PLAYER;
            case '.':
                return DESTINATION;
            case '$':
                return BOX;
        }
        console.log("unknown code ", code);
        return EMPTY;
    }
}

const WALL = MazeCell.buildWall();
const EMPTY = MazeCell.buildEmpty();
const PLAYER = new MazeCell('@',"cellstart");
const DESTINATION = new MazeCell('.',"celldestination");
const BOX = new MazeCell('$',"cellbox");

class Maze {

    constructor() {
        // Location->MazeCell
        this.elements = new Map();
        this.rows = 0;
        this.cols = 0;
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

    getCell(location) {
        var cell = this.elements.get(location.toString());
        if (cell == null) {
            console.log("no cell at ",location);
        }
        return cell;
    }

    static buildEmpty(width, height) {
        return new Maze();
    }

    static buildFromGrid(grid) {

        var lines = grid.split(/\n/g);

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
        return maze;
    }
}