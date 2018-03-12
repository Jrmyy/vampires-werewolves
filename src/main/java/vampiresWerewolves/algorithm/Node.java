package algorithm;

import board.Board;
import board.Cell;
import board.Position;
import utils.Utils;
import java.util.ArrayList;

public class Node {

    private Board board;

    private Result lastMove;

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
                Cell originCell = this.board.getCells()[pos.getX()][pos.getY()];
                ArrayList<Position> futureMoves = Utils.findAdjacentCells(this.board.getCols(), this.board.getRows(), pos);
                ArrayList<Position> toRemove = new ArrayList<>();
                for (Position futureMove: futureMoves) {
                    Cell futureMoveCell = this.board.getCells()[futureMove.getX()][futureMove.getY()];
                    if (
                            (futureMoveCell.getKind().equals("humans")
                                    && (futureMoveCell.getPopulation() >= originCell.getPopulation())
                            ) ||
                                    futureMoveCell.getKind().equals(this.board.getOpponent().getRace())
                                            && !(1.5 * futureMoveCell.getPopulation() <= originCell.getPopulation())
                        ) toRemove.add(futureMove);
                }
                futureMoves.removeAll(toRemove);
                System.out.println("Moves for " + pos + " are " + futureMoves);
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
                Cell originCell = this.board.getCells()[pos.getX()][pos.getY()];
                ArrayList<Position> futureMoves = Utils.findAdjacentCells(this.board.getCols(), this.board.getRows(), pos);
                ArrayList<Position> toRemove = new ArrayList<>();
                for (Position futureMove: futureMoves) {
                    Cell futureMoveCell = this.board.getCells()[futureMove.getX()][futureMove.getY()];
                    if (
                            futureMoveCell.getKind().equals("humans")
                                    && futureMoveCell.getPopulation() >= originCell.getPopulation() ||
                                    (futureMoveCell.getKind().equals(this.board.getOpponent().getRace())
                                            && !(1.5 * futureMoveCell.getPopulation() <= originCell.getPopulation())
                                    )
                            ) toRemove.add(futureMove);
                }
                futureMoves.removeAll(toRemove);
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

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Result getLastMove() {
        return lastMove;
    }

}
