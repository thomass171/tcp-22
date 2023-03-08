/**
 * JS related to servermanager.html
 */

var host = "http://localhost:8080";


function switchView(view, mazeName) {
    //console.log("switchView to ", view);

    if (view == "listview") {
         $('#detailview').removeClass('w3-show');
         $('#detailview').addClass('w3-hide');
         $('#listview').removeClass('w3-hide');
         $('#listview').addClass('w3-show');
    }
}

function addListElement(maze, contentProvider, optionalElement) {

    var table = createTable(null, "mazetable");
    var content = "<div onclick='switchView(\"detailview\",\"" + maze.name + "\")' class='w3-bar-item'>";
    content += "<span class='w3-large'>" + maze.name + "</span><br>" +
        "<span>" + maze.description + "</span>";
    content += "<br>" + table.html + "</div>";

    var item_id = addListItem("mazelist", content, "w3-bar");
    populateHtmlTableForMaze(table.bodyid, maze, previewCellBuilder);
}

/**
 * Create a HTML table for list of server
 */
function populateHtmlTableForServer(bodyid, maze, cellbuilder) {

    var i,j;
    for (i = maze.rows - 1; i >= 0; i--) {

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




/**
 * From https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch
 *
 */
async function httpPost(url = '', data = {}) {
  // Default options are marked with *
  const response = await fetch(url, {
    method: 'POST', // *GET, POST, PUT, DELETE, etc.
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

async function httpDelete(url = '') {
  // Default options are marked with *
  const response = await fetch(url, {
    method: 'DELETE', // *GET, POST, PUT, DELETE, etc.
    cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
    //credentials: 'same-origin', // include, *same-origin, omit
    headers: {
      // 'Content-Type': 'application/x-www-form-urlencoded',
    }

  });
}

/**
 * Start a new server
 */
function startServer(gridname) {
    $("#btn_start").prop("disabled", true);
    console.log(gridname);
    httpPost(host + "/server?scenename=de.yard.threed.maze.MazeScene&gridname="+gridname, {  })
        .then(data => {
            console.log(data); // JSON data parsed by `data.json()` call
            loadList();
            $("#btn_start").prop("disabled", false);
        });
}

/**
 * Load list of server
 */
function loadList() {
    fetch(host + "/server/list")
        .then(response => response.json())
        .then(data => {
            console.log(data);
            removeTableRows("tab_server");
            data.serverInstanceList.forEach(function(s) {
                console.log("s:",s);
                var rowid = addTableRow('bod_server');
                var colid = addTableCol(s.gridname, rowid, "");
                var colid = addTableCol(s.started, rowid, "");
                var colid = addTableCol(s.upTime, rowid, "");
                var colid = addTableCol(s.state, rowid, "");
                var btn_stop = createButton("Stop", "w3-button w3-round w3-khaki");
                var colid = addTableCol(btn_stop.html, rowid, "");

                $("#"+btn_stop.id).prop("disabled", s.state != "running");
                $("#"+btn_stop.id).click(function(){
                   $("#"+btn_stop.id).prop("disabled", true);
                   httpDelete(host + "/server?id="+s.id);
                });
            })
        });
}

/**
 * init for servermanager.html
 */
function init() {
    var url = new URL(window.location.href);
    console.log("url=" + url);
    var hostparam = url.searchParams.get("host");
    if (hostparam != null) {
        host = hostparam;
        $("#debuginfo").html("(host="+hostparam+")");
    }


      switchView("listview");

       loadList();

}

