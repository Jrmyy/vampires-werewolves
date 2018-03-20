package algorithm;

import board.Board;
import java.io.IOException;
import java.util.ArrayList;

public class MinMax extends Algorithm{

    /**
     * Creates MinMax with root of a search tree
     *
     * @param rootBoard Board at the beginning of the algorithm
     */
    public MinMax(Board rootBoard) throws IOException {
        super(rootBoard);
    }

    /**
     * Runs minimax algorithm up to given depth
     *
     * @return Point found in search tree
     */
    @Override
    public ArrayList<Result> algorithm(int depth){
        minMax(root, depth);
        Node.logger.info("Best moves chosen are " + bestMoves);
        return bestMoves;
    }


    private double minMax(Node node, int depth) {

        Board currentBoard = node.getBoard();

        if (depth == 0) {
            return node.heuristic();
        }

        ArrayList<Node> impliedBoards = node.createAlternatives();

        if (currentBoard.getCurrentPlayer().equals(currentBoard.getUs())) {
            double bestValue = Double.NEGATIVE_INFINITY;
            for (Node child : impliedBoards) {
                double temp = minMax(child, depth - 1);
                if (temp > bestValue) {
                    bestValue = temp;
                    if (node == root) {
                        Node.logger.info("Heuristic for max for best move is " + bestValue);
                        bestMoves = child.getAllyMoves().get(0);
                    }
                }
            }
            return bestValue;
        } else {
            double bestValue = Double.POSITIVE_INFINITY;
            for (Node child : impliedBoards) {
                bestValue = Math.min(minMax(child, depth - 1), bestValue);
            }
            return bestValue;
        }
    }

}

