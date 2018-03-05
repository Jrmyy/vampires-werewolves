import java.io.Serializable;

/**
 * Petite classe pour simplement éviter de se balader avec des tableaux alors que l'on peut avoir un objet plus simple
 * à manipuler
 */
public class Coord implements Serializable {

    public int x;
    public int y;

    Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coord) {
            Coord coord = (Coord) obj;
            return this.x == coord.x && this.y == coord.y;
        }

        return false;
    }

    @Override
    public String toString() {
        return "X: " + this.x + " Y: " + this.y;
    }
}
