package eduard.zaripov;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    private static void printInfoAboutPath(Solution solution, ArrayList<ArrayList<Coordinate>> path, String nameOfAlgorithm, long elapsedTime) {
        IO.printString("Path by: " + nameOfAlgorithm);
        if (path.size() == 0) {
            IO.printString("There is no path");
            IO.printString("Lose");
        }
        else {
            int length = calculatePathLength(path);

            IO.printString("Length of the path: " + length);
            IO.printString(solution.toString(path));
            IO.printString("Elapsed time: " + elapsedTime);
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, FileNotFoundException {
        try {
            int inputMode = IO.readInputMode();
            Solution solution;

            if (inputMode == 1) {
                solution = new Solution(IO.readCoordinates(), IO.readHarryMode());
            } else if (inputMode == 2) {
                solution = new Solution(IO.readHarryMode());
            }
            else {
                StatisticsCalculator.printInfoAboutStatistics("samples/sampleForBacktrackingVar1");
                StatisticsCalculator.printInfoAboutStatistics("samples/sampleForBFSVar1");
                StatisticsCalculator.printInfoAboutStatistics("samples/sampleForBacktrackingVar2");
                StatisticsCalculator.printInfoAboutStatistics("samples/sampleForBFSVar2");
                solution = new Solution(new Perception(1));
                main(null);
            }

            boolean isBacktrackingFindShortestPath = IO.readBacktrackingMode();
            int maxTimeoutOfBacktracking = IO.readMaxTimeout();

            IO.printString("Initial grid: ");
            IO.printString(solution.toString());

            long startStamp = System.currentTimeMillis();
            AtomicReference<ArrayList<ArrayList<Coordinate>>> pathBacktracking = new AtomicReference<>(new ArrayList<>());

            // For timeout tracking
            ExecutorService service = Executors.newSingleThreadExecutor();
            Future<?> future = service.submit(() -> pathBacktracking.set(solution.findPath(new Backtracking(isBacktrackingFindShortestPath))));
            try {
                IO.printString("Backtracking started..");
                future.get(maxTimeoutOfBacktracking, TimeUnit.SECONDS);
            }
            catch (TimeoutException e) { // if there is timeout
                future.cancel(true);
                IO.printString("Timeout!");
            }
            service.shutdownNow();
            long endStamp = System.currentTimeMillis();
            printInfoAboutPath(solution, pathBacktracking.get(), "Backtracking", endStamp - startStamp);

            IO.newLine();

            startStamp = System.currentTimeMillis();
            ArrayList<ArrayList<Coordinate>> pathBFS = solution.findPath(new BFS());
            endStamp = System.currentTimeMillis();
            printInfoAboutPath(solution, pathBFS, "BFS", endStamp - startStamp);

            startStamp = System.currentTimeMillis();
            ArrayList<ArrayList<Coordinate>> pathAStar = solution.findPath(new AStar());
            endStamp = System.currentTimeMillis();
            printInfoAboutPath(solution, pathAStar, "A*", endStamp - startStamp);
        }
        catch (NumberFormatException | IllegalInputCoordinate e) {
            IO.printString("Illegal input: " + e.getMessage());
            main(null);
        }



    }

    public static int calculatePathLength(ArrayList<ArrayList<Coordinate>> path) {
        int length = 0;
        for (ArrayList<Coordinate> partOfThePath : path) {
            length += partOfThePath.size();
            length--;
        }
        return length;
    }
}
