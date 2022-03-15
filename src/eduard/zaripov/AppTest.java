package eduard.zaripov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    @Test
    void testAStar1() {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,2] [2,7] [7,4] [0,8] [1,4]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new AStar());

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 16,
                "Wrong!");
    }

    @Test
    void testAStar2() {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,2] [5,6] [7,1] [8,2] [8,1]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new AStar());

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 18,
                "Wrong!");
    }

    @Test
    void testAStar3() {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,5] [5,1] [7,1] [8,2] [8,1]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new AStar());

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 20,
                "Wrong!");
    }

    @Test
    void testBacktracking1() {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,2] [5,6] [7,1] [8,2] [8,1]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new Backtracking());

        System.out.println(solution.toString(path2));

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 19,
                "Wrong!");
    }


    @Test
    void testMode2() {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[6,6] [4,2] [2,7] [5,5] [7,6] [7,0]");
        int mode = 2;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new AStar());

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
