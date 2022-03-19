package eduard.zaripov;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple inheritor of Exception to signalize about incorrect input coordinates on the board
 * <p> It has two fields of TypeOfNode type for indicating which node create this exception</p>
 * <p> It checked exception -> it is needed to handle</p>
 */
class IllegalInputCoordinate extends Exception {
    TypeOfCell first;
    TypeOfCell second;

    /**
     * Constructs an IllegalInputCoordinate with input types of node
     * @param first  the first node in the correct position
     * @param second  the second node in the correct position
     */
    public IllegalInputCoordinate(TypeOfCell first, TypeOfCell second) {
        super();
        this.first = first;
        this.second = second;
    }

    public IllegalInputCoordinate() {
        super();
    }

    /**
     * @return
     * string with incorrect nodes
     */
    @Override
    public String getMessage() {
        if (first == null || second == null) {
            return "index out of bounds of board";
        }
        return TypeOfCell.toString(first) + " cannot be at the same coordinate as " + TypeOfCell.toString(second);
    }
}

/**
 * Simple inheritor of Exception to signalize about Harry's captured
 */
class HarryIsCapturedException extends Exception {
    /**
     * @return  simple message
     */
    @Override
    public String getMessage() {
        return "Harry is captured";
    }
}

/**
 * For Harry's type of perception
 * <p>He can see only nodes in coordinates that locates in radius across him. The crucial idea is that Harry cannot
 * see nodes inside the rectangle of vision and also vertices of rectangle</p>
 * <p>Example:(H - harry, V - he can see, D - he cannot see)</p>
 * <p>D V V V D</p>
 * <p>V D D D V</p>
 * <p>V D H D V</p>
 * <p>V D D D V</p>
 * <p>D V V V D</p>
 */
class Perception{
    /**
     * Radius of view
     */
    private final int radius;

    public Perception(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    /**
     * Finds and check all nodes in radius of view
     * @param board  info about nodes in coordinates
     * @param coordinate  coordinate of Harry
     * @return list of nodes detected as danger
     */
    public ArrayList<Coordinate> detectDangerNodes(Board board, Coordinate coordinate) {
        ArrayList<Coordinate> dangerNodes = new ArrayList<>();
        if (radius == 1) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }
                    dangerNodes.add(new Coordinate(coordinate.getX() + i, coordinate.getY() + j));
                }
            }
        }
        else if (radius == 2) {
            int X = coordinate.getX();
            int Y = coordinate.getY();
            for (int i = -1; i <= 1; i++) {
                if (X + i >= 0 && X + i < board.size() && Y + 2 >= 0 && Y + 2 < board.size()) {
                    if (board.getCell(new Coordinate(X + i, Y + 2)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X + i, Y + 2));
                    }
                }
                if (X + 2 >= 0 && X + 2 < board.size() && Y + i >= 0 && Y + i < board.size()) {
                    if (board.getCell(new Coordinate(X + 2, Y + i)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X + 2, Y + i));
                    }
                }
                if (X + i >= 0 && X + i < board.size() && Y - 2 >= 0 && Y - 2 < board.size()) {
                    if (board.getCell(new Coordinate(X + i, Y - 2)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X + i, Y - 2));
                    }
                }
                if (X - 2 >= 0 && X - 2 < board.size() && Y + i >= 0 && Y + i < board.size()) {
                    if (board.getCell(new Coordinate(X - 2, Y + i)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X - 2, Y + i));
                    }
                }
            }
        }
        else {
            // In case of future extension
            throw new IllegalArgumentException("No support radius > 2");
        }
        return dangerNodes;
    }

}

/**
 * Functional interface. User of this interface can find path from some position to the needed subject
 */
interface FindPathInterface {
    /**
     * Inner class for finding paths
     */
    class Node {
        /**
         * Contains reference to the previous coordinate in path. The previous field of the first coordinate in path should be null
         */
        private Coordinate previous = null;
        /**
         * Contains a flag whether the node has been visited
         */
        private Boolean isPath = false;

        public Coordinate getPrevious() {
            return previous;
        }

        public void setPrevious(Coordinate previous) {
            this.previous = previous;
        }

        public Boolean isPath() {
            return isPath;
        }

        public void setIsPath(Boolean path) {
            isPath = path;
        }
    }

    /**
     * Finds path in input board from startPosition to the subjectToFind
     * @param board  all info about cells in coordinates
     * @param startPosition  position of start
     * @param subjectToFind  subject to find
     * @param isInvisible  If isInvisible is true, it can go through Danger {@link TypeOfCell}
     * @param mode  Type of perception of harry vision
     * @param updateDetection  If true clear all detected nodes as danger
     * @return  path as coordinates list
     * @throws HarryIsCapturedException   if Harry moved to danger node which was not detected previously
     */
    ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException;

    /**
     * Checks if the current coordinate is safe
     * @param board  all info about cells in coordinates
     * @param coordinate  coordinate to check
     * @param isInvisible  Has Harry the cloak. If it has, the DANGER nodes will be safe for him
     * @return  true if it is safe for him
     */
    default boolean isSafe(Board board, Coordinate coordinate, boolean isInvisible) {
        if (isInvisible) {
            return (coordinate.getY() >= 0 && coordinate.getY() < board.size()) &&
                    (coordinate.getX() >= 0 && coordinate.getX() < board.size()) &&
                    (!board.getCell(coordinate).isInspector());
        }
        return (coordinate.getY() >= 0 && coordinate.getY() < board.size()) &&
                (coordinate.getX() >= 0 && coordinate.getX() < board.size()) &&
                (!board.getCell(coordinate).isDangerOrInspector());
    }

    /**
     * Construct path through the references to the previous coordinate in the {@link Node} class:
     * <p>null <- First <- Second <- Third</p>
     * @param cellsInfo
     * @param coordinate
     * @return path as a list of coordinates
     */
    default ArrayList<Coordinate> restorePath(ArrayList<ArrayList<Node>> cellsInfo, Coordinate coordinate) {
        ArrayList<Coordinate> path = new ArrayList<>();
        while (cellsInfo.get(coordinate.getX()).get(coordinate.getY()).getPrevious() != null) {
            path.add(coordinate);
            coordinate = cellsInfo.get(coordinate.getX()).get(coordinate.getY()).getPrevious();
        }
        path.add(coordinate);
        Collections.reverse(path);
        return path;
    }

    /**
     * Finds all coordinates of neighbors of input coordianate that don't go off the map
     * @param coordinate  current coordinate
     * @param sizeOfGrid  size of map to check validity of neighbors
     * @return all neighbors as a list of coordinates
     */
    default ArrayList<Coordinate> getNeighbors(Coordinate coordinate, int sizeOfGrid) {
        ArrayList<Coordinate> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                Coordinate neighbor = new Coordinate(coordinate.getX() + i, coordinate.getY() + j);
                if ((neighbor.getY() >= 0 && neighbor.getY() < sizeOfGrid) && (neighbor.getX() >= 0 && neighbor.getX() < sizeOfGrid)) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }
}

/**
 * Recursive algorithm for finding path from coordinate to type of node(For example: from (0,0) to book)
 * <p>On each step of recursion:</p>
 * <p>* It checks if the node is final. If it is, return true</p>
 * <p>* Then it take neighbor coordinate </p>
 * <p>* If there is no accessible neighbor coordinate, returns back to previous step</p>
 */
class Backtracking implements FindPathInterface {
    /**
     * Define if algorithm finds the first right way or the shortest
     */
    private final boolean isTheShortestPathNeeded;

    /**
     * For save detected nodes if we have complex path. For example: start -> book -> exit
     */
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();
    /**
     * Min length founded length path
     */
    private int minLengthPath = Integer.MAX_VALUE;
    private ArrayList<Coordinate> minPath = new ArrayList<>();

    public Backtracking(boolean isTheShortestPathNeeded) {
        this.isTheShortestPathNeeded = isTheShortestPathNeeded;
    }

    /**
     * Initialize all needed fields and run recursive algorithm
     * @param board  all info about cells in coordinates
     * @param startPosition  position of start
     * @param subjectToFind  subject to find
     * @param isInvisible  If isInvisible is true, it can go through Danger {@link TypeOfCell}
     * @param mode  Type of perception of harry vision
     * @param updateDetection  If true clear all detected nodes as danger
     * @return path as a list of coordinates or null if there is no path
     * @throws HarryIsCapturedException
     */
    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException {
        ArrayList<ArrayList<Node>> cellsInfo = new ArrayList<>();
        if (updateDetection) {
            detectedDangerNodes = new ArrayList<>();
        }
        for (int row = 0; row < board.size(); row++) {
            cellsInfo.add(new ArrayList<>());
            for (int column = 0; column < board.size(); column++) {
                cellsInfo.get(row).add(new Node());
            }
        }

        minLengthPath = Integer.MAX_VALUE;
        minPath.clear();
        try {
            findPathBacktrackingRecursive(0, board, cellsInfo, startPosition, subjectToFind, isInvisible, mode, detectedDangerNodes);
        }
        catch (InterruptedException e) {
            return new ArrayList<>(minPath);
        }
        if (minPath.size() == 0) {
            return null;
        }
        return new ArrayList<>(minPath);
    }

    /**
     * Recursive algorithm
     * @param currentLength the depth of recursive which is also current length of path
     * @param board  all info about cells in coordinates
     * @param cellsInfo  all info node in cells
     * @param startPosition
     * @param subjectToFind
     * @param isInvisible  is Harry have a cloak
     * @param mode  perception mode of Harry
     * @param detectedDangerNodes  current detected danger nodes
     * @return true if there is path or false if there is no path
     * @throws HarryIsCapturedException
     * @throws InterruptedException
     */
    private boolean findPathBacktrackingRecursive(int currentLength, Board board, ArrayList<ArrayList<Node>> cellsInfo, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, ArrayList<Coordinate> detectedDangerNodes) throws HarryIsCapturedException, InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Thread interrupted");
        }

        if (board.getCell(startPosition).getTypesOfNode().contains(subjectToFind)) {
            if (isSafe(board, startPosition, isInvisible) && currentLength < minLengthPath) {
                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(true);
                minLengthPath = currentLength;
                minPath = restorePath(cellsInfo, startPosition);

                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(false);
                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);

                return true;
            }
        }
        else {
            if (isSafe(board, startPosition, isInvisible)) {
                detectedDangerNodes.addAll(mode.detectDangerNodes(board, startPosition));

                Node nodeInStartPosition = cellsInfo.get(startPosition.getX()).get(startPosition.getY());
                if (nodeInStartPosition.isPath()) {
                    return false;
                }

                nodeInStartPosition.setIsPath(true);
                currentLength++;
                if (currentLength > minLengthPath) {
                    cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(false);
                    cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);
                    return false;
                }

                LinkedList<Coordinate> nextCoordinatesToStep = getOperationSequence(startPosition, new Coordinate(0, 0), board.size());

                for (Coordinate next : nextCoordinatesToStep) {
                    if (!cellsInfo.get(next.getX()).get(next.getY()).isPath()) {
                        cellsInfo.get(next.getX()).get(next.getY()).setPrevious(startPosition);
                    }

                    if (findPathBacktrackingRecursive(currentLength, board, cellsInfo, next, subjectToFind, isInvisible, mode, detectedDangerNodes) && !isTheShortestPathNeeded) {
                        return true;
                    }
                }
                nodeInStartPosition.setIsPath(false);
            }

            if (!detectedDangerNodes.contains(startPosition)) {
                throw new HarryIsCapturedException();
            }

        }
        return false;
    }

    /**
     * Finds sequence of neighbors node. If we know the end position, it can optimize backtracking
     * @param startPosition
     * @param endPosition
     * @param sizeOfGrid
     * @return list of neighbors node
     */
    private LinkedList<Coordinate> getOperationSequence(Coordinate startPosition, Coordinate endPosition, int sizeOfGrid) {
        LinkedList<Coordinate> priority = new LinkedList<>();
        LinkedList<Integer> priorityX = new LinkedList<>();
        if (endPosition.getX() == startPosition.getX()) {
            priorityX.add(0);
            priorityX.add(+1);
            priorityX.add(-1);
        }
        else {
            int sign = (endPosition.getX() - startPosition.getX()) / Math.abs(endPosition.getX() - startPosition.getX());
            priorityX.add(sign);
            priorityX.add(0);
            priorityX.add(-sign);
        }

        LinkedList<Integer> priorityY = new LinkedList<>();
        if (endPosition.getY() == startPosition.getY()) {
            priorityY.add(0);
            priorityY.add(+1);
            priorityY.add(-1);
        }
        else {
            int sign = (endPosition.getY() - startPosition.getY()) / Math.abs(endPosition.getY() - startPosition.getY());
            priorityY.add(sign);
            priorityY.add(0);
            priorityY.add(-sign);
        }

        for (int operationX : priorityX) {
            for (int operationY : priorityY) {
                if (operationX == 0 && operationY == 0) {
                    continue;
                }
                if (startPosition.getX() + operationX < 0 || startPosition.getX() + operationX >= sizeOfGrid ||
                        startPosition.getY() + operationY < 0 || startPosition.getY() + operationY >= sizeOfGrid) {
                    continue;
                }
                priority.add(new Coordinate(startPosition.getX() + operationX, startPosition.getY() + operationY));
            }
        }

        return priority;
    }

}

/**
 * Algorithm for finding the shortest path from coordinate to subject using Breadth-First search algorithm
 */
class BFS implements FindPathInterface {
    /**
     * For save detected nodes if we have complex path. For example: start -> book -> exit
     */
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();

    /**
     * Iterative algorithm that use queue.
     * @param board  all info about cells in coordinates
     * @param startPosition  position of start
     * @param subjectToFind  subject to find
     * @param isInvisible  If isInvisible is true, it can go through Danger {@link TypeOfCell}
     * @param mode  Type of perception of harry vision
     * @param updateDetection  If true clear all detected nodes as danger
     * @return path as a list of coordinates or null if there is no path
     * @throws HarryIsCapturedException
     */
    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException {
        Queue<Coordinate> queue = new LinkedList<>();
        if (updateDetection) {
            detectedDangerNodes = new ArrayList<>();
        }

        ArrayList<ArrayList<Node>> cellsInfo = new ArrayList<>();
        for (int row = 0; row < board.size(); row++) {
            cellsInfo.add(new ArrayList<>());
            for (int column = 0; column < board.size(); column++) {
                cellsInfo.get(row).add(new Node());
            }
        }

        queue.add(startPosition);
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(true);

        while (!queue.isEmpty()) {
            Coordinate current = queue.remove();

            if (board.getCell(current).getTypesOfNode().contains(subjectToFind)) {
                return restorePath(cellsInfo, current);
            }


            detectedDangerNodes.addAll(mode.detectDangerNodes(board, current));


            if (!isSafe(board, current, isInvisible)) {
                if (!detectedDangerNodes.contains(current)) {
                    throw new HarryIsCapturedException();
                }
                continue;
            }

            ArrayList<Coordinate> neighbors = getNeighbors(current, board.size());
            for (Coordinate adjacentCell : neighbors) {
                Node adjacentNode = cellsInfo.get(adjacentCell.getX()).get(adjacentCell.getY());
                if (!adjacentNode.isPath()) {
                    queue.add(adjacentCell);
                    adjacentNode.setIsPath(true);
                    adjacentNode.setPrevious(current);

                    if (board.getCell(adjacentCell).getTypesOfNode().contains(subjectToFind)) {
                        return restorePath(cellsInfo, adjacentCell);
                    }
                }
            }
        }
        return null;
    }

}

/**
 * Keeps coordinate of point as (X,Y)
 */
class Coordinate implements Comparable<Coordinate>{
    private final int X;
    private final int Y;

    /**
     * Creates coordinate in (X,Y)
     * @param x number of column
     * @param y number of row
     */
    public Coordinate(int x, int y) {
        X = x;
        Y = y;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    /**
     * Equals if the x and y equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return X == that.X && Y == that.Y;
    }

    @Override
    public String toString() {
        return "X=" + X +", Y=" + Y;
    }

    /**
     * Parse string in format of "[X,Y]"
     * @param coordinatesInString string to parse
     * @return new Coordinate
     */
    public static Coordinate deserialize(String coordinatesInString) {
        String[] split = coordinatesInString.split(",");
        int X = Integer.parseInt(split[0].substring(1));
        int Y = Integer.parseInt(split[1].substring(0, split[1].length()-1));

        return new Coordinate(X, Y);
    }

    /**
     * Comparing two coordinates by euclidean distance
     * @param o coordinate to compare
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Coordinate o) {
        return Integer.compare(getX()*getY(), o.getX()*o.getY());
    }


}

/**
 * Class for input and output all needed info
 */
class IO {
    /**
     * Standard console scanner
     */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Print message and read 6 coordinates
     * @return list with input coordinates
     * @throws NumberFormatException in case of exception in parseCoordinate() method
     */
    public static ArrayList<Coordinate> readCoordinates() throws NumberFormatException {
        printString("Input 6 coordinates in format [x1,y1] [x2,y2] ... ");
        return parseCoordinates(scanner.nextLine());
    }

    /**
     * Parse 6 coordinates from string in format of:
     *  [X,Y] [X,Y] [X,Y] [X,Y] [X,Y] [X,Y] [X,Y]
     * @param inputString string for parse
     * @return list with coordinates
     * @throws NumberFormatException if number of coordinates != 6 or coordinates are not in the correct format
     */
    public static ArrayList<Coordinate> parseCoordinates(String inputString) throws NumberFormatException {
        String[] stringCoordinates = inputString.split(" ");
        ArrayList<Coordinate> coordinates = new ArrayList<>();

        if (stringCoordinates.length != 6) {
            throw new NumberFormatException("Should be 6 coordinates");
        }

        for (String coordinate : stringCoordinates) {
            coordinates.add(Coordinate.deserialize(coordinate));
        }
        return coordinates;
    }

    /**
     * Print message and read answer from user
     * @return instance of class perception with input atrribute
     */
    public static Perception readHarryMode() {
        printString("Input mode of Harry vision:\n 1: Radius = 1\n 2: Radius = 2");
        int answer = parseAnswer(scanner.nextLine(), 1, 2);
        if (answer == 1) {
            return new Perception(1);
        }
        else {
            return new Perception(2);
        }
    }

    /**
     * Print message and read answer from user
     * @return answer
     */
    public static Integer readInputMode() {
        printString("Input mode:\n 1: Keyboard\n 2: Random");
        return parseAnswer(scanner.nextLine(), 1, 2);
    }

    /**
     * Print message and read answer from user
     * @return answer
     */
    public static boolean readBacktrackingMode() {
        printString("Backtracking finds the shortest path? (If not, it will find the first compatible)\n (1 - Yes, 2 - No)");
        int answer = parseAnswer(scanner.nextLine(), 1, 2);
        return answer == 1;
    }

    /**
     * Print message and read max timeout
     * @return  max timeout
     */
    public static int readMaxTimeout() {
        printString("Input max timeout(in seconds) of backtracking. (Backtracking can work too long)");
        return scanner.nextInt();
    }

    /**
     * Check input string if it is int and belongs to input interval
     * @param answerString string to check
     * @param lowerBound left bound of interval
     * @param upperBound right bound of interval
     * @return answer
     * @throws NumberFormatException if the number is not in the interval
     */
    private static int parseAnswer(String answerString, int lowerBound, int upperBound) throws NumberFormatException{
        int answer = Integer.parseInt(answerString);
        if (answer < lowerBound || answer > upperBound) {
            throw new NumberFormatException("Illegal answer");
        }
        return answer;
    }

    /**
     * Add new line
     */
    public static void newLine() {
        System.out.println();
    }

    /**
     * Print input string. This method is needed if in future we want to change output to file or smth else
     * @param string string ot output
     */
    public static void printString(String string) {
        System.out.println(string);
    }
}

/**
 * Enumerate all types of cell.
 * <p>DEFAULT - there is no any subject of agents</p>
 * <p>START - start cell of Harry</p>
 * <p>DANGER - cell in radius of Inspector's vision</p>
 * <p>BOOK, CLOAK, EXIT, INSPECTOR - cell with these subjects respectively</p>
 * <p>Each has associated number of danger: -1, 0, 1, 2.</p>
 * It is needed for convenient comparison harry without cloak and with cloak
 */
enum TypeOfCell {
    DEFAULT(0), START(0), BOOK(-1), CLOAK(-1), EXIT(-1), DANGER(1), INSPECTOR(2);
    private final int danger;

    TypeOfCell(int danger) {
        this.danger = danger;
    }

    public int getDanger() {
        return danger;
    }

    /**
     * Matching each type to string for output
     * @param typeOfCell type to match
     * @return string
     */
    static String toString(TypeOfCell typeOfCell) {
        switch (typeOfCell) {
            case DEFAULT: return "-";
            case DANGER: return "D";
            case BOOK: return "B";
            case INSPECTOR: return "I";
            case CLOAK: return "C";
            case EXIT: return "E";
            case START: return "S";
            default: throw new IllegalArgumentException();
        }
    }
}

/**
 * Contains set of {@link TypeOfCell} and methods to work with it
 */
class Cell{
    private final TreeSet<TypeOfCell> typesOfNode;

    public Cell() {
        this.typesOfNode = new TreeSet<>();
    }

    public TreeSet<TypeOfCell> getTypesOfNode() {
        return typesOfNode;
    }

    public void addTypeOfNode(TypeOfCell typeOfCell) {
        typesOfNode.add(typeOfCell);
    }

    /**
     * Check if the cell contains typesOfCell with danger less than 0
     * @return true if contains, false if not
     */
    public boolean containsCrucialElements() {
        for (TypeOfCell typeOfCell : typesOfNode) {
            if (typeOfCell.getDanger() < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the cell is Inspector(cell with danger = 2)
     * @return true if it's Inspector, false if not
     */
    public boolean isInspector() {
        for (TypeOfCell typeOfCell : typesOfNode) {
            if (typeOfCell.getDanger() == 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the cell is Inspector or in radius of Inspector (cell with danger = 2 or = 1)
     * @return true if it's Inspector or in radius of Inspector, false if not
     */
    public boolean isDangerOrInspector() {
        for (TypeOfCell typeOfCell : typesOfNode) {
            if (typeOfCell.getDanger() == 1 || typeOfCell.getDanger() == 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create string by appending all types to one string
     * @return created string
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (TypeOfCell typeOfCell : typesOfNode) {
            output.append(TypeOfCell.toString(typeOfCell));
        }
        return output.toString();
    }
}

/**
 * Contains all info about coordinates and cell in these coordinates
 */
class Board{
    /**
     * Matrix of cells
     */
    private final ArrayList<ArrayList<Cell>> grid;

    /**
     * Creates board with input coordinates
     * @param radiusOfStrongInspector strong = with greater radius
     * @throws IllegalInputCoordinate if
     * @throws HarryIsCapturedException
     */
    public Board(int sizeOfGrid,
                 int radiusOfStrongInspector,
                 int radiusOfInspector,
                 Coordinate startPosition,
                 Coordinate filthPosition,
                 Coordinate catPosition,
                 Coordinate bookPosition,
                 Coordinate cloakPosition,
                 Coordinate exitPosition) throws IllegalInputCoordinate, HarryIsCapturedException {
        grid = new ArrayList<>();
        try {
            // Initialization of grid
            for (int row = 0; row < sizeOfGrid; row++) {
                grid.add(new ArrayList<>());
                for (int column = 0; column < sizeOfGrid; column++) {
                    grid.get(row).add(new Cell());
                }
            }

            getCell(startPosition).addTypeOfNode(TypeOfCell.START);
            getCell(bookPosition).addTypeOfNode(TypeOfCell.BOOK);
            getCell(cloakPosition).addTypeOfNode(TypeOfCell.CLOAK);

            // Exit cell cannot be at the same coordinate as Book cell
            if (!getCell(exitPosition).getTypesOfNode().contains(TypeOfCell.BOOK)) {
                getCell(exitPosition).addTypeOfNode(TypeOfCell.EXIT);
            } else {
                throw new IllegalInputCoordinate(TypeOfCell.BOOK, TypeOfCell.EXIT);
            }

            // If Harry spawned with cloak
            boolean isInvisible = startPosition.equals(cloakPosition);

            addInspector(filthPosition, radiusOfStrongInspector, isInvisible);
            addInspector(catPosition, radiusOfInspector, isInvisible);


            // Initialize the remaining cells as DEFAULT
            for (int row = 0; row < sizeOfGrid; row++) {
                for (int column = 0; column < sizeOfGrid; column++) {
                    Coordinate currentCoordinate = new Coordinate(row, column);
                    if (getCell(currentCoordinate).getTypesOfNode().isEmpty()) {
                        getCell(currentCoordinate).addTypeOfNode(TypeOfCell.DEFAULT);
                    }
                }
            }

            // If harry spawned inside the danger zone
            if (getCell(startPosition).isDangerOrInspector()) {
                throw new HarryIsCapturedException();
            }
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalInputCoordinate("out of bounds of board");
        }
    }

    /**
     * Adding inspector to board and set cells as danger in input radius.
     * @param coordinate coordinate of inspector
     * @param radius radius of danger
     * @param isInvisible for special condition when harry spawned with cloak, crucial elements can be spawned inside the danger zone
     * @throws IllegalInputCoordinate if the in danger radius there is crucial elements
     */
    public void addInspector(Coordinate coordinate, int radius, boolean isInvisible) throws IllegalInputCoordinate {
        for (int row = coordinate.getX() - radius; row <= coordinate.getX() + radius; row++) {
            for (int column = coordinate.getY() - radius; column <= coordinate.getY() + radius; column++) {
                Coordinate currentCoordinate = new Coordinate(row, column);
                if(row < 0 || row >= grid.size() || column < 0 || column >= grid.size()){
                    continue;
                }
                if (getCell(currentCoordinate).containsCrucialElements() && !isInvisible) {
                    throw new IllegalInputCoordinate(getCell(currentCoordinate).getTypesOfNode().first(), TypeOfCell.DANGER);
                }
                else if (!getCell(currentCoordinate).getTypesOfNode().contains(TypeOfCell.INSPECTOR)) {
                    getCell(currentCoordinate).addTypeOfNode(TypeOfCell.DANGER);
                }
            }
        }
        getCell(coordinate).getTypesOfNode().remove(TypeOfCell.DANGER); // for replacing danger with inspector
        getCell(coordinate).addTypeOfNode(TypeOfCell.INSPECTOR);
    }

    public Cell getCell(Coordinate coordinate) {
        return grid.get(coordinate.getX()).get(coordinate.getY());
    }

    /**
     * Wrapper with empty list for toString(List) method
     * @return board in string
     */
    @Override
    public String toString() {
        ArrayList<Coordinate> path = new ArrayList<>();
        return toString(path);
    }

    /**
     * Created string from board and input path. Path is indicated as green numbers. Danger zones as red
     * @param path path to view
     * @return board with path in string
     */
    public String toString(ArrayList<Coordinate> path) {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_WHITE = "\u001B[37m";
        StringBuilder gridString = new StringBuilder();

        for (int row = grid.size() - 1; row >= 0; row--) {
            for (int column = 0; column < grid.size(); column++) {
                int lengthOfNode;
                Coordinate inverseCoordinate = new Coordinate(column, row);

                if (path.contains(inverseCoordinate)) {
                    gridString.append(ANSI_GREEN);
                }
                else if (getCell(inverseCoordinate).isDangerOrInspector()) {
                    gridString.append(ANSI_RED);
                }
                else {
                    gridString.append(ANSI_WHITE);
                }

                if (path.contains(inverseCoordinate) && !getCell(inverseCoordinate).containsCrucialElements() && !getCell(inverseCoordinate).getTypesOfNode().contains(TypeOfCell.START)) {
                    gridString.append(path.indexOf(inverseCoordinate));
                    lengthOfNode = Integer.toString(path.indexOf(inverseCoordinate)).length();
                }
                else {
                    gridString.append(getCell(inverseCoordinate));
                    lengthOfNode = getCell(inverseCoordinate).toString().length();
                }
                gridString.append(ANSI_RESET);

                StringBuilder spaces = new StringBuilder("   ");
                for (int i = 1; i < lengthOfNode; i++) {
                    spaces.delete(spaces.length() - 2, spaces.length() - 1);
                }
                gridString.append(spaces);
            }
            gridString.append("\n");
        }
        gridString.deleteCharAt(gridString.length() - 1);
        return gridString.toString();
    }

    /**
     * @return length of one row (or column)
     */
    public int size() {
        return grid.size();
    }
}

/**
 * Manages work with I/O, finding paths and print them
 */
class Solution {
    static final int sizeOfGrid = 9; // length of row (or column)
    static final int filchRadius = 2; // radius of danger zone
    static final int catRadius = 1;

    /**
     * Class for creating random input coordinates and vision of Harry
     */
    static class RandomInput {
        /**
         * Upper bound for creating coordinates
         */
        private static final int upperBound = Solution.sizeOfGrid;
        private static final Random random = new Random();

        public static ArrayList<Coordinate> getRandomInputCoordinates(){
            ArrayList<Coordinate> coordinates = new ArrayList<>();

            // Save them before adding
            Coordinate filchCoordinate = getRandomCoordinates();
            Coordinate catCoordinate = getRandomCoordinates();

            Coordinate startCoordinate = getRandomCoordinates();
            // Until not in the danger zone start coordinate generates
            while (isInDanger(startCoordinate, filchCoordinate, filchRadius) ||
                    isInDanger(startCoordinate, catCoordinate, catRadius)) {
                startCoordinate = getRandomCoordinates();
            }
            coordinates.add(startCoordinate);

            // Adding them after start coordinate
            coordinates.add(filchCoordinate);
            coordinates.add(catCoordinate);

            for (int i = 0; i < 3; i++) {
                Coordinate currentCoordinate = getRandomCoordinates();
                // Until not in the danger zone coordinate of crucial cell generates
                while (isInDanger(currentCoordinate, filchCoordinate, filchRadius) ||
                        isInDanger(currentCoordinate, catCoordinate, catRadius)) {
                    currentCoordinate = getRandomCoordinates();
                }
                coordinates.add(currentCoordinate);
            }

            // Until not equal start and exit coordinates generates
            while (coordinates.get(5).equals(coordinates.get(3)) ||
                    isInDanger(coordinates.get(5), filchCoordinate, filchRadius) ||
                    isInDanger(coordinates.get(5), catCoordinate, catRadius)) {
                coordinates.set(5, getRandomCoordinates());
            }

            return coordinates;
        }

        /**
         * @return coordinate with random values from 0 to upperBound
         */
        private static Coordinate getRandomCoordinates() {
            return new Coordinate(random.nextInt(upperBound), random.nextInt(upperBound));
        }

        /**
         * Check if generated coordinate in the danger zone
         * @param currentCoordinate coordinate to check
         * @param inspectorCoordinate coordinate of Inspector
         * @param radius radius of Inspector's danger zone
         * @return true if coordinate in the danger zone, false otherwise
         */
        private static  boolean isInDanger(Coordinate currentCoordinate, Coordinate inspectorCoordinate, int radius) {
            for (int i = inspectorCoordinate.getX() - radius; i <= inspectorCoordinate.getX() + radius; i++) {
                for (int j = inspectorCoordinate.getY() - filchRadius; j <= inspectorCoordinate.getY() + filchRadius; j++) {
                    if (currentCoordinate.equals(new Coordinate(i, j))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    Board board;
    Perception mode;
    Coordinate harryPosition;
    Coordinate cloakPosition;
    Coordinate bookPosition;
    Coordinate exitPosition;
    /**
     * Scenario - sequence of crucial cell to find. Example of scenario:
     * <p> HARRY -> CLOAK -> BOOK -> EXIT</p>
     */
    HashMap<ArrayList<TypeOfCell>, Boolean> allScenarios;

    public Solution(ArrayList<Coordinate> inputCoordinates, Perception mode) throws IllegalArgumentException {
        this.harryPosition = inputCoordinates.get(0);
        this.bookPosition = inputCoordinates.get(3);
        this.cloakPosition = inputCoordinates.get(4);
        this.exitPosition = inputCoordinates.get(5);

        try {
            this.board = new Board(sizeOfGrid, filchRadius, catRadius, harryPosition, inputCoordinates.get(1), inputCoordinates.get(2), bookPosition, cloakPosition, exitPosition);
        }
        catch (IllegalInputCoordinate e) {
            IO.printString(e.getMessage());
            System.exit(1);
        }
        catch (HarryIsCapturedException e) {
            IO.printString(e.getMessage() + ". He is spawned inside the danger zone");
            System.exit(1);
        }

        this.mode = mode;
        this.allScenarios = new HashMap<>();
        ArrayList<ArrayList<TypeOfCell>> scenarios = getAllPossibleScenarios();
        for (ArrayList<TypeOfCell> scenario : scenarios) {
            allScenarios.put(scenario, true);
        }
    }

    public Solution(Perception mode) {
        this(RandomInput.getRandomInputCoordinates(), mode);
    }

    public ArrayList<ArrayList<Coordinate>> findPath(FindPathInterface typeOfSearch) {
        ArrayList<ArrayList<Coordinate>> minPath = null;
        int minLength = Integer.MAX_VALUE;
        for(ArrayList<TypeOfCell> scenario : allScenarios.keySet()) {
            try {
                ArrayList<ArrayList<Coordinate>> path = calculatePath(typeOfSearch, board, mode, scenario);
                if (path != null) {
                    int overallLength = 0;
                    for (ArrayList<Coordinate> currentPath : path) {
                        overallLength += currentPath.size();
                    }
                    if (minPath == null) {
                        minPath = path;
                        minLength = overallLength;
                    } else if (overallLength < minLength) {
                        minPath = path;
                        minLength = overallLength;
                    }
                } else {
                    allScenarios.replace(scenario, false);
                }
            }
            catch (HarryIsCapturedException e) {
                 IO.printString(e.getMessage());
                return new ArrayList<>();
            }
        }
        if (minPath == null) {
            return new ArrayList<>();
        }
        return minPath;
    }

    private ArrayList<ArrayList<Coordinate>> calculatePath(FindPathInterface typeOfSearch, Board board, Perception mode, ArrayList<TypeOfCell> scenario) throws HarryIsCapturedException {
        boolean isCloakInPath = false;
        ArrayList<ArrayList<Coordinate>> overallScenarioPath = new ArrayList<>();
        Coordinate currentCheckpoint = harryPosition;

        for (TypeOfCell subjectToFind : scenario) {
            ArrayList<Coordinate> currentPath;
            if (scenario.indexOf(subjectToFind) == 0) {
                currentPath = typeOfSearch.findPath(board, currentCheckpoint, subjectToFind, isCloakInPath, mode, true);
            } else {
                currentPath = typeOfSearch.findPath(board, currentCheckpoint, subjectToFind, isCloakInPath, mode, false);
            }

            if (currentPath == null) {
                return null;
            }

            currentCheckpoint = currentPath.get(currentPath.size() - 1);
            overallScenarioPath.add(currentPath);

            if (!isCloakInPath && subjectToFind == TypeOfCell.CLOAK) {
                isCloakInPath = true;
            }
        }
        return overallScenarioPath;
    }

    private ArrayList<ArrayList<TypeOfCell>> getAllPossibleScenarios() {
        ArrayList<ArrayList<TypeOfCell>> allScenarios = new ArrayList<>();

        ArrayList<TypeOfCell> currentScenario = new ArrayList<>();
        currentScenario.add(TypeOfCell.BOOK);
        currentScenario.add(TypeOfCell.EXIT);
        allScenarios.add(new ArrayList<>(currentScenario));

        currentScenario.add(currentScenario.indexOf(TypeOfCell.BOOK), TypeOfCell.CLOAK);
        allScenarios.add(new ArrayList<>(currentScenario));

        currentScenario.remove(TypeOfCell.CLOAK);
        currentScenario.add(currentScenario.indexOf(TypeOfCell.EXIT), TypeOfCell.CLOAK);
        allScenarios.add(new ArrayList<>(currentScenario));

        return allScenarios;
    }

    public String toString(ArrayList<ArrayList<Coordinate>> path) {
        StringBuilder output = new StringBuilder();

        for (ArrayList<Coordinate> currentPath : path) {
            String start = board.getCell(currentPath.get(0)).toString();
            String end = board.getCell(currentPath.get(currentPath.size() - 1)).toString();
            output.append("Path from ").append(start).append(" to ").append(end).append(": \n");

            output.append(board.toString(currentPath)).append("\n");
        }


        return output.substring(0, output.length() - 1);
    }

    @Override
    public String toString() {
        return board.toString();
    }
}

class StatisticsCalculator{
    /*
    Backtracking (variant 1) compared to 2nd algorithm (variant 1)
        * For time - BacktrackingShortest
        * For steps - Backtracking
    Backtracking (variant 2) compared to 2nd algorithm (variant 2)
        * For winrate - Backtracking
    Backtracking (variant 1) compared to Backtracking (variant 2)
        * For steps - Backtracking
    2nd algorithm (variant 1) compared to 2nd algorithm (variant 2)
        * For time -
     */

    private static final int numberOfExperiments = 10;

    static class ResultOfExperiment {
        long time;
        int numberOfSteps;
        boolean isWin;

        public ResultOfExperiment(int numberOfSteps, long time, String isWin) {
            this.time = time;
            this.numberOfSteps = numberOfSteps;
            this.isWin = isWin.equals("w");
        }

        public static ResultOfExperiment parseString(String str) {
            String[] words = str.split(" ");
            return new ResultOfExperiment(Integer.parseInt(words[0]), Integer.parseInt(words[1]), words[2]);
        }

        @Override
        public String toString() {
            return numberOfSteps + " " + time + " " + isWin;
        }
    }

    static LinkedList<Solution> createSample(Perception mode) {
        LinkedList<Solution> sample = new LinkedList<>();
        for (int i = 0; i < numberOfExperiments; i++) {
            sample.add(new Solution(mode));
        }

        return sample;
    }


    static void startExperiments(LinkedList<Solution> sample, String pathToFile, int mode, FindPathInterface typeOfSearch) throws ExecutionException, InterruptedException {
        try {
            FileWriter writer = new FileWriter(pathToFile);
            writer.write("# [length time win]\n");

            for (Solution solution : sample) {
                int maxTimeoutOfBacktracking = 1;
                boolean isTimeout = false;

                ExecutorService service = Executors.newSingleThreadExecutor();
                Future<Integer> future = service.submit(() -> Main.calculatePathLength(solution.findPath(typeOfSearch)));
                try {
                    long startStamp = System.currentTimeMillis();
                    int length = future.get(maxTimeoutOfBacktracking, TimeUnit.SECONDS);
                    long endStamp = System.currentTimeMillis();

                    String result = length == 0 ? "l" : "w";
                    writer.write(length + " " + (endStamp - startStamp) + " " + result + "\n");
                }
                catch (TimeoutException e) {
                    future.cancel(true);
                }
                service.shutdownNow();
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static double getMedianOfTime(LinkedList<ResultOfExperiment> results) {
        int sum = 0;
        for (ResultOfExperiment result : results) {
            sum += result.time;
        }
        return sum / (double) results.size();
    }

    static double getMedianOfLength(LinkedList<ResultOfExperiment> results) {
        int numberOfSteps = 0;
        for (ResultOfExperiment result : results) {
            numberOfSteps += result.numberOfSteps;
        }
        return numberOfSteps / (double) results.size();
    }

    static double getWinRate(LinkedList<ResultOfExperiment> results) {
        int wins = 0;
        for (ResultOfExperiment result : results) {
            if (result.isWin) {
                wins++;
            }
        }
        return wins / (double) results.size();
    }

    static LinkedList<ResultOfExperiment> parseResultsFromFile(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(fileName));
        LinkedList<StatisticsCalculator.ResultOfExperiment> results = new LinkedList<>();
        while (scanner.hasNext()) {
            String row = scanner.nextLine();
            if (row.charAt(0) == '#') {
                continue;
            }
            StatisticsCalculator.ResultOfExperiment result = StatisticsCalculator.ResultOfExperiment.parseString(row);
            results.add(result);
        }
        return results;
    }
}


public class Main {
    private static void printInfoAboutPath(Solution solution, ArrayList<ArrayList<Coordinate>> path, String nameOfAlgorithm, long elapsedTime) {
        IO.printString("Path by: " + nameOfAlgorithm);
        if (path.size() == 0) {
            IO.printString("There is no path");
            IO.printString("Lose");
        }
        else {
            int length = calculatePathLength(path);

            IO.printString("Length of the path: " + length);
            IO.printString(solution.toString(path));
            IO.printString("Elapsed time: " + elapsedTime);
        }
    }



    public static void main(String[] args) throws ExecutionException, InterruptedException, FileNotFoundException {
//        String file1 = "samples/sampleForBacktrackingVar1";
//        String file2 = "samples/sampleForBFSVar1";
//        String file3 = "samples/sampleForBacktrackingVar2";
//        String file4 = "samples/sampleForBFSVar2";
//
//
//        LinkedList<Solution> sample1 = StatisticsCalculator.createSample(new Perception(1));
//        StatisticsCalculator.startExperiments(sample1, file1, 1, new Backtracking(true));
//        StatisticsCalculator.startExperiments(sample1, file2, 1, new BFS());
//
//        LinkedList<Solution> sample2 = StatisticsCalculator.createSample(new Perception(2));
//        StatisticsCalculator.startExperiments(sample2, file3, 2, new Backtracking(false));
//        StatisticsCalculator.startExperiments(sample2, file4, 2, new BFS());
//
//        LinkedList<StatisticsCalculator.ResultOfExperiment> results1 = StatisticsCalculator.parseResultsFromFile(file1);
//        LinkedList<StatisticsCalculator.ResultOfExperiment> results2 = StatisticsCalculator.parseResultsFromFile(file2);
//        System.out.println(StatisticsCalculator.getMedianOfTime(results1));
//        System.out.println(StatisticsCalculator.getMedianOfTime(results2));
//

        try {
            int inputMode = IO.readInputMode();
            Solution solution;

            if (inputMode == 1) {
                solution = new Solution(IO.readCoordinates(), IO.readHarryMode());
            } else {
                solution = new Solution(IO.readHarryMode());
            }

            boolean isBacktrackingFindShortestPath = IO.readBacktrackingMode();
            int maxTimeoutOfBacktracking = IO.readMaxTimeout();

            IO.printString("Initial grid: ");
            IO.printString(solution.toString());

            long startStamp = System.currentTimeMillis();
            AtomicReference<ArrayList<ArrayList<Coordinate>>> pathBacktracking = new AtomicReference<>(new ArrayList<>());

            ExecutorService service = Executors.newSingleThreadExecutor();
            Future<?> future = service.submit(() -> pathBacktracking.set(solution.findPath(new Backtracking(isBacktrackingFindShortestPath))));
            try {
                IO.printString("Backtracking started..");
                future.get(maxTimeoutOfBacktracking, TimeUnit.SECONDS);
            }
            catch (TimeoutException e) {
                future.cancel(true);
                IO.printString("Timeout!");
            }
            service.shutdownNow();
            long endStamp = System.currentTimeMillis();
            printInfoAboutPath(solution, pathBacktracking.get(), "Backtracking", endStamp - startStamp);

            IO.newLine();

            startStamp = System.currentTimeMillis();
            ArrayList<ArrayList<Coordinate>> pathBFS = solution.findPath(new BFS());
            endStamp = System.currentTimeMillis();
            printInfoAboutPath(solution, pathBFS, "BFS", endStamp - startStamp);
        }
        catch (NumberFormatException | IndexOutOfBoundsException e) {
            IO.printString("Illegal input: " + e.getMessage());
            main(null);
        }



    }

    public static int calculatePathLength(ArrayList<ArrayList<Coordinate>> path) {
        int length = 0;
        for (ArrayList<Coordinate> partOfThePath : path) {
            length += partOfThePath.size();
            length--;
        }
        return length;
    }
}
