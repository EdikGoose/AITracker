package eduard.zaripov;

/**
 * Keeps coordinate of point as (X,Y)
 */
public class Coordinate implements Comparable<Coordinate> {
    private final int X;
    private final int Y;

    /**
     * Creates coordinate in (X,Y)
     *
     * @param x number of column
     * @param y number of row
     */
    public Coordinate(int x, int y) {
        X = x;
        Y = y;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    /**
     * Equals if the x and y equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return X == that.X && Y == that.Y;
    }

    @Override
    public String toString() {
        return "X=" + X + ", Y=" + Y;
    }

    /**
     * Parse string in format of "[X,Y]"
     *
     * @param coordinatesInString string to parse
     * @return new Coordinate
     */
    public static Coordinate deserialize(String coordinatesInString) {
        String[] split = coordinatesInString.split(",");
        int X = Integer.parseInt(split[0].substring(1));
        int Y = Integer.parseInt(split[1].substring(0, split[1].length() - 1));

        return new Coordinate(X, Y);
    }

    /**
     * Comparing two coordinates by euclidean distance
     *
     * @param o coordinate to compare
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Coordinate o) {
        return Integer.compare(getX() * getY(), o.getX() * o.getY());
    }


}
