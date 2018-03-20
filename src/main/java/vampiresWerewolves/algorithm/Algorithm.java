package algorithm;

import board.Board;

import java.io.IOException;
import java.util.ArrayList;

abstract class Algorithm {

    Node root;

    ArrayList<Result> bestMoves = new ArrayList<>();

    /**
     * Creates algorithm with root of a search tree
     * @param rootBoard Board at the beginning of the algorithm
     */
    Algorithm(Board rootBoard) throws IOException {
        root = new Node(rootBoard);
    }

    public abstract ArrayList<Result> algorithm (int depth);

}
