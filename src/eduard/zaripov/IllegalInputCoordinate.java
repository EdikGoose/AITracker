package eduard.zaripov;

/**
 * Simple inheritor of Exception to signalize about incorrect input coordinates on the board
 * <p> It has two fields of TypeOfNode type for indicating which node create this exception</p>
 * <p> It checked exception -> it is needed to handle</p>
 */
public class IllegalInputCoordinate extends Exception {
    TypeOfCell first;
    TypeOfCell second;
    String message;

    /**
     * Constructs an IllegalInputCoordinate with input types of node
     *
     * @param first  the first node in the correct position
     * @param second the second node in the correct position
     */
    public IllegalInputCoordinate(TypeOfCell first, TypeOfCell second) {
        super();
        this.first = first;
        this.second = second;
        this.message = "";
    }

    public IllegalInputCoordinate(String message) {
        super();
        this.message = message;
    }

    /**
     * @return string with incorrect nodes
     */
    @Override
    public String getMessage() {
        if (first == null || second == null) {
            return message;
        }
        return TypeOfCell.toString(first) + " cannot be at the same coordinate as " + TypeOfCell.toString(second);
    }
}
