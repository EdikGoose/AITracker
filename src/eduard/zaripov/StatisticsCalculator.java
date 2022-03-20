package eduard.zaripov;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Calculate statistics for samples. Parse data from file
 */
public class StatisticsCalculator {
    /**
     * For > 10, the execution will be greater than 5 min
     */
    private static final int numberOfExperiments = 1000;

    /**
     * Nested class for convenient work with result of experiment
     */
    private static class ResultOfExperiment {
        long time;
        int numberOfSteps;
        boolean isWin;

        public ResultOfExperiment(int numberOfSteps, long time, String isWin) {
            this.time = time;
            this.numberOfSteps = numberOfSteps;
            this.isWin = isWin.equals("w");
        }

        public static ResultOfExperiment parseString(String str) {
            String[] words = str.split(" ");
            return new ResultOfExperiment(Integer.parseInt(words[0]), Integer.parseInt(words[1]), words[2]);
        }

        @Override
        public String toString() {
            return numberOfSteps + " " + time + " " + isWin;
        }
    }

    /**
     * For input coordinates and mode creates numberOfExperiments samples
     *
     * @return sample as list
     */
    static LinkedList<Solution> createSample(LinkedList<ArrayList<Coordinate>> coordinates, Perception mode) {
        LinkedList<Solution> sample = new LinkedList<>();
        for (ArrayList<Coordinate> currentCoordinate : coordinates) {
            try {
                sample.add(new Solution(currentCoordinate, mode));
            } catch (IllegalInputCoordinate e) {
                e.printStackTrace();
            }
        }
        return sample;
    }

    /**
     * Creates coordinates for samples
     *
     * @return coordinates as list
     */
    static LinkedList<ArrayList<Coordinate>> createCoordinatesForSample() {
        LinkedList<ArrayList<Coordinate>> coordinates = new LinkedList<>();
        for (int i = 0; i < numberOfExperiments; i++) {
            coordinates.add(Solution.RandomInput.getRandomInputCoordinates());
        }
        return coordinates;
    }

    /**
     * Starts experiments from sample and write results to file
     *
     * @param pathToFile file to write results
     */
    static void startExperiments(LinkedList<Solution> sample, String pathToFile, int mode, FindPathInterface typeOfSearch) throws ExecutionException, InterruptedException {
        try {
            FileWriter writer = new FileWriter(pathToFile);
            writer.write("# [length time win]\n");

            for (Solution solution : sample) {
                int maxTimeoutOfBacktracking = 3;

                ExecutorService service = Executors.newSingleThreadExecutor();
                Future<Integer> future = service.submit(() -> Main.calculatePathLength(solution.findPath(typeOfSearch)));
                try {
                    long startStamp = System.currentTimeMillis();
                    int length = future.get(maxTimeoutOfBacktracking, TimeUnit.SECONDS);
                    long endStamp = System.currentTimeMillis();

                    String result = length == 0 ? "l" : "w";
                    writer.write(length + " " + (endStamp - startStamp) + " " + result + "\n");
                } catch (TimeoutException e) {
                    future.cancel(true);
                }
                service.shutdownNow();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double getMedianOfTime(LinkedList<ResultOfExperiment> results) {
        int sum = 0;
        int numberOfWins = 0;
        for (ResultOfExperiment result : results) {
            if (result.isWin) {
                sum += result.time;
                numberOfWins++;
            }
        }
        return sum / (double) numberOfWins;
    }

    private static double getMedianOfLength(LinkedList<ResultOfExperiment> results) {
        int numberOfSteps = 0;
        int numberOfWins = 0;
        for (ResultOfExperiment result : results) {
            if (result.isWin) {
                numberOfSteps += result.numberOfSteps;
                numberOfWins++;
            }
        }
        return numberOfSteps / (double) numberOfWins;
    }

    private static double getWinRate(LinkedList<ResultOfExperiment> results) {
        int wins = 0;
        for (ResultOfExperiment result : results) {
            if (result.isWin) {
                wins++;
            }
        }
        return wins / (double) results.size();
    }

    /**
     * Print info about statistics from file:
     * <p>Number of experiments</p>
     * <p>Mean value of time execution</p>
     * <p>Mean value of path length</p>
     * <p>Win rate</p>
     *
     * @param nameOfFile file to read
     */
    static void printInfoAboutStatistics(String nameOfFile) throws FileNotFoundException {
        LinkedList<ResultOfExperiment> results = parseResultsFromFile(nameOfFile);
        IO.printString("Statistics from file: " + nameOfFile);
        IO.printString("Number of experiments: " + results.size());
        IO.printString("Mean value of time execution: " + getMedianOfTime(results) + " ms");
        IO.printString("Mean value of path length: " + getMedianOfLength(results) + " steps");
        IO.printString("Win rate: " + getWinRate(results) * 100 + "%");
    }

    /**
     * Parse data from file in format of:
     * <p>X Y Z</p>
     * where X=length, Y=time, Z=win or lose
     * <p>Comments in file starts from #</p>
     *
     * @param fileName file to parse
     * @return parsed results as list
     */
    private static LinkedList<ResultOfExperiment> parseResultsFromFile(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(fileName));
        LinkedList<ResultOfExperiment> results = new LinkedList<>();
        while (scanner.hasNext()) {
            String row = scanner.nextLine();
            if (row.charAt(0) == '#') {
                continue;
            }
            ResultOfExperiment result = ResultOfExperiment.parseString(row);
            results.add(result);
        }
        return results;
    }
}
