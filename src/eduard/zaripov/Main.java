package eduard.zaripov;

import java.util.*;

class IllegalInputCoordinate extends Exception {
    TypeOfNode first;
    TypeOfNode second;

    public IllegalInputCoordinate(TypeOfNode first, TypeOfNode second) {
        super();
        this.first = first;
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

    @Override
    public String getMessage() {
        return "Harry is captured";
    }
}


interface FindPathInterface {
    class Node {
        private Coordinate previous = null;
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

    ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, int mode, boolean updateDetection) throws HarryIsCapturedException;

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

class Backtracking implements FindPathInterface {
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();
    private int minLengthPath = Integer.MAX_VALUE;
    private ArrayList<Coordinate> minPath = new ArrayList<>();

    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, int mode, boolean updateDetection) throws HarryIsCapturedException {
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
        findPathBacktrackingRecursive(0, board, cellsInfo, startPosition, subjectToFind, isInvisible, mode, detectedDangerNodes);

        if (minPath.size() == 0) {
            return null;
        }
        return new ArrayList<>(minPath);
    }

    private void findPathBacktrackingRecursive(int currentLength, Board board, ArrayList<ArrayList<Node>> cellsInfo, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, int mode, ArrayList<Coordinate> detectedDangerNodes) throws HarryIsCapturedException {
        if (board.getCell(startPosition).getTypesOfNode().contains(subjectToFind)) {
            if (isSafe(board, startPosition, isInvisible) && currentLength < minLengthPath) {
                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(true);
                minLengthPath = currentLength;
                minPath = restorePath(cellsInfo, startPosition);

                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(false);
                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);
            }
        }
        else {
            if (isSafe(board, startPosition, isInvisible)) {
                if (mode == 2) {
                    detectedDangerNodes.addAll(detectDangerNodes(board, startPosition));
                }

                Node nodeInStartPosition = cellsInfo.get(startPosition.getX()).get(startPosition.getY());
                if (nodeInStartPosition.isPath()) {
                    return;
                }

                nodeInStartPosition.setIsPath(true);
                currentLength++;
                if (currentLength > minLengthPath) {
                    cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(false);
                    cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);
                    return;
                }

                LinkedList<Coordinate> nextCoordinatesToStep = getOperationPriority(startPosition, new Coordinate(0, 0), board.size());

                for (Coordinate next : nextCoordinatesToStep) {
                    if (!cellsInfo.get(next.getX()).get(next.getY()).isPath()) {
                        cellsInfo.get(next.getX()).get(next.getY()).setPrevious(startPosition);
                    }

                    findPathBacktrackingRecursive(currentLength, board, cellsInfo, next, subjectToFind, isInvisible, mode, detectedDangerNodes);
                }
                nodeInStartPosition.setIsPath(false);
            }

            if (mode == 2 && !detectedDangerNodes.contains(startPosition)) {
                throw new HarryIsCapturedException(restorePath(cellsInfo, startPosition));
            }
        }
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

class BFS implements FindPathInterface {
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();

    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfNode subjectToFind, boolean isInvisible, int mode, boolean updateDetection) throws HarryIsCapturedException {
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

            if (mode == 2) {
                detectedDangerNodes.addAll(detectDangerNodes(board, current));
            }

            if (!isSafe(board, current, isInvisible)) {
                if (mode == 2 && !detectedDangerNodes.contains(current)) {
                    throw new HarryIsCapturedException(restorePath(cellsInfo, current));
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

    static ArrayList<Coordinate> readCoordinates() throws NumberFormatException {
        return parseCoordinates(scanner.nextLine());
    }

    static ArrayList<Coordinate> parseCoordinates(String inputString) throws NumberFormatException {
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

    static Integer readMode() {
        return scanner.nextInt();
    }

    static Integer readInputMode() {
        System.out.println("Input mode:\n 1: Keyboard\n 2: Random");
        int answer = Integer.parseInt(scanner.nextLine());
        if (answer < 1 || answer > 2) {
            throw new NumberFormatException("Illegal answer");
        }
        return answer;
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
            throw new HarryIsCapturedException(capturedPath);
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

            IO.printString(coordinates.toString());

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
    HashMap<ArrayList<TypeOfNode>, Boolean> allScenarios;

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
        ArrayList<ArrayList<TypeOfNode>> scenarios = getAllPossibleScenarios();
        for (ArrayList<TypeOfNode> scenario : scenarios) {
            allScenarios.put(scenario, true);
        }
    }

    public Solution() {
        this(RandomInput.getRandomInputCoordinates(), IO.readMode());
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
                return null;
            }
        }
        return minPath;
    }

    private ArrayList<ArrayList<Coordinate>> calculatePath(FindPathInterface typeOfSearch, Board board, int mode, ArrayList<TypeOfNode> scenario) throws HarryIsCapturedException {
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

        return output.toString();
    }

    public int sizeOfPath(ArrayList<ArrayList<Coordinate>> path) {
        int length = 0;
        for (ArrayList<Coordinate> partOfThePath : path) {
            length += partOfThePath.size();
            length--;
        }

        return length;
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            int inputMode = IO.readInputMode();
            Solution solution;

            if (inputMode == 1) {
                solution = new Solution(IO.readCoordinates(), IO.readMode());
            } else {
                solution = new Solution();
            }

            IO.printString("1) Breadth-first search: ");
            long start3 = System.currentTimeMillis();
            ArrayList<ArrayList<Coordinate>> pathBFS = solution.findPath(new BFS());
            long end3 = System.currentTimeMillis();

            int length1 = solution.sizeOfPath(pathBFS);
            IO.printString("Number of steps: " + length1);

            IO.printString("Elapsed Time in milli seconds: " + (end3 - start3));
            if (pathBFS == null) {
                IO.printString("There is no path");
            } else {
                IO.printString(solution.toString(pathBFS));
            }
            IO.newLine();

            IO.newLine();

            IO.printString("2) Backtracking algorithm: ");
            long start0 = System.currentTimeMillis();
            ArrayList<ArrayList<Coordinate>> pathBacktracking = solution.findPath(new Backtracking());
            long end0 = System.currentTimeMillis();

            int length2 = solution.sizeOfPath(pathBacktracking);
            IO.printString("Number of steps: " + length2);

            IO.printString("Elapsed Time in milli seconds: " + (end0 - start0));
            if (pathBacktracking == null) {
                IO.printString("There is no path");
            } else {
                IO.printString(solution.toString(pathBacktracking));
            }
            IO.newLine();
        }
        catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            main(null);
        }
    }
}
