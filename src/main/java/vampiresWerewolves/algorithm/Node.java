package algorithm;

import board.Board;
import board.Cell;
import board.Position;
import utils.Utils;
import java.util.ArrayList;

public class Node {

    private Board board;

    private ArrayList<Result> lastMoves;

    private int humansEaten = 0;

    private int humansEatenByOpponent = 0;

    Node(Board board) {
        this.board = board;
        this.lastMoves = new ArrayList<>();
    }

    private Node(Board board, ArrayList<Result> lastMoves, int humansEaten, int humansEatenByOpponent) {
        this.board = board;
        this.lastMoves = lastMoves;
        this.humansEaten = humansEaten;
        this.humansEatenByOpponent = humansEatenByOpponent;
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
                    Cell toCell = this.board.getCells()[to.getX()][to.getY()];
                    int humansEaten = this.humansEaten;
                    if (toCell.getPopulation() > 0 && toCell.getKind().equals("humans")) {
                        humansEaten += toCell.getPopulation();
                    }
                    ArrayList<Result> lastMoves = new ArrayList<>(this.lastMoves);
                    lastMoves.add(new Result(pos, this.board.getCells()[pos.getX()][pos.getY()].getPopulation(), to));
                    alternatives.add(
                            new Node(
                                    impliedBoard,
                                    lastMoves,
                                    humansEaten,
                                    this.humansEatenByOpponent
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
                    Cell toCell = this.board.getCells()[to.getX()][to.getY()];
                    int humansEaten = this.humansEatenByOpponent;
                    if (toCell.getPopulation() > 0 && toCell.getKind().equals("humans")) {
                        humansEaten += toCell.getPopulation();
                    }
                    ArrayList<Result> lastMoves = new ArrayList<>(this.lastMoves);
                    lastMoves.add(new Result(pos, this.board.getCells()[pos.getX()][pos.getY()].getPopulation(), to));
                    alternatives.add(
                            new Node(
                                    impliedBoard,
                                    lastMoves,
                                    this.humansEaten,
                                    humansEaten
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

    public ArrayList<Result> getLastMoves() {
        return lastMoves;
    }

    public int getHumansEaten() {
        return humansEaten;
    }

    public void setHumansEaten(int humansEaten) {
        this.humansEaten = humansEaten;
    }

    public int getHumansEatenByOpponent() {
        return humansEatenByOpponent;
    }

    public void setHumansEatenByOpponent(int humansEatenByOpponent) {
        this.humansEatenByOpponent = humansEatenByOpponent;
    }
}
