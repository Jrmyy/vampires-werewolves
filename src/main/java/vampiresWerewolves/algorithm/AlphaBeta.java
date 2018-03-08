package algorithm;

import board.Board;
import board.Position;

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
     * Fonction heuristique pour un seul groupe de crétures
     * @param map
     * @param position
     * @return
     */
    private static double localHeuristic(Board map, Position position) {
        int nbCreatures;
        int nbHumans;
        int distance;
        double proba;
        double nbConverted; // Nombre d'humain convertis espérés (avec prise en compte des pertes subies)
        // Facteurs à déterminer expérimentalement
        double a = 1; // Importance dans le score des créatures effectives dans chaque camp
        double b = 1; // Importance dans le score des humains pouvant être convertis
        double phi = 0.8; // Facteur de décroissance pour accorder moins d'importance aux humains éloignés
        double score = 0;
        // Score des créatures alliées sur la case
        nbCreatures = map.getCells()[position.getX()][position.getY()].getPopulation();
        score += a * nbCreatures;
        for (Position hPosition : map.getHumans()) {
            // Score des humains à distance des créatures alliées
            nbHumans = map.getCells()[hPosition.getX()][hPosition.getY()].getPopulation();
            distance = Math.max(Math.abs(
                    position.getX() - hPosition.getX()), Math.abs(position.getY() - hPosition.getY()
            ));
            if (nbCreatures >= nbHumans){
                nbConverted = nbHumans;
            } else {
                proba = nbCreatures / (2 * nbHumans);
                nbConverted = proba * (proba * nbHumans - (1 - proba) * nbCreatures) - (1 - proba) * nbCreatures;
            }
            score += b * Math.pow(phi, distance - 1) * nbConverted;
        }
        return score;
    }

    /**
     * Fonction heuristique pour évaluer une situation donnée de la carte
     * @param map
     * @return
     */
    private static double heuristic(Board map) {
        int nbCreatures;
        int nbOpponents;
        int distance;
        // Facteurs à déterminer expérimentalement
        double c = 1; // Importance dans le score des attaques entre les deux équipes
        double phi = 0.8; // Facteur de décroissance pour accorder moins d'importance aux ennemis éloignés
        double proba;
        double battleGain; // Différence des pertes ennemies par nos pertes
        double score = 0;
        // Fonction heuristique locale pour chaque case alliée
        for (Position position : map.getAllies()) {
            score += AlphaBeta.localHeuristic(map, position);
        }
        // Fonction heuristique locale pour chaque case ennemi
        for (Position opponentPosition : map.getOpponents()) {
            score -= AlphaBeta.localHeuristic(map, opponentPosition);
        }
        // Fonction heuristique de rapport de force entre les cases alliés-ennemies
        for (Position position : map.getAllies()) {
            nbCreatures = map.getCells()[position.getX()][position.getY()].getPopulation();
            for (Position opponentPosition : map.getOpponents()) {
                nbOpponents = map.getCells()[opponentPosition.getX()][opponentPosition.getY()].getPopulation();
                distance = Math.max(
                        Math.abs(position.getX() - opponentPosition.getY()),
                        Math.abs(position.getY() - opponentPosition.getY())
                );
                if (nbCreatures >= 1.5 * nbOpponents) {
                    // Cas de victoire sûre
                    battleGain = nbOpponents;
                } else if (nbOpponents >= 1.5 * nbCreatures) {
                    // Cas de défaite sûre
                    battleGain = - nbCreatures;
                } else {
                    // Cas de bataille
                    if (nbCreatures > nbOpponents) {
                        proba = (nbCreatures / nbOpponents) - 0.5;
                    } else {
                        proba = nbCreatures / (2 * nbOpponents);
                    }
                    battleGain = proba * (nbOpponents - (1 - proba) * nbCreatures)
                            + (1 - proba) * (proba * nbOpponents - nbCreatures);
                }
                score += c * Math.pow(phi, distance - 1) * battleGain;
            }
        }
        return score;
    }


}
