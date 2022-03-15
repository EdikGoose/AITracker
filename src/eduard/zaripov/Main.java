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

/*
interface findPathInterface{
    ArrayList<Coordinate> findPath(Grid grid, Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode);

    default boolean isSafe(Grid grid, Coordinate coordinate, boolean isInvisible) {
        if (isInvisible) {
            return (coordinate.getY() >= 0 && coordinate.getY() < grid.size()) &&
                    (coordinate.getX() >= 0 && coordinate.getX() < grid.size()) &&
                    (!grid.getNode(coordinate).isInspector());
        }
        return (coordinate.getY() >= 0 && coordinate.getY() < grid.size()) &&
                (coordinate.getX() >= 0 && coordinate.getX() < grid.size()) &&
                (!grid.getNode(coordinate).isDangerOrInspector());
    }

    default void clearPaths(Grid grid) {
        for (Node[] row : grid) {
            for (Node node : row) {
                node.setIsPath(false);
                node.setIsDetectedAsDanger(false);
                node.setPrevious(null);
            }
        }
    }
}

class Backtracking implements findPathInterface{
    @Override
    public ArrayList<Coordinate> findPath(Grid grid, Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) {
        Grid copyOfGrid = new Grid()
        if(!findPathBacktrackingRecursive(startPosition, endPosition, isInvisible, mode)) {
            return null;
        }
        return restorePath(endPosition);
    }

    private boolean findPathBacktrackingRecursive(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) throws HarryIsCapturedException {
        if (startPosition.equals(endPosition) && isSafe(startPosition, isInvisible)) {
            grid[startPosition.getX()][startPosition.getY()].setIsPath(true);
            return true;
        }

        if (isSafe(startPosition, isInvisible)) {
            if (mode == 2) {
                detectDangerNodes(startPosition);
            }

            Node nodeInStartPosition = grid[startPosition.getX()][startPosition.getY()];
            if (nodeInStartPosition.isPath()) {
                return false;
            }


            nodeInStartPosition.setIsPath(true);

            LinkedList<Coordinate> nextCoordinateToStep = getOperationPriority(startPosition, endPosition);

            for (Coordinate next : nextCoordinateToStep) {
                getNode(next).setPrevious(startPosition);
                if (findPathBacktrackingRecursive(next, endPosition, isInvisible, mode)) {
                    return true;
                }
            }

            nodeInStartPosition.setIsPath(false);
            return false;
        }

        if(mode == 2 && !grid[startPosition.getX()][startPosition.getY()].isDetectedAsDanger()) {
            throw new HarryIsCapturedException(restorePath(startPosition));
        }

        return false;
    }

}

 */

class Coordinate {
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
}

class IO {
    private static final Scanner scanner = new Scanner(System.in);

    static ArrayList<Coordinate> readCoordinates() {
        String inputString = scanner.nextLine();
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

class Node {
    private final TreeSet<TypeOfNode> typesOfNode;
    private Boolean isPath = false;
    private Boolean isDetectedAsDanger = false;

    private int g = -1;
    private double h = -1;
    private Coordinate previous = null;

    public Node() {
        this.typesOfNode = new TreeSet<>();
    }

    public Boolean isPath() {
        return isPath;
    }

    public void setIsPath(Boolean path) {
        isPath = path;
    }

    public TreeSet<TypeOfNode> getTypesOfNode() {
        return typesOfNode;
    }

    public void addTypeOfNode(TypeOfNode typeOfNode) {
        typesOfNode.add(typeOfNode);
    }

    public Boolean isDetectedAsDanger() {
        return isDetectedAsDanger;
    }

    public void setIsDetectedAsDanger(Boolean detectedAsDanger) {
        isDetectedAsDanger = detectedAsDanger;
    }

    public boolean containsCloak() {
        for (TypeOfNode typeOfNode : typesOfNode) {
            if (typeOfNode == TypeOfNode.CLOAK) {
                return true;
            }
        }
        return false;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getF() {
        return g+h;
    }

    public Coordinate getPrevious() {
        return previous;
    }

    public void setPrevious(Coordinate previous) {
        this.previous = previous;
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

class Grid {
    private final ArrayList<ArrayList<Node>> grid;

    public Grid(int sizeOfGrid,
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
                grid.get(row).add(new Node());
            }
        }

        getNode(startPosition).addTypeOfNode(TypeOfNode.START);
        getNode(bookPosition).addTypeOfNode(TypeOfNode.BOOK);
        getNode(cloakPosition).addTypeOfNode(TypeOfNode.CLOAK);
        if (!getNode(exitPosition).getTypesOfNode().contains(TypeOfNode.BOOK)) {
            getNode(exitPosition).addTypeOfNode(TypeOfNode.EXIT);
        }
        else {
            throw new IllegalInputCoordinate(TypeOfNode.BOOK, TypeOfNode.EXIT);
        }

        addInspector(filthPosition, radiusOfStrongInspector);
        addInspector(catPosition, radiusOfInspector);

        for (int row = 0; row < sizeOfGrid; row++) {
            for (int column = 0; column < sizeOfGrid; column++) {
                Coordinate currentCoordinate = new Coordinate(row, column);
                if (getNode(currentCoordinate).getTypesOfNode().isEmpty()) {
                    getNode(currentCoordinate).addTypeOfNode(TypeOfNode.DEFAULT);
                }
            }
        }

        if (!isSafe(startPosition, false)) {
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
                if (getNode(currentCoordinate).containsCrucialElements()) {
                    throw new IllegalInputCoordinate(getNode(coordinate).getTypesOfNode().first(), TypeOfNode.DANGER);
                }
                if (!getNode(currentCoordinate).getTypesOfNode().contains(TypeOfNode.INSPECTOR)) {
                    getNode(currentCoordinate).addTypeOfNode(TypeOfNode.DANGER);
                }
            }
        }
        if(getNode(coordinate).containsCrucialElements()) {
            throw new IllegalInputCoordinate(getNode(coordinate).getTypesOfNode().first(), TypeOfNode.INSPECTOR);
        }
        getNode(coordinate).getTypesOfNode().remove(TypeOfNode.DANGER);
        getNode(coordinate).addTypeOfNode(TypeOfNode.INSPECTOR);
    }

    private boolean isSafe(Coordinate coordinate, boolean isInvisible) {
        if (isInvisible) {
            return (coordinate.getY() >= 0 && coordinate.getY() < grid.size()) &&
                    (coordinate.getX() >= 0 && coordinate.getX() < grid.size()) &&
                    (!getNode(coordinate).isInspector());
        }
        return (coordinate.getY() >= 0 && coordinate.getY() < grid.size()) &&
                (coordinate.getX() >= 0 && coordinate.getX() < grid.size()) &&
                (!getNode(coordinate).isDangerOrInspector());
    }

    public ArrayList<Coordinate> findPathBacktracking(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) throws HarryIsCapturedException {
        clearPaths();
        if(!findPathBacktrackingRecursive(startPosition, endPosition, isInvisible, mode)) {
            return null;
        }
        return restorePath(endPosition);
    }

    private boolean findPathBacktrackingRecursive(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) throws HarryIsCapturedException {
        if (startPosition.equals(endPosition) && isSafe(startPosition, isInvisible)) {
            getNode(startPosition).setIsPath(true);
            return true;
        }

        if (isSafe(startPosition, isInvisible)) {
            if (mode == 2) {
                detectDangerNodes(startPosition);
            }

            Node nodeInStartPosition = getNode(startPosition);
            if (nodeInStartPosition.isPath()) {
                return false;
            }


            nodeInStartPosition.setIsPath(true);

            LinkedList<Coordinate> nextCoordinateToStep = getOperationPriority(startPosition, endPosition);

            for (Coordinate next : nextCoordinateToStep) {
                getNode(next).setPrevious(startPosition);
                if (findPathBacktrackingRecursive(next, endPosition, isInvisible, mode)) {
                    return true;
                }
            }

            nodeInStartPosition.setIsPath(false);
            return false;
        }

        if(mode == 2 && !getNode(startPosition).isDetectedAsDanger()) {
            throw new HarryIsCapturedException(restorePath(startPosition));
        }

        return false;
    }

    private void clearPaths() {
        for (ArrayList<Node> row : grid) {
            for (Node node : row) {
                node.setIsPath(false);
                node.setIsDetectedAsDanger(false);
                node.setPrevious(null);
            }
        }
    }

    private LinkedList<Coordinate> getOperationPriority(Coordinate startPosition, Coordinate endPosition) {
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
                if (startPosition.getX() + operationX < 0 || startPosition.getX() + operationX >= grid.size() ||
                        startPosition.getY() + operationY < 0 || startPosition.getY() + operationY >= grid.size()) {
                    continue;
                }
                priority.add(new Coordinate(startPosition.getX() + operationX, startPosition.getY() + operationY));
            }
        }

        return priority;
    }

    private void detectDangerNodes(Coordinate coordinate) {
        int X = coordinate.getX();
        int Y = coordinate.getY();
        for (int i = -1; i <= 1; i++) {
            if (X + i >= 0 && X + i < grid.size() && Y + 2 >= 0 && Y + 2 < grid.size()) {
                if (getNode(new Coordinate(X + i, Y + 2)).isDangerOrInspector()) {
                    getNode(new Coordinate(X + i, Y + 2)).setIsDetectedAsDanger(true);
                }
            }
            if (X + 2 >= 0 && X + 2 < grid.size() && Y + i >= 0 && Y + i < grid.size()){
                if (getNode(new Coordinate(X + 2, Y + i)).isDangerOrInspector()) {
                    getNode(new Coordinate(X + 2, Y + i)).setIsDetectedAsDanger(true);
                }
            }
            if (X + i >= 0 && X + i < grid.size() && Y - 2 >= 0 && Y - 2 < grid.size()) {
                if (getNode(new Coordinate(X + i, Y - 2)).isDangerOrInspector()) {
                    getNode(new Coordinate(X + i, Y - 2)).setIsDetectedAsDanger(true);
                }
            }
            if (X - 2 >= 0 && X - 2 < grid.size() && Y + i >= 0 && Y + i < grid.size()) {
                if (getNode(new Coordinate(X - 2, Y + i)).isDangerOrInspector()) {
                    getNode(new Coordinate(X - 2, Y + i)).setIsDetectedAsDanger(true);
                }
            }
        }
    }

    private double getHeuristic(Coordinate startPosition, Coordinate endPosition) {
        return Math.sqrt(Math.pow(startPosition.getX() - endPosition.getX(), 2) + Math.pow(startPosition.getY() - endPosition.getY(),2));
    }

    public ArrayList<Coordinate> findPathAStar(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) throws HarryIsCapturedException {
        clearPaths();
        LinkedList<Coordinate> open = new LinkedList<>();
        ArrayList<Coordinate> closed = new ArrayList<>();
        getNode(startPosition).setG(0);
        getNode(startPosition).setH(getHeuristic(startPosition, endPosition));
        getNode(startPosition).setPrevious(null);
        open.add(startPosition);
        while (!open.isEmpty()) {
            Coordinate current = open.peek();
            for (Coordinate coordinate : open) {
                if (getNode(coordinate).getF() < getNode(current).getF()) {
                    current = coordinate;
                }
            }
            open.remove(current);
            closed.add(current);

            if (mode == 2) {
                detectDangerNodes(startPosition);
            }

            if (isSafe(current, isInvisible)) {
                if (current.equals(endPosition)) {
                    return restorePath(current);
                }

                ArrayList<Coordinate> neighbors = getNeighbors(current);
                for (Coordinate neighbor : neighbors) {
                    if (closed.contains(neighbor)) {
                        continue;
                    }

                    if (open.contains(neighbor)) {
                        Node neighborNode = getNode(open.get(open.indexOf(neighbor)));
                        if (getNode(current).getG() + 1 < neighborNode.getG()) {
                            neighborNode.setG(getNode(current).getG() + 1);
                            neighborNode.setPrevious(current);
                        }
                    } else {
                        Node neighborNode = getNode(neighbor);
                        neighborNode.setG(getNode(current).getG() + 1);
                        neighborNode.setH(getHeuristic(neighbor, endPosition));
                        neighborNode.setPrevious(current);
                        open.add(neighbor);
                    }
                }
            }
            else {
                if (mode == 2 && !getNode(current).isDetectedAsDanger()) {
                    throw new HarryIsCapturedException(restorePath(current));
                }
            }
        }
        return null;
    }

    public ArrayList<Coordinate> getNeighbors(Coordinate coordinate) {
        ArrayList<Coordinate> neighbors = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                Coordinate neighbor = new Coordinate(coordinate.getX() + i, coordinate.getY() + j);
                if ((neighbor.getY() >= 0 && neighbor.getY() < grid.size()) && (neighbor.getX() >= 0 && neighbor.getX() < grid.size())) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }

    private ArrayList<Coordinate> restorePath(Coordinate coordinate) {
        ArrayList<Coordinate> path = new ArrayList<>();
        while (getNode(coordinate).getPrevious() != null) {
            path.add(coordinate);
            coordinate = getNode(coordinate).getPrevious();
        }
        path.add(coordinate);
        Collections.reverse(path);
        return path;
    }

    public Node getNode(Coordinate coordinate) {
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
                else if (getNode(inverseCoordinate).isDangerOrInspector()) {
                    gridString.append(ANSI_RED);
                }
                else {
                    gridString.append(ANSI_WHITE);
                }

                if (path.contains(inverseCoordinate) && !getNode(inverseCoordinate).containsCrucialElements()) {
                    gridString.append(path.indexOf(inverseCoordinate));
                    lengthOfNode = Integer.toString(path.indexOf(inverseCoordinate)).length();
                }
                else {
                    gridString.append(getNode(inverseCoordinate));
                    lengthOfNode = getNode(inverseCoordinate).toString().length();
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

    enum TypeOfSearch {
        ASTAR, BACKTRACKING;
    }

    Grid grid;
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
            this.grid = new Grid(sizeOfGrid, filchRadius, catRadius, harryPosition, inputCoordinates.get(1), inputCoordinates.get(2), bookPosition, cloakPosition, exitPosition);
        }
        catch (IllegalInputCoordinate e) {
            IO.printString(e.getMessage());
            System.exit(1);
        }
        catch (HarryIsCapturedException e) {
            IO.printString(e.getMessage());
            System.exit(1);
        }
        IO.printString("Initial grid: ");
        IO.printString(grid.toString());

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

    public ArrayList<ArrayList<Coordinate>> findPath(TypeOfSearch typeOfSearch) {
        ArrayList<ArrayList<Coordinate>> minPath = null;
        int minLength = Integer.MAX_VALUE;
        for(ArrayList<Coordinate> scenario : allScenarios.keySet()) {
//            if (!allScenarios.get(scenario)) {
//                continue;
//            }
            try {
//                if (typeOfSearch == TypeOfSearch.BACKTRACKING && mostProfitScenario != null) {
//                    return calculatePath(typeOfSearch, grid, mode, mostProfitScenario);
//                }

                ArrayList<ArrayList<Coordinate>> path = calculatePath(typeOfSearch, grid, mode, scenario);
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
                IO.printString(grid.toString(e.getCapturedPath()));
                System.exit(1);
            }
        }
        return minPath;
    }

    private ArrayList<ArrayList<Coordinate>> calculatePath(TypeOfSearch typeOfSearch, Grid grid, int mode, ArrayList<Coordinate> scenario) throws HarryIsCapturedException {
        boolean isCloakInPath = false;
        ArrayList<ArrayList<Coordinate>> path = new ArrayList<>();

        for (int i = 0; i < scenario.size() - 1; i++) {
            if (!isCloakInPath && grid.getNode(scenario.get(i)).containsCloak()) {
                isCloakInPath = true;
            }
            ArrayList<Coordinate> currentPath;
            switch (typeOfSearch) {
                case BACKTRACKING:
                    currentPath = grid.findPathBacktracking(scenario.get(i), scenario.get(i + 1), isCloakInPath, mode);
                    break;
                case ASTAR:
                    currentPath = grid.findPathAStar(scenario.get(i), scenario.get(i + 1), isCloakInPath, mode);
                    break;
                default:
                    currentPath = grid.findPathAStar(scenario.get(i), scenario.get(i + 1), isCloakInPath, mode);
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
            String start = grid.getNode(currentPath.get(0)).toString();
            String end = grid.getNode(currentPath.get(currentPath.size() - 1)).toString();
            output.append("Path from ").append(start).append(" to ").append(end).append(": \n");

            output.append(grid.toString(currentPath)).append("\n");
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

        long start0 = System.currentTimeMillis();
        ArrayList<ArrayList<Coordinate>> path1 = solution.findPath(Solution.TypeOfSearch.ASTAR);
        long end0 = System.currentTimeMillis();
        IO.printString("Elapsed Time in milli seconds: "+ (end0 - start0));
        IO.printString(solution.toString(path1));

        IO.newLine();

        long start1 = System.currentTimeMillis();
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(Solution.TypeOfSearch.BACKTRACKING);
        long end1 = System.currentTimeMillis();
        IO.printString("Elapsed Time in milli seconds: "+ (end1-start1));
        IO.printString(solution.toString(path2));

        IO.newLine();
        //IO.printString(solution.toString(path2));
    }
}
