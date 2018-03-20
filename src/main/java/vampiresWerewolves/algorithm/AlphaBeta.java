package algorithm;

import board.Board;
import java.io.IOException;
import java.util.ArrayList;


public class AlphaBeta extends Algorithm {

    /**
     * Creates AlphaBeta with root of a search tree
     *
     * @param rootBoard Board at the beginning of the algorithm
     */
    public AlphaBeta(Board rootBoard) throws IOException {
        super(rootBoard);
    }

    @Override
    public ArrayList<Result> algorithm(int depth) {
        alphaBeta(root, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Node.logger.info("Best moves chosen are " + bestMoves);
        return bestMoves;
    }


    /**
     * Runs alphaBeta algorithm up to given depth
     *
     * @return Point found in search tree
     */

    private double alphaBeta(Node node, int depth, double alpha, double beta) {

        if (depth == 0) {
            return node.heuristic();
        }

        ArrayList<Node> children = node.createAlternatives();

        if (node.getBoard().getCurrentPlayer().equals(node.getBoard().getUs())) {
            for (Node child: children) {
                double temp = alphaBeta(child, depth - 1, alpha, beta);
                if (temp > alpha) {
                    this.bestMoves = child.getAllyMoves().get(0);
                    alpha = temp;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return alpha;
        } else {
            for (Node child: children) {
                beta = Math.min(beta, alphaBeta(child, depth - 1, alpha, beta));
                if (beta <= alpha) {
                    break;
                }
            }
            return beta;
        }
    }
}
