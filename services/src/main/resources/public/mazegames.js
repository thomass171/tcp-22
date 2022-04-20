
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
        var table = buildHtmlTableForMaze(maze);
        $(this).append(table.html);
    });
}





