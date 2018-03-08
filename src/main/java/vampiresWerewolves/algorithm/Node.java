package algorithm;

import board.Board;
import board.Cell;
import board.Position;
import utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class Node {

    private Board board;

    private Result lastMove;

    private static final String[] MOVEMENT_TYPES = {"attack", "transform", "escape"};

    Node(Board board) {
        this.board = board;
        this.lastMove = new Result();
    }

    private Node(Board board, Result lastMove) {
        this.board = board;
        this.lastMove = lastMove;
    }

    public ArrayList<Node> createAlternatives() {
        ArrayList<Node> alternatives = new ArrayList<>();
        if (this.board.getCurrentPlayer().equals(this.board.getUs())) {
            for(Position pos: this.board.getAllies()) {
                ArrayList<Position> futureMoves = new ArrayList<>();
                for (String strategy: MOVEMENT_TYPES) {
                    futureMoves.addAll(this.findMoves(this.board, pos, strategy, 3));
                }
                System.out.println("Moves are " + futureMoves);
                for (Position to: futureMoves) {
                    Board impliedBoard = this.board.simulateMove(pos, to);
                    alternatives.add(
                            new Node(
                                    impliedBoard,
                                    new Result(pos, this.board.getCells()[pos.getX()][pos.getY()].getPopulation(), to)
                            )
                    );
                }
            }
        } else {
            for(Position pos: this.board.getOpponents()) {
                ArrayList<Position> futureMoves = new ArrayList<>();
                for (String strategy: MOVEMENT_TYPES) {
                    futureMoves.addAll(this.findMoves(this.board, pos, strategy, 3));
                }
                System.out.println("Moves are " + futureMoves);
                for (Position to: futureMoves) {
                    Board impliedBoard = this.board.simulateMove(pos, to);
                    alternatives.add(
                            new Node(
                                    impliedBoard,
                                    new Result(pos, this.board.getCells()[pos.getX()][pos.getY()].getPopulation(), to)
                            )
                    );
                }
            }
        }
        return alternatives;
    }

    private ArrayList<Position> findMoves(Board board, Position currentPosition, String strategy, int maxElements) {
        ArrayList<Position> moves = new ArrayList<>();
        Cell positionCell = board.getCells()[currentPosition.getX()][currentPosition.getY()];
        int maxDistance = -1;
        System.out.println("Finding best move for strategy " + strategy + " and position " + currentPosition + " " +
                "with population " + positionCell.getPopulation());
        switch (strategy) {
            case "attack":
                // Dans le cas d'une attaque, le meilleur coup sera d'attaquer le groupe le plus proche avec le plus
                // grand nombre d'adversaires mais dont leur nombre <= 1.5*la taille de notre groupe
                for (Position opp: board.getOpponents()) {
                    Cell oCell = board.getCells()[opp.getX()][opp.getY()];
                    if (1.5 * oCell.getPopulation() < positionCell.getPopulation()) {
                        // On ajoute que si elle est plus proche que la plus lointaine des solutions si leur nombre
                        // dépasse maxElements
                        Position nextMoveToMake = Utils.findNextMove(board, currentPosition, opp);
                        maxDistance =addOrNotMovePosition(currentPosition, maxElements, moves, maxDistance, nextMoveToMake);
                    }
                }
                return moves;
            case "transform":
                // Dans le cas d'une transformation, on va essayer de récupérer le plus grand nombre d'humains le plus
                // proche avec comme contrainte que le nombre d'humain doit être inférieur ou égal à la population de
                // notre position
                for (Position human: board.getHumans()) {
                    Cell hCell = board.getCells()[human.getX()][human.getY()];
                    if (hCell.getPopulation() < positionCell.getPopulation()) {
                        // On ajoute que si elle est plus proche que la plus lointaine des solutions si leur nombre
                        // dépasse maxElements
                        Position nextMoveToMake = Utils.findNextMove(board, currentPosition, human);
                        maxDistance = addOrNotMovePosition(currentPosition, maxElements, moves, maxDistance, nextMoveToMake);
                    }
                }
                return moves;
            case "escape":
                // TODO
                ArrayList<Position> positionsToEscape = Utils.findAdjacentCells(board.getCols(), board.getRows(), currentPosition);
                ArrayList<Position> unsafeAdjacentPositions = new ArrayList<>();
                for (Position escape: positionsToEscape) {
                    if (!board.getCells()[escape.getX()][escape.getY()].getKind().equals("empty")) {
                        unsafeAdjacentPositions.add(escape);
                    }
                }
                positionsToEscape.removeAll(unsafeAdjacentPositions);
                ArrayList<Position> dangerousPositions = new ArrayList<>();
                for (Position opp: board.getOpponents()) {
                    Cell oCell = board.getCells()[opp.getX()][opp.getY()];
                    if (oCell.getPopulation() > positionCell.getPopulation()) {
                        dangerousPositions.add(opp);
                    }
                }
                double minScore = 1000000;
                Position bestMove = null;
                for (Position escape : positionsToEscape) {
                    double score = 0;
                    int minDistance = 10000;
                    for (Position danger: dangerousPositions) {
                        score += board.getCells()[danger.getX()][danger.getY()].getPopulation() / Utils.minDistance(escape, danger);
                        if (Utils.minDistance(escape, danger) < minDistance) {
                            minDistance = Utils.minDistance(escape, danger);
                        }
                    }
                    if (score < minScore && minDistance <= 3) {
                        minScore = score;
                        bestMove = escape;
                    }
                }
                if (bestMove != null) {
                    return new ArrayList<>(Collections.singleton(bestMove));
                } else {
                    return new ArrayList<>();
                }
            default: return new ArrayList<>();
        }
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Result getLastMove() {
        return lastMove;
    }

    private int addOrNotMovePosition(Position position, int maxResult, ArrayList<Position> moves,
                                            int maxDistance, Position to) {
        // Si la taille maximal est atteinte
        if (!moves.contains(to)) {
            if (moves.size() == maxResult) {
                int newMaxDistance = -1;
                // Si la distance de la position que l'on veut ajouter est plus petite que la distance max on va chercher
                // l'élément qui répond à cette distance, le supprimer et mettre l'élément à la place
                if (Utils.minDistance(position, to) < maxDistance) {
                    ArrayList<Position> toRemove = new ArrayList<>();
                    for (Position movePosition: moves) {
                        if (Utils.minDistance(movePosition, position) == maxDistance) {
                            toRemove.add(movePosition);
                        } else {
                            newMaxDistance = Math.max(newMaxDistance, Utils.minDistance(position, movePosition));
                        }
                    }
                    moves.removeAll(toRemove);
                    // On ajoute l'élément que l'on veut
                    moves.add(to);
                    maxDistance = newMaxDistance;
                }
            } else {
                // Sinon on l'ajoute directement en changeant la distance max
                maxDistance = Math.max(maxDistance, Utils.minDistance(position, to));
                moves.add(to);
            }
        }
        return maxDistance;
    }

}
