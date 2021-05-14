import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.impworld.*;

import java.awt.Color;
import javalib.worldimages.*;

//constants for the game, configurable scale (size of board), number of blocks in a row/column,
//  and number of Colors on the board [2, 8]
interface Cnst {

  /////////////////////////////////////////////////////////////
  // These constants can be changed to edit the big bang ran //
  /////////////////////////////////////////////////////////////

  //full size is eight, scales the size of the board
  int scale = 8;

  //numbers of blocks in a row and a column
  int blocks = 10;

  //number of colors the game will use, between 2-8 inclusive
  int numOfColors = 8;

  //the number of turns you have to win the game
  int maxTurns = 25;


  ////////////////////////////////////////
  // Please do not edit these constants //
  ////////////////////////////////////////

  //the visible board width, scales with scale
  int boardWidth = 70 * scale;

  //the visible board height, scales with scale
  int boardHeight = 70 * scale;

  //cell width scales with the board and scale
  int cellWidth = boardWidth / blocks;

  //cell height scales with board and scale
  int cellHeight = boardHeight / blocks;

  //new constant for text on the board
  int textHeight = boardWidth / 12;

  //the 8 colors that can be selected by the user
  ArrayList<Color> colorsToChoose =
      new ArrayList<Color>(Arrays.asList(
          Color.CYAN, Color.PINK, Color.ORANGE,
          Color.BLUE, Color.RED, Color.GREEN,
          Color.MAGENTA, Color.YELLOW));
}


//represents the world that the user plays the game on
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<ACell> board = new ArrayList<ACell>();

  //represents the current turn that the game is on
  int currTurn;

  //represents how many turns the player has to win in
  int maxTurn;

  //represents the random seed/object for this world
  Random rand;

  //represents how many visible blocks are in a row and column
  int blocks;

  //represents the number of colors the board will use to make the board
  int colors;

  //represents the list of the colors each cell can choose from
  ArrayList<Color> loc;

  //worklist for the on-tick, always initialized to nothing
  ArrayList<ACell> worklist;

  //constructor used for testing purposes
  FloodItWorld(ArrayList<ACell> board, int currTurn, int maxTurn, Random rand,
      ArrayList<Color> loc, int blocks, int colors, ArrayList<ACell> worklist) {
    this.board = board;
    this.currTurn = currTurn;
    this.maxTurn = maxTurn;
    this.rand = rand;
    this.loc = loc;
    this.blocks = blocks;
    this.colors = colors;
    this.worklist = worklist;
  }

  //between convinience and testing constructor
  FloodItWorld(int currTurn, int maxTurn, Random rand, int blocks, int colors) {
    this.board = new ArrayList<ACell>();
    this.currTurn = currTurn;
    this.maxTurn = maxTurn;
    this.rand = rand;
    this.loc = new ArrayList<Color>();
    this.blocks = blocks;
    this.colors = colors;
    this.worklist = new ArrayList<ACell>();
  }

  //constructor used for playing the game with a random board, and uses the blocks constant
  //  for the number of blocks in a row. Has a set list of colors 
  FloodItWorld(int maxTurn) {
    this.board = new ArrayList<ACell>();
    this.currTurn = 0;
    this.maxTurn = maxTurn;
    this.rand = new Random();
    this.loc = new ArrayList<Color>();
    this.blocks = Cnst.blocks;
    this.colors = Cnst.numOfColors;
    this.worklist = new ArrayList<ACell>();
  }

  //Constructor which only uses the constants, so that the player can edit
  //  the constants to run the game
  FloodItWorld() {
    this.board = new ArrayList<ACell>();
    this.currTurn = 0;
    this.maxTurn = Cnst.maxTurns;
    this.rand = new Random();
    this.loc = new ArrayList<Color>();
    this.blocks = Cnst.blocks;
    this.colors = Cnst.numOfColors;
    this.worklist = new ArrayList<ACell>();
  }


  //initialize fields needed for the game, and create the board and stitch the cells together
  public void initializeBoard() {
    int x;
    int y;

    this.loc = this.userColors();

    this.board = new ArrayList<ACell>();

    for (y = -1; y < this.blocks + 1; y += 1) {
      for (x = -1; x < this.blocks + 1; x += 1) {
        ACell nextCell;

        if (x == -1 || x == this.blocks || y == -1 || y == this.blocks) {
          nextCell = new EndCell(x, y);
        } else {
          nextCell = new Cell(x, y, this.randColor(), false);
        }
        if (x == 0 && y == 0) {
          nextCell.flood();
        }
        this.board.add(nextCell);
      }
    }
    this.stitchCells();
    this.indexHelp(0, 0).floodInitStarter();
  }

  //fills the world's list of colors with the correct number of colors
  public ArrayList<Color> userColors() {
    int colorIndex;
    ArrayList<Color> dest = new ArrayList<Color>();
    for (colorIndex = 0; colorIndex < this.colors; colorIndex += 1) {
      dest.add(Cnst.colorsToChoose.get(colorIndex));
    }
    return dest;
  }

  //stitch together all of the cells so that they point to each other
  public void stitchCells() {
    int x;
    int y;
    ACell currCell;
    ACell nextRight;
    ACell nextDown;

    for (x = -1; x < this.blocks; x += 1) {
      for (y = -1; y < this.blocks; y += 1) {

        currCell = this.indexHelp(x, y);
        nextRight = this.indexHelp(x + 1, y);
        nextDown = this.indexHelp(x, y + 1);

        currCell.stitchCells(nextRight, nextDown);
      }
    }
  }

  //produces a random color from this world's list of colors
  public Color randColor() {
    return this.loc.get(this.rand.nextInt(this.loc.size()));
  }

  //takes an x and a y position and given the size of the board returns the cell at that
  //  x, y position
  public ACell indexHelp(int x, int y) {
    return this.board.get((x + 1) + (y + 1) * (this.blocks + 2));
  }

  //populates the worklist with the given correct cells that need to be flooded and/or redrawn
  public void collector(ArrayList<ACell> cells) {
    for (ACell c : cells) {
      c.collector(this);
    }
  }

  //returns whether all of the worlds' cells are flooded
  public boolean allCellsFlooded() {
    for (ACell c : this.board) {
      if (!c.floodCheck()) {
        return false;
      }
    }
    return true;
  }


  /////////////////////////////
  //     Big Bang methods    //
  /////////////////////////////

  //Override big bang and return a world scene
  public WorldScene makeScene() {
    WorldScene scene = this.getEmptyScene();
    //make the image to draw \/  \/
    WorldImage cell = new EmptyImage();

    if (this.allCellsFlooded()) {
      cell = this.drawWin();
    }
    else if (this.currTurn >= this.maxTurn) {
      cell = this.drawLose();
    }
    else {
      cell = this.drawBoard();
    }

    //place the image on the scene \/ \/
    scene.placeImageXY(cell, 100, 200);

    return scene;
  }

  //checks if we won, lost, or if there is anything in the worklist currently, and if
  //  there is it starts the cascade and recurs concurrently with the collector method
  public void onTick() {
    if (this.worklist.size() > 0) {
      ArrayList<ACell> tempWorklist = new ArrayList<ACell>();
      for (ACell c : this.worklist) {
        tempWorklist.add(c);
      }
      this.worklist = new ArrayList<ACell>();
      this.collector(tempWorklist);
    }
    else if (this.allCellsFlooded()) {
      if (this.worklist.size() > 0) {
        this.worklist = new ArrayList<ACell>();
      }
      this.drawWin();
    }
    else if (this.currTurn >= this.maxTurn) {
      if (this.worklist.size() > 0) {
        this.worklist = new ArrayList<ACell>();
      }
      this.drawLose();
    }
  }

  //Overriding the onMouseClick, finding where the user clicked and starting the
  //  worklist for ontick to run
  public void onMouseClicked(Posn pos) {
    if (this.worklist.size() == 0) {
      if (this.currTurn < this.maxTurn) {
        if ((pos.x >= 100 && pos.x <= 100 + Cnst.boardWidth)
            && (pos.y >= 200 && pos.y <= 200 + Cnst.boardHeight)) {
          int x = (pos.x - 100) * this.blocks / Cnst.boardWidth;
          int y = (pos.y - 200) * this.blocks / Cnst.boardHeight;
          ACell chosenCell = this.indexHelp(x, y);
          ACell topLeft = this.indexHelp(0, 0);
          chosenCell.clickHelp(this, topLeft);
        }
      }
    }
  }


  //key handler for resetting the world
  public void onKeyReleased(String key) {
    if (key.equals("r")) {
      if (this.worklist.size() == 0) {
        this.currTurn = 0;
        this.initializeBoard();
      }
    }
  }

  //Overriding endOfWorld and displaying the losing or winning screen
  public WorldScene lastScene(String msg) {
    WorldScene scene = this.getEmptyScene();
    //make the image to draw \/  \/

    WorldImage cell = new EmptyImage();

    if (msg.equals("win")) {
      cell = this.drawWin();
    }
    else {
      cell = this.drawLose();
    }

    //place the image on the scene \/ \/
    scene.placeImageXY(cell, 100, 200);

    return scene;
  }

  //draws the winning board
  public WorldImage drawWin() {
    return new OverlayOffsetAlign("center", "bottom",
        new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
        0, Cnst.boardHeight + Cnst.scale,
        new AboveAlignImage("center",
            new TextImage("You Win! Congrats!", Cnst.textHeight, Color.BLACK),
            this.indexHelp(0,0).drawBoard(this.blocks)).movePinhole(0, (Cnst.scale * 7) / 2)
        ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2);
  }

  //draws the losing board
  public WorldImage drawLose() {
    return new OverlayOffsetAlign("center", "bottom",
        new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
        0, Cnst.boardHeight + Cnst.scale,
        new AboveAlignImage("center",
            new TextImage("You Lose!", Cnst.textHeight, Color.BLACK),
            this.indexHelp(0,0).drawBoard(this.blocks)).movePinhole(0, (Cnst.scale * 7) / 2)
        ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2);
  }


  //draws the board and the text above the board
  public WorldImage drawBoard() {
    return new OverlayOffsetAlign("center", "bottom",
        new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
        0, Cnst.boardHeight + Cnst.scale,
        new AboveAlignImage("center",
            new TextImage("Turn Count: " + Integer.toString(this.currTurn) + "/"
                + Integer.toString(this.maxTurn), Cnst.textHeight, Color.BLACK),
            this.indexHelp(0,0).drawBoard(this.blocks)).movePinhole(0, (Cnst.scale * 7) / 2)
        ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2);
  }
}



//represents an abstract cell which can be an end cell or a visible cell
abstract class ACell {
  //the x position on the grid of this cell
  int x;

  //the y position on the grid of this cell
  int y;

  //the reference to the ACell to the left
  ACell left;

  //the reference to the ACell on top of this cell
  ACell top;

  //the reference to the ACell to the right
  ACell right;

  //the reference to the ACell on the bottom of this cell
  ACell bottom;

  //floods the cell if it is a visible cell, nothing otherwise
  public abstract void flood();

  //draws this current cell
  public abstract WorldImage drawCell(int blocks);

  //draws this cell's whole row
  public abstract WorldImage drawRow(int blocks);

  //draws the board starting at this current cell
  public abstract WorldImage drawBoard(int blocks);

  //makes the current cell and the cell to the right point to each other,
  //  and this cell + the cell below point to each other
  public void stitchCells(ACell right, ACell down) {
    this.right = right;
    right.left = this;

    this.bottom = down;
    down.top = this;
  }

  //floods the first cell's adjacent cells
  public abstract void floodInitStarter();

  //helper method to floodInitStarter, checks color and floods the correct cells
  public abstract void colorChecker(Color c);

  //method on an ACell which collects cell that need to be added to the worklist
  public abstract void collector(FloodItWorld w);

  //helper to the collector method
  public abstract void collectorHelp(FloodItWorld w, Color c);

  //helper 1 to the click method
  public abstract void clickHelp(FloodItWorld w, ACell topLeft);

  //helper 2 to the click method
  public abstract void clickHelp2(FloodItWorld w, Color chosenColor);

  //checks if this current cell is an ACell and returns if the cell is flooded
  public boolean floodCheck() {
    return true;
  }
}


// Represents a single square of the game area, is a visible cell
class Cell extends ACell {
  //represents this cells color
  Color color;

  //represents if this cell has been flooded or not
  boolean flooded;

  //first convinience constructor that sets all pointers to null
  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }

  //constructor for testing purposes, can set all fields
  Cell(int x, int y, Color color, boolean flooded, ACell left, ACell top, ACell right,
      ACell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  //floods this cell because it is a visible cell
  public void flood() {
    this.flooded = true;
  }

  //draws this current cell
  public WorldImage drawCell(int blocks) {
    return new FrameImage(new RectangleImage(Cnst.boardWidth / blocks,
        Cnst.boardHeight / blocks,
        OutlineMode.SOLID, this.color));
  }

  //draw all cells in the row recursively
  public WorldImage drawRow(int blocks) {
    return new BesideImage(this.drawCell(blocks), this.right.drawRow(blocks));
  }

  //draws all cells on the board, recursively over the rows
  public WorldImage drawBoard(int blocks) {
    return new AboveImage(this.drawRow(blocks), this.bottom.drawBoard(blocks));
  }

  @Override
  //rename to something that describes the method better
  public void floodInitStarter() {
    this.left.colorChecker(this.color);
    this.right.colorChecker(this.color);
    this.bottom.colorChecker(this.color);
    this.top.colorChecker(this.color);
  }

  @Override
  //helper method to the flooder method, recurs to the neighbors and floods this current
  //  cell if the colors are the same and the cell isn't flooded
  public void colorChecker(Color c) {
    if (!this.flooded && this.color.equals(c)) {
      this.flood();
      this.left.colorChecker(this.color);
      this.right.colorChecker(this.color);
      this.top.colorChecker(this.color);
      this.bottom.colorChecker(this.color);
    }
  }

  @Override
  //go to the neighboring cells and see if they should be added to the worklist/flooded
  public void collector(FloodItWorld w) {
    this.right.collectorHelp(w, this.color);
    this.left.collectorHelp(w, this.color);
    this.top.collectorHelp(w, this.color);
    this.bottom.collectorHelp(w, this.color);
  }

  @Override
  //helps add cells to the worklist or flooded cells that should be flooded
  public void collectorHelp(FloodItWorld w, Color c) {
    if (this.color.equals(c) && !this.flooded) {
      this.flooded = true;
      this.floodInitStarter();
    }
    if (!this.color.equals(c) && this.flooded) {
      if (!w.worklist.contains(this)) {
        this.color = c;
        w.worklist.add(this);
      }

    }
  }

  @Override
  //helps the click method, and passes more information to the next helper
  public void clickHelp(FloodItWorld w, ACell topLeft) {
    Color chosenColor = this.color;
    topLeft.clickHelp2(w, chosenColor);
  }

  @Override
  //checks if this cell should have its color changed and get added to the worklist
  public void clickHelp2(FloodItWorld w, Color chosenColor) {
    if (this.color != chosenColor) {
      this.color = chosenColor;
      w.worklist.add(this);
      if (w.currTurn < w.maxTurn) {
        w.currTurn++;
      }
    }
  }

  //returns the field for this, knowing that it is a cell
  public boolean floodCheck() {
    return this.flooded;
  }

}

//represents an endcell that isn't visible, works as a stopping condition
class EndCell extends ACell {
  //testing constructor that sets all fields
  EndCell(int x, int y, ACell left, ACell top, ACell right, ACell bottom) {
    this.x = x;
    this.y = y;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  //convinience constructor that sets all references to null
  EndCell(int x, int y) {
    this.x = x;
    this.y = y;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }

  //this is not a floodable cell, so do nothing
  public void flood() {
    return;
  }

  //return empty image because this isn't visible
  public WorldImage drawCell(int blocks) {
    return new EmptyImage();
  }

  //return empty image because this isn't visible, acts as stopping condition for recursion
  public WorldImage drawRow(int blocks) {
    return new EmptyImage();
  }

  //return empty image because this isn't visible, acts as stopping condition for recursion
  public WorldImage drawBoard(int blocks) {
    return new EmptyImage();
  }

  @Override
  //do nothing for an End Cell
  public void floodInitStarter() {
    return;
  }

  @Override
  //do nothing for an End Cell
  public void colorChecker(Color c) {
    return;
  }

  @Override
  //do nothing for an End Cell
  public void collector(FloodItWorld w) {
    return;
  }

  @Override
  //do nothing for an End Cell
  public void collectorHelp(FloodItWorld w, Color c) {
    return;
  }

  @Override
  //do nothing for an End Cell
  public void clickHelp(FloodItWorld w, ACell topLeft) {
    return;
  }

  @Override
  //do nothing for an End Cell
  public void clickHelp2(FloodItWorld w, Color chosenColor) {
    return;
  }
}

//examples class used to test and whatnot
class ExamplesFloodItWorld {

  ArrayList<ACell> unconnectedCells = new ArrayList<ACell>(Arrays.asList(
      new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1),
      new EndCell(-1, 0), new Cell(0, 0, Color.BLUE, true), new EndCell(0, 1),
      new EndCell(-1, 1), new EndCell(0, 1), new EndCell(1, 1)));

  ArrayList<ACell> cells1 = new ArrayList<ACell>(Arrays.asList(
      new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1), new EndCell(2,-1),
      new EndCell(-1, 0), new Cell(0, 0, Color.RED, false),
      new Cell(1, 0, Color.RED, false), new EndCell(2, 0),
      new EndCell(-1, 1), new Cell(0, 1, Color.BLUE, false),
      new Cell(1, 1, Color.RED, false), new EndCell(2, 1),
      new EndCell(-1, 2), new EndCell(0, 2), new EndCell(1, 2), new EndCell(2, 2)));

  ArrayList<ACell> floodedCells = new ArrayList<ACell>(Arrays.asList(
      new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1), new EndCell(2,-1),
      new EndCell(-1, 0), new Cell(0, 0, Color.RED, false),
      new Cell(1, 0, Color.RED, false), new EndCell(2, 0),
      new EndCell(-1, 1), new Cell(0, 1, Color.BLUE, false),
      new Cell(1, 1, Color.RED, false), new EndCell(2, 1),
      new EndCell(-1, 2), new EndCell(0, 2), new EndCell(1, 2), new EndCell(2, 2)));

  ArrayList<ACell> halfFlooded = new ArrayList<ACell>(Arrays.asList(
      new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1), new EndCell(2,-1),
      new EndCell(-1, 0), new Cell(0, 0, Color.PINK, true),
      new Cell(1, 0, Color.CYAN, true), new EndCell(2, 0),
      new EndCell(-1, 1), new Cell(0, 1, Color.CYAN, true),
      new Cell(1, 1, Color.PINK, false), new EndCell(2, 1),
      new EndCell(-1, 2), new EndCell(0, 2), new EndCell(1, 2), new EndCell(2, 2)));

  ArrayList<ACell> mtACell = new ArrayList<ACell>();

  //constructor that only uses the constant to initialize the board
  FloodItWorld runGame = new FloodItWorld();

  //old game that would be ran
  FloodItWorld game1 = new FloodItWorld(20);

  //games used for testing
  FloodItWorld game2 = new FloodItWorld(1, 100, new Random(20),
      2, 3);

  FloodItWorld game3 = new FloodItWorld(1, 10, new Random(21),
      3, 8);

  //non-initialized world
  FloodItWorld game4 = new FloodItWorld(1, 10, new Random(22),
      2, 4);

  FloodItWorld game5 = new FloodItWorld(5, 5, new Random(23),
      4, 2);

  FloodItWorld game6 = new FloodItWorld(this.unconnectedCells,
      0, 5, new Random(24), new ArrayList<Color>(Arrays.asList(Color.BLUE)),1, 1,
      new ArrayList<ACell>());

  FloodItWorld game7 = new FloodItWorld(this.cells1, 0, 3, new Random(25),
      new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.RED)), 2, 2,
      new ArrayList<ACell>());

  FloodItWorld game8 = new FloodItWorld(this.floodedCells, 0, 3, new Random(26),
      new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.RED)), 2, 2,
      new ArrayList<ACell>());

  FloodItWorld game9 = new FloodItWorld(this.halfFlooded, 0, 3, new Random(27),
      new ArrayList<Color>(Arrays.asList(Color.PINK, Color.BLUE)), 2, 2,
      new ArrayList<ACell>());

  //cells for testing
  Cell c1 = new Cell(0, 0, Color.PINK, false);
  Cell c2 = new Cell(1, 0, Color.GREEN, false);
  Cell c3 = new Cell(0, 1, Color.CYAN, false);
  Cell c4 = new Cell(1, 1, Color.BLUE, true);
  EndCell e1 = new EndCell(0, 0);
  ACell e2 = new EndCell(0,0);

  //initializes the game board
  void initData() {
    this.unconnectedCells = new ArrayList<ACell>(Arrays.asList(
        new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1),
        new EndCell(-1, 0), new Cell(0, 0, Color.BLUE, true), new EndCell(0, 1),
        new EndCell(-1, 1), new EndCell(0, 1), new EndCell(1, 1)));

    this.cells1  = new ArrayList<ACell>(Arrays.asList(
        new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1), new EndCell(2,-1),
        new EndCell(-1, 0), new Cell(0, 0, Color.RED, true),
        new Cell(1, 0, Color.RED, false), new EndCell(2, 0),
        new EndCell(-1, 1), new Cell(0, 1, Color.BLUE, false),
        new Cell(1, 1, Color.RED, false), new EndCell(2, 1),
        new EndCell(-1, 2), new EndCell(0, 2), new EndCell(1, 2), new EndCell(2, 2)));

    this.floodedCells = new ArrayList<ACell>(Arrays.asList(
        new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1), new EndCell(2,-1),
        new EndCell(-1, 0), new Cell(0, 0, Color.RED, true),
        new Cell(1, 0, Color.RED, true), new EndCell(2, 0),
        new EndCell(-1, 1), new Cell(0, 1, Color.BLUE, true),
        new Cell(1, 1, Color.RED, true), new EndCell(2, 1),
        new EndCell(-1, 2), new EndCell(0, 2), new EndCell(1, 2), new EndCell(2, 2)));

    this.halfFlooded = new ArrayList<ACell>(Arrays.asList(
        new EndCell(-1,-1), new EndCell(0,-1), new EndCell(1, -1), new EndCell(2,-1),
        new EndCell(-1, 0), new Cell(0, 0, Color.PINK, true),
        new Cell(1, 0, Color.CYAN, true), new EndCell(2, 0),
        new EndCell(-1, 1), new Cell(0, 1, Color.CYAN, true),
        new Cell(1, 1, Color.PINK, false), new EndCell(2, 1),
        new EndCell(-1, 2), new EndCell(0, 2), new EndCell(1, 2), new EndCell(2, 2)));

    this.mtACell = new ArrayList<ACell>();

    this.c1 = new Cell(0, 0, Color.PINK, false);
    this.c2 = new Cell(1, 0, Color.GREEN, false);
    this.c3 = new Cell(0, 1, Color.CYAN, false);
    this.e1 = new EndCell(0, 0);

    this.runGame = new FloodItWorld();
    this.runGame.initializeBoard();

    this.game1 = new FloodItWorld(20);
    this.game1.initializeBoard();

    this.game2 = new FloodItWorld(1, 100, new Random(20),
        2, 3);
    this.game2.initializeBoard();

    this.game3 = new FloodItWorld(1, 10, new Random(21),
        3, 8);
    this.game3.initializeBoard();

    this.game5 = new FloodItWorld(5, 5, new Random(23),
        4, 2);
    this.game5.initializeBoard();
    this.game6 = new FloodItWorld(this.unconnectedCells,
        0, 5, new Random(24), new ArrayList<Color>(Arrays.asList(Color.BLUE)),1, 1,
        new ArrayList<ACell>());
    this.game7 = new FloodItWorld(this.cells1, 0, 3, new Random(25),
        new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.RED)), 2, 2,
        new ArrayList<ACell>());
    this.game7.stitchCells();

    this.game8 = new FloodItWorld(this.floodedCells, 0, 3, new Random(26),
        new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.RED)), 2, 2,
        new ArrayList<ACell>());
    this.game8.stitchCells();

    this.game9 = new FloodItWorld(this.halfFlooded, 0, 3, new Random(27),
        new ArrayList<Color>(Arrays.asList(Color.PINK, Color.BLUE)), 2, 2,
        new ArrayList<ACell>());
    this.game9.stitchCells();
  }

  //test the indexHelp
  void testIndexHelp(Tester t) {
    initData();

    //test on visible cells
    t.checkExpect(this.game2.indexHelp(0, 0), this.game2.board.get(5));
    t.checkExpect(this.game2.indexHelp(1, 0), this.game2.board.get(6));
    t.checkExpect(this.game2.indexHelp(0, 1), this.game2.board.get(9));
    t.checkExpect(this.game2.indexHelp(1, 1), this.game2.board.get(10));

    //test on end cells
    t.checkExpect(this.game2.indexHelp(-1, -1), this.game2.board.get(0));
    t.checkExpect(this.game2.indexHelp(0, -1), this.game2.board.get(1));
    t.checkExpect(this.game2.indexHelp(-1, 1), this.game2.board.get(8));

    //tests on different world
    t.checkExpect(this.game3.indexHelp(2, 2), this.game3.board.get(18));
    t.checkExpect(this.game3.indexHelp(3, 3), this.game3.board.get(24));
  }


  //tests that the board was created correctly
  void testInitializeBoard(Tester t) {
    initData();
    t.checkExpect(this.game4.board.isEmpty(), true);
    t.checkExpect(this.game4.loc.isEmpty(), true);

    this.game4.initializeBoard();

    t.checkExpect(this.game4.indexHelp(0, 0).bottom,
        this.game4.indexHelp(0,  1));
    t.checkExpect(this.game4.indexHelp(0, 0).right,
        this.game4.indexHelp(1,  0));
    t.checkExpect(this.game4.indexHelp(0, 0).left,
        this.game4.indexHelp(-1,  0));
    t.checkExpect(this.game4.indexHelp(0, 0).top,
        this.game4.indexHelp(0,  -1));
  }

  //testing flooding at the start
  void testFloodInitStarter(Tester t) {
    initData();
    Cell topLeft = (Cell) this.game7.indexHelp(0, 0);
    Cell topRight = (Cell) this.game7.indexHelp(1, 0);
    Cell botLeft = (Cell) this.game7.indexHelp(0, 1);
    Cell botRight = (Cell) this.game7.indexHelp(1, 1);
    t.checkExpect(topLeft.flooded, true);
    t.checkExpect(topRight.flooded, false);
    t.checkExpect(botLeft.flooded, false);
    t.checkExpect(botRight.flooded, false);

    topLeft.floodInitStarter();
    t.checkExpect(topLeft.flooded, true);
    t.checkExpect(topRight.flooded, true);
    t.checkExpect(botLeft.flooded, false);
    t.checkExpect(botRight.flooded, true);
  }

  //check the helper function to flood init starter, which knows the color of the prev
  //  cell taht we came from
  void testColorChecker(Tester t) {
    initData();
    Cell topLeft = (Cell) this.game7.indexHelp(0, 0);
    Cell topRight = (Cell) this.game7.indexHelp(1, 0);
    Cell botLeft = (Cell) this.game7.indexHelp(0, 1);
    Cell botRight = (Cell) this.game7.indexHelp(1, 1);
    t.checkExpect(topLeft.flooded, true);
    t.checkExpect(topRight.flooded, false);
    t.checkExpect(botLeft.flooded, false);
    t.checkExpect(botRight.flooded, false);

    topRight.colorChecker(topLeft.color);
    t.checkExpect(topRight.flooded, true);
    t.checkExpect(botRight.flooded, true);
    t.checkExpect(botLeft.flooded, false);
    t.checkExpect(topLeft.flooded, true);
  }

  void testFloodChecker(Tester t) {
    initData();
    t.checkExpect(this.c3.floodCheck(), false);
    t.checkExpect(this.c4.floodCheck(), true);
    t.checkExpect(this.e1.floodCheck(), true);
  }

  void testUserColors(Tester t) {
    initData();
    t.checkExpect(this.game2.userColors(),
        new ArrayList<Color>(Arrays.asList(
            Color.CYAN, Color.PINK, Color.ORANGE)));
    t.checkExpect(this.game3.userColors(), 
        new ArrayList<Color>(Arrays.asList(
            Color.CYAN, Color.PINK, Color.ORANGE,
            Color.BLUE, Color.RED, Color.GREEN,
            Color.MAGENTA, Color.YELLOW)));
    t.checkExpect(this.game5.userColors(),
        new ArrayList<Color>(Arrays.asList(
            Color.CYAN, Color.PINK)));
  }

  //testing that cells point to each other correctly
  void testStitchCells(Tester t) {
    initData();

    //testing stitch cells on a world
    t.checkExpect(this.game6.indexHelp(-1, -1).right, null);
    t.checkExpect(this.game6.indexHelp(-1, -1).bottom, null);
    t.checkExpect(this.game6.indexHelp(0, 0).top, null);
    t.checkExpect(this.game6.indexHelp(0, 0).left, null);

    //stitching the cells together
    this.game6.stitchCells();

    //show that the cells point to the right other cells
    t.checkExpect(this.game6.indexHelp(-1, -1).right, 
        this.game6.indexHelp(0, -1));
    t.checkExpect(this.game6.indexHelp(-1, -1).bottom, 
        this.game6.indexHelp(-1, 0));
    t.checkExpect(this.game6.indexHelp(0, 0).top, 
        this.game6.indexHelp(0, -1));
    t.checkExpect(this.game6.indexHelp(0, 0).left,
        this.game6.indexHelp(-1, 0));

    //first showing that these cells' fields are null
    t.checkExpect(this.c1.right, null);
    t.checkExpect(this.c2.left, null);
    t.checkExpect(this.c3.top, null);

    //now stitch the cells together so they point to each right and down
    this.c1.stitchCells(this.c2, this.c3);

    //testing the references        
    t.checkExpect(this.c1.right, this.c2);
    t.checkExpect(this.c1.bottom, this.c3);
    t.checkExpect(this.c2.left, this.c1);
    t.checkExpect(this.c3.top, this.c1);
  }                                 

  void testFlood(Tester t) {        
    initData();                     
    t.checkExpect(this.c1.flooded, false);
    t.checkExpect(this.c2.flooded, false);
    t.checkExpect(this.c3.flooded, false);

    //flood the cells
    this.c1.flood();
    this.c2.flood();
    this.c3.flood();

    t.checkExpect(this.c1.flooded, true);
    t.checkExpect(this.c2.flooded, true);
    t.checkExpect(this.c3.flooded, true);

    //does nothing lol, can't even test that is does nothing
    this.e1.flood();
  }

  //tests getting a random color with specified seeds
  void testRandColor(Tester t) {
    initData();
    t.checkExpect(this.game2.randColor(), Color.ORANGE);
    t.checkExpect(this.game3.randColor(), Color.RED);
    t.checkExpect(this.game5.randColor(), Color.PINK);
  }

  //testing checking to see if all cells have been flooded
  void testAllCellsFlooded(Tester t) {
    initData();
    t.checkExpect(this.game8.allCellsFlooded(), true);
    t.checkExpect(this.game2.allCellsFlooded(), false);
  }


  //Testing methods that collect for the worklist and
  void testCollector(Tester t) {
    initData();
    t.checkExpect(this.game2.worklist, this.mtACell);
    this.game2.collector(this.mtACell);
    t.checkExpect(this.game2.worklist, this.mtACell);

    t.checkExpect(this.game9.worklist, this.mtACell);
    this.game9.collector(new ArrayList<ACell>(Arrays.asList(
        this.game9.indexHelp(0, 0))));
    t.checkExpect(this.game9.worklist,
        new ArrayList<ACell>(Arrays.asList(this.game9.indexHelp(1, 0),
            this.game9.indexHelp(0, 1))));

    //testing for an ACell
    initData();
    t.checkExpect(this.game2.worklist, this.mtACell);
    this.e2.collector(this.game2);
    t.checkExpect(this.game2.worklist, this.mtACell);

    t.checkExpect(this.game9.worklist, this.mtACell);
    this.game9.indexHelp(0, 0).collector(this.game9);
    t.checkExpect(this.game9.worklist,
        new ArrayList<ACell>(Arrays.asList(this.game9.indexHelp(1, 0),
            this.game9.indexHelp(0, 1))));

  }

  //Testing the first helper for the collector function
  void testCollectorHelp(Tester t) {
    initData();
    t.checkExpect(this.game2.worklist, this.mtACell);
    t.checkExpect(this.game2.indexHelp(1, 0).floodCheck(), false);
    this.game2.indexHelp(1,0).collectorHelp(this.game2, Color.PINK);
    t.checkExpect(this.game2.worklist, this.mtACell);
    t.checkExpect(this.game2.indexHelp(1, 0).floodCheck(), true);

    t.checkExpect(this.game9.worklist, this.mtACell);
    this.game9.indexHelp(0,1).collectorHelp(this.game9, Color.PINK);
    t.checkExpect(this.game9.worklist,
        new ArrayList<ACell>(Arrays.asList(this.game9.indexHelp(0, 1))));
  }

  //tests drawwing a cell of proper size and color
  void testDrawCell(Tester t) {
    initData();
    t.checkExpect(this.game2.indexHelp(0, 0).drawCell(2),
        new FrameImage(new RectangleImage(Cnst.boardWidth / 2, Cnst.boardHeight / 2,
            OutlineMode.SOLID, Color.ORANGE)));
    t.checkExpect(this.game3.indexHelp(0, 0).drawCell(3),
        new FrameImage(new RectangleImage(Cnst.boardWidth / 3, Cnst.boardHeight / 3,
            OutlineMode.SOLID, Color.GREEN)));
    t.checkExpect(this.game5.indexHelp(0, 0).drawCell(4),
        new FrameImage(new RectangleImage(Cnst.boardWidth / 4, Cnst.boardHeight / 4,
            OutlineMode.SOLID, Color.PINK)));
    t.checkExpect(this.game2.indexHelp(-1, -1).drawCell(1), new EmptyImage());
  }

  //tests drawing a row in a board
  void testDrawRow(Tester t) {
    initData();
    t.checkExpect(this.game2.indexHelp(0, 0).drawRow(2),
        new BesideImage(this.game2.indexHelp(0, 0).drawCell(2),
            this.game2.indexHelp(0, 0).right.drawRow(2)));

    t.checkExpect(this.game3.indexHelp(0, 0).drawRow(3),
        new BesideImage(this.game3.indexHelp(0, 0).drawCell(3),
            this.game3.indexHelp(0, 0).right.drawRow(3)));

    t.checkExpect(this.game5.indexHelp(0, 0).drawRow(4),
        new BesideImage(this.game5.indexHelp(0, 0).drawCell(4),
            this.game5.indexHelp(0, 0).right.drawRow(4)));
    t.checkExpect(this.game2.indexHelp(-1, -1).drawRow(3), new EmptyImage());
  }

  //tests drawing a board both on a cell and on a world
  void testDrawBoard(Tester t) {
    initData();

    //testing draw board on a world
    t.checkExpect(this.game2.drawBoard(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("Turn Count: " + Integer.toString(1) + "/"
                    + Integer.toString(100), Cnst.textHeight, Color.BLACK),
                this.game2.indexHelp(0,0).drawBoard(2)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

    t.checkExpect(this.game3.drawBoard(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("Turn Count: " + Integer.toString(1) + "/"
                    + Integer.toString(10), Cnst.textHeight, Color.BLACK),
                this.game3.indexHelp(0,0).drawBoard(3)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

    t.checkExpect(this.game5.drawBoard(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("Turn Count: " + Integer.toString(5) + "/"
                    + Integer.toString(5), Cnst.textHeight, Color.BLACK),
                this.game5.indexHelp(0,0).drawBoard(4)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

    //testing draw board on a visible cell
    t.checkExpect(this.game2.indexHelp(0, 0).drawBoard(2),
        new AboveImage(this.game2.indexHelp(0, 0).drawRow(2),
            this.game2.indexHelp(0, 0).bottom.drawBoard(2)));

    t.checkExpect(this.game3.indexHelp(0, 0).drawBoard(3),
        new AboveImage(this.game3.indexHelp(0, 0).drawRow(3),
            this.game3.indexHelp(0, 0).bottom.drawBoard(3)));

    t.checkExpect(this.game5.indexHelp(0, 0).drawBoard(4),
        new AboveImage(this.game5.indexHelp(0, 0).drawRow(4),
            this.game5.indexHelp(0, 0).bottom.drawBoard(4)));

    //testing it on an end cell
    t.checkExpect(this.game2.indexHelp(-1, 1).drawBoard(2), new EmptyImage());
  }

  void testDrawWin(Tester t) {
    initData();
    t.checkExpect(this.game3.drawWin(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("You Win! Congrats!", Cnst.textHeight, Color.BLACK),
                this.game3.indexHelp(0,0).drawBoard(3)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

    t.checkExpect(this.game2.drawWin(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("You Win! Congrats!", Cnst.textHeight, Color.BLACK),
                this.game2.indexHelp(0,0).drawBoard(2)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

    t.checkExpect(this.game5.drawWin(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("You Win! Congrats!", Cnst.textHeight, Color.BLACK),
                this.game5.indexHelp(0,0).drawBoard(4)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

  }

  void testDrawLose(Tester t) {
    initData();
    t.checkExpect(this.game2.drawLose(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("You Lose!", Cnst.textHeight, Color.BLACK),
                this.game2.indexHelp(0,0).drawBoard(2)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

    t.checkExpect(this.game3.drawLose(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("You Lose!", Cnst.textHeight, Color.BLACK),
                this.game3.indexHelp(0,0).drawBoard(3)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));

    t.checkExpect(this.game5.drawLose(),
        new OverlayOffsetAlign("center", "bottom",
            new TextImage("Flood-It!", Cnst.textHeight, Color.BLUE),
            0, Cnst.boardHeight + Cnst.scale,
            new AboveAlignImage("center",
                new TextImage("You Lose!", Cnst.textHeight, Color.BLACK),
                this.game5.indexHelp(0,0).drawBoard(4)).movePinhole(0, (Cnst.scale * 7) / 2)
            ).movePinhole(0,  (Cnst.scale * 76) / 10)
        .movePinhole(-Cnst.boardWidth / 2, -Cnst.boardHeight / 2));
  }

  //tests make scene
  void testMakeScene(Tester t) {
    initData();
    WorldScene currScene = this.game2.getEmptyScene();
    currScene.placeImageXY(this.game2.drawBoard(), 400, 400);
    t.checkExpect(this.game2.makeScene(), currScene);

    currScene = this.game3.getEmptyScene();
    currScene.placeImageXY(this.game3.drawBoard(), 400, 400);
    t.checkExpect(this.game3.makeScene(), currScene);

    //testing drawing a losing board
    currScene = this.game5.getEmptyScene();
    currScene.placeImageXY(this.game5.drawLose(), 400, 400);
    t.checkExpect(this.game5.makeScene(), currScene);

    //testing drawwing a winning board
    currScene = this.game8.getEmptyScene();
    currScene.placeImageXY(this.game8.drawWin(), 400, 400);
    t.checkExpect(this.game8.makeScene(), currScene);
  }


  //////////////////////////////////////////
  // testing big bang methods
  //////////////////////////////////////////

  public void testOnTick(Tester t) {
    initData();
    //making a copy of world 5 before the mutation method
    FloodItWorld game5Copy = new FloodItWorld(5, 5, new Random(23),
        4, 2);
    game5Copy.initializeBoard();

    //showing that even if the player magically puts something in the worklist when the game is
    //  lost, nothing will change anyway
    t.checkExpect(game5Copy, this.game5);
    this.game5.worklist.add(this.game5.indexHelp(0,0));
    this.game5.onTick();
    t.checkExpect(game5Copy, this.game5);

    //making a copy of world 8 before the mutation method
    FloodItWorld game8Copy = new FloodItWorld(this.floodedCells, 0, 3, new Random(26),
        new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.RED)), 2, 2,
        new ArrayList<ACell>());
    game8Copy.stitchCells();

    //showing that even if the player magically puts something in the worklist when the game is
    //  won, nothing will change anyway
    t.checkExpect(game8Copy, this.game8);
    this.game8.worklist.add(this.game8.indexHelp(0,0));
    this.game8.onTick();
    this.game8.onTick();
    t.checkExpect(this.game8, game8Copy);


    //testing that the onTick will go one cell deep when it has something in the worklist
    Cell topLeft = (Cell) this.game3.indexHelp(0, 0);
    Cell topMid = (Cell) this.game3.indexHelp(1, 0);
    Cell midLeft = (Cell) this.game3.indexHelp(0, 1);

    t.checkExpect(topLeft.color, Color.GREEN);
    t.checkExpect(topMid.floodCheck(), false);
    t.checkExpect(midLeft.floodCheck(), false);

    topLeft.color = Color.magenta;
    this.game3.board.remove(6);
    this.game3.board.add(6, topLeft);
    this.game3.worklist.add(topLeft);

    topLeft = (Cell) this.game3.indexHelp(0, 0);
    topMid = (Cell) this.game3.indexHelp(1, 0);
    midLeft = (Cell) this.game3.indexHelp(0, 1);

    t.checkExpect(topLeft.color, Color.MAGENTA);
    t.checkExpect(this.game3.worklist,
        new ArrayList<ACell>(Arrays.asList(
            topLeft)));
  }


  public void testOnMouseClicked(Tester t) {
    //testing clicking offscreen
    initData();
    Cell topLeft = (Cell) this.game2.indexHelp(0, 0);
    Cell topRight = (Cell) this.game2.indexHelp(1, 0);
    Cell botLeft = (Cell) this.game2.indexHelp(0, 1);
    Cell botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.ORANGE);
    t.checkExpect(topRight.color, Color.PINK);
    t.checkExpect(botLeft.color, Color.CYAN);
    t.checkExpect(botRight.color, Color.CYAN);

    this.game2.onMouseClicked(new Posn(86, 589));
    topLeft = (Cell) this.game2.indexHelp(0, 0);
    topRight = (Cell) this.game2.indexHelp(1, 0);
    botLeft = (Cell) this.game2.indexHelp(0, 1);
    botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.ORANGE);
    t.checkExpect(topRight.color, Color.PINK);
    t.checkExpect(botLeft.color, Color.CYAN);
    t.checkExpect(botRight.color, Color.CYAN);

    t.checkExpect(this.game2.worklist, this.mtACell);

    //testing clicking on pink, the adjacent cell to the right
    initData();
    topLeft = (Cell) this.game2.indexHelp(0, 0);
    topRight = (Cell) this.game2.indexHelp(1, 0);
    botLeft = (Cell) this.game2.indexHelp(0, 1);
    botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.ORANGE);
    t.checkExpect(topRight.color, Color.PINK);
    t.checkExpect(botLeft.color, Color.CYAN);
    t.checkExpect(botRight.color, Color.CYAN);


    this.game2.onMouseClicked(new Posn(570,260));
    topLeft = (Cell) this.game2.indexHelp(0, 0);
    topRight = (Cell) this.game2.indexHelp(1, 0);
    botLeft = (Cell) this.game2.indexHelp(0, 1);
    botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.PINK);
    t.checkExpect(topRight.color, Color.PINK);
    t.checkExpect(botLeft.color, Color.CYAN);
    t.checkExpect(botRight.color, Color.CYAN);

    t.checkExpect(this.game2.worklist,
        new ArrayList<ACell>(Arrays.asList(topLeft)));
  }

  public void testClickHelp(Tester t) {    
    //testing clicking on pink, the adjacent cell to the right
    initData();
    Cell topLeft = (Cell) this.game2.indexHelp(0, 0);
    Cell topRight = (Cell) this.game2.indexHelp(1, 0);
    Cell botLeft = (Cell) this.game2.indexHelp(0, 1);
    Cell botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.ORANGE);
    t.checkExpect(topRight.color, Color.PINK);
    t.checkExpect(botLeft.color, Color.CYAN);
    t.checkExpect(botRight.color, Color.CYAN);


    topRight.clickHelp(this.game2, topLeft);
    topLeft = (Cell) this.game2.indexHelp(0, 0);
    topRight = (Cell) this.game2.indexHelp(1, 0);
    botLeft = (Cell) this.game2.indexHelp(0, 1);
    botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.PINK);
    t.checkExpect(topRight.color, Color.PINK);
    t.checkExpect(botLeft.color, Color.CYAN);
    t.checkExpect(botRight.color, Color.CYAN);

    t.checkExpect(this.game2.worklist,
        new ArrayList<ACell>(Arrays.asList(topLeft)));

    //testing that nothing changes over an end cell
    initData();
    FloodItWorld game2Copy = new FloodItWorld(1, 100, new Random(20),
        2, 3);
    game2Copy.initializeBoard();
    t.checkExpect(this.game2, game2Copy);
    this.game2.indexHelp(-1, 0).clickHelp(this.game2, topLeft);
    t.checkExpect(this.game2, game2Copy);
  }

  public void testClickHelp2(Tester t) {
    //testing on a Cell
    initData();
    Cell topLeft = (Cell) this.game2.indexHelp(0, 0);
    t.checkExpect(topLeft.color, Color.ORANGE);

    topLeft.clickHelp2(this.game2, Color.PINK);

    topLeft = (Cell) this.game2.indexHelp(0, 0);
    t.checkExpect(topLeft.color, Color.PINK);
    t.checkExpect(this.game2.currTurn, 2);
    t.checkExpect(this.game2.worklist,
        new ArrayList<ACell>(Arrays.asList(topLeft)));
  }

  public void testOnKeyReleased(Tester t) {
    initData();
    Cell topLeft = (Cell) this.game2.indexHelp(0, 0);
    Cell topRight = (Cell) this.game2.indexHelp(1, 0);
    Cell botLeft = (Cell) this.game2.indexHelp(0, 1);
    Cell botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.ORANGE);
    t.checkExpect(topRight.color, Color.PINK);
    t.checkExpect(botLeft.color, Color.CYAN);
    t.checkExpect(botRight.color, Color.CYAN);

    this.game2.onKeyReleased("r");
    topLeft = (Cell) this.game2.indexHelp(0, 0);
    topRight = (Cell) this.game2.indexHelp(1, 0);
    botLeft = (Cell) this.game2.indexHelp(0, 1);
    botRight = (Cell) this.game2.indexHelp(1, 1);

    t.checkExpect(topLeft.color, Color.ORANGE);
    t.checkExpect(topRight.color, Color.CYAN);
    t.checkExpect(botLeft.color, Color.PINK);
    t.checkExpect(botRight.color, Color.CYAN);
  }


  //trying to invoke big bang
  void testBigBang(Tester t) {
    initData();
    FloodItWorld g = this.runGame;
    int worldWidth = 1000;
    int worldHeight = 1000;
    double tickRate = .012;
    g.bigBang(worldWidth, worldHeight, tickRate);
  }

}





