package eduard.zaripov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



public class AppTest {

    @Test
    @DisplayName("Simple multiplication should work")
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
    @DisplayName("Simple multiplication should work")
    void testAStar2() {
        ArrayList<Coordinate> inputCoordinates = IO.parseCoordinates("[0,0] [4,2] [5,6] [7,1] [8,2] [8,1]");
        int mode = 1;
        Solution solution = new Solution(inputCoordinates, mode);
        ArrayList<ArrayList<Coordinate>> path2 = solution.findPath(new AStar());

        int length = 0;
        for (ArrayList<Coordinate> currentPath : path2) {
            length += currentPath.size();
            length--;
        }

        assertEquals(length, 18,
                "Wrong!");
    }

}
