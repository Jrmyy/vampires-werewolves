/**
 * Petite classe pour simplement éviter de se balader avec des tableaux alors que l'on peut avoir un objet plus simple
 * à manipuler
 */
public class Coord {

    public int x;
    public int y;

    Coord() {
        this.x = 0;
        this.y = 0;
    }

    Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

}
