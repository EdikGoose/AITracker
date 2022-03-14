package eduard.zaripov;

import java.util.*;

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


    static void viewGameOverMessage() {
        System.out.println("The Harry is captured by a guard! Game over.");
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
    private final Node[][] grid;

    public Grid(int sizeOfGrid,
                int radiusOfStrongInspector,
                int radiusOfInspector,
                Coordinate startPosition,
                Coordinate filthPosition,
                Coordinate catPosition,
                Coordinate bookPosition,
                Coordinate cloakPosition,
                Coordinate exitPosition) {
        grid = new Node[sizeOfGrid][sizeOfGrid];

        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid.length; column++) {
                grid[row][column] = new Node();
            }
        }

        grid[startPosition.getX()][startPosition.getY()].addTypeOfNode(TypeOfNode.START);
        grid[bookPosition.getX()][bookPosition.getY()].addTypeOfNode(TypeOfNode.BOOK);
        grid[cloakPosition.getX()][cloakPosition.getY()].addTypeOfNode(TypeOfNode.CLOAK);
        if (!grid[exitPosition.getX()][exitPosition.getY()].getTypesOfNode().contains(TypeOfNode.BOOK)) {
            grid[exitPosition.getX()][exitPosition.getY()].addTypeOfNode(TypeOfNode.EXIT);
        }
        else {
            throw new IllegalArgumentException("Illegal map configuration");
        }

        addInspector(filthPosition, radiusOfStrongInspector);
        addInspector(catPosition, radiusOfInspector);

        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid.length; column++) {
                if (grid[row][column].getTypesOfNode().isEmpty()) {
                    grid[row][column].addTypeOfNode(TypeOfNode.DEFAULT);
                }
            }
        }

        if (!isSafe(startPosition, false)) {
            IO.viewGameOverMessage();
            IO.printString(toString());
            System.exit(0);
        }
    }

    public void addInspector(Coordinate coordinate, int radius) {
        for (int row = coordinate.getX() - radius; row <= coordinate.getX() + radius; row++) {
            for (int column = coordinate.getY() - radius; column <= coordinate.getY() + radius; column++) {
                if(row < 0 || row >= grid.length || column < 0 || column >= grid.length){
                    continue;
                }
                if (grid[row][column].containsCrucialElements()) {
                    throw new IllegalArgumentException("Illegal map configuration");
                }
                if (!grid[row][column].getTypesOfNode().contains(TypeOfNode.INSPECTOR)) {
                    grid[row][column].addTypeOfNode(TypeOfNode.DANGER);
                }
            }
        }
        if(getNode(coordinate).containsCrucialElements()) {
            throw new IllegalArgumentException("Illegal map configuration");
        }
        grid[coordinate.getX()][coordinate.getY()].getTypesOfNode().remove(TypeOfNode.DANGER);
        grid[coordinate.getX()][coordinate.getY()].addTypeOfNode(TypeOfNode.INSPECTOR);
    }

    private boolean isSafe(Coordinate coordinate, boolean isInvisible) {
        if (isInvisible) {
            return (coordinate.getY() >= 0 && coordinate.getY() < grid.length) &&
                    (coordinate.getX() >= 0 && coordinate.getX() < grid.length) &&
                    (!grid[coordinate.getX()][coordinate.getY()].isInspector());
        }
        return (coordinate.getY() >= 0 && coordinate.getY() < grid.length) &&
                (coordinate.getX() >= 0 && coordinate.getX() < grid.length) &&
                (!grid[coordinate.getX()][coordinate.getY()].isDangerOrInspector());
    }

    public ArrayList<Coordinate> findPathBacktracking(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) {
        clearPaths();
        if(!findPathBacktrackingRecursive(startPosition, endPosition, isInvisible, mode)) {
            return null;
        }
        return restorePath(endPosition);
    }

    private boolean findPathBacktrackingRecursive(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) {
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
            IO.viewGameOverMessage();

            toString(restorePath(startPosition));

            System.exit(0);
        }

        return false;
    }

    private void clearPaths() {
        for (Node[] row : grid) {
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
                if (startPosition.getX() + operationX < 0 || startPosition.getX() + operationX >= grid.length ||
                        startPosition.getY() + operationY < 0 || startPosition.getY() + operationY >= grid.length) {
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
            if (X + i >= 0 && X + i < grid.length && Y + 2 >= 0 && Y + 2 < grid.length) {
                if (grid[X + i][Y + 2].isDangerOrInspector()) {
                    grid[X + i][Y + 2].setIsDetectedAsDanger(true);
                }
            }
            if (X + 2 >= 0 && X + 2 < grid.length && Y + i >= 0 && Y + i < grid.length){
                if(grid[X + 2][Y + i].isDangerOrInspector()) {
                    grid[X + 2][Y + i].setIsDetectedAsDanger(true);
                }
            }
            if (X + i >= 0 && X + i < grid.length && Y - 2 >= 0 && Y - 2 < grid.length) {
                if (grid[X + i][Y - 2].isDangerOrInspector()) {
                    grid[X + i][Y - 2].setIsDetectedAsDanger(true);
                }
            }
            if (X - 2 >= 0 && X - 2 < grid.length && Y + i >= 0 && Y + i < grid.length) {
                if(grid[X - 2][Y + i].isDangerOrInspector()) {
                    grid[X - 2][Y + i].setIsDetectedAsDanger(true);
                }
            }


        }
    }

    private double getHeuristic(Coordinate startPosition, Coordinate endPosition) {
        return Math.sqrt(Math.pow(startPosition.getX() - endPosition.getX(), 2) + Math.pow(startPosition.getY() - endPosition.getY(),2));
    }

    public ArrayList<Coordinate> findPathAStar(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) {
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
                    IO.viewGameOverMessage();

                    IO.printString(toString(restorePath(current)));

                    System.exit(0);
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
                if ((neighbor.getY() >= 0 && neighbor.getY() < grid.length) && (neighbor.getX() >= 0 && neighbor.getX() < grid.length)) {
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
        return grid[coordinate.getX()][coordinate.getY()];
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

        for (int row = grid.length - 1; row >= 0; row--) {
            for (int column = 0; column < grid.length; column++) {
                int lengthOfNode;

                if (path.contains(new Coordinate(column, row))) {
                    gridString.append(ANSI_GREEN);
                }
                else if (grid[column][row].isDangerOrInspector()) {
                    gridString.append(ANSI_RED);
                }
                else {
                    gridString.append(ANSI_WHITE);
                }

                if (path.contains(new Coordinate(column, row)) && !grid[column][row].containsCrucialElements()) {
                    gridString.append(path.indexOf(new Coordinate(column, row)));
                    lengthOfNode = Integer.toString(path.indexOf(new Coordinate(column, row))).length();
                }
                else {
                    gridString.append(grid[column][row]);
                    lengthOfNode = grid[column][row].toString().length();
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
}

class RandomCoordinates{
    static int upperBound = Solution.sizeOfGrid;
    private static final Random random = new Random();

    public static int getRandomMode() {
        return random.nextInt(2) + 1;
    }

    public static ArrayList<Coordinate> getRandomInputCoordinates(){
        ArrayList<Coordinate> coordinates = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            coordinates.add(getRandomCoordinates());
        }

        return coordinates;
    }

    private static Coordinate getRandomCoordinates() {
        return new Coordinate(random.nextInt(upperBound), random.nextInt(upperBound));
    }
}

class Solution {
    static int sizeOfGrid = 9;
    static int filchRadius = 2;
    static int catRadius = 1;

    enum TypeOfSearch {
        ASTAR, BACKTRACKING;
    }

    Grid grid;
    int mode;
    Coordinate harryPosition;
    Coordinate cloakPosition;
    Coordinate bookPosition;
    Coordinate exitPosition;

    public Solution(ArrayList<Coordinate> inputCoordinates, int mode) {
        this.harryPosition = inputCoordinates.get(0);
        this.bookPosition = inputCoordinates.get(3);
        this.cloakPosition = inputCoordinates.get(4);
        this.exitPosition = inputCoordinates.get(5);
        this.grid = new Grid(sizeOfGrid, filchRadius, catRadius, harryPosition, inputCoordinates.get(1), inputCoordinates.get(2), bookPosition, cloakPosition, exitPosition);
        this.mode = mode;
    }

    public ArrayList<ArrayList<Coordinate>> findPath(TypeOfSearch typeOfSearch) {
        ArrayList<ArrayList<Coordinate>> allScenarios = getAllPossibleScenarios(harryPosition, cloakPosition, bookPosition, exitPosition);

        ArrayList<ArrayList<Coordinate>> minPath = null;
        int minLength = Integer.MAX_VALUE;
        for(ArrayList<Coordinate> scenario : allScenarios) {
            ArrayList<ArrayList<Coordinate>> path = calculatePath(typeOfSearch, grid, mode, scenario);
            if (path != null) {
                int overallLength = 0;
                for (ArrayList<Coordinate> currentPath : path) {
                    overallLength += currentPath.size();
                }

                if (minPath == null) {
                    minPath = path;
                    minLength = overallLength;
                }

                if (overallLength < minLength) {
                    minPath = path;
                }

            }
        }

        return minPath;
    }

    private ArrayList<ArrayList<Coordinate>> calculatePath(TypeOfSearch typeOfSearch, Grid grid, int mode, ArrayList<Coordinate> scenario) {
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
        allScenarios.add(currentScenario);

        currentScenario.add(2, cloakPosition);
        allScenarios.add(currentScenario);

        currentScenario.remove(cloakPosition);
        currentScenario.add(1, cloakPosition);
        allScenarios.add(currentScenario);

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
        //int inputMode = IO.readInputMode();

        ArrayList<Coordinate> coordinates = IO.readCoordinates();
        int mode = IO.readMode();

        Solution solution = new Solution(coordinates, mode);

        ArrayList<ArrayList<Coordinate>> path1 = solution.findPath(Solution.TypeOfSearch.BACKTRACKING);

        IO.printString(solution.toString(path1));

        /*
        int inputMode;

        if(args.length > 0) {
            inputMode = 2;
        }
        else {
            inputMode = IO.readInputMode();
        }

        ArrayList<Coordinate> coordinates = new ArrayList<>();
        int mode = 0;
        try {
            switch (inputMode) {
                case 1:
                    coordinates = IO.readCoordinates();
                    mode = IO.readMode();
                    break;
                case 2:
                    coordinates = RandomCoordinates.getRandomInputCoordinates();
                    mode = RandomCoordinates.getRandomMode();
                    break;
                default:
                    IO.viewErrorInputMessage();
            }
        }
        catch (IllegalArgumentException e) {
            IO.printString(e.getMessage());
            main(null);
        }

        try {
            Grid grid = new Grid(sizeOfGrid, filchRadius, catRadius, coordinates.get(0), coordinates.get(1), coordinates.get(2), coordinates.get(3), coordinates.get(4), coordinates.get(5));

            for (Coordinate coordinate : coordinates) {
                System.out.print("["+coordinate.getX()+","+coordinate.getY()+"] ");
            }
            IO.newLine();
            IO.printString("Mode: " + mode);

            IO.printString("Initial grid: ");
            IO.printString(grid.toString());

            IO.newLine();

            ArrayList<Coordinate> pathHarryToCloak = grid.findPathAStar(coordinates.get(0), coordinates.get(4), false, mode);
            if (pathHarryToCloak == null) {
                IO.printString("No path");
            }
            else {
                IO.printString(grid.toString(pathHarryToCloak));
            }

            IO.newLine();

            ArrayList<Coordinate> pathCloakToBook = grid.findPathAStar(coordinates.get(4), coordinates.get(3), true, mode);
            if (pathCloakToBook == null) {
                IO.printString("No path");
            }
            else {
                IO.printString(grid.toString(pathCloakToBook));
            }

            IO.newLine();

            ArrayList<Coordinate> pathBookToExit = grid.findPathAStar(coordinates.get(3), coordinates.get(5), true, mode);
            if (pathBookToExit == null) {
                IO.printString("No path");
            }
            else {
                IO.printString(grid.toString(pathBookToExit));
            }

            IO.newLine();
            IO.printString("BACKTRACKING!");
            IO.newLine();

            ArrayList<Coordinate> pathHarryToCloakB = grid.findPathBacktracking(coordinates.get(0), coordinates.get(4), false, mode);
            if (pathHarryToCloakB == null) {
                IO.printString("No path");
            }
            else {
                IO.printString(grid.toString(pathHarryToCloakB));
            }

            IO.newLine();

            ArrayList<Coordinate> pathCloakToBookB = grid.findPathBacktracking(coordinates.get(4), coordinates.get(3), true, mode);
            if (pathCloakToBookB == null) {
                IO.printString("No path");
            }
            else {
                IO.printString(grid.toString(pathCloakToBookB));
            }

            IO.newLine();

            ArrayList<Coordinate> pathBookToExitB = grid.findPathBacktracking(coordinates.get(3), coordinates.get(5), true, mode);
            if (pathBookToExitB == null) {
                IO.printString("No path");
            }
            else {
                IO.printString(grid.toString(pathBookToExitB));
            }

        }
        catch (IllegalArgumentException e) {
            String[] arr = new String[1];
            arr[0] = "2";
            main(arr);

        }
         */
    }
}
