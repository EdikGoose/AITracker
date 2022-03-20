package eduard.zaripov;

import java.util.ArrayList;

/**
 * Contains all info about coordinates and cell in these coordinates
 */
public class Board {
    /**
     * Matrix of cells
     */
    private final ArrayList<ArrayList<Cell>> grid;

    private final Coordinate startPosition;
    private final Coordinate filthPosition;
    private final Coordinate catPosition;
    private final Coordinate bookPosition;
    private final Coordinate cloakPosition;
    private final Coordinate exitPosition;

    /**
     * Creates board with input coordinates
     *
     * @param radiusOfStrongInspector strong = with greater radius
     * @throws IllegalInputCoordinate if the coordinate is incorrect of crucial subjects are in the danger zone
     */
    public Board(int sizeOfGrid,
                 int radiusOfStrongInspector,
                 int radiusOfInspector,
                 Coordinate startPosition,
                 Coordinate filthPosition,
                 Coordinate catPosition,
                 Coordinate bookPosition,
                 Coordinate cloakPosition,
                 Coordinate exitPosition) throws IllegalInputCoordinate, HarryIsCapturedException {
        grid = new ArrayList<>();
        try {
            // Initialization of grid
            for (int row = 0; row < sizeOfGrid; row++) {
                grid.add(new ArrayList<>());
                for (int column = 0; column < sizeOfGrid; column++) {
                    grid.get(row).add(new Cell());
                }
            }

            this.startPosition = startPosition;
            this.filthPosition = filthPosition;
            this.catPosition = catPosition;
            this.cloakPosition= cloakPosition;
            this.bookPosition = bookPosition;
            this.exitPosition = exitPosition;

            getCell(startPosition).addTypeOfNode(TypeOfCell.START);
            getCell(bookPosition).addTypeOfNode(TypeOfCell.BOOK);
            getCell(cloakPosition).addTypeOfNode(TypeOfCell.CLOAK);

            // Exit cell cannot be at the same coordinate as Book cell
            if (!getCell(exitPosition).getTypesOfNode().contains(TypeOfCell.BOOK)) {
                getCell(exitPosition).addTypeOfNode(TypeOfCell.EXIT);
            } else {
                throw new IllegalInputCoordinate(TypeOfCell.BOOK, TypeOfCell.EXIT);
            }

            // If Harry spawned with cloak
            boolean isInvisible = startPosition.equals(cloakPosition);

            addInspector(filthPosition, radiusOfStrongInspector, isInvisible);
            addInspector(catPosition, radiusOfInspector, isInvisible);


            // Initialize the remaining cells as DEFAULT
            for (int row = 0; row < sizeOfGrid; row++) {
                for (int column = 0; column < sizeOfGrid; column++) {
                    Coordinate currentCoordinate = new Coordinate(row, column);
                    if (getCell(currentCoordinate).getTypesOfNode().isEmpty()) {
                        getCell(currentCoordinate).addTypeOfNode(TypeOfCell.DEFAULT);
                    }
                }
            }

            // If harry spawned inside the danger zone
            if (getCell(startPosition).isDangerOrInspector()) {
                throw new HarryIsCapturedException();
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalInputCoordinate("out of bounds of board");
        }
    }

    /**
     * Adding inspector to board and set cells as danger in input radius.
     *
     * @param coordinate  coordinate of inspector
     * @param radius      radius of danger
     * @param isInvisible for special condition when harry spawned with cloak, crucial elements can be spawned inside the danger zone
     * @throws IllegalInputCoordinate if the in danger radius there is crucial elements
     */
    public void addInspector(Coordinate coordinate, int radius, boolean isInvisible) throws IllegalInputCoordinate {
        for (int row = coordinate.getX() - radius; row <= coordinate.getX() + radius; row++) {
            for (int column = coordinate.getY() - radius; column <= coordinate.getY() + radius; column++) {
                Coordinate currentCoordinate = new Coordinate(row, column);
                if (row < 0 || row >= grid.size() || column < 0 || column >= grid.size()) {
                    continue;
                }
                if (getCell(currentCoordinate).containsCrucialElements() && !isInvisible) {
                    throw new IllegalInputCoordinate(getCell(currentCoordinate).getTypesOfNode().first(), TypeOfCell.DANGER);
                } else if (!getCell(currentCoordinate).getTypesOfNode().contains(TypeOfCell.INSPECTOR)) {
                    getCell(currentCoordinate).addTypeOfNode(TypeOfCell.DANGER);
                }
            }
        }
        getCell(coordinate).getTypesOfNode().remove(TypeOfCell.DANGER); // for replacing danger with inspector
        getCell(coordinate).addTypeOfNode(TypeOfCell.INSPECTOR);
    }

    /**
     * Map coordinate to cell
     *
     * @param coordinate to map
     * @return cell in this coordinate
     */
    public Cell getCell(Coordinate coordinate) {
        return grid.get(coordinate.getX()).get(coordinate.getY());
    }

    /**
     * Wrapper with empty list for toString(List) method
     *
     * @return board in string
     */
    @Override
    public String toString() {
        ArrayList<Coordinate> path = new ArrayList<>();
        return toString(path);
    }

    /**
     * Created string from board and input path. Path is indicated as green numbers. Danger zones as red
     *
     * @param path path to view
     * @return board with path in string
     */
    public String toString(ArrayList<Coordinate> path) {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_WHITE = "\u001B[37m";
        StringBuilder gridString = new StringBuilder();

        for (int row = grid.size() - 1; row >= 0; row--) {
            for (int column = 0; column < grid.size(); column++) {
                int lengthOfNode;
                Coordinate inverseCoordinate = new Coordinate(column, row);

                if (path.contains(inverseCoordinate)) {
                    gridString.append(ANSI_GREEN);
                } else if (getCell(inverseCoordinate).isDangerOrInspector()) {
                    gridString.append(ANSI_RED);
                } else {
                    gridString.append(ANSI_WHITE);
                }

                if (path.contains(inverseCoordinate) && !getCell(inverseCoordinate).containsCrucialElements() && !getCell(inverseCoordinate).getTypesOfNode().contains(TypeOfCell.START)) {
                    gridString.append(path.indexOf(inverseCoordinate));
                    lengthOfNode = Integer.toString(path.indexOf(inverseCoordinate)).length();
                } else {
                    gridString.append(getCell(inverseCoordinate));
                    lengthOfNode = getCell(inverseCoordinate).toString().length();
                }
                gridString.append(ANSI_RESET);

                StringBuilder spaces = new StringBuilder("   ");
                for (int i = 1; i < lengthOfNode; i++) {
                    spaces.delete(spaces.length() - 2, spaces.length() - 1);
                }
                gridString.append(spaces);
            }
            gridString.append("\n");
        }
        gridString.deleteCharAt(gridString.length() - 1);
        return gridString.toString();
    }

    public Coordinate getPositionOfSubject(TypeOfCell subject) {
        switch (subject) {
            case BOOK: return bookPosition;
            case CLOAK: return cloakPosition;
            case EXIT: return exitPosition;
            default: throw new IllegalArgumentException();
        }
    }

    /**
     * @return length of one row (or column)
     */
    public int size() {
        return grid.size();
    }
}
