/**
 * Petite classe pour simplement éviter de se balader avec des tableaux alors que l'on peut avoir un objet plus simple
 * à manipuler
 */
public class Coord {

    public int x;
    public int y;

    Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Coord obj) {
        return this.x == obj.x && this.y == obj.y;
    }
}
