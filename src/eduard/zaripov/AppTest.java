package eduard.zaripov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    void testBacktracking1() throws IllegalInputCoordinate {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,2] [2,7] [7,4] [0,8] [1,4]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new Backtracking());

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 16,
                "Wrong!");
    }

    @Test
    void testBacktracking2() throws IllegalInputCoordinate {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [5,2] [1,5] [8,1] [2,1] [2,2]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new Backtracking());

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 16,
                "Wrong!");
    }

    @Test
    void testBFS1() throws IllegalInputCoordinate {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,2] [2,7] [7,4] [0,8] [1,4]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        long start = System.currentTimeMillis();
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new BFS());
        long end = System.currentTimeMillis();
        System.out.println("Elapsed Time in milli seconds: "+ (end-start));
        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 16,
                "Wrong!");

    }

    @Test
    void testMode2() throws IllegalInputCoordinate {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[6,6] [4,2] [2,7] [5,5] [7,6] [7,0]");
        int mode = 2;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new BFS());

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 9,
                "Wrong!");
    }

}
