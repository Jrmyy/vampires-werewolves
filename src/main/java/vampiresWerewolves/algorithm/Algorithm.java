package algorithm;

import board.Board;

import java.util.ArrayList;

abstract class Algorithm {

    Node root;

    ArrayList<Result> bestMoves = new ArrayList<>();

    /**
     * Créer un algorithme avec comme noeud racine une carte
     * @param rootBoard
     */
    Algorithm(Board rootBoard) {
        root = new Node(rootBoard);
    }

    /**
     * Logique de l'algorithme avec une profondeur donnée
     * @param depth
     * @return
     */
    public abstract ArrayList<Result> algorithm (int depth);

}
