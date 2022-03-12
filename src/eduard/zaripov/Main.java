package eduard.zaripov;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

class Coordinate {
    private int X;
    private int Y;

    public Coordinate(int x, int y) {
        X = x;
        Y = y;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
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
    static ArrayList<Coordinate> readCoordinates() {
        Scanner scanner = new Scanner(System.in);
        String inputString = scanner.nextLine();
        String[] stringCoordinates = inputString.split(" ");
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (String coordinate : stringCoordinates) {
            coordinates.add(Coordinate.deserialize(coordinate));
        }

        return coordinates;
    }

    static Integer readMode() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    static void newLine() {
        System.out.println();
    }

    static void printString(String string) {
        System.out.println(string);
    }

}

enum TypeOfNode{
    DEFAULT(0), START(-1), BOOK(-1), CLOAK(-1), EXIT(-1), DANGER(1), INSPECTOR(2);
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
    private TypeOfNode typeOfNode;
    private Boolean isPath = false;
    private Boolean isDetectedAsDanger = false;

    public Node(TypeOfNode typeOfNode) {
        this.typeOfNode = typeOfNode;
    }

    public TypeOfNode getTypeOfNode() {
        return typeOfNode;
    }

    public Boolean isPath() {
        return isPath;
    }

    public void setIsPath(Boolean path) {
        isPath = path;
    }

    public void setTypeOfNode(TypeOfNode typeOfNode) {
        this.typeOfNode = typeOfNode;
    }

    public Boolean isDetectedAsDanger() {
        return isDetectedAsDanger;
    }

    public void setIsDetectedAsDanger(Boolean detectedAsDanger) {
        isDetectedAsDanger = detectedAsDanger;
    }
}

class Grid {
    private final Node[][] grid;
    private ArrayList<Coordinate> path = new ArrayList<>();

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
                grid[row][column] = new Node(TypeOfNode.DEFAULT);
            }
        }

        grid[startPosition.getX()][startPosition.getY()] = new Node(TypeOfNode.START);
        addInspector(filthPosition, radiusOfStrongInspector);
        addInspector(catPosition, radiusOfInspector);
        grid[bookPosition.getX()][bookPosition.getY()] = new Node(TypeOfNode.BOOK);
        grid[cloakPosition.getX()][cloakPosition.getY()] = new Node(TypeOfNode.CLOAK);
        grid[exitPosition.getX()][exitPosition.getY()] = new Node(TypeOfNode.EXIT);
    }

    public void addInspector(Coordinate coordinate, int radius) {
        for (int row = coordinate.getX() - radius; row <= coordinate.getX() + radius; row++) {
            for (int column = coordinate.getY() - radius; column <= coordinate.getY() + radius; column++) {
                if(row < 0 || row >= grid.length || column < 0 || column >= grid.length){
                    continue;
                }
                grid[row][column] = new Node(TypeOfNode.DANGER);
            }
        }
        grid[coordinate.getX()][coordinate.getY()] = new Node(TypeOfNode.INSPECTOR);

    }

    private boolean isSafe(Coordinate coordinate, boolean isInvisible) {
        if (isInvisible) {
            return (coordinate.getY() >= 0 && coordinate.getY() < grid.length) &&
                    (coordinate.getX() >= 0 && coordinate.getX() < grid.length) &&
                    (grid[coordinate.getX()][coordinate.getY()].getTypeOfNode().getDanger() <= 1);
        }
        return (coordinate.getY() >= 0 && coordinate.getY() < grid.length) &&
                (coordinate.getX() >= 0 && coordinate.getX() < grid.length) &&
                (grid[coordinate.getX()][coordinate.getY()].getTypeOfNode().getDanger() <= 0);

    }

    public ArrayList<Coordinate> findPathBacktracking(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) {
        clearPaths();
        if(!findPathBacktrackingRecursive(startPosition, endPosition, isInvisible, mode)) {
            return null;
        }

        return path;
    }

    private void clearPaths() {
        for (Node[] row : grid) {
            for (Node node : row) {
                node.setIsPath(false);
            }
        }
        path.clear();
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
                if (grid[X + i][Y + 2].getTypeOfNode().getDanger() > 0) {
                    grid[X + i][Y + 2].setIsDetectedAsDanger(true);
                }
            }
            if (X + 2 >= 0 && X + 2 < grid.length && Y + i >= 0 && Y + i < grid.length){
                if(grid[X + 2][Y + i].getTypeOfNode().getDanger() > 0) {
                    grid[X + 2][Y + i].setIsDetectedAsDanger(true);
                }
            }
            if (X + i >= 0 && X + i < grid.length && Y - 2 >= 0 && Y - 2 < grid.length) {
                if (grid[X + i][Y - 2].getTypeOfNode().getDanger() > 0) {
                    grid[X + i][Y - 2].setIsDetectedAsDanger(true);
                }
            }
            if (X - 2 >= 0 && X - 2 < grid.length && Y + i >= 0 && Y + i < grid.length) {
                if(grid[X - 2][Y + i].getTypeOfNode().getDanger() > 0) {
                    grid[X - 2][Y + i].setIsDetectedAsDanger(true);
                }
            }


        }
    }

    private boolean findPathBacktrackingRecursive(Coordinate startPosition, Coordinate endPosition, boolean isInvisible, int mode) {
        if (startPosition.equals(endPosition) && isSafe(startPosition, isInvisible)) {
            path.add(startPosition);
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

            path.add(startPosition);
            nodeInStartPosition.setIsPath(true);

            LinkedList<Coordinate> nextCoordinateToStep = getOperationPriority(startPosition, endPosition);

            for (Coordinate next : nextCoordinateToStep) {
                 if (findPathBacktrackingRecursive(next, endPosition, isInvisible, mode)) {
                    return true;
                }
            }

            path.remove(path.size() - 1);
            nodeInStartPosition.setIsPath(false);
            return false;
        }

        if(mode == 2 && !grid[startPosition.getX()][startPosition.getY()].isDetectedAsDanger()) {
            System.out.println("The Harry is captured by a guard!");
            System.exit(1);
        }

        return false;
    }

    public int getLengthOfPath() {
        int length = 0;
        for (Node[] row : grid) {
            for (Node node : row) {
                if (node.isPath()) {
                    length++;
                }
            }
        }
        return length;
    }


    @Override
    public String toString() {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_WHITE = "\u001B[37m";
        StringBuilder gridString = new StringBuilder();

        for (int row = grid.length - 1; row >= 0; row--) {
            for (int column = 0; column < grid.length; column++) {
                if (grid[column][row].getTypeOfNode().getDanger() > 0) {
                    gridString.append(ANSI_RED);
                }
                else {
                    gridString.append(ANSI_WHITE);
                }

                gridString.append(TypeOfNode.toString(grid[column][row].getTypeOfNode()));
                gridString.append(ANSI_RESET);
                gridString.append("  ");
            }
            gridString.append("\n");
        }
        gridString.deleteCharAt(gridString.length() - 1);
        return gridString.toString();
    }

    public String toStringWithPath(ArrayList<Coordinate> path) {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_WHITE = "\u001B[37m";
        StringBuilder gridString = new StringBuilder();

        for (int row = grid.length - 1; row >= 0; row--) {
            for (int column = 0; column < grid.length; column++) {
                if (path.contains(new Coordinate(column, row))) {
                    gridString.append(ANSI_GREEN);
                }
                else if (grid[column][row].getTypeOfNode().getDanger() > 0) {
                    gridString.append(ANSI_RED);
                }
                else {
                    gridString.append(ANSI_WHITE);
                }

                if (path.contains(new Coordinate(column, row)) && grid[column][row].getTypeOfNode().getDanger() != -1) {
                    gridString.append(path.indexOf(new Coordinate(column, row)));
                }
                else {
                    gridString.append(TypeOfNode.toString(grid[column][row].getTypeOfNode()));
                }
                gridString.append(ANSI_RESET);
                gridString.append("  ");
            }
            gridString.append("\n");
        }
        gridString.deleteCharAt(gridString.length() - 1);
        return gridString.toString();
    }
}

public class Main {
    static int sizeOfGrid = 9;
    static int filchRadius = 2;
    static int catRadius = 1;


    public static void main(String[] args) {
        ArrayList<Coordinate> coordinates = IO.readCoordinates();
        int mode = IO.readMode();
        if (mode == 1) {

        }
        Grid grid = new Grid(sizeOfGrid, filchRadius, catRadius, coordinates.get(0), coordinates.get(1), coordinates.get(2), coordinates.get(3), coordinates.get(4), coordinates.get(5));
        IO.printString("Initial grid: ");
        IO.printString(grid.toString());

        IO.newLine();

        ArrayList<Coordinate> pathHarryToCloak = grid.findPathBacktracking(coordinates.get(0), coordinates.get(4), false, mode);
        IO.printString(grid.toStringWithPath(pathHarryToCloak));
        IO.newLine();
        ArrayList<Coordinate> pathCloakToBook = grid.findPathBacktracking(coordinates.get(4), coordinates.get(3), true, mode);
        IO.printString(grid.toStringWithPath(pathCloakToBook));
        if (pathHarryToCloak == null || pathCloakToBook == null) {

        }
        int overallPathLength = pathHarryToCloak.size() + pathCloakToBook.size();
        IO.printString("The path length: " + overallPathLength);

        IO.newLine();




    }
}
