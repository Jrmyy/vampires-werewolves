package board;

import java.io.Serializable;

/**
 * Petite classe pour simplement éviter de se balader avec des tableaux alors que l'on peut avoir un objet plus simple
 * à manipuler
 */
public class Position implements Serializable {

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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
