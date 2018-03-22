package utils;

import board.Board;
import board.Cell;
import board.Position;
import java.util.*;

/**
 * Quelques fonctions utils
 */
public class Utils {

    /**
     * Calcule la distance minimum entre deux positions sur la carte
     * @param a
     * @param b
     * @return
     */
    public static int minDistance(Position a, Position b) {
        return Math.max(Math.abs(b.getX() - a.getX()), Math.abs(b.getY() - a.getY()));
    }

    /**
     * Avec une position de départ et une position d'arrivée sur une carte donnée, calcule sur quelle case aller au
     * prochain mouvement
     * @param start
     * @param goal
     * @return
     */
    public static Position findNextMove(Board map, Position start, Position goal, int itemsMoved) {

        // Si la destination est égale au départ, on retourne le départ
        if (goal.equals(start)) {
            return start;
        }

        ArrayList<Position> adjacentCells = findAdjacentCells(map.getCols(), map.getRows(), start);

        // Si la destination est sur une case adjacente, on y va directement également
        if (adjacentCells.contains(goal)) {
            return goal;
        }

        // Le but va être désormais de se déplacer sur une case adjacente qui va minimiser la distance par rapport à
        // l'arrivée et à distance égale on va privilégier de se déplacer vers des humains.
        int minDistance = Integer.MAX_VALUE;
        Position bestMove = null;
        // Donner un poids aux cases sur lesquelles on se déplace
        HashMap<String, Double> bestMoveKindScore = new HashMap<String, Double>() {{
            put("empty", 0.5);
            put(map.getUs().getRace(), 0.0);
            put(map.getOpponent().getRace(), 1.0);
            put("humans", 1.5);
        }};
        String bestMoveKind = "empty";

        for (Position adj: adjacentCells) {
            Cell adjCell = map.getCells()[adj.getX()][adj.getY()];
            /**
             * En gros:
             * - Si la case adjacente est vide
             * - Si la case adjacente est de notre espèce
             * - Si la case adjacente est humaine et son nombre est inférieure ou égale au nombre d'éléments bougés
             * - Si la case adjacente est de l'espèce adverse et 1.5 fois son nombre est strictement inférieur au nombre d'éléments bougés
             *
             * On regarde si la 1 + la distance min de la case adjacente à l'objectif est plus petite que la distance minimale déjà trouvée
             * On regarde également, à distance égale, si le score est plus important
             */
            if (adjCell.getKind().equals("empty")
                || adjCell.getKind().equals(map.getUs().getRace())
                || adjCell.getKind().equals("humans") && adjCell.getPopulation() <= itemsMoved
                || adjCell.getKind().equals(map.getOpponent().getRace()) && 1.5 * adjCell.getPopulation() < itemsMoved) {
                if (
                        Utils.minDistance(adj, goal) + 1 < minDistance
                    || (Utils.minDistance(adj, goal) + 1 == minDistance
                                && bestMoveKindScore.get(bestMoveKind) < bestMoveKindScore.get(adjCell.getKind()))
                    ) {
                    bestMove = adj;
                    bestMoveKind = adjCell.getKind();
                    minDistance = Utils.minDistance(adj, goal) + 1;
                }
            }
        }

        // On retourne le meilleur mouvement trouvé à la fin
        return bestMove;
    }

    /**
     * Retourne la liste des cases adjacentes à une position selon une grille donnée
     * @param cols : Nombre de colonnes de la grille
     * @param rows : Nombre de lignes de la grille
     * @param start : Position de départ
     * @return
     */
    public static ArrayList<Position> findAdjacentCells(Integer cols, Integer rows, Position start) {

        ArrayList<Position> adjacentCells = new ArrayList<>();

        // Nord
        if (start.getY() - 1 >= 0) {
            adjacentCells.add(new Position(start.getX(), start.getY() - 1));
        }

        // Ouest
        if (start.getX() - 1 >= 0) {
            adjacentCells.add(new Position(start.getX() - 1, start.getY()));
        }

        // Est
        if (start.getX() + 1 <= cols - 1) {
            adjacentCells.add(new Position(start.getX() + 1, start.getY()));
        }

        // Sud
        if (start.getY() + 1 <= rows - 1) {
            adjacentCells.add(new Position(start.getX(), start.getY() + 1));
        }

        // Nord ouest
        if (start.getY() - 1 >= 0 && start.getX() - 1 >= 0) {
            adjacentCells.add(new Position(start.getX() - 1, start.getY() - 1));
        }

        // Nord est
        if (start.getY() - 1 >= 0 && start.getX() + 1 <= cols - 1) {
            adjacentCells.add(new Position(start.getX() + 1, start.getY() - 1));
        }

        // Sud ouest
        if (start.getY() + 1 <= rows - 1 && start.getX() - 1 >= 0) {
            adjacentCells.add(new Position(start.getX() - 1, start.getY() + 1));
        }

        // Sud est
        if (start.getY() + 1 <= rows - 1 && start.getX() + 1 <= cols - 1) {
            adjacentCells.add(new Position(start.getX() + 1, start.getY() + 1));
        }

        return adjacentCells;

    }
}
