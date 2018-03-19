package utils;

import algorithm.Result;
import board.Board;
import board.Cell;
import board.Position;
import java.util.*;

/**
 * Quelques fonctions utils
 */
public class Utils {

    /**
     * Calcule la distance minimum entre deux positiononées sur la carte
     * @param a
     * @param b
     * @return
     */
    public static int minDistance(Position a, Position b) {
        return Math.max(Math.abs(b.getX() - a.getX()), Math.abs(b.getY() - a.getY()));
    }

    public static ArrayList<Result> dropDuplicates(ArrayList<Result> undeduped) {
        Set<Result> listWithoutDuplicates = new LinkedHashSet<>(undeduped);
        undeduped.clear();
        undeduped.addAll(listWithoutDuplicates);
        return undeduped;
    }

    /**
     * Avec une position de départ et une position d'arrivée, calcule sur quelle case aller au prochain mouvement
     * @param start
     * @param goal
     * @return
     */
    public static Position findNextMove(Board map, Position start, Position goal, int itemsMoved) {

        if (goal.equals(start)) {
            return start;
        }

        ArrayList<Position> adjacentCells = findAdjacentCells(map.getCols(), map.getRows(), start);
        int minDistance = Integer.MAX_VALUE;
        Position bestMove = null;

        if (adjacentCells.contains(goal)) {
            return goal;
        }

        for (Position adj: adjacentCells) {
            Cell adjCell = map.getCells()[adj.getX()][adj.getY()];
            if (adjCell.getKind().equals(map.getUs().getRace())) {
                if (Utils.minDistance(adj, goal) < minDistance) {
                    bestMove = adj;
                    minDistance = Utils.minDistance(adj, goal) + 1;
                }
            } else if (adjCell.getKind().equals("humans") && adjCell.getPopulation() <= itemsMoved) {
                if (Utils.minDistance(adj, goal) < minDistance) {
                    bestMove = adj;
                    minDistance = Utils.minDistance(adj, goal) + 1;
                }
            } else if (adjCell.getKind().equals(map.getOpponent().getRace()) && 1.5 * adjCell.getPopulation() < itemsMoved) {
                if (Utils.minDistance(adj, goal) < minDistance) {
                    bestMove = adj;
                    minDistance = Utils.minDistance(adj, goal) + 1;
                }
            }
        }
        return bestMove;
    }

    private static ArrayList<Position> findAdjacentCells(Integer cols, Integer rows, Position start) {

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
