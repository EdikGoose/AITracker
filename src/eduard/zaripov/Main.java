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
    TypeOfNode first;
    TypeOfNode second;

    /**
     * Constructs an IllegalInputCoordinate with input types of node
     * @param first  the first node in the correct position
     * @param second  the second node in the correct position
     */
    public IllegalInputCoordinate(TypeOfNode first, TypeOfNode second) {
        super();
        this.first = first;
        this.second = second;
    }

    /**
     * @return  string with incorrect nodes
     */
    @Override
    public String getMessage() {
        return TypeOfNode.toString(first) + " cannot be at the same coordinate as " + TypeOfNode.toString(second);
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
     * @param isInvisible  If isInvisible is true, it can go through Danger {@link TypeOfNode}
     * @param mode  Type of perception of harry vision
     * @param updateDetection  If true clear all detected nodes as danger
     * @return  path as coordinates list
     * @throws HarryIsCapturedException   if Harry moved to danger node which was not detected previously
     */
    ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException;

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
     * @param isInvisible  If isInvisible is true, it can go through Danger {@link TypeOfNode}
     * @param mode  Type of perception of harry vision
     * @param updateDetection  If true clear all detected nodes as danger
     * @return path as a list of coordinates or null if there is no path
     * @throws HarryIsCapturedException
     */
    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException {
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
    private boolean findPathBacktrackingRecursive(int currentLength, Board board, ArrayList<ArrayList<Node>> cellsInfo, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, Perception mode, ArrayList<Coordinate> detectedDangerNodes) throws HarryIsCapturedException, InterruptedException {
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
     * @param isInvisible  If isInvisible is true, it can go through Danger {@link TypeOfNode}
     * @param mode  Type of perception of harry vision
     * @param updateDetection  If true clear all detected nodes as danger
     * @return path as a list of coordinates or null if there is no path
     * @throws HarryIsCapturedException
     */
    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException {
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
     * @param x  number of column
     * @param y  number of row
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
     * @param coordinatesInString  string to parse
     * @return new Coordinate
     */
    public static Coordinate deserialize(String coordinatesInString) {
        String[] split = coordinatesInString.split(",");
        int X = Integer.parseInt(split[0].substring(1));
        int Y = Integer.parseInt(split[1].substring(0, split[1].length()-1));

        return new Coordinate(X, Y);
    }

    @Override
    public int compareTo(Coordinate o) {
        return Integer.compare(getX()*getY(), o.getX()*o.getY());
    }


}

class IO {
    private static final Scanner scanner = new Scanner(System.in);
    
    public static ArrayList<Coordinate> readCoordinates() throws NumberFormatException {
        printString("Input 6 coordinates in format [x1,y1] [x2,y2] ... ");
        return parseCoordinates(scanner.nextLine());
    }

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

    public static Perception readHarryMode() {
        printString("Input mode of Harry vision:\n 1: Radius = 1\n 2: Radius = 2");
        int answer = checkAnswerCorrection(scanner.nextLine(), 1, 2);
        if (answer == 1) {
            return new Perception(1);
        }
        else {
            return new Perception(2);
        }
    }

    public static Integer readInputMode() {
        printString("Input mode:\n 1: Keyboard\n 2: Random");
        return checkAnswerCorrection(scanner.nextLine(), 1, 2);
    }

    public static boolean readBacktrackingMode() {
        printString("Backtracking finds the shortest path? (If not, it will find the first compatible)\n (1 - Yes, 2 - No)");
        int answer = checkAnswerCorrection(scanner.nextLine(), 1, 2);
        return answer == 1;
    }

    public static int readMaxTimeout() {
        printString("Input max timeout(in seconds) of backtracking. (Backtracking can work too long)");
        return scanner.nextInt();
    }

    private static int checkAnswerCorrection(String answerString, int lowerBound, int upperBound) throws NumberFormatException{
        int answer = Integer.parseInt(answerString);
        if (answer < lowerBound || answer > upperBound) {
            throw new NumberFormatException("Illegal answer");
        }
        return answer;
    }

    public static void newLine() {
        System.out.println();
    }

    public static void printString(String string) {
        System.out.println(string);
    }

}

enum TypeOfNode{
    DEFAULT(0), START(0), BOOK(-1), CLOAK(-1), EXIT(-1), DANGER(1), INSPECTOR(2);
    private final int danger;

    TypeOfNode(int danger) {
        this.danger = danger;
    }

    public int getDanger() {
        return danger;
    }

    static String toString(TypeOfNode typeOfNode) {
        switch (typeOfNode) {
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

class Cell{
    private final TreeSet<TypeOfNode> typesOfNode;

    public Cell() {
        this.typesOfNode = new TreeSet<>();
    }

    public TreeSet<TypeOfNode> getTypesOfNode() {
        return typesOfNode;
    }

    public void addTypeOfNode(TypeOfNode typeOfNode) {
        typesOfNode.add(typeOfNode);
    }

    public boolean containsCrucialElements() {
        for (TypeOfNode typeOfNode : typesOfNode) {
            if (typeOfNode.getDanger() == -1) {
                return true;
            }
        }
        return false;
    }

    public boolean isInspector() {
        for (TypeOfNode typeOfNode : typesOfNode) {
            if (typeOfNode.getDanger() == 2) {
                return true;
            }
        }
        return false;
    }

    public boolean isDangerOrInspector() {
        for (TypeOfNode typeOfNode : typesOfNode) {
            if (typeOfNode.getDanger() == 1 || typeOfNode.getDanger() == 2) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (TypeOfNode typeOfNode : typesOfNode) {
            output.append(TypeOfNode.toString(typeOfNode));
        }

        return output.toString();
    }
}

class Board{
    private final ArrayList<ArrayList<Cell>> grid;

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

        for (int row = 0; row < sizeOfGrid; row++) {
            grid.add(new ArrayList<>());
            for (int column = 0; column < sizeOfGrid; column++) {
                grid.get(row).add(new Cell());
            }
        }

        getCell(startPosition).addTypeOfNode(TypeOfNode.START);
        getCell(bookPosition).addTypeOfNode(TypeOfNode.BOOK);
        getCell(cloakPosition).addTypeOfNode(TypeOfNode.CLOAK);
        if (!getCell(exitPosition).getTypesOfNode().contains(TypeOfNode.BOOK)) {
            getCell(exitPosition).addTypeOfNode(TypeOfNode.EXIT);
        }
        else {
            throw new IllegalInputCoordinate(TypeOfNode.BOOK, TypeOfNode.EXIT);
        }

        boolean isInvisible = startPosition.equals(cloakPosition);

        addInspector(filthPosition, radiusOfStrongInspector, isInvisible);
        addInspector(catPosition, radiusOfInspector, isInvisible);

        for (int row = 0; row < sizeOfGrid; row++) {
            for (int column = 0; column < sizeOfGrid; column++) {
                Coordinate currentCoordinate = new Coordinate(row, column);
                if (getCell(currentCoordinate).getTypesOfNode().isEmpty()) {
                    getCell(currentCoordinate).addTypeOfNode(TypeOfNode.DEFAULT);
                }
            }
        }

        if (getCell(startPosition).isDangerOrInspector()) {
            ArrayList<Coordinate> capturedPath = new ArrayList<>();
            capturedPath.add(startPosition);
            throw new HarryIsCapturedException();
        }
    }

    public void addInspector(Coordinate coordinate, int radius, boolean isInvisible) throws IllegalInputCoordinate {
        for (int row = coordinate.getX() - radius; row <= coordinate.getX() + radius; row++) {
            for (int column = coordinate.getY() - radius; column <= coordinate.getY() + radius; column++) {
                Coordinate currentCoordinate = new Coordinate(row, column);
                if(row < 0 || row >= grid.size() || column < 0 || column >= grid.size()){
                    continue;
                }
                if (getCell(currentCoordinate).containsCrucialElements() && !isInvisible) {
                    throw new IllegalInputCoordinate(getCell(currentCoordinate).getTypesOfNode().first(), TypeOfNode.DANGER);
                }
                else if (!getCell(currentCoordinate).getTypesOfNode().contains(TypeOfNode.INSPECTOR)) {
                    getCell(currentCoordinate).addTypeOfNode(TypeOfNode.DANGER);
                }
            }
        }
        getCell(coordinate).getTypesOfNode().remove(TypeOfNode.DANGER);
        getCell(coordinate).addTypeOfNode(TypeOfNode.INSPECTOR);
    }

    public Cell getCell(Coordinate coordinate) {
        return grid.get(coordinate.getX()).get(coordinate.getY());
    }

    @Override
    public String toString() {
        ArrayList<Coordinate> path = new ArrayList<>();
        return toString(path);
    }

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

                if (path.contains(inverseCoordinate) && !getCell(inverseCoordinate).containsCrucialElements() && !getCell(inverseCoordinate).getTypesOfNode().contains(TypeOfNode.START)) {
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

    public int size() {
        return grid.size();
    }
}

class Solution {
    static int sizeOfGrid = 9;
    static int filchRadius = 2;
    static int catRadius = 1;

    static class RandomInput {
        private static final int upperBound = Solution.sizeOfGrid;
        private static final Random random = new Random();

        public static ArrayList<Coordinate> getRandomInputCoordinates(){
            ArrayList<Coordinate> coordinates = new ArrayList<>();

            Coordinate filchCoordinate = getRandomCoordinates();
            Coordinate catCoordinate = getRandomCoordinates();

            Coordinate startCoordinate = getRandomCoordinates();
            while (isInDanger(startCoordinate, filchCoordinate, filchRadius) ||
                    isInDanger(startCoordinate, catCoordinate, catRadius)) {
                startCoordinate = getRandomCoordinates();
            }
            coordinates.add(startCoordinate);

            coordinates.add(filchCoordinate);
            coordinates.add(catCoordinate);

            for (int i = 0; i < 3; i++) {
                Coordinate currentCoordinate = getRandomCoordinates();
                while (isInDanger(currentCoordinate, filchCoordinate, filchRadius) ||
                        isInDanger(currentCoordinate, catCoordinate, catRadius)) {
                    currentCoordinate = getRandomCoordinates();
                }
                coordinates.add(currentCoordinate);
            }

            while (coordinates.get(5).equals(coordinates.get(3)) ||
                    isInDanger(coordinates.get(5), filchCoordinate, filchRadius) ||
                    isInDanger(coordinates.get(5), catCoordinate, catRadius)) {
                coordinates.set(5, getRandomCoordinates());
            }

            StringBuilder stringVersion = new StringBuilder();
            for (Coordinate coordinate : coordinates) {
                stringVersion.append("[").append(coordinate.getX()).append(",").append(coordinate.getY()).append("]").append(" ");
            }
            //IO.printString(stringVersion.toString());

            return coordinates;
        }

        private static Coordinate getRandomCoordinates() {
            return new Coordinate(random.nextInt(upperBound), random.nextInt(upperBound));
        }

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
    HashMap<ArrayList<TypeOfNode>, Boolean> allScenarios;

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
        ArrayList<ArrayList<TypeOfNode>> scenarios = getAllPossibleScenarios();
        for (ArrayList<TypeOfNode> scenario : scenarios) {
            allScenarios.put(scenario, true);
        }
    }

    public Solution(Perception mode) {
        this(RandomInput.getRandomInputCoordinates(), mode);
    }

    public ArrayList<ArrayList<Coordinate>> findPath(FindPathInterface typeOfSearch) {
        ArrayList<ArrayList<Coordinate>> minPath = null;
        int minLength = Integer.MAX_VALUE;
        for(ArrayList<TypeOfNode> scenario : allScenarios.keySet()) {
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

    private ArrayList<ArrayList<Coordinate>> calculatePath(FindPathInterface typeOfSearch, Board board, Perception mode, ArrayList<TypeOfNode> scenario) throws HarryIsCapturedException {
        boolean isCloakInPath = false;
        ArrayList<ArrayList<Coordinate>> overallScenarioPath = new ArrayList<>();
        Coordinate currentCheckpoint = harryPosition;

        for (TypeOfNode subjectToFind : scenario) {
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

            if (!isCloakInPath && subjectToFind == TypeOfNode.CLOAK) {
                isCloakInPath = true;
            }
        }
        return overallScenarioPath;
    }

    private ArrayList<ArrayList<TypeOfNode>> getAllPossibleScenarios() {
        ArrayList<ArrayList<TypeOfNode>> allScenarios = new ArrayList<>();

        ArrayList<TypeOfNode> currentScenario = new ArrayList<>();
        currentScenario.add(TypeOfNode.BOOK);
        currentScenario.add(TypeOfNode.EXIT);
        allScenarios.add(new ArrayList<>(currentScenario));

        currentScenario.add(currentScenario.indexOf(TypeOfNode.BOOK), TypeOfNode.CLOAK);
        allScenarios.add(new ArrayList<>(currentScenario));

        currentScenario.remove(TypeOfNode.CLOAK);
        currentScenario.add(currentScenario.indexOf(TypeOfNode.EXIT), TypeOfNode.CLOAK);
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
        String file1 = "samples/sampleForBacktrackingVar1";
        String file2 = "samples/sampleForBFSVar1";
        String file3 = "samples/sampleForBacktrackingVar2";
        String file4 = "samples/sampleForBFSVar2";


        LinkedList<Solution> sample1 = StatisticsCalculator.createSample(new Perception(1));
        StatisticsCalculator.startExperiments(sample1, file1, 1, new Backtracking(true));
        StatisticsCalculator.startExperiments(sample1, file2, 1, new BFS());

        LinkedList<Solution> sample2 = StatisticsCalculator.createSample(new Perception(2));
        StatisticsCalculator.startExperiments(sample2, file3, 2, new Backtracking(false));
        StatisticsCalculator.startExperiments(sample2, file4, 2, new BFS());

        LinkedList<StatisticsCalculator.ResultOfExperiment> results1 = StatisticsCalculator.parseResultsFromFile(file1);
        LinkedList<StatisticsCalculator.ResultOfExperiment> results2 = StatisticsCalculator.parseResultsFromFile(file2);
        System.out.println(StatisticsCalculator.getMedianOfTime(results1));
        System.out.println(StatisticsCalculator.getMedianOfTime(results2));


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
