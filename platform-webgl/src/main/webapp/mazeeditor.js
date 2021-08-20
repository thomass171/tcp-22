/* derived from https://pwmarcz.pl/kaboom/ */

const cyle = [' ','W','D','S'];
var nextElement = new Map();
nextElement.set(' ', 'W');
nextElement.set('W', 'D');
nextElement.set('D', 'S');
nextElement.set('S', 'B');
nextElement.set('B', ' ');

var codeOfElement = new Map();
codeOfElement.set(' ', ' ');
codeOfElement.set('W', '#');
codeOfElement.set('D', '.');
codeOfElement.set('S', '@');
codeOfElement.set('B', '$');

var elementTitle = new Map();
elementTitle.set(' ', 'empty');
elementTitle.set('W', 'Wall');
elementTitle.set('D', 'Destination');
elementTitle.set('S', 'Start');
elementTitle.set('B', 'Box');

class Maze {
  constructor(width, height) {
    this.width = width;
    this.height = height;

    this.mazeGrid = makeGrid(this.width, this.height, " ");
    for (let y = 0; y < this.height; y++) {
      for (let x = 0; x < this.width; x++) {
        if (x == 0 || y == 0 || x == this.width-1 || y == this.height-1) {
          this.mazeGrid[y][x] = "W";
        }
      }
    }
    this.numRevealed = 0;

  }

  mount(guiElement) {
    const boardElement = document.createElement('div');
    boardElement.className = 'board';
    boardElement.id = 'board';
    guiElement.appendChild(boardElement);

    this.cells = [];

    const isTouch = (('ontouchstart' in window) || (navigator.MaxTouchPoints > 0) || (navigator.msMaxTouchPoints > 0));

    for (let y = 0; y < this.height; y++) {
      this.cells.push([]);
      const row = document.createElement('div');
      row.className = 'board-row';
      for (let x = 0; x < this.width; x++) {
        const cell = document.createElement('div');
        cell.className = 'cell clickable empty';
        cell.onclick = e => this.cellClick(e, x, y);
        cell.onmousedown = e => this.cellMouseDown(e, x, y);
        cell.ondblclick = e => this.cellDblClick(e, x, y);
        cell.oncontextmenu = e => e.preventDefault();
        if (isTouch) {
          cell.setAttribute('data-long-press-delay', 500);
          cell.addEventListener('long-press', e => this.cellLongPress(e, x, y));
        }
        row.appendChild(cell);
        this.cells[y].push(cell);
      }
      boardElement.appendChild(row);
    }

    this.stateElement = document.createElement('div');
    guiElement.appendChild(this.stateElement);

    this.refresh();
  }

  cellClick(e, x, y) {
    e.preventDefault();

    //console.log("clicked");
    if (!this.safeMode) {
      this.reveal(x, y);
    }
  }

  clearTimeout() {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
      this.timeoutId = null;
    }
  }

  cellMouseDown(e, x, y) {
    switch(e.button) {
      case 1:
        e.preventDefault();
        this.revealAround(x, y);
        break;
      case 2:
        e.preventDefault();

        break;
    }
  }

  cellDblClick(e, x, y) {
    e.preventDefault();
      this.reveal(x, y);
  }

  cellLongPress(e, x, y) {
  }

  reveal(x, y, isAround) {
    //console.log("reveal, labels=",this.map.labels[y][x],"y=",y);

    this.mazeGrid[y][x] = nextElement.get(this.mazeGrid[y][x]);
    this.refresh();
  }

  refresh() {
    //console.log("refresh");
    for (let y = 0; y < this.height; y++) {
      for (let x = 0; x < this.width; x++) {


        let className;

        const mazeElement = this.mazeGrid[y][x];

        className = 'empty';
          //console.log("found wall");
        switch (mazeElement) {
          case "W":
            className = 'wall';
            break;
          case "D":
            className = 'destination';
            break;
          case "S":
            className = 'start';
            break;
          case "B":
            className = 'box';
            break;
        }

        //console.log("setting classname ",className);
        this.cells[y][x].className = 'cell ' + className;
        this.cells[y][x].title = elementTitle.get(mazeElement);
      }
    }
  }

  getGrid() {
    var grid = "";
    for (let y = 0; y < this.height; y++) {
      for (let x = 0; x < this.width; x++) {
        grid += codeOfElement.get(this.mazeGrid[y][x])
      }
      grid += "\n";
    }
    return grid;
  }
  // end of class Maze
}

function makeGrid(width, height, value) {
  const grid = [];
  for (let y = 0; y < height; y++) {
    grid.push(new Array(width).fill(value));
  }
  return grid;
}

function shuffle(a) {
  for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      const x = a[i];
      a[i] = a[j];
      a[j] = x;
  }
  return a;
}

let maze;

function newMaze(event) {
  if (event) {
    event.preventDefault();
  }

  const width = parseInt(document.getElementById('width').value, 10);
  const height = parseInt(document.getElementById('height').value, 10);

  const guiElement = document.getElementById('maze');
  guiElement.innerHTML = '';
  maze = new Maze(width, height);
  maze.mount(guiElement);

  updateSize();
}

function save(event) {
  if (event) {
    event.preventDefault();
  }

  //console.log("save");
  var grid = maze.getGrid();
  console.log(grid);
  //$("#grid").val(grid);
  document.getElementById('grid').innerHTML = grid.replaceAll("\n","<br>");
}


function updateSize() {
  const board = document.getElementById('board');
  if (board.scrollWidth > board.offsetWidth) {
    const factor = board.offsetWidth / board.scrollWidth;
    board.style.transform = `scale(${factor})`;
    board.style.transformOrigin = 'top left';
    board.style.height = (board.scrollHeight * factor) + 'px';
  } else {
    board.style.transform = '';
    board.style.height = 'auto';
  }
}

function updateMax() {
  const width = parseInt(document.getElementById('width').value, 10);
  const height = parseInt(document.getElementById('height').value, 10);

}

function setParams(width, height) {
  document.getElementById('width').value = width;
  document.getElementById('height').value = height;
  updateMax();
}

window.addEventListener('resize', updateSize);

updateMax();
document.getElementById('new-maze').click();  // this will trigger validation
