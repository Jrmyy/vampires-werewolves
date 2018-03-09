package algorithm;

import board.Board;
import board.Position;
import utils.Utils;

import java.util.ArrayList;

public class AlphaBeta {

    private Node root;

    private Result bestMove = new Result();

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
            return heuristic(currentBoard);
        }

        ArrayList<Node> impliedBoards = node.createAlternatives();

        if (currentBoard.getCurrentPlayer() == currentBoard.getUs()) {

            for (Node child : impliedBoards) {
                double temp = alphaBeta(child, depth - 1, alpha, beta);
                if (alpha < temp) {
                    alpha = temp;
                    if (node == root) {
                        bestMove = child.getLastMove();
                    }
                }

                if (beta <= alpha) {
                    return alpha;
                }
            }
            return alpha;
        } else {

            for (Node child : impliedBoards) {
                double temp = alphaBeta(child, depth - 1, alpha, beta);
                if (beta > temp) {
                    beta = temp;
                    if (node == root) {
                        bestMove = child.getLastMove();
                    }
                }
                if(beta <= alpha) {
                    return beta;
                }
            }
            return beta;
        }
    }
    

    /**
     * Fonction heuristique pour évaluer une situation donnée de la carte
     * @param map
     * @return
     */
    private static double heuristic(Board map) {
        int minDistance;
        int nbHumans;
        int nbAllies;
        int nbOpponents;
        double score = 0;
        // Nombre d'alliés
        for (Position position : map.getAllies()) {
            score += map.getCells()[position.getX()][position.getY()].getPopulation();
        }
        // Nombre d'ennemis
        for (Position position : map.getOpponents()) {
            score -= map.getCells()[position.getX()][position.getY()].getPopulation();
        }
        // Nombre d'humains avec min distance d'un groupe pouvant le convertir
        for (Position positionHumans : map.getHumans()) {
            nbHumans = map.getCells()[positionHumans.getX()][positionHumans.getY()].getPopulation();
            // Alliés
            minDistance = map.getCols() + map.getRows();
            for (Position positionAllies : map.getAllies()) {
                nbAllies = map.getCells()[positionAllies.getX()][positionAllies.getY()].getPopulation();
                if (nbAllies > nbHumans) {
                    minDistance = Math.min(minDistance, Utils.minDistance(positionHumans, positionAllies));
                }
            }
            score += nbHumans / minDistance;
            // Ennemis
            minDistance = map.getCols() + map.getRows();
            for (Position positionOpponents : map.getOpponents()) {
                nbAllies = map.getCells()[positionOpponents.getX()][positionOpponents.getY()].getPopulation();
                if (nbAllies > nbHumans) {
                    minDistance = Math.min(minDistance, Utils.minDistance(positionHumans, positionOpponents));
                }
            }
            score -= nbHumans / minDistance;
        }
        // Ennemis à proximité
        // TODO

        return score;
    }


}
