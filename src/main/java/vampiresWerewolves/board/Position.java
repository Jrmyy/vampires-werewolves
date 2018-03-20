package board;

import java.io.Serializable;

/**
 * Petite classe pour simplement éviter de se balader avec des tableaux alors que l'on peut avoir un objet plus simple
 * à manipuler
 */
public class Position implements Serializable, Comparable<Position> {

    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            Position position = (Position) obj;
            return this.x == position.x && this.y == position.y;
        }

        return false;
    }

    @Override
    public String toString() {
        return "X: " + this.x + " Y: " + this.y;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(Position other) {
        if (this.equals(other)) {
            return 0;
        }

        if (other.getX() > this.getX()) {
            return -1;
        } else if (other.getX() < this.getX()) {
            return 1;
        } else {
            if (other.getY() > this.getY()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
