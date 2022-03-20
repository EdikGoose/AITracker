package eduard.zaripov;

/**
 * Enumerate all types of cell.
 * <p>DEFAULT - there is no any subject of agents</p>
 * <p>START - start cell of Harry</p>
 * <p>DANGER - cell in radius of Inspector's vision</p>
 * <p>BOOK, CLOAK, EXIT, INSPECTOR - cell with these subjects respectively</p>
 * <p>Each has associated number of danger: -1, 0, 1, 2.</p>
 * It is needed for convenient comparison harry without cloak and with cloak
 */
public enum TypeOfCell {
    DEFAULT(0), START(0), BOOK(-1), CLOAK(-1), EXIT(-1), DANGER(1), INSPECTOR(2);
    private final int danger;

    TypeOfCell(int danger) {
        this.danger = danger;
    }

    public int getDanger() {
        return danger;
    }

    /**
     * Matching each type to string for output
     *
     * @param typeOfCell type to match
     * @return string
     */
    static String toString(TypeOfCell typeOfCell) {
        switch (typeOfCell) {
            case DEFAULT:
                return "-";
            case DANGER:
                return "D";
            case BOOK:
                return "B";
            case INSPECTOR:
                return "I";
            case CLOAK:
                return "C";
            case EXIT:
                return "E";
            case START:
                return "S";
            default:
                throw new IllegalArgumentException();
        }
    }
}
