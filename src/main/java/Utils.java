/**
 * Quelques fonctions utils
 */
public class Utils {

    /**
     * Calcule la distance minimum entre deux coordonées sur la carte
     * @param a
     * @param b
     * @return
     */
    public static int minDistance(Coord a, Coord b) {
        return Math.max(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
    }

    /**
     * Avec une coordonnée de départ et une coordonnée d'arrivée, calcule sur quelle case aller au prochain mouvement
     * @param start
     * @param goal
     * @return
     */
    public static Coord findNextMove(Coord start, Coord goal) {

        if (goal.equals(start)) {
            return start;
        }

        if (goal.x == start.x) {
            return new Coord(
                    start.x,
                    start.y + (goal.y - start.y) / Math.abs(goal.y - start.y)
            );
        }

        if (goal.y == start.y) {
            return new Coord(
                    start.x + (goal.x - start.x) / Math.abs(goal.x - start.x),
                    start.y
            );
        }

        return new Coord(
                start.x + (goal.x - start.x) / Math.abs(goal.x - start.x),
                start.y + (goal.y - start.y) / Math.abs(goal.y - start.y)
        );
    }
}
