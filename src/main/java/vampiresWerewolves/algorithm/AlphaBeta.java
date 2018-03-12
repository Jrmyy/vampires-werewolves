package algorithm;

import board.Board;
import board.Cell;
import board.Position;
import utils.Utils;
import java.util.ArrayList;

public class AlphaBeta {

    private Node root;

    private Result bestMove = new Result();

    // Gain pour manger un groupe d'humain adjacent à nous
    private static final double HUMAN_TRANSFORMED_ADJACENT_GAIN = 0.01;

    /**
     * Creates AlphaBeta with root of a search tree
     * @param rootBoard Board at the beginning of the algorithm
     */
    public AlphaBeta(Board rootBoard){
        root = new Node(rootBoard);
    }


    /**
     * Runs alpha-beta algorithm up to given depth
     *
     * @return Point found in search tree
     */
    public Result algorithm(int depth){
        alphaBeta(root, depth, -1000000000, 1000000000);
        System.out.println(bestMove);
        return bestMove;
    }

    private double alphaBeta(Node node, int depth, double alpha, double beta) {

        Board currentBoard = node.getBoard();

        if (depth == 0) {
            return heuristic(node);
        }

        ArrayList<Node> impliedBoards = node.createAlternatives();

        if (currentBoard.getCurrentPlayer() == currentBoard.getUs()) {

            for (Node child : impliedBoards) {
                double temp = alphaBeta(child, depth - 1, alpha, beta);
                if (temp > alpha) {
                    alpha = temp;
                    if (node == root) {
                        bestMove = child.getLastMove();
                    }
                }

                if (alpha > beta) {
                    return beta;
                }
            }
            return alpha;
        } else {

            for (Node child : impliedBoards) {
                double temp = alphaBeta(child, depth - 1, alpha, beta);
                if (temp < beta) {
                    beta = temp;
                    if (node == root) {
                        bestMove = child.getLastMove();
                    }
                }
                if (alpha >= beta) {
                    return beta;
                }
            }

            return beta;
        }
    }
    

    /**
     * Fonction heuristique pour évaluer une situation donnée de la carte
     * @param node
     * @return
     */
    private static double heuristic(Node node) {

        System.out.println("Starting heuristic for move " + node.getLastMove());

        Board map = node.getBoard();

        double score = map.alliesPopulation() - map.opponentsPopulation();

        double humanAllyScore = 0;
        double adjToKillEnemy = 0;
        for (Position ally: map.getAllies()) {
            // Si on a des ennemis à proximité que l'on peut tuer à coup sur, on doit absolument jouer ce coup
            Cell allyCell = map.getCells()[ally.getX()][ally.getY()];
            for (Position adj: Utils.findAdjacentCells(map.getCols(), map.getRows(), ally)) {
                Cell adjCell = map.getCells()[adj.getX()][adj.getY()];
                if (adjCell.getKind().equals(map.getOpponent().getRace())
                        && 1.5 * adjCell.getPopulation() < allyCell.getPopulation()) {
                    adjToKillEnemy += 100000;
                }
            }

            // On va maintenant viser, tant que l'on peut gagner le match à coup sur les ennemis présentant le meilleur
            // ratio nb humains / distance
            double bestRatio = 0;
            if (node.getHumansEaten() > 0) {
                // A ratios égaux, on veut manger l'élément que l'on peut manger directement
                bestRatio = node.getHumansEaten() + HUMAN_TRANSFORMED_ADJACENT_GAIN;
            }
            for (Position human: map.getHumans()) {
                if (map.getCells()[ally.getX()][ally.getY()].getPopulation()
                        > map.getCells()[human.getX()][human.getY()].getPopulation()) {
                    double humanPop = (double) map.getCells()[human.getX()][human.getY()].getPopulation();
                    bestRatio = Math.max(bestRatio, humanPop / (double) Utils.minDistance(ally, human));
                }
            }
            humanAllyScore += bestRatio;
        }

        score += humanAllyScore;
        score += adjToKillEnemy;

        System.out.println("Heuristic for move " + node.getLastMove() + " is : " + score);
        return score;

    }


}

