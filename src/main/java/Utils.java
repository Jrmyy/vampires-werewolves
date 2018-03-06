import java.util.ArrayList;

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
    public static Coord findNextMove(MapManager map, Coord start, Coord goal) {

        if (goal.equals(start)) {
            return start;
        }

        ArrayList<Coord> adjacentCells = findAdjacentCells(map.cols, map.rows, start);
        int minDistance = Integer.MAX_VALUE;
        Coord bestMove = null;
        for (Coord adj: adjacentCells) {
            Cell startCell = map.map[start.x][start.y];
            Cell adjCell = map.map[adj.x][adj.y];
            if (adjCell.kind.equals(map.race)) {
                if (Utils.minDistance(adj, goal) + 1 <= minDistance) {
                    bestMove = adj;
                    minDistance = Utils.minDistance(adj, goal) + 1;
                }
            } else if (adjCell.kind.equals("humans") && adjCell.population <= startCell.population) {
                if (Utils.minDistance(adj, goal) + 1 <= minDistance) {
                    bestMove = adj;
                    minDistance = Utils.minDistance(adj, goal) + 1;
                }
            } else if (1.5 * adjCell.population <= startCell.population) {
                if (Utils.minDistance(adj, goal) + 1 <= minDistance) {
                    bestMove = adj;
                    minDistance = Utils.minDistance(adj, goal) + 1;
                }
            }
        }
        return bestMove;
    }

    private static ArrayList<Coord> findAdjacentCells(Integer cols, Integer rows, Coord start) {

        ArrayList<Coord> adjacentCells = new ArrayList<>();

        // Nord
        if (start.y - 1 >= 0) {
            adjacentCells.add(new Coord(start.x, start.y - 1));
        }

        // Ouest
        if (start.x - 1 >= 0) {
            adjacentCells.add(new Coord(start.x - 1, start.y));
        }

        // Est
        if (start.x + 1 <= cols - 1) {
            adjacentCells.add(new Coord(start.x + 1, start.y));
        }

        // Sud
        if (start.y + 1 <= rows - 1) {
            adjacentCells.add(new Coord(start.x, start.y + 1));
        }

        // Nord ouest
        if (start.y - 1 >= 0 && start.x - 1 >= 0) {
            adjacentCells.add(new Coord(start.x - 1, start.y - 1));
        }

        // Nord est
        if (start.y - 1 >= 0 && start.x + 1 <= cols - 1) {
            adjacentCells.add(new Coord(start.x + 1, start.y - 1));
        }

        // Sud ouest
        if (start.y + 1 <= rows - 1 && start.x - 1 >= 0) {
            adjacentCells.add(new Coord(start.x - 1, start.y + 1));
        }

        // Sud est
        if (start.y + 1 <= rows - 1 && start.x + 1 <= cols - 1) {
            adjacentCells.add(new Coord(start.x + 1, start.y + 1));
        }

        return adjacentCells;

    }
}
