package algorithm;

import board.Board;
import board.Position;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class MinMax {

    private Node root;

    private Result bestMove = new Result();

    /**
     * Creates MinMax with root of a search tree
     * @param rootBoard Board at the beginning of the algorithm
     */
    public MinMax(Board rootBoard) throws IOException {
        root = new Node(rootBoard);
    }


    /**
     * Runs minimax algorithm up to given depth
     *
     * @return Point found in search tree
     */
    public Result algorithm(int depth){
        minMax(root, depth);
        root.logger.info("Best move chosen is " + bestMove);
        return bestMove;
    }

    private double minMax(Node node, int depth) {

        Board currentBoard = node.getBoard();

        if (depth == 0) {
            return heuristic(node);
        }

        ArrayList<Node> impliedBoards = node.createAlternatives();

        if (currentBoard.getCurrentPlayer() == currentBoard.getUs()) {
            double bestValue = Double.NEGATIVE_INFINITY;
            for (Node child : impliedBoards) {
                double temp = minMax(child, depth - 1);
                if (temp > bestValue) {
                    bestValue = temp;
                    if (node == root) {
                        root.logger.info("Heuristic for max for best move is " + bestValue);
                        bestMove = child.getAllyMoves().get(0);
                    }
                }
            }
            return bestValue;
        } else {
            double bestValue = Double.POSITIVE_INFINITY;
            for (Node child : impliedBoards) {
                double temp = minMax(child, depth - 1);
                bestValue = Math.min(temp, bestValue);
            }
            return bestValue;
        }
    }

    /**
     * Fonction heuristique pour évaluer une situation donnée de la carte
     * @param node
     * @return
     */
    private static double heuristic(Node node) {

        Board map = node.getBoard();

        if (map.getAllies().size() == 0) {
            return -2 * map.opponentsPopulation() - map.humansPopulation();
        }

        if (map.getOpponents().size() == 0) {
            return 2 * map.alliesPopulation() + map.humansPopulation();
        }

        double score = 2 * map.alliesPopulation() - map.opponentsPopulation();

        for (Position human : map.getHumans()) {
            int humanPop = map.getCells()[human.getX()][human.getY()].getPopulation();

            double minDistAlly = Double.POSITIVE_INFINITY;
            for (Position ally: map.getAllies()) {
                int allyPop = map.getCells()[ally.getX()][ally.getY()].getPopulation();
                double temp = Utils.minDistance(ally, human);
                if (temp < minDistAlly && allyPop >= humanPop) {
                    minDistAlly = temp;
                }
            }

            double minDistOpponent = Double.POSITIVE_INFINITY;
            for (Position opp: map.getOpponents()) {
                int OpponentPop = map.getCells()[opp.getX()][opp.getY()].getPopulation();
                double temp = Utils.minDistance(opp, human);
                if (temp < minDistAlly && OpponentPop >= humanPop) {
                    minDistOpponent = temp;
                }
            }

            if ((minDistAlly < minDistOpponent) || (minDistAlly == minDistOpponent && map.getCurrentPlayer().equals(map.getUs()))) {
                score += (double) humanPop / Math.max(1, minDistAlly);
            } else {
                score -= (double) humanPop / Math.max(1, minDistOpponent);
            }

        }

        for (Position ally: map.getAllies()) {
            double minDistance = Double.POSITIVE_INFINITY;
            Position opponent = null;
            for (Position opp: map.getOpponents()) {
                double temp = Utils.minDistance(ally, opp);
                if (temp < minDistance) {
                    minDistance = temp;
                    opponent = opp;
                }
            }

            if (minDistance <= 2 || (minDistance == 1 && map.getCurrentPlayer().equals(map.getUs()))) {
                int allyPop = map.getCells()[ally.getX()][ally.getY()].getPopulation();
                int opponentPop = map.getCells()[opponent.getX()][opponent.getY()].getPopulation();

                if (allyPop > 1.5 * opponentPop) {
                    score += opponentPop / Math.max(1, minDistance);
                } else if (1.5 * allyPop < opponentPop) {
                    score -= allyPop / Math.max(1, minDistance);
                } else {
                    double p = allyPop / (2 * opponentPop);
                    score += Math.pow(p, 2) * allyPop / Math.max(1, minDistance)
                            - Math.pow(1 - p, 2) * opponentPop / Math.max(1, minDistance);
                }
            }
        }

        score += 2 * node.getHumansEaten();
        score -= 2 * node.getHumansEatenByOpponent();

        node.logger.info("Heuristic for move " + node.getAllyMoves().get(0) + " is " + score);

        return score;

    }


}

