package algorithm;

import board.Board;
import java.util.ArrayList;

public class MinMax extends Algorithm{

    public MinMax(Board rootBoard) {
        super(rootBoard);
    }

    /**
     * Algorithme alpha beta
     * @param depth
     * @return
     */
    @Override
    public ArrayList<Result> algorithm(int depth){
        // On lance minmax avec comme noeud courant le noeud racine
        minMax(root, depth);
        // Node.logger.info("Best moves chosen are " + bestMoves);
        // On retourne les meilleurs mouvements
        return bestMoves;
    }


    /**
     * Algorithme minMax à une profondeur donnée pour un noeud donné, retourne la valeur de l'heuristique
     * @param node
     * @param depth
     * @return
     */
    private double minMax(Node node, int depth) {

        Board currentBoard = node.getBoard();

        // Si on atteint la profondeur maximale, on évalue le noeud
        if (depth == 0) {
            return node.heuristic();
        }

        // On crée les branches enfantes de la branche source
        ArrayList<Node> impliedBoards = node.createAlternatives();

        // Si c'est à nous de jouer, on veut maximiser le score
        if (currentBoard.getCurrentPlayer().equals(currentBoard.getUs())) {
            double bestValue = Double.NEGATIVE_INFINITY;
            for (Node child : impliedBoards) {
                double temp = minMax(child, depth - 1);
                if (temp > bestValue) {
                    bestValue = temp;
                    // Si on est à la racine, on assigne les valeurs des mouvements à faire
                    if (node == root) {
                        // Node.logger.info("Heuristic for max for best move is " + bestValue);
                        bestMoves = child.getAllyMoves().get(0);
                    }
                }
            }
            return bestValue;
        } else {
            // Si c'est à l'adversaire de jouer, on minimise le score
            double bestValue = Double.POSITIVE_INFINITY;
            for (Node child : impliedBoards) {
                bestValue = Math.min(minMax(child, depth - 1), bestValue);
            }
            return bestValue;
        }
    }

}

