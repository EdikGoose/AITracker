package eduard.zaripov;

import java.util.TreeSet;

/**
 * Contains set of {@link TypeOfCell} and methods to work with it
 */
public class Cell {
    private final TreeSet<TypeOfCell> typesOfNode;

    public Cell() {
        this.typesOfNode = new TreeSet<>();
    }

    public TreeSet<TypeOfCell> getTypesOfNode() {
        return typesOfNode;
    }

    public void addTypeOfNode(TypeOfCell typeOfCell) {
        typesOfNode.add(typeOfCell);
    }

    /**
     * Check if the cell contains typesOfCell with danger less than 0
     *
     * @return true if contains, false if not
     */
    public boolean containsCrucialElements() {
        for (TypeOfCell typeOfCell : typesOfNode) {
            if (typeOfCell.getDanger() < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the cell is Inspector(cell with danger = 2)
     *
     * @return true if it's Inspector, false if not
     */
    public boolean isInspector() {
        for (TypeOfCell typeOfCell : typesOfNode) {
            if (typeOfCell.getDanger() == 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the cell is Inspector or in radius of Inspector (cell with danger = 2 or = 1)
     *
     * @return true if it's Inspector or in radius of Inspector, false if not
     */
    public boolean isDangerOrInspector() {
        for (TypeOfCell typeOfCell : typesOfNode) {
            if (typeOfCell.getDanger() == 1 || typeOfCell.getDanger() == 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create string by appending all types to one string
     *
     * @return created string
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (TypeOfCell typeOfCell : typesOfNode) {
            output.append(TypeOfCell.toString(typeOfCell));
        }
        return output.toString();
    }
}
