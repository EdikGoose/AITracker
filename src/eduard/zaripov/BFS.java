package eduard.zaripov;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Algorithm for finding the shortest path from coordinate to subject using Breadth-First search algorithm
 */
public class BFS implements FindPathInterface {
    /**
     * For save detected nodes if we have complex path. For example: start -> book -> exit
     */
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();

    /**
     * Iterative algorithm that use queue.
     *
     * @param board           all info about cells in coordinates
     * @param startPosition   position of start
     * @param subjectToFind   subject to find
     * @param isInvisible     If isInvisible is true, it can go through Danger {@link TypeOfCell}
     * @param mode            Type of perception of harry vision
     * @param updateDetection If true clear all detected nodes as danger
     * @return path as a list of coordinates or null if there is no path
     * @throws HarryIsCapturedException if Harry lose
     */
    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException {
        Queue<Coordinate> queue = new LinkedList<>();
        if (updateDetection) {
            detectedDangerNodes = new ArrayList<>();
        }

        ArrayList<ArrayList<Node>> cellsInfo = new ArrayList<>();
        for (int row = 0; row < board.size(); row++) {
            cellsInfo.add(new ArrayList<>());
            for (int column = 0; column < board.size(); column++) {
                cellsInfo.get(row).add(new Node());
            }
        }

        queue.add(startPosition);
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(true);

        while (!queue.isEmpty()) {
            Coordinate current = queue.remove();

            if (board.getCell(current).getTypesOfNode().contains(subjectToFind)) {
                return restorePath(cellsInfo, current);
            }


            detectedDangerNodes.addAll(mode.detectDangerNodes(board, current));


            if (!isSafe(board, current, isInvisible)) {
                if (!detectedDangerNodes.contains(current)) {
                    throw new HarryIsCapturedException();
                }
                continue;
            }

            ArrayList<Coordinate> neighbors = getNeighbors(current, board.size());
            for (Coordinate adjacentCell : neighbors) {
                Node adjacentNode = cellsInfo.get(adjacentCell.getX()).get(adjacentCell.getY());
                if (!adjacentNode.isPath()) {
                    queue.add(adjacentCell);
                    adjacentNode.setIsPath(true);
                    adjacentNode.setPrevious(current);

                    if (board.getCell(adjacentCell).getTypesOfNode().contains(subjectToFind)) {
                        return restorePath(cellsInfo, adjacentCell);
                    }
                }
            }
        }
        return null;
    }

}
