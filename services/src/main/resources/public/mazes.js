/**
 * JS related to mazes.html
 */

var host = "http://localhost:8080";

// maze name is key
var allMazesMap = new Map();
var mazeInEdit = null;

var nextElement = new Map();
nextElement.set(' ', '#');
nextElement.set('#', 'T');
nextElement.set('T', 'P');
nextElement.set('P', 'B');
nextElement.set('B', 'M');
nextElement.set('M', 'D');
nextElement.set('D', ' ');

// Tooltips
var elementTitle = new Map();
elementTitle.set(' ', 'empty');
elementTitle.set('W', 'Wall');
elementTitle.set('D', 'Destination');
elementTitle.set('S', 'Start');
elementTitle.set('B', 'Box');

function switchView(view, mazeName) {
    //console.log("switchView to ", view);
    if (view == "detailview") {
         $('#listview').removeClass('w3-show');
         $('#listview').addClass('w3-hide');
         $('#detailview').addClass('w3-show');
         $('#detailview').removeClass('w3-hide');
         mazeInEdit = allMazesMap.get(mazeName);
         initMazeEdit();
    }
    if (view == "listview") {
         $('#detailview').removeClass('w3-show');
         $('#detailview').addClass('w3-hide');
         $('#listview').removeClass('w3-hide');
         $('#listview').addClass('w3-show');
    }
}

function addMazeListElement(maze, contentProvider, optionalElement) {

    var table = createTable(null, "mazetable");
    var content = "<div onclick='switchView(\"detailview\",\"" + maze.name + "\")' class='w3-bar-item'>";
    content += "<span class='w3-large'>" + maze.name + "</span><br>" +
        "<span>" + maze.description + "</span>";
    content += "<br>" + table.html + "</div>";

    var item_id = addListItem("mazelist", content, "w3-bar");
    populateHtmlTableForMaze(table.bodyid, maze, previewCellBuilder);
}

/**
 * Create a HTML table for a maze
 */
function populateHtmlTableForMaze(bodyid, maze, cellbuilder) {

    var i,j;
    for (i = maze.rows - 1; i >= 0; i--) {
        var rowid = addTableRow(bodyid);
        for (j = 0; j < maze.cols; j++) {
            var location = new Location(j,i);
            var mazeCell = maze.getCell(location);
            // cell won't be null because non rectangular grids are equalized
            var cell = cellbuilder(maze.name, mazeCell, location);
            var colid = addTableCol(cell.html, rowid, "mazetabletd");
        }
    }
}

function previewCellBuilder(mazeName, mazecell, location) {
    return createDiv("","cell cellsize8x8 " + mazecell.getStyle());
}

function editCellBuilder(mazeName, mazecell, location) {
    return createClickableDiv(mazecell.getContent(), "cycleCellElement(this, "+location.x+","+location.y+")",
        "cell cellsize30x30 " + mazecell.getStyle());
}

function initMazeEdit() {
    var table = createTable(null, "mazetable");
    $("#mazeeditor").html(table.html);
    populateHtmlTableForMaze(table.bodyid, mazeInEdit, editCellBuilder);
    $("#resultgrid").html("");
    $("#btn_save").prop("disabled", !mazeInEdit.dirty);
}

function cycleCellElement(cellDiv, x, y) {
    //console.log("cycleCellElement cellDiv=", cellDiv);
    var location = new Location(x,y);
    var mazeCell = mazeInEdit.getCell(location);

    mazeCell.code = nextElement.get(mazeCell.code);
    var div = $("#" + cellDiv.id);
    // console.log("cycleCellElement code=", mazeCell.code, mazeCell.getStyle());
    div.removeClass("celldestination");
    div.removeClass("cellwall");
    div.removeClass("cellstart");
    div.removeClass("cellbox");
    div.removeClass("cellmonster");
    div.removeClass("celldiamond");
    div.removeClass("cellempty");
    div.addClass(mazeCell.getStyle());
    div.html(mazeCell.getContent());
    $("#btn_save").prop("disabled", false);
    mazeInEdit.dirty = true;
}

function save() {
    var grid = mazeInEdit.getGrid();
    console.log(grid);
    $("#resultgrid").html(grid.replaceAll("\n","<br>"));
    postData(mazeInEdit.selfHref, { grid: grid.replaceAll("\n","n") })
        .then(data => {
            console.log(data); // JSON data parsed by `data.json()` call
            mazeInEdit.dirty = false;
            $("#btn_save").prop("disabled", true);
        });
}

/**
 * From https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch
 *
 * Currently using PATCH!
 */
async function postData(url = '', data = {}) {
  // Default options are marked with *
  const response = await fetch(url, {
    method: 'PATCH', // *GET, POST, PUT, DELETE, etc.
    //mode: 'cors', // no-cors, *cors, same-origin
    cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
    //credentials: 'same-origin', // include, *same-origin, omit
    headers: {
      'Content-Type': 'application/json'
      // 'Content-Type': 'application/x-www-form-urlencoded',
    },
    //redirect: 'follow', // manual, *follow, error
    //referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
    body: JSON.stringify(data) // body data type must match "Content-Type" header
  });
  return response.json(); // parses JSON response into native JavaScript objects
}

/**
 * init for mazes.html
 */
function init() {
    var url = new URL(window.location.href);
    console.log("url=" + url);
    var hostparam = url.searchParams.get("host");
    if (hostparam != null) {
        host = hostparam;
        $("#debuginfo").html("(host="+hostparam+")");
    }

    fetch(host + "/mazes")
      .then(response => response.json())
      .then(data => {
        console.log(data);
        data._embedded.mazes.forEach(function(m) {
          m.grid = m.grid.replaceAll("\\n","\n");
          console.log("m:",m);
          var maze = Maze.buildFromGrid(m.grid);
          maze.name = m.name;
          maze.description = m.description;
          maze.selfHref = m._links.self.href;
          allMazesMap.set(m.name, maze);
          addMazeListElement(maze);
        })
      });

      switchView("listview");
}

