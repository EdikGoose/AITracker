package eduard.zaripov;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Recursive algorithm for finding path from coordinate to type of node(For example: from (0,0) to book)
 * <p>On each step of recursion:</p>
 * <p>* It checks if the node is final. If it is, return true</p>
 * <p>* Then it take neighbor coordinate </p>
 * <p>* If there is no accessible neighbor coordinate, returns back to previous step</p>
 */
public class Backtracking implements FindPathInterface {
    /**
     * Define if algorithm finds the first right way or the shortest
     */
    private final boolean isTheShortestPathNeeded;

    /**
     * For save detected nodes if we have complex path. For example: start -> book -> exit
     */
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();
    /**
     * Min length founded length path
     */
    private int minLengthPath = Integer.MAX_VALUE;
    private ArrayList<Coordinate> minPath = new ArrayList<>();

    public Backtracking(boolean isTheShortestPathNeeded) {
        this.isTheShortestPathNeeded = isTheShortestPathNeeded;
    }

    /**
     * Initialize all needed fields and run recursive algorithm
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
        ArrayList<ArrayList<Node>> cellsInfo = new ArrayList<>();
        if (updateDetection) {
            detectedDangerNodes = new ArrayList<>();
        }
        for (int row = 0; row < board.size(); row++) {
            cellsInfo.add(new ArrayList<>());
            for (int column = 0; column < board.size(); column++) {
                cellsInfo.get(row).add(new Node());
            }
        }

        minLengthPath = Integer.MAX_VALUE;
        minPath.clear();
        try {
            findPathBacktrackingRecursive(0, board, cellsInfo, startPosition, subjectToFind, isInvisible, mode, detectedDangerNodes);
        } catch (InterruptedException e) {
            return new ArrayList<>(minPath);
        }
        if (minPath.size() == 0) {
            return null;
        }
        return new ArrayList<>(minPath);
    }

    /**
     * Recursive algorithm
     *
     * @param currentLength       the depth of recursive which is also current length of path
     * @param board               all info about cells in coordinates
     * @param cellsInfo           all info node in cells
     * @param isInvisible         is Harry have a cloak
     * @param mode                perception mode of Harry
     * @param detectedDangerNodes current detected danger nodes
     * @return true if there is path or false if there is no path
     * @throws HarryIsCapturedException if Harry lose
     * @throws InterruptedException     if timeout of backtracking working
     */
    private boolean findPathBacktrackingRecursive(int currentLength, Board board, ArrayList<ArrayList<Node>> cellsInfo, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, ArrayList<Coordinate> detectedDangerNodes) throws HarryIsCapturedException, InterruptedException {
        // It is needed for limit time of backtracking working
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Thread interrupted");
        }

        if (board.getCell(startPosition).getTypesOfNode().contains(subjectToFind)) {
            if (isSafe(board, startPosition, isInvisible) && currentLength < minLengthPath) {
                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(true);
                minLengthPath = currentLength;
                minPath = restorePath(cellsInfo, startPosition);

                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(false);
                cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);

                return true;
            }
        } else {
            if (isSafe(board, startPosition, isInvisible)) {
                detectedDangerNodes.addAll(mode.detectDangerNodes(board, startPosition));

                Node nodeInStartPosition = cellsInfo.get(startPosition.getX()).get(startPosition.getY());
                if (nodeInStartPosition.isPath()) {
                    return false;
                }

                nodeInStartPosition.setIsPath(true);
                currentLength++;
                if (currentLength > minLengthPath) {
                    cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setIsPath(false);
                    cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);
                    return false;
                }

                LinkedList<Coordinate> nextCoordinatesToStep = getOperationSequence(startPosition, new Coordinate(0, 0), board.size());

                for (Coordinate next : nextCoordinatesToStep) {
                    if (!cellsInfo.get(next.getX()).get(next.getY()).isPath()) {
                        cellsInfo.get(next.getX()).get(next.getY()).setPrevious(startPosition);
                    }

                    if (findPathBacktrackingRecursive(currentLength, board, cellsInfo, next, subjectToFind, isInvisible, mode, detectedDangerNodes) && !isTheShortestPathNeeded) {
                        return true;
                    }
                }
                nodeInStartPosition.setIsPath(false);
            }

            if (!detectedDangerNodes.contains(startPosition)) {
                throw new HarryIsCapturedException();
            }

        }
        return false;
    }

    /**
     * Finds sequence of neighbors node. If we know the end position, it can optimize backtracking
     *
     * @return list of neighbors node
     */
    private LinkedList<Coordinate> getOperationSequence(Coordinate startPosition, Coordinate endPosition, int sizeOfGrid) {
        LinkedList<Coordinate> priority = new LinkedList<>();
        LinkedList<Integer> priorityX = new LinkedList<>();
        if (endPosition.getX() == startPosition.getX()) {
            priorityX.add(0);
            priorityX.add(+1);
            priorityX.add(-1);
        } else {
            int sign = (endPosition.getX() - startPosition.getX()) / Math.abs(endPosition.getX() - startPosition.getX());
            priorityX.add(sign);
            priorityX.add(0);
            priorityX.add(-sign);
        }

        LinkedList<Integer> priorityY = new LinkedList<>();
        if (endPosition.getY() == startPosition.getY()) {
            priorityY.add(0);
            priorityY.add(+1);
            priorityY.add(-1);
        } else {
            int sign = (endPosition.getY() - startPosition.getY()) / Math.abs(endPosition.getY() - startPosition.getY());
            priorityY.add(sign);
            priorityY.add(0);
            priorityY.add(-sign);
        }

        for (int operationX : priorityX) {
            for (int operationY : priorityY) {
                if (operationX == 0 && operationY == 0) {
                    continue;
                }
                if (startPosition.getX() + operationX < 0 || startPosition.getX() + operationX >= sizeOfGrid ||
                        startPosition.getY() + operationY < 0 || startPosition.getY() + operationY >= sizeOfGrid) {
                    continue;
                }
                priority.add(new Coordinate(startPosition.getX() + operationX, startPosition.getY() + operationY));
            }
        }

        return priority;
    }

}
