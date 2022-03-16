package eduard.zaripov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    @Test
    void testBacktracking1() throws IllegalInputCoordinate {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,2] [2,7] [7,4] [0,8] [1,4]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new Backtracking(true));

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
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new Backtracking(true));

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(14, length,
                "Wrong!");
    }

    @Test
    void testShortestBacktracking3() throws IllegalInputCoordinate {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[2,0] [6,3] [5,7] [8,8] [0,8] [7,7]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);

        assertThrows(AssertionFailedError.class, () -> {
            assertTimeoutPreemptively(ofSeconds(4), () -> {
                solution.findPath(new Backtracking(true));
            });
        });
    }

    @Test
    void testBacktracking3() throws IllegalInputCoordinate {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[2,0] [6,3] [5,7] [8,8] [0,8] [7,7]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new Backtracking(false));

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(14, length,
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

        assertEquals(path2.size(), 0);
    }

}
