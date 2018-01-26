// Assignment 10 - Part 2
// Fliedner Jillian
// jfliedner
// Allen David
// dallen1212

// press "p" to generate a new maze
// press "d" to start a depth search
// press "b" to start a breadth search



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// a class to represent a node
class Node {

  Posn loc; // (x,y) position on app window

  // flags for if there is a connection to the nodes surrounding this node
  boolean above = false;
  boolean below = false;
  boolean left = false;
  boolean right = false;

  Color nodeColor = Color.LIGHT_GRAY;

  ArrayList<Node> neighbors = new ArrayList<Node>();

  // constructor
  Node(int k, int i, Color nodeColor) {
    this.loc = new Posn(k, i);
    this.nodeColor = nodeColor;
  }

  // convenience constructor
  Node(Posn loc) {
    this.loc = loc;
  }

  // convenience constructor
  Node(int k, int i) {
    this.loc = new Posn(k, i);
  }

  // overrides hashCode(), valid if loc.y < 9999
  public int hashCode() {
    return loc.x * 10000 + loc.y;
  }

  // overrides equals method
  public boolean equals(Object o) {
    if (!(o instanceof Node)) {
      return false;
    }
    else {
      Node other = (Node)o;
      return this.loc.x == other.loc.x
          && this.loc.y == other.loc.y;
    }
  }
 
  // overrides toString(), formats this Node's string representation based on
  // its position as "x,y"
  public String toString() {
    return String.format("%d,%d", loc.x, loc.y);
  }

  // draws this Node on a given scene based on its connections to the
  // surrounding nodes
  // EFFECT: draws lines on scene s
  public void renderNode(WorldScene s) {

    RectangleImage bgd = new RectangleImage(MazeWorld.NODE_SIZE, MazeWorld.NODE_SIZE,
        "solid", this.nodeColor);
    s.placeImageXY(bgd, this.loc.x * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2,
        this.loc.y * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2);

    // draw a green square if the Node is in the top left corner (0,0)
    if (loc.x == 0 && loc.y == 0) {
      RectangleImage startRect = new RectangleImage(MazeWorld.NODE_SIZE, MazeWorld.NODE_SIZE,
          "solid", Color.GREEN);
      s.placeImageXY(startRect, MazeWorld.NODE_SIZE / 2, MazeWorld.NODE_SIZE / 2);
    }

    // draw a purple square if the Node is in the bottom right corner
    if (loc.x == MazeWorld.XNODE_NUMBER - 1 && loc.y == MazeWorld.YNODE_NUMBER - 1) {
      RectangleImage endRect = new RectangleImage(MazeWorld.NODE_SIZE, MazeWorld.NODE_SIZE,
          "solid", Color.MAGENTA);
      s.placeImageXY(endRect, MazeWorld.WIDTH - MazeWorld.NODE_SIZE / 2,
          MazeWorld.HEIGHT - MazeWorld.NODE_SIZE / 2);
    }

    // draw top line if no connection
    if (!this.above) {
      LineImage line = new LineImage(new Posn(MazeWorld.NODE_SIZE, 0), Color.BLACK);
      s.placeImageXY(line, this.loc.x * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2,
          this.loc.y * MazeWorld.NODE_SIZE);
    }

    // draw right line if no connection
    if (!this.right) {
      LineImage line = new LineImage(new Posn(0, MazeWorld.NODE_SIZE), Color.BLACK);
      s.placeImageXY(line, this.loc.x * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE,
          this.loc.y * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2);
    }

    // draw bottom line if no connection
    if (!this.below) {
      LineImage line = new LineImage(new Posn(MazeWorld.NODE_SIZE, 0), Color.BLACK);
      s.placeImageXY(line, this.loc.x * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2,
          this.loc.y * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE);
    }

    // draw left line if no connection
    if (!this.left) {
      LineImage line = new LineImage(new Posn(0, MazeWorld.NODE_SIZE), Color.BLACK);
      s.placeImageXY(line, this.loc.x * MazeWorld.NODE_SIZE,
          this.loc.y * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2);
    }
  }

  // a method to connect Nodes
  // EFFECT: update the connection flags of this Node and a given Node so that
  // they are now connected
  public void connectNodes(Node n) {

    this.neighbors.add(n);
    n.neighbors.add(this);

    // get the locations of this Node and the given Node
    Posn n1 = this.loc;
    Posn n2 = n.loc;

    // if given Node is to the right of this Node
    if (n2.x - n1.x == 1) {
      n.left = true;
      this.right = true;
    }

    // if given Node is to the left of this Node
    if (n2.x - n1.x == -1) {
      n.right = true;
      this.left = true;
    }

    // if given Node is below this Node
    if (n2.y - n1.y == 1) {
      n.above = true;
      this.below = true;
    }

    // if given Node is above this Node
    if (n2.y - n1.y == -1) {
      n.below = true;
      this.above = true;
    }
  }

  // change the color of this node
  void changeColor(ITrail t) {
    t.paintTrail(this);
  }
}

// abstract class to represent a runner
abstract class ARunner extends Node {

  ITrail trail;

  ARunner(int x, int y, Color nodeColor, ITrail trail) {
    super(x, y, nodeColor);
    this.trail = trail;
  }

  // renders the runner
  public void renderNode(WorldScene s) {
    RectangleImage runnerRect = new RectangleImage(MazeWorld.NODE_SIZE,
        MazeWorld.NODE_SIZE, "solid", this.nodeColor);
    s.placeImageXY(runnerRect, this.loc.x * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2,
        this.loc.y * MazeWorld.NODE_SIZE + MazeWorld.NODE_SIZE / 2);
  }

  // searches on tick
  abstract void searchTick();
}

// to represent a Depth Search runner
class DepthSearchRun extends ARunner {
  HashMap<String, Edge> cameFromEdge = new HashMap<String, Edge>();
  Stack<Node> workList = new Stack<Node>();
  ArrayList<Node> visited = new ArrayList<Node>();
  ArrayList<Node> maze;
  Node last;
  boolean justTrail = false;
  boolean done = false;

  DepthSearchRun(int x, int y, Color nodeColor, ITrail trail, ArrayList<Node> maze) {
    super(x, y, nodeColor, trail);
    this.maze = maze;
    this.workList.push(maze.get(0));
  }

  DepthSearchRun(ArrayList<Node> maze, boolean justTrail) {
    super(0, 0, Color.RED, null);
    this.maze = maze;
    this.workList.push(maze.get(0));
    this.justTrail = justTrail;
  }

  // searches on tick
  public void searchTick() {
    String target = maze.get(MazeWorld.XNODE_NUMBER * MazeWorld.YNODE_NUMBER - 1).toString();
    if (this.workList.contents.size() > 0) {
      Node next = this.workList.pop();
      this.loc.x = next.loc.x;
      this.loc.y = next.loc.y;
      if (!justTrail) {
        next.changeColor(this.trail);
      }
      if (this.visited.contains(next)) {
        // discard and do not process
      } else if (next.toString().equals(target)) {
        System.out.println("success");
        this.cameFromEdge.put(next.toString(), new Edge(this.last, next, 0));
        this.recur(this.last);
        this.workList = new Stack<Node>();
      } else {
        Node nextNode = new Node(0, 0);
        for (int i = 0; i <= next.neighbors.size() - 1; i++) {
          nextNode = next.neighbors.get(i);
          if (!this.visited.contains(nextNode)) {
            this.workList.push(nextNode);
            Edge e = new Edge(next, nextNode, 0);
            this.cameFromEdge.put(nextNode.toString(), e);
          }
        }

      }

      this.visited.add(next);
      this.last = next;
    }
  }

  // recur until you reach the last node
  public void recur(Node last) {
    Node endNode = last;
    while (!endNode.toString().equals("0,0")) {
      Edge backOne = this.cameFromEdge.get(endNode.toString());
      endNode.nodeColor = Color.red;
      endNode = backOne.from;
      System.out.println(endNode.toString());
    }
    this.done = true;
  }
}

// to represent a Breadth Search runner
class BreadthSearchRun extends ARunner {
  HashMap<String, Edge> cameFromEdge = new HashMap<String, Edge>();
  Queue<Node> workList = new Queue<Node>();
  ArrayList<Node> visited = new ArrayList<Node>();
  ArrayList<Node> maze;
  boolean forward = true;
  Node last;

  BreadthSearchRun(int x, int y, Color nodeColor, ITrail trail, ArrayList<Node> maze) {
    super(x, y, nodeColor, trail);
    this.maze = maze;
    this.workList.enqueue(maze.get(0));
  }

  // searches on tick
  public void searchTick() {
    String target = maze.get(MazeWorld.XNODE_NUMBER * MazeWorld.YNODE_NUMBER - 1).toString();
    if (this.workList.contents.size() > 0) {
      Node next = this.workList.dequeue();
      this.loc.x = next.loc.x;
      this.loc.y = next.loc.y;
      next.changeColor(this.trail);
      if (this.visited.contains(next)) {
        // discard and do not process
      } else if (next.toString().equals(target)) {
        System.out.println("success");
        this.recur(this);
        this.workList = new Queue<Node>();
      } else {
        Node nextNode = new Node(0, 0);
        for (int i = 0; i <= next.neighbors.size() - 1; i++) {
          nextNode = next.neighbors.get(i);
          if (!this.visited.contains(nextNode)) {
            this.workList.enqueue(nextNode);
            Edge e = new Edge(next, nextNode, 0);
            this.cameFromEdge.put(nextNode.toString(), e);
          }
        }

      }

      this.visited.add(next);
      this.last = next;
    }
  }

  // recurs until last node
  public void recur(Node last) {
    Node endNode = last;
    while (!endNode.toString().equals("0,0")) {
      Edge backOne = this.cameFromEdge.get(endNode.toString());
      endNode.nodeColor = Color.red;
      endNode = backOne.from;
      System.out.println(endNode.toString());
    }
  }
}

// to represent a player
class Player extends ARunner {

  Player(int x, int y, Color nodeColor, ITrail trail) {
    super(x, y, nodeColor, trail);
  }

  // 0 is left, 1 is up, 2 is right, 3 is down
  public void movePlayer(int dir, ArrayList<Node> maze) {
    int ind = this.loc.y * MazeWorld.XNODE_NUMBER + this.loc.x;
    Node onNode = maze.get(ind);
    if (dir == 0 && this.loc.x > 0 && onNode.left) {
      this.loc.x = this.loc.x - 1;
      onNode.changeColor(this.trail);
    }
    if (dir == 1 && this.loc.y > 0 && onNode.above) {
      this.loc.y = this.loc.y - 1;
      onNode.changeColor(this.trail);
    }
    if (dir == 2 && this.loc.x < MazeWorld.XNODE_NUMBER - 1 && onNode.right) {
      this.loc.x = this.loc.x + 1;
      onNode.changeColor(this.trail);
    }
    if (dir == 3 && this.loc.y < MazeWorld.YNODE_NUMBER - 1 && onNode.below) {
      this.loc.y = this.loc.y + 1;
      onNode.changeColor(this.trail);
    }
  }

  void searchTick() {
    // doesn't do a searchTick because it is a Player
  }

}

// a class to represent an Edge (which connects two Nodes)
class Edge {
  Node from;
  Node to;
  int weight;

  // constructor
  Edge(Node from, Node to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }
}

// a class to represent a Graph
class Graph {
  // used for connections between Nodes (as represented by String)
  HashMap<String, String> representatives = new HashMap<String, String>();

  // accumulates Edges between two Nodes
  ArrayList<Edge> edgesInTree = new ArrayList<Edge>();

  // all Edges in Graph to be passed through Kruskal
  ArrayList<Edge> workList = new ArrayList<Edge>();

  // the Nodes of the maze
  ArrayList<Node> allNodes = new ArrayList<Node>();

  // a method to initialize a maze Graph
  void initGraph() {

    // initialize Nodes
    for (int i = 0; i < MazeWorld.YNODE_NUMBER; i++) {
      for (int k = 0; k < MazeWorld.XNODE_NUMBER; k++) {
        Node node = new Node(new Posn(k, i));
        this.allNodes.add(node);
      }
    }

    // map each Node to itself in the HashMap
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = allNodes.get(i);
      this.representatives.put(node.toString(), node.toString());
    }

    // accumulate edges that have already been made
    ArrayList<String> visited = new ArrayList<String>();

    // while the edges we've already made are less than the total possible
    // edges, generate more edges for the workList
    while (visited.size() < 4 * MazeWorld.XNODE_NUMBER * MazeWorld.YNODE_NUMBER + 50) {

      Random rand = new Random();

      // get a random node
      Node node1 = allNodes.get(rand.nextInt(allNodes.size()));

      Utils util = new Utils();

      // make an adjacent Node
      Node node2 = util.sideNode(node1.loc);

      // generate String representations of the Edge between these Nodes, both
      // forward and backward
      String forwardString = String.format("%s + %s", node1.toString(), node2.toString());
      String revString = String.format("%s + %s", node2.toString(), node1.toString());

      // if the Edge hasn't been made yet, add it to the workList with a random
      // weight
      if (!visited.contains(forwardString) || !visited.contains(revString)) {
        Edge randEdge = new Edge(node1, node2, rand.nextInt(allNodes.size()));
        this.workList.add(randEdge);
        visited.add(forwardString);
      }

    }

    this.kruskal(); // run kruskals algorithm

  }

  // a method to draw all the Nodes in this Graph on a given WorldScene
  public WorldScene renderMaze(WorldScene s) {
    for (int i = 0; i < this.allNodes.size(); i++) {
      this.allNodes.get(i).renderNode(s);
    }
    return s;
  }

  // kruskal algorithm
  void kruskal() {

    Utils util = new Utils();

    // sort this.workList by Edge weights from smallest to largest
    util.quicksort(this.workList, new EdgeComparator());

    int i = 0;

    // to accumulate previously formed Edges in this.edgeInTree
    ArrayList<String> temp = new ArrayList<String>();

    // keep looping until number of Edges made is one less than total Nodes
    while (i < workList.size() && edgesInTree.size() < allNodes.size() - 1) {
      // get the smallest Edge, by weight
      Edge curEdge = this.workList.get(i);
      // check if we already have that Edge
      if (!temp.contains(String.format("%s %s", curEdge.from, curEdge.to))
          || !temp.contains(String.format("%s %s", curEdge.to, curEdge.from))) {
        // check if the Nodes in the Edge are already linked in the HashMap
        // using find()
        if (find(this.representatives, curEdge.from.toString())
            .equals(find(this.representatives, curEdge.to.toString()))) {
          // do nothing if they are
        }
        // if the Nodes aren't connected, connect them
        else {
          // calculate the indices of the from Node (ind1) and the to Node
          // (ind2) using the formula:
          // y-position * number of Nodes per row + x-position
          int ind1 = curEdge.from.loc.y * MazeWorld.XNODE_NUMBER + curEdge.from.loc.x;
          int ind2 = curEdge.to.loc.y * MazeWorld.XNODE_NUMBER + curEdge.to.loc.x;

          // get the from Node and connect it to the to Node
          this.allNodes.get(ind1).connectNodes(this.allNodes.get(ind2));

          // add the current Edge to the tree being formed in this.edgesInTree
          this.edgesInTree.add(curEdge);
          // add the String representation of the Edge to the accumulator
          temp.add(String.format("%s %s", curEdge.from, curEdge.to));

          // connect the Nodes in the current Edge in the HashMap
          union(this.representatives, find(this.representatives, curEdge.from.toString()),
              find(this.representatives, curEdge.to.toString()));
        }
      }
      i++;
    }
  }

  // a method for find in the union-find data structure
  public String find(HashMap<String, String> reps, String s) {
    // base case: is the given String mapped to itself
    if (reps.get(s).equals(s)) {
      return s;
    }
    // if not, recur on the connection returned by getting the given String
    else {
      return find(reps, reps.get(s));
    }
  }

  // a method for union in the union-find data structure; connects two Nodes (as
  // String) in the HashMap
  public void union(HashMap<String, String> reps, String s1, String s2) {
    reps.put(s1, s2);
  }
}

// an interface to represent an IComparator
interface IComparator<T> {

  // method to do a comparison between two objects of type T
  int compare(T t1, T t2);
}

// a class to represent an EdgeComparator
class EdgeComparator implements IComparator<Edge> {

  // a method to compare two Edges by weight
  public int compare(Edge t1, Edge t2) {
    if (t1.weight < t2.weight) {
      return -1;
    } else if (t1.weight > t2.weight) {
      return 1;
    } else {
      return 0;
    }
  }
}

// a class representing a MazeWorld
class MazeWorld extends World {

  public static int WIDTH; // width of window

  public static int HEIGHT; // height of window

  public static int NODE_SIZE; // width & height of one, square Node

  public static int XNODE_NUMBER; // number of Nodes in a row

  public static int YNODE_NUMBER; // number of Nodes in a column

  Graph graph = new Graph(); // Graph which represents the maze

  ARunner runner;

  Player player;

  MazeWorld(int width, int height, int nodeSize, Player p) {
    MazeWorld.WIDTH = width;
    MazeWorld.HEIGHT = height;
    MazeWorld.NODE_SIZE = nodeSize;
    MazeWorld.XNODE_NUMBER = width / nodeSize;
    MazeWorld.YNODE_NUMBER = height / nodeSize;
    this.player = p;
  }

  // initialize the Graph, which in turn generates a maze
  void initMaze() {
    this.graph.initGraph();
    this.player = new Player(0, 0, Color.CYAN, new BlueTrail());
  }

  public WorldScene makeScene() {
    WorldScene s = this.getEmptyScene();
    if (runner == null) {
      this.player.renderNode(this.graph.renderMaze(s));
    } else {
      this.player.renderNode(this.graph.renderMaze(s));
      this.runner.renderNode(s);
    }
    return s;
  }

  public void onTick() {
    if (this.runner != null) {
      this.runner.searchTick();
    }

    if (this.player.loc.x == XNODE_NUMBER - 1 && this.player.loc.y == YNODE_NUMBER - 1) {
      DepthSearchRun dfs = new DepthSearchRun(this.graph.allNodes, true);
      while (!dfs.done) {
        dfs.searchTick();
      }
    }
  }

  public void onKeyEvent(String ke) {

    if (ke.equals("p")) {
      this.graph = new Graph();
      this.initMaze();
      this.runner = null;
      this.player = new Player(0, 0, this.player.nodeColor, this.player.trail);
    }

    if (ke.equals("d")) {
      this.runner = new DepthSearchRun(0, 0, Color.RED, new BlueTrail(), this.graph.allNodes);
    }

    if (ke.equals("b")) {
      this.runner = new BreadthSearchRun(0, 0, Color.RED, new BlueTrail(), this.graph.allNodes);
    }

    if (ke.equals("left")) {
      this.player.movePlayer(0, this.graph.allNodes);
    }
    if (ke.equals("up")) {
      this.player.movePlayer(1, this.graph.allNodes);
    }
    if (ke.equals("right")) {
      this.player.movePlayer(2, this.graph.allNodes);
    }
    if (ke.equals("down")) {
      this.player.movePlayer(3, this.graph.allNodes);
    }
  }
}

// represents a trail, for tracking paths
interface ITrail {

  void paintTrail(Node n);
}

// a blue tail
class BlueTrail implements ITrail {

  public void paintTrail(Node n) {
    if (n.nodeColor.equals(Color.RED)) {
      // don't change red Nodes
    } else if (n.nodeColor.equals(Color.LIGHT_GRAY)) {
      n.nodeColor = new Color(150, 150, 175);
    } else if (n.nodeColor.getBlue() < 250) {
      n.nodeColor = new Color(n.nodeColor.getRed() - 5,
          n.nodeColor.getGreen() - 5, n.nodeColor.getBlue() + 5);
    }
  }

}

// a rainbow tail
class RainbowTrail implements ITrail {

  int count = 0;
  Random rand = new Random();

  public void paintTrail(Node n) {
    n.nodeColor = new Color(rand.nextInt(254), rand.nextInt(254), rand.nextInt(254)).brighter();

  }

}

// Utils class
class Utils {

  // given a Posn, generates a random, adjacent Node either to the right, or
  // below a given Posn
  Node sideNode(Posn p) {
    Random rand = new Random();
    boolean flag = rand.nextBoolean(); // true if new Node will be to the right

    // check if Node is in bottom right corner
    if (p.x >= MazeWorld.XNODE_NUMBER - 1 && p.y >= MazeWorld.YNODE_NUMBER - 1) {
      return new Node(p);
    }

    // check if Node is flagged to be a right Node, but the given Posn is at the
    // x limit
    else if (flag && p.x >= MazeWorld.XNODE_NUMBER - 1) {
      return new Node(p.x, p.y + 1);
    }

    // check if Node is flagged to be a bottom Node, but the given Posn is at
    // the y limit
    else if (p.y >= MazeWorld.YNODE_NUMBER - 1) {
      return new Node(p.x + 1, p.y);
    }

    // if Node should be to the right
    else if (flag) {
      return new Node(p.x + 1, p.y);
    }

    // Node is bottom to the given Node
    else {
      return new Node(p.x, p.y + 1);
    }
  }

  // In ArrayUtils
  <T> void swap(ArrayList<T> arr, int index1, int index2) {
    T oldValueAtIndex2 = arr.get(index2);

    arr.set(index2, arr.get(index1));
    arr.set(index1, oldValueAtIndex2);
  }

  // Returns the index where the pivot element ultimately ends up in the sorted
  // source
  // EFFECT: Modifies the source list in the range [loIdx, hiIdx) such that
  // all values to the left of the pivot are less than (or equal to) the pivot
  // and all values to the right of the pivot are greater than it
  <T> int partition(ArrayList<T> source, IComparator<T> comp, int loIdx, int hiIdx, T pivot) {
    int curLo = loIdx;
    int curHi = hiIdx - 1;
    while (curLo < curHi) {
      // Advance curLo until we find a too-big value (or overshoot the end of
      // the list)
      while (curLo < hiIdx && comp.compare(source.get(curLo), pivot) <= 0) {
        curLo = curLo + 1;
      }
      // Advance curHi until we find a too-small value (or undershoot the start
      // of the list)
      while (curHi >= loIdx && comp.compare(source.get(curHi), pivot) > 0) {
        curHi = curHi - 1;
      }
      if (curLo < curHi) {
        swap(source, curLo, curHi);
      }
    }
    swap(source, loIdx, curHi); // place the pivot in the remaining spot
    return curHi;
  }

  // In ArrayUtils
  // EFFECT: Sorts the given ArrayList according to the given comparator
  <T> void quicksort(ArrayList<T> arr, IComparator<T> comp) {
    quicksortHelp(arr, comp, 0, arr.size());
  }

  // EFFECT: sorts the source array according to comp, in the range of indices
  // [loIdx, hiIdx)
  <T> void quicksortHelp(ArrayList<T> source, IComparator<T> comp, int loIdx, int hiIdx) {
    // Step 0: check for completion
    if (loIdx >= hiIdx) {
      return; // There are no items to sort
    }
    // Step 1: select pivot
    T pivot = source.get(loIdx);
    // Step 2: partition items to lower or upper portions of the temp list
    int pivotIdx = partition(source, comp, loIdx, hiIdx, pivot);
    // Step 3: sort both halves of the list
    quicksortHelp(source, comp, loIdx, pivotIdx);
    quicksortHelp(source, comp, pivotIdx + 1, hiIdx);
  }
}

// to represent a Deque
class Deque<T> {

  Sentinel<T> header;

  Deque(Sentinel<T> header) {
    this.header = header;
  }

  Deque() {
    this.header = new Sentinel<T>();
  }

  // returns the size of the deque
  int size() {
    return this.header.count();
  }

  // adds the given t to the front of the list
  void addAtHead(T t) {
    this.header.addOnEnd(t, true);
  }

  // adds the given t to the end of the list
  void addAtTail(T t) {
    this.header.addOnEnd(t, false);
  }

  // removes the head of the list
  T removeFromHead() {
    return this.header.next.removeFromEnd(this.header, true);
  }

  // removes the end of the list
  T removeFromTail() {
    return this.header.prev.removeFromEnd(this.header, false);
  }
}

// to represent an abstract Point<T>
abstract class APoint<T> {

  APoint<T> next;
  APoint<T> prev;

  APoint(APoint<T> next, APoint<T> prev) {
    this.next = next;
    this.prev = prev;
  }

  // adds the given Point to the prev
  void addBefore(APoint<T> point) {
    this.prev = point;
  }

  // adds the given Point to the next
  void addAfter(APoint<T> point) {
    this.next = point;
  }

  // adds the given type T att the front or end
  void addOnEnd(T t, boolean front) {
    if (this.next == null || this.prev == null) {
      this.next = new Point<T>(t, this, this);
    } else if (front) {
      this.next = new Point<T>(t, this.next, this);
    } else {
      this.prev = new Point<T>(t, this, this.prev);
    }
  }

  // helper function for count
  abstract int countHelp();

  // removes the given Point from the front or end
  abstract T removeFromEnd(APoint<T> point, boolean front);

  // helper function for removePoint
  abstract void removePointHelp(APoint<T> point, APoint<T> prev);
}

// to represent a sentinel
class Sentinel<T> extends APoint<T> {

  Sentinel(APoint<T> next, APoint<T> prev) {
    super(next, prev);
  }

  Sentinel() {
    super(null, null);
    this.next = this;
    this.prev = this;
  }

  // counts the number of Points
  int count() {
    return this.next.countHelp();
  }

  // helper for count
  int countHelp() {
    return 0;
  }

  // helper for removePoint
  void removePointHelp(APoint<T> point, APoint<T> prev) {
    // empty so that when searching the list for Point, if
    // the method gets to the Sentinel, do nothing
  }

  // removes the Point from the front or end
  T removeFromEnd(APoint<T> point, boolean front) {
    throw new RuntimeException("can't remove from an empty Deque");
  }

  T peakHelp() {
    throw new RuntimeException("can't remove from an empty Deque");
  }
}

// to represent a Point
class Point<T> extends APoint<T> {

  T data;

  Point(T data, APoint<T> next, APoint<T> prev) {
    super(next, prev);
    this.data = data;
    if (next == null || prev == null) {
      throw new IllegalArgumentException("can't contruct with null");
    }
    this.next.addBefore(this);
    this.prev.addAfter(this);
  }

  Point(T data) {
    super(null, null);
    this.data = data;
  }

  // counts the number of Points
  int countHelp() {
    return 1 + this.next.countHelp();
  }

  // helper for removePoint
  void removePointHelp(APoint<T> point, APoint<T> prev) {
    if (this == point) {
      prev.next = this.next;
      this.next.prev = this.prev;
    } else {
      this.next.removePointHelp(point, this);
    }
  }

  // removes the Point from the front or end
  T removeFromEnd(APoint<T> point, boolean front) {
    if (front) {
      point.next = this.next;
      this.next.prev = this.prev;
      return this.data;
    } else {
      point.prev = this.prev;
      this.prev.next = this.next;
      return this.data;
    }
  }
}

// to represent a stack
class Stack<T> {
  Deque<T> contents = new Deque<T>();

  // pushes an element onto the front of this stack
  void push(T item) {
    this.contents.addAtHead(item);
  }

  // is this stack empty
  boolean isEmpty() {
    return this.contents.size() == 0;
  }

  // pops the first element off the stack and returns it
  T pop() {
    return this.contents.removeFromHead();
  }
}

// to represent a queue
class Queue<T> {
  Deque<T> contents = new Deque<T>();

  // adds an element to the end of the queue
  void enqueue(T item) {
    this.contents.addAtTail(item);
  }

  // is this queue empty
  boolean isEmpty() {
    return this.contents.size() == 0;
  }

  // removes element from the front of the queue and returns it
  T dequeue() {
    return this.contents.removeFromHead();
  }
}

// examples
class Examples {
  Random rand = new Random();
  Graph graph = new Graph();
  Player player = new Player(0, 0, Color.CYAN, new BlueTrail());
  MazeWorld world = new MazeWorld(500, 500, 20, player);
  Node node = new Node(new Posn(3, 3));
  HashMap<Node, Node> test = new HashMap<Node, Node>();
  Node node2 = new Node(3, 3);
  Queue<String> q1 = new Queue<String>();
  Stack<String> s1 = new Stack<String>();
  Edge e1 = new Edge(this.node, this.node2, 5);
  Edge e2 = new Edge(this.node2, this.node, 7);
  IComparator<Edge> ec = new EdgeComparator();
  ITrail blue = new BlueTrail();
  ITrail rainbow = new RainbowTrail();

  //set to true to run tests or false to render a MazeWorld
  boolean debugMode = false;

  //initialize for tests
  void init() {
    this.node.connectNodes(this.node2);
    this.node.nodeColor = Color.gray;
  }

  // to test queue methods
  void testQueue(Tester t) {
    t.checkExpect(this.q1.isEmpty(), true);
    t.checkExpect(this.q1.contents.size(), 0);
    this.q1.enqueue("abc");
    t.checkExpect(this.q1.contents.size(), 1);
    this.q1.dequeue();
    t.checkExpect(this.q1.contents.size(), 0);

  }

  // to test stack methods
  void testStack(Tester t) {
    t.checkExpect(this.s1.contents.size(), 0);
    t.checkExpect(this.s1.isEmpty(), true);
    this.s1.push("abc");
    t.checkExpect(this.s1.contents.size(), 1);
    this.s1.push("cde");
    t.checkExpect(this.s1.contents.size(), 2);
    this.s1.pop();
    t.checkExpect(this.s1.contents.size(), 1);
    this.s1.pop();
    t.checkExpect(this.s1.contents.size(), 0);
  }

  // to test effect of paint trail method
  void testPaintTrail(Tester t) {
    this.init();
    this.blue.paintTrail(this.node);
    t.checkExpect(this.node.nodeColor.equals(this.node2.nodeColor), false);
    this.rainbow.paintTrail(this.node);
    t.checkExpect(this.node.nodeColor.equals(this.node2.nodeColor), false);
  }

  // to test graph fields
  void testGraph(Tester t) {
    // in the case of a maze created with new MazeWorld(500, 500, 20, player)
    t.checkExpect(this.world.graph.allNodes.size(), 625);
    t.checkExpect(this.world.graph.representatives.size(), 625);
    t.checkExpect(this.world.graph.workList.size(), 2550);
  }

  // to test edge comparator
  void testEdge(Tester t) {
    t.checkExpect(this.ec.compare(this.e1, this.e2), -1);
    t.checkExpect(this.ec.compare(this.e2, this.e1), 1);
    t.checkExpect(this.ec.compare(this.e1, this.e1), 0);

  }

  //to test connectNodes
  boolean testConnectNodes(Tester t) {
    if (debugMode) {
      init();
      return t.checkExpect(this.node.above, false) && t.checkExpect(this.node.below, true)
          && t.checkExpect(this.node.left, false) && t.checkExpect(this.node.right, true)
          && t.checkExpect(this.node2.below, false) && t.checkExpect(this.node2.left, true)
          && t.checkExpect(this.node2.right, false) && t.checkExpect(this.node2.above, true);
    } else {
      return true;
    }

  }

  //initialize for game
  void initGame() {
    this.world.initMaze();
  }

  // to run the game
  void test(Tester t) {
    if (!debugMode) {
      initGame();
      this.world.bigBang(MazeWorld.WIDTH + 1, MazeWorld.HEIGHT + 1, 0.3);
    }
  }


}
