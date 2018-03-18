package algorithm;

import board.Board;
import board.Cell;
import board.Position;
import utils.Utils;
import java.util.ArrayList;

public class Node {

    private Board board;

    private ArrayList<Result> opponentMoves;

    private ArrayList<Result> allyMoves;

    private int humansEaten = 0;

    private int humansEatenByOpponent = 0;

    Node(Board board) {
        this.board = board;
        this.opponentMoves = new ArrayList<>();
        this.allyMoves = new ArrayList<>();
    }

    private Node(Board board, ArrayList<Result> allyMoves, ArrayList<Result> opponentMoves, int humansEaten,
                 int humansEatenByOpponent) {
        this.board = board;
        this.allyMoves = allyMoves;
        this.opponentMoves = opponentMoves;
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
                    ArrayList<Result> allyMoves = new ArrayList<>(this.allyMoves);
                    allyMoves.add(new Result(pos, this.board.getCells()[pos.getX()][pos.getY()].getPopulation(), to));
                    alternatives.add(
                            new Node(
                                    impliedBoard,
                                    allyMoves,
                                    this.opponentMoves,
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
                    ArrayList<Result> opponentMoves = new ArrayList<>(this.opponentMoves);
                    opponentMoves.add(new Result(pos, this.board.getCells()[pos.getX()][pos.getY()].getPopulation(), to));
                    alternatives.add(
                            new Node(
                                    impliedBoard,
                                    this.allyMoves,
                                    opponentMoves,
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

    public int getHumansEaten() {
        return humansEaten;
    }

    public int getHumansEatenByOpponent() {
        return humansEatenByOpponent;
    }

    public ArrayList<Result> getOpponentMoves() {
        return opponentMoves;
    }

    public ArrayList<Result> getAllyMoves() {
        return allyMoves;
    }

}
