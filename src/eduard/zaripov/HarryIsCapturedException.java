package eduard.zaripov;

/**
 * Simple inheritor of Exception to signalize about Harry's captured
 */
public class HarryIsCapturedException extends Exception {
    /**
     * @return simple message
     */
    @Override
    public String getMessage() {
        return "Harry is captured";
    }
}
