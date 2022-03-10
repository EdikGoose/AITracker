package eduard.zaripov;

import java.util.ArrayList;
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

}

enum TypeOfNode{
    DEFAULT, START, DANGER, INSPECTOR, BOOK, CLOAK, EXIT;

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

    public Node(TypeOfNode typeOfNode) {
        this.typeOfNode = typeOfNode;
    }

    public TypeOfNode getTypeOfNode() {
        return typeOfNode;
    }

    public void setTypeOfNode(TypeOfNode typeOfNode) {
        this.typeOfNode = typeOfNode;
    }
}

class Grid {
    private final Node[][] grid;

    public Grid(int sizeOfGrid,
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
        addInspector(filthPosition, 2);
        addInspector(catPosition, 1);
        grid[bookPosition.getX()][bookPosition.getY()] = new Node(TypeOfNode.BOOK);
        grid[cloakPosition.getX()][cloakPosition.getY()] = new Node(TypeOfNode.CLOAK);
        grid[exitPosition.getX()][exitPosition.getY()] = new Node(TypeOfNode.EXIT);
    }

    public void addInspector(Coordinate coordinate, int radius) {
        for (int row = coordinate.getX() - radius; row <= coordinate.getX() + radius; row++) {
            for (int column = coordinate.getY() - radius; column <= coordinate.getY() + radius; column++) {
                grid[row][column] = new Node(TypeOfNode.DANGER);
            }
        }
        grid[coordinate.getX()][coordinate.getY()] = new Node(TypeOfNode.INSPECTOR);

    }

    @Override
    public String toString() {
        StringBuilder gridString = new StringBuilder();

        for (int row = grid.length - 1; row >= 0; row--) {
            for (int column = 0; column < grid.length; column++) {
                gridString.append(TypeOfNode.toString(grid[column][row].getTypeOfNode())).append(" ");
            }
            gridString.append("\n");
        }

        return gridString.toString();
    }
}


public class Main {
    static int sizeOfGrid = 9;

    public static void main(String[] args) {
        ArrayList<Coordinate> coordinates = IO.readCoordinates();
        int mode = IO.readMode();

        String input = "[0,0] [4,2] [2,7] [7,4] [0,8] [1,4]\n" + "1";
        /*
[0,0] [4,2] [2,7] [7,4] [0,8] [1,4]
         */

        Grid grid = new Grid(sizeOfGrid, coordinates.get(0), coordinates.get(1), coordinates.get(2), coordinates.get(3), coordinates.get(4), coordinates.get(5));
        System.out.println(grid);
    }
}
