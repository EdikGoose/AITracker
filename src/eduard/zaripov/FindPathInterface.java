package eduard.zaripov;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Functional interface. User of this interface can find path from some position to the needed subject
 */
public interface FindPathInterface {
    /**
     * Inner class for finding paths
     */
    class Node {
        /**
         * Contains reference to the previous coordinate in path. The previous field of the first coordinate in path should be null
         */
        private Coordinate previous = null;
        /**
         * Contains a flag whether the node has been visited
         */
        private Boolean isPath = false;

        public Coordinate getPrevious() {
            return previous;
        }

        public void setPrevious(Coordinate previous) {
            this.previous = previous;
        }

        public Boolean isPath() {
            return isPath;
        }

        public void setIsPath(Boolean path) {
            isPath = path;
        }
    }

    /**
     * Finds path in input board from startPosition to the subjectToFind
     *
     * @param board           all info about cells in coordinates
     * @param startPosition   position of start
     * @param subjectToFind   subject to find
     * @param isInvisible     If isInvisible is true, it can go through Danger {@link TypeOfCell}
     * @param mode            Type of perception of harry vision
     * @param updateDetection If true clear all detected nodes as danger
     * @return path as coordinates list
     * @throws HarryIsCapturedException if Harry moved to danger node which was not detected previously
     */
    ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException;

    /**
     * Checks if the current coordinate is safe
     *
     * @param board       all info about cells in coordinates
     * @param coordinate  coordinate to check
     * @param isInvisible Has Harry the cloak. If it has, the DANGER nodes will be safe for him
     * @return true if it is safe for him
     */
    default boolean isSafe(Board board, Coordinate coordinate, boolean isInvisible) {
        if (isInvisible) {
            return (coordinate.getY() >= 0 && coordinate.getY() < board.size()) &&
                    (coordinate.getX() >= 0 && coordinate.getX() < board.size()) &&
                    (!board.getCell(coordinate).isInspector());
        }
        return (coordinate.getY() >= 0 && coordinate.getY() < board.size()) &&
                (coordinate.getX() >= 0 && coordinate.getX() < board.size()) &&
                (!board.getCell(coordinate).isDangerOrInspector());
    }

    /**
     * Construct path through the references to the previous coordinate in the {@link Node} class:
     * <p>null <- First <- Second <- Third</p>
     *
     * @param cellsInfo  all nodes as a list
     * @param coordinate last coordinate of path
     * @return path as a list of coordinates
     */
    default ArrayList<Coordinate> restorePath(ArrayList<ArrayList<Node>> cellsInfo, Coordinate coordinate) {
        ArrayList<Coordinate> path = new ArrayList<>();
        while (cellsInfo.get(coordinate.getX()).get(coordinate.getY()).getPrevious() != null) {
            path.add(coordinate);
            coordinate = cellsInfo.get(coordinate.getX()).get(coordinate.getY()).getPrevious();
        }
        path.add(coordinate);
        Collections.reverse(path);
        return path;
    }

    /**
     * Finds all coordinates of neighbors of input coordianate that don't go off the map
     *
     * @param coordinate current coordinate
     * @param sizeOfGrid size of map to check validity of neighbors
     * @return all neighbors as a list of coordinates
     */
    default ArrayList<Coordinate> getNeighbors(Coordinate coordinate, int sizeOfGrid) {
        ArrayList<Coordinate> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                Coordinate neighbor = new Coordinate(coordinate.getX() + i, coordinate.getY() + j);
                if ((neighbor.getY() >= 0 && neighbor.getY() < sizeOfGrid) && (neighbor.getX() >= 0 && neighbor.getX() < sizeOfGrid)) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }
}
