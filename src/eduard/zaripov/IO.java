package eduard.zaripov;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class for input and output all needed info
 */
public class IO {
    /**
     * Standard console scanner
     */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Print message and read 6 coordinates
     *
     * @return list with input coordinates
     * @throws NumberFormatException in case of exception in parseCoordinate() method
     */
    public static ArrayList<Coordinate> readCoordinates() throws NumberFormatException {
        printString("Input 6 coordinates in format [x1,y1] [x2,y2] ... ");
        return parseCoordinates(scanner.nextLine());
    }

    /**
     * Parse 6 coordinates from string in format of:
     * [X,Y] [X,Y] [X,Y] [X,Y] [X,Y] [X,Y] [X,Y]
     *
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
     *
     * @return instance of class perception with input atrribute
     */
    public static Perception readHarryMode() {
        printString("Input mode of Harry vision:\n 1: Radius = 1\n 2: Radius = 2");
        int answer = parseAnswer(scanner.nextLine(), 2);
        if (answer == 1) {
            return new Perception(1);
        } else {
            return new Perception(2);
        }
    }

    /**
     * Print message and read answer from user
     *
     * @return answer
     */
    public static Integer readInputMode() {
        printString("Input mode:\n 1: Keyboard\n 2: Random\n 3: Print statistics");
        return parseAnswer(scanner.nextLine(), 3);
    }

    /**
     * Print message and read answer from user
     *
     * @return answer
     */
    public static boolean readBacktrackingMode() {
        printString("Backtracking finds the shortest path? (If not, it will find the first compatible)\n (1 - Yes, 2 - No)");
        int answer = parseAnswer(scanner.nextLine(), 2);
        return answer == 1;
    }

    /**
     * Print message and read max timeout
     *
     * @return max timeout
     */
    public static int readMaxTimeout() {
        printString("Input max timeout(in seconds) of backtracking. (Backtracking can work too long)");
        return scanner.nextInt();
    }

    /**
     * Check input string if it is int and belongs to input interval
     *
     * @param answerString string to check
     * @param upperBound   right bound of interval
     * @return answer
     * @throws NumberFormatException if the number is not in the interval
     */
    private static int parseAnswer(String answerString, int upperBound) throws NumberFormatException {
        int answer = Integer.parseInt(answerString);
        if (answer < 1 || answer > upperBound) {
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
     *
     * @param string string ot output
     */
    public static void printString(String string) {
        System.out.println(string);
    }
}
