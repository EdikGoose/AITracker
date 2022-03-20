package eduard.zaripov;

import java.util.ArrayList;

/**
 * For Harry's type of perception
 * <p>He can see only nodes in coordinates that locates in radius across him. The crucial idea is that Harry cannot
 * see nodes inside the rectangle of vision and also vertices of rectangle</p>
 * <p>Example:(H - harry, V - he can see, D - he cannot see)</p>
 * <p>D V V V D</p>
 * <p>V D D D V</p>
 * <p>V D H D V</p>
 * <p>V D D D V</p>
 * <p>D V V V D</p>
 */
public class Perception {
    /**
     * Radius of view
     */
    private final int radius;

    public Perception(int radius) {
        this.radius = radius;
    }

    /**
     * Finds and check all nodes in radius of view
     *
     * @param board      info about nodes in coordinates
     * @param coordinate coordinate of Harry
     * @return list of nodes detected as danger
     */
    public ArrayList<Coordinate> detectDangerNodes(Board board, Coordinate coordinate) {
        ArrayList<Coordinate> dangerNodes = new ArrayList<>();
        if (radius == 1) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }
                    dangerNodes.add(new Coordinate(coordinate.getX() + i, coordinate.getY() + j));
                }
            }
        } else if (radius == 2) {
            int X = coordinate.getX();
            int Y = coordinate.getY();
            for (int i = -1; i <= 1; i++) {
                if (X + i >= 0 && X + i < board.size() && Y + 2 >= 0 && Y + 2 < board.size()) {
                    if (board.getCell(new Coordinate(X + i, Y + 2)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X + i, Y + 2));
                    }
                }
                if (X + 2 >= 0 && X + 2 < board.size() && Y + i >= 0 && Y + i < board.size()) {
                    if (board.getCell(new Coordinate(X + 2, Y + i)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X + 2, Y + i));
                    }
                }
                if (X + i >= 0 && X + i < board.size() && Y - 2 >= 0 && Y - 2 < board.size()) {
                    if (board.getCell(new Coordinate(X + i, Y - 2)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X + i, Y - 2));
                    }
                }
                if (X - 2 >= 0 && X - 2 < board.size() && Y + i >= 0 && Y + i < board.size()) {
                    if (board.getCell(new Coordinate(X - 2, Y + i)).isDangerOrInspector()) {
                        dangerNodes.add(new Coordinate(X - 2, Y + i));
                    }
                }
            }
        } else {
            // In case of future extension
            throw new IllegalArgumentException("No support radius > 2");
        }
        return dangerNodes;
    }

}
