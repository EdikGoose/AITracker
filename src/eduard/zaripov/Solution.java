package eduard.zaripov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Manages work with I/O, finding paths and print them
 */
public class Solution {
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

        public static ArrayList<Coordinate> getRandomInputCoordinates() {
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
         *
         * @param currentCoordinate   coordinate to check
         * @param inspectorCoordinate coordinate of Inspector
         * @param radius              radius of Inspector's danger zone
         * @return true if coordinate in the danger zone, false otherwise
         */
        private static boolean isInDanger(Coordinate currentCoordinate, Coordinate inspectorCoordinate, int radius) {
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
     * Each scenario maps to bool in case of optimization. If the scenario maps to bool, it means that there is no path for this scenario
     */
    HashMap<ArrayList<TypeOfCell>, Boolean> allScenarios;

    /**
     * Constructs a Solution class with input coordinates and mode
     */
    public Solution(ArrayList<Coordinate> inputCoordinates, Perception mode) throws IllegalArgumentException, IllegalInputCoordinate {
        this.harryPosition = inputCoordinates.get(0);
        this.bookPosition = inputCoordinates.get(3);
        this.cloakPosition = inputCoordinates.get(4);
        this.exitPosition = inputCoordinates.get(5);

        try {
            this.board = new Board(sizeOfGrid, filchRadius, catRadius, harryPosition, inputCoordinates.get(1), inputCoordinates.get(2), bookPosition, cloakPosition, exitPosition);
        } catch (HarryIsCapturedException e) {
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

    /**
     * Constructs a Solution class with random coordinate and input mode
     */
    public Solution(Perception mode) throws IllegalInputCoordinate {
        this(RandomInput.getRandomInputCoordinates(), mode);
    }

    /**
     * Finding path from start to book and then to exit(using or not using cloak) {@link FindPathInterface}
     * It checks each scenario and find the shortest variant.
     *
     * @param typeOfSearch input type of search
     * @return path divided in parts. If the path is empty -> there is no path
     * <p>for example: path HARRY -> BOOK -> EXIT will be divided into paths: HARRY -> BOOK and BOOK -> EXIT</p>
     */
    public ArrayList<ArrayList<Coordinate>> findPath(FindPathInterface typeOfSearch) {
        ArrayList<ArrayList<Coordinate>> minPath = null;
        int minLength = Integer.MAX_VALUE;

        for (ArrayList<TypeOfCell> scenario : allScenarios.keySet()) {
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
            } catch (HarryIsCapturedException e) {
                IO.printString(e.getMessage());
                return new ArrayList<>();
            }
        }
        if (minPath == null) {
            return new ArrayList<>();
        }
        return minPath;
    }

    /**
     * Calculates path for input scenario
     *
     * @param scenario as a list of types of cell
     */
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

    /**
     * @return list of possible scenario. Each scenario is the list of cell's type in current order
     */
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

    /**
     * Using input path and board creates string
     *
     * @param path to view
     * @return creates string
     */
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

    /**
     * @return just board in string
     */
    @Override
    public String toString() {
        return board.toString();
    }
}
