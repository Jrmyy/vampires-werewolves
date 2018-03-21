package algorithm;

import board.Board;
import java.util.ArrayList;


public class AlphaBeta extends Algorithm {

    public AlphaBeta(Board rootBoard) {
        super(rootBoard);
    }

    /**
     * Algorithme alpha beta
     * @param depth
     * @return
     */
    @Override
    public ArrayList<Result> algorithm(int depth) {
        // On lance la logique à la profondeur de départ, sur le noeud racine avec alpha à -inf et beta à +inf
        alphaBeta(root, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Node.logger.info("Best moves chosen are " + bestMoves);
        // On retourne les meilleurs mouvements
        return bestMoves;
    }


    /**
     * Lance alphaBeta à une certaine profondeur
     *
     * @return la valeur de alpha ou beta selon le joueur courant
     */

    private double alphaBeta(Node node, int depth, double alpha, double beta) {

        // Si la profondeur est égale à 0, c'est à dire que l'on atteint la profondeur max de notre arbre, on évalue le noeud
        if (depth == 0) {
            return node.heuristic();
        }

        // Sinon on crée les branches du noeud courants (toutes les possibilités induites par ce noeud)
        ArrayList<Node> children = node.createAlternatives();

        // Si c'est à nous de jouer, on va influer sur le alpha
        if (node.getBoard().getCurrentPlayer().equals(node.getBoard().getUs())) {
            for (Node child: children) {
                // Pour chaque enfant du noeud courant, on va appliquer alpha beta à une profondeur - 1
                double temp = alphaBeta(child, depth - 1, alpha, beta);
                // Si la valeur est strictement plus grande que alpha
                if (temp > alpha) {
                    // Si le noeud est la racine, c'est qu'on est remonté en haut, et donc on assigne les mouvements à faire
                    if (node.equals(root)) {
                        this.bestMoves = child.getAllyMoves().get(0);
                    }
                    // Dans tous les cas, alpha = max(alpha, temp)
                    alpha = temp;
                }

                // Si beta <= alpha, on arrête l'étude de la branche, on élague
                if (beta <= alpha) {
                    break;
                }
            }
            // On retourne le alpha déjà instantié
            return alpha;
        } else {
            // Si c'est à l'adversaire de jouer
            for (Node child: children) {
                // Beta = min(beta, alphabeta au niveau inférieur)
                beta = Math.min(beta, alphaBeta(child, depth - 1, alpha, beta));
                // Si beta est plus petit que alpha on élague
                if (beta <= alpha) {
                    break;
                }
            }
            // On retourne beta à la fin
            return beta;
        }
    }
}
