package eduard.zaripov;

import java.util.*;

class IllegalInputCoordinate extends Exception {
    TypeOfNode first;
    TypeOfNode second;

    public IllegalInputCoordinate(TypeOfNode fist, TypeOfNode second) {
        super();
        this.first = fist;
        this.second = second;
    }

    @Override
    public String getMessage() {
        return TypeOfNode.toString(first) + " cannot be at the same coordinate as " + TypeOfNode.toString(second);
    }
}

class HarryIsCapturedException extends Exception {
    private final ArrayList<Coordinate> capturedPath;

    public HarryIsCapturedException(ArrayList<Coordinate> capturedPath) {
        this.capturedPath = capturedPath;
    }

    public ArrayList<Coordinate> getCapturedPath() {
        return capturedPath;
    }

    @Override
    public String getMessage() {
        return "Harry is captured";
    }
}


interface FindPathInterface {
    class Node {
        private int g = -1;
        private double h = -1;
        private Coordinate previous = null;
        private Boolean isPath = false;

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public void setH(double h) {
            this.h = h;
        }

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

        public double getF() {
            return g + h;
        }
    }

    ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode, boolean updateDetection) throws HarryIsCapturedException;

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

    default ArrayList<Coordinate> detectDangerNodes(Board board, Coordinate coordinate) {
        ArrayList<Coordinate> dangerNodes = new ArrayList<>();

        int X = coordinate.getX();
        int Y = coordinate.getY();
        for (int i = -1; i <= 1; i++) {
            if (X + i >= 0 && X + i < board.size() && Y + 2 >= 0 && Y + 2 < board.size()) {
                if (board.getCell(new Coordinate(X + i, Y + 2)).isDangerOrInspector()) {
                    dangerNodes.add(new Coordinate(X + i, Y + 2));
                }
            }
            if (X + 2 >= 0 && X + 2 < board.size() && Y + i >= 0 && Y + i < board.size()){
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
        return dangerNodes;
    }
}

class Backtracking implements FindPathInterface {
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();
    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode, boolean updateDetection) throws HarryIsCapturedException {
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

        if(!findPathBacktrackingRecursive(board, cellsInfo, startPosition, endPosition, isInvisible, mode, detectedDangerNodes)) {
            return null;
        }
        return restorePath(cellsInfo, endPosition);
    }

    private boolean findPathBacktrackingRecursive(Board board, ArrayList<ArrayList<Node>> cellsInfo, Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode, ArrayList<Coordinate> detectedDangerNodes) throws HarryIsCapturedException {
        if (startPosition.equals(endPosition)) {
            if (isSafe(board, startPosition, isInvisible)) {
                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(true);
                return true;
            }
            return false;
        }

        if (isSafe(board, startPosition, isInvisible)) {
            if (mode == 2) {
                detectedDangerNodes.addAll(detectDangerNodes(board, startPosition));
            }

            Node nodeInStartPosition = cellsInfo.get(startPosition.getX()).get(startPosition.getY());
            if (nodeInStartPosition.isPath()) {
                return false;
            }

            nodeInStartPosition.setIsPath(true);

            LinkedList<Coordinate> nextCoordinatesToStep = getOperationPriority(startPosition, endPosition, board.size());

            for (Coordinate next : nextCoordinatesToStep) {
                if (!cellsInfo.get(next.getX()).get(next.getY()).isPath()) {
                    cellsInfo.get(next.getX()).get(next.getY()).setPrevious(startPosition);
                }
                if (findPathBacktrackingRecursive(board, cellsInfo, next, endPosition, isInvisible, mode, detectedDangerNodes)) {
                    return true;
                }
            }

            nodeInStartPosition.setIsPath(false);
            return false;
        }

        if(mode == 2 && !detectedDangerNodes.contains(startPosition)) {
            throw new HarryIsCapturedException(restorePath(cellsInfo, startPosition));
        }

        return false;
    }


    private LinkedList<Coordinate> getOperationPriority(Coordinate startPosition, Coordinate endPosition, int sizeOfGrid) {
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

class AStar implements FindPathInterface {
    TreeSet<Coordinate> detectedDangerNodes = new TreeSet<>();

    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode, boolean updateDetection) throws HarryIsCapturedException {
        ArrayList<ArrayList<Node>> cellsInfo = new ArrayList<>();
        if (updateDetection) {
            detectedDangerNodes = new TreeSet<>();
        }
        for (int row = 0; row < board.size(); row++) {
            cellsInfo.add(new ArrayList<>());
            for (int column = 0; column < board.size(); column++) {
                cellsInfo.get(row).add(new Node());
            }
        }

        LinkedList<Coordinate> open = new LinkedList<>();
        ArrayList<Coordinate> closed = new ArrayList<>();
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setG(0);
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setH(getHeuristic(startPosition, endPosition));
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);

        open.add(startPosition);

        while (!open.isEmpty()) {
            Coordinate current = open.peek();
            for (Coordinate coordinate : open) {
                if (cellsInfo.get(coordinate.getX()).get(coordinate.getY()).getF() < cellsInfo.get(current.getX()).get(current.getY()).getF()) {
                    current = coordinate;
                }
            }
            open.remove(current);
            closed.add(current);

            if (mode == 2) {
                detectedDangerNodes.addAll(detectDangerNodes(board, current));
            }


            if (isSafe(board, current, isInvisible)) {
                if (current.equals(endPosition)) {
                    return restorePath(cellsInfo, current);
                }

                ArrayList<Coordinate> neighbors = getNeighbors(current, board.size());
                for (Coordinate neighbor : neighbors) {
                    if (closed.contains(neighbor)) {
                        continue;
                    }

                    if (open.contains(neighbor)) {
                        Coordinate coordinateOfNeighborNode = open.get(open.indexOf(neighbor));
                        Node neighborNode = cellsInfo.get(coordinateOfNeighborNode.getX()).get(coordinateOfNeighborNode.getY());
                        if (cellsInfo.get(current.getX()).get(current.getY()).getG() + 1 < neighborNode.getG()) {
                            neighborNode.setG(cellsInfo.get(current.getX()).get(current.getY()).getG() + 1);
                            neighborNode.setPrevious(current);
                        }
                    } else {
                        Node neighborNode = cellsInfo.get(neighbor.getX()).get(neighbor.getY());
                        neighborNode.setG(cellsInfo.get(current.getX()).get(current.getY()).getG() + 1);
                        neighborNode.setH(getHeuristic(neighbor, endPosition));
                        neighborNode.setPrevious(current);
                        open.add(neighbor);
                    }
                }
            }
            else {
                if (mode == 2 && !detectedDangerNodes.contains(current)) {
                    throw new HarryIsCapturedException(restorePath(cellsInfo, current));
                }
            }
        }
        return null;
    }

    private double getHeuristic(Coordinate startPosition, Coordinate endPosition) {
        return Math.sqrt(Math.pow(startPosition.getX() - endPosition.getX(), 2) + Math.pow(startPosition.getY() - endPosition.getY(),2));
    }

    public ArrayList<Coordinate> getNeighbors(Coordinate coordinate, int sizeOfGrid) {
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


class Coordinate implements Comparable{
    private final int X;
    private final int Y;

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

    public static Coordinate deserialize(String coordinatesInString) {
        String[] split = coordinatesInString.split(",");
        int X = Integer.parseInt(split[0].substring(1));
        int Y = Integer.parseInt(split[1].substring(0, split[1].length()-1));

        return new Coordinate(X, Y);
    }

    @Override
    public int compareTo(Object o) {
        Coordinate coordinateToCompare = (Coordinate) o;
        return Integer.compare(getX()*getY(), coordinateToCompare.getX()*coordinateToCompare.getY());
    }


}

class IO {
    private static final Scanner scanner = new Scanner(System.in);

    static ArrayList<Coordinate> readCoordinates() {
        return parseCoordinates(scanner.nextLine());
    }

    static ArrayList<Coordinate> parseCoordinates(String inputString) {
        String[] stringCoordinates = inputString.split(" ");
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (String coordinate : stringCoordinates) {
            coordinates.add(Coordinate.deserialize(coordinate));
        }

        return coordinates;
    }

    static Integer readMode() {
        return scanner.nextInt();
    }

    static Integer readInputMode() {
        System.out.println("Input mode:\n 1: Keyboard\n 2: Random");
        return Integer.parseInt(scanner.nextLine());
    }

    static void newLine() {
        System.out.println();
    }

    static void printString(String string) {
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

    public boolean containsCloak() {
        for (TypeOfNode typeOfNode : typesOfNode) {
            if (typeOfNode == TypeOfNode.CLOAK) {
                return true;
            }
        }
        return false;
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

        addInspector(filthPosition, radiusOfStrongInspector);
        addInspector(catPosition, radiusOfInspector);

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
            throw new HarryIsCapturedException(capturedPath);
        }
    }

    public void addInspector(Coordinate coordinate, int radius) throws IllegalInputCoordinate {
        for (int row = coordinate.getX() - radius; row <= coordinate.getX() + radius; row++) {
            for (int column = coordinate.getY() - radius; column <= coordinate.getY() + radius; column++) {
                Coordinate currentCoordinate = new Coordinate(row, column);
                if(row < 0 || row >= grid.size() || column < 0 || column >= grid.size()){
                    continue;
                }
                if (getCell(currentCoordinate).containsCrucialElements()) {
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

        public static int getRandomInputMode() {
            return random.nextInt(2) + 1;
        }

        public static ArrayList<Coordinate> getRandomInputCoordinates(){
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                coordinates.add(getRandomCoordinates());
            }
            Coordinate filchCoordinate = coordinates.get(1);
            Coordinate catCoordinate = coordinates.get(2);

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
    int mode;
    Coordinate harryPosition;
    Coordinate cloakPosition;
    Coordinate bookPosition;
    Coordinate exitPosition;
    HashMap<ArrayList<Coordinate>, Boolean> allScenarios;
    ArrayList<Coordinate> mostProfitScenario;

    public Solution(ArrayList<Coordinate> inputCoordinates, int mode) throws IllegalArgumentException {
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
        IO.printString("Initial grid: ");
        IO.printString(board.toString());

        this.mode = mode;
        this.allScenarios = new HashMap<>();
        ArrayList<ArrayList<Coordinate>> scenarios = getAllPossibleScenarios(harryPosition, cloakPosition, bookPosition, exitPosition);
        for (ArrayList<Coordinate> scenario : scenarios) {
            allScenarios.put(scenario, true);
        }
    }

    public Solution() {
        this(RandomInput.getRandomInputCoordinates(), RandomInput.getRandomInputMode());
    }

    public ArrayList<ArrayList<Coordinate>> findPath(FindPathInterface typeOfSearch) {
        ArrayList<ArrayList<Coordinate>> minPath = null;
        int minLength = Integer.MAX_VALUE;
        for(ArrayList<Coordinate> scenario : allScenarios.keySet()) {
//            if (!allScenarios.get(scenario)) {
//                continue;
//            }
            try {
//                if (typeOfSearch.getClass().toString().equals(Backtracking.class.toString()) && mostProfitScenario != null) {
//                    return calculatePath(typeOfSearch, board, mode, mostProfitScenario);
//                }

                ArrayList<ArrayList<Coordinate>> path = calculatePath(typeOfSearch, board, mode, scenario);
                if (path != null) {
                    int overallLength = 0;
                    for (ArrayList<Coordinate> currentPath : path) {
                        overallLength += currentPath.size();
                    }
                    if (minPath == null) {
                        mostProfitScenario = scenario;
                        minPath = path;
                        minLength = overallLength;
                    } else if (overallLength < minLength) {
                        mostProfitScenario = scenario;
                        minPath = path;
                        minLength = overallLength;
                    }
                } else {
                    allScenarios.replace(scenario, false);
                }
            }
            catch (HarryIsCapturedException e) {
                IO.printString(e.getMessage());
                IO.printString(board.toString(e.getCapturedPath()));

                System.exit(1);
            }
        }
        return minPath;
    }

    private ArrayList<ArrayList<Coordinate>> calculatePath(FindPathInterface typeOfSearch, Board board, int mode, ArrayList<Coordinate> scenario) throws HarryIsCapturedException {
        boolean isCloakInPath = false;
        ArrayList<ArrayList<Coordinate>> path = new ArrayList<>();

        for (int idx = 0; idx < scenario.size() - 1; idx++) {
            if (!isCloakInPath && board.getCell(scenario.get(idx)).containsCloak()) {
                isCloakInPath = true;
            }

            ArrayList<Coordinate> currentPath;
            if (idx == 0) {
                currentPath = typeOfSearch.findPath(board, scenario.get(idx), scenario.get(idx + 1), isCloakInPath, mode, true);
            }
            else {
                currentPath = typeOfSearch.findPath(board, scenario.get(idx), scenario.get(idx + 1), isCloakInPath, mode, false);
            }

            if (currentPath == null) {
                return null;
            }
            path.add(currentPath);
        }
        return path;
    }

    private ArrayList<ArrayList<Coordinate>> getAllPossibleScenarios(Coordinate harryPosition, Coordinate cloakPosition, Coordinate bookPosition, Coordinate exitPosition) {
        ArrayList<ArrayList<Coordinate>> allScenarios = new ArrayList<>();

        ArrayList<Coordinate> currentScenario = new ArrayList<>();
        currentScenario.add(harryPosition);
        currentScenario.add(bookPosition);
        currentScenario.add(exitPosition);
        allScenarios.add(new ArrayList<>(currentScenario));

        currentScenario.add(2, cloakPosition);
        allScenarios.add(new ArrayList<>(currentScenario));

        currentScenario.remove(cloakPosition);
        currentScenario.add(1, cloakPosition);
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

        return output.toString();
    }
}

public class Main {
    public static void main(String[] args) {
        int inputMode = IO.readInputMode();
        Solution solution;
        if (inputMode == 1) {
             solution = new Solution(IO.readCoordinates(), IO.readMode());
        }
        else {
            solution = new Solution();
        }

        long start1 = System.currentTimeMillis();
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new AStar());
        long end1 = System.currentTimeMillis();

        IO.printString("Elapsed Time in milli seconds: "+ (end1-start1));
        IO.printString(solution.toString(path2));
        IO.newLine();

        long start0 = System.currentTimeMillis();
        ArrayList<ArrayList<Coordinate>> path1 = solution.findPath(new Backtracking());
        long end0 = System.currentTimeMillis();

        IO.printString("Elapsed Time in milli seconds: "+ (end0 - start0));
        IO.printString(solution.toString(path1));
        IO.newLine();


        //IO.printString(solution.toString(path2));
    }
}
