package innopolis.university;

import java.util.ArrayList;
import java.util.Scanner;

class Coordinates{
    int X;
    int Y;

    @Override
    public String toString() {
        return "X=" + X +
                ", Y=" + Y;
    }
}
class IO{
    static ArrayList<Coordinates> readCoordinates() {
        Scanner scanner = new Scanner(System.in);
        return parseString(scanner.next());
    }

    static Integer readMode() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    private static ArrayList<Coordinates> parseString(String input) {
        ArrayList<Coordinates> coordinates = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);

            if (symbol == '[') {
                Coordinates currentCoordinates = new Coordinates();
                currentCoordinates.X = input.charAt(i+1);
                currentCoordinates.Y = input.charAt(i+2);
                coordinates.add(currentCoordinates);
            }

        }
        return coordinates;
    }
}

public class Main {

    public static void main(String[] args) {
	    ArrayList<Coordinates> coordinates = IO.readCoordinates();
        int mode = IO.readMode();

        for(Coordinates currentCoordinates : coordinates) {
            System.out.println(currentCoordinates.toString());
        }
     }
}
