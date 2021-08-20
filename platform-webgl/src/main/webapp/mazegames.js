
// build a css based grid image

function buildMazeList() {

    var grid = "##########\n"+
               "#        #\n"+
               "#  $    .#\n"+
               "# #  $ . #\n"+
               "# ###   .#\n"+
               "#   # ####\n"+
               "#   #$#  #\n"+
               "#       @#\n"+
               "#        #\n"+
               "##########";

    $( "div[data-maze]" ).each(function( index ) {

        var name = $(this).attr("data-maze");
        console.log("Building maze table for " + name)
        var maze = Maze.buildFromGrid(grid);
        var table = createTable(null, "mazetable");
        $(this).append(table.html);

        var i,j;
        for (i = maze.rows - 1; i >= 0; i--) {
            var rowid = addTableRow(table.bodyid);
            for (j = 0; j < maze.cols; j++) {
                var mz = maze.getCell(new Location(j,i));
                var cell = createDiv("","cell cellsize8x8 " + mz.getStyle());
                var colid = addTableCol(cell.html, rowid, "mazetabletd");
            }
        }
    });
}



