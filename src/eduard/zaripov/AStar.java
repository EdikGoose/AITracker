package eduard.zaripov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class AStar implements FindPathInterface {
    private ArrayList<Coordinate> detectedDangerNodes = new ArrayList<>();

    private static class NodeForAStar extends Node {
        private int g = -1;
        private double h = -1;

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public double getH() {
            return h;
        }

        public double getF() {
            return g + h;
        }

        public void setH(double h) {
            this.h = h;
        }
    }

    @Override
    public ArrayList<Coordinate> findPath(Board board, Coordinate startPosition, TypeOfCell subjectToFind, boolean isInvisible, Perception mode, boolean updateDetection) throws HarryIsCapturedException {
        ArrayList<ArrayList<NodeForAStar>> cellsInfo = new ArrayList<>();
        if (updateDetection) {
            detectedDangerNodes = new ArrayList<>();
        }
        for (int row = 0; row < board.size(); row++) {
            cellsInfo.add(new ArrayList<>());
            for (int column = 0; column < board.size(); column++) {
                cellsInfo.get(row).add(new NodeForAStar());
            }
        }

        LinkedList<Coordinate> open = new LinkedList<>();
        ArrayList<Coordinate> closed = new ArrayList<>();
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setG(0);
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setH(getHeuristic(startPosition, board.getPositionOfSubject(subjectToFind)));
        cellsInfo.get(startPosition.getX()).get(startPosition.getY()).setPrevious(null);
        open.add(startPosition);
        ArrayList<Coordinate> path = new ArrayList<>();

        while (!open.isEmpty()) {
            Coordinate current = open.peek();
            for (Coordinate coordinate : open) {
                if (cellsInfo.get(coordinate.getX()).get(coordinate.getY()).getF() < cellsInfo.get(coordinate.getX()).get(coordinate.getY()).getF()) {
                    current = coordinate;
                }
            }
            open.remove(current);
            closed.add(current);

            detectedDangerNodes.addAll(mode.detectDangerNodes(board, startPosition));

            if (isSafe(board, current, isInvisible)) {
                if (board.getCell(current).getTypesOfNode().contains(subjectToFind)) {
                    while (cellsInfo.get(current.getX()).get(current.getY()).getPrevious() != null) {
                        path.add(current);
                        current = cellsInfo.get(current.getX()).get(current.getY()).getPrevious();
                    }
                    path.add(current);
                    Collections.reverse(path);
                    return path;
                }

                ArrayList<Coordinate> neighbors = getNeighbors(current, board.size());
                for (Coordinate neighbor : neighbors) {
                    if (closed.contains(neighbor)) {
                        continue;
                    }

                    if (open.contains(neighbor)) {
                        NodeForAStar neighborNode = cellsInfo.get(open.get(open.indexOf(neighbor)).getX()).get(open.get(open.indexOf(neighbor)).getY());

                        if (cellsInfo.get(current.getX()).get(current.getY()).getG() + 1 < neighborNode.getG()) {
                            neighborNode.setG(cellsInfo.get(current.getX()).get(current.getY()).getG() + 1);
                            neighborNode.setPrevious(current);
                        }
                    } else {
                        NodeForAStar neighborNode = cellsInfo.get(neighbor.getX()).get(neighbor.getY());
                        neighborNode.setG(cellsInfo.get(neighbor.getX()).get(neighbor.getY()).getG() + 1);
                        neighborNode.setH(getHeuristic(neighbor, board.getPositionOfSubject(subjectToFind)));
                        neighborNode.setPrevious(current);
                        open.add(neighbor);
                    }
                }
            } else {
                if (!detectedDangerNodes.contains(current)) {
                    IO.printString("The Harry is captured by a guard!");
                    System.exit(0);
                }
            }
        }
        return null;
    }


    private double getHeuristic(Coordinate startPosition, Coordinate endPosition) {
        return Math.sqrt(Math.pow(startPosition.getX() - endPosition.getX(), 2) + Math.pow(startPosition.getY() - endPosition.getY(), 2));
    }
}
