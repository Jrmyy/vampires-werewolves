package algorithm;

import board.Board;
import board.Cell;
import board.Position;
import utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Node {

    public static Logger logger = Logger.getLogger("my logger");

    private Board board;

    private ArrayList<ArrayList<Result>> allyMoves;

    private int humansEaten = 0;

    private int humansEatenByOpponent = 0;

    private static final String[] MOVEMENT_TYPES = {"transform"};

    Node(Board board) throws IOException {
        Handler fh = new FileHandler("logs/myLog_" + board.getCurrentPlayer().getRace() +".log");
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);
        this.board = board;
        this.allyMoves = new ArrayList<>();
    }

    private Node(Board board, ArrayList<ArrayList<Result>> allyMoves, int humansEaten, int humansEatenByOpponent) {
        this.board = board;
        this.allyMoves = allyMoves;
        this.humansEaten = humansEaten;
        this.humansEatenByOpponent = humansEatenByOpponent;
    }

    public ArrayList<Node> createAlternatives() {
        ArrayList<Node> alternatives = new ArrayList<>();
        HashMap<Position, ArrayList<Position>> goalMoves = new HashMap<>();

        if (board.getCurrentPlayer().equals(board.getUs())) {
            for (Position ally: board.getAllies()) {
                for (String strategy: MOVEMENT_TYPES) {
                    goalMoves.put(ally, this.findBestMoveForStrategy(strategy, ally));
                }
                logger.info("Wants to reach : " + goalMoves.get(ally));
            }

            for (Position ally: goalMoves.keySet()) {
                for (Position goal: goalMoves.get(ally)) {
                    int allyPop = board.getCells()[ally.getX()][ally.getY()].getPopulation();
                    int goalPop = board.getCells()[goal.getX()][goal.getY()].getPopulation();
                    Position nextMoveFromGoal = Utils.findNextMove(board, ally, goal);
                    Cell nextMoveCell = board.getCells()[nextMoveFromGoal.getX()][nextMoveFromGoal.getY()];
                    int humansEaten = this.humansEaten;
                    if (nextMoveCell.getPopulation() > 0 && nextMoveCell.getKind().equals("humans")) {
                        humansEaten += nextMoveCell.getPopulation();
                    }
                    // On choisit quelle quantité de population on déplace pour ce groupe, ce qui peut le diviser
                    int movedPopulation = Math.min(allyPop, goalPop);
                    Board impliedBoard = board.simulateMove(ally, nextMoveFromGoal, movedPopulation);
                    // Les mouvements alliés sont maintenant une liste de mouvement afin de pouvoir déplacer plusieurs groupes en même temps
                    ArrayList<ArrayList<Result>> newAllyMoves = new ArrayList<>(this.allyMoves);
                    ArrayList<Result> thisTurnMoves = new ArrayList<>();
                    thisTurnMoves.add(new Result(ally, movedPopulation, nextMoveFromGoal)); // Ajouter chaque groupe à déplacer durant ce tour
                    // TODO: ajouter les mouvements effectués par les autres groupes, en particulier la deuxième partie du groupe divisé (attention alors au nombre de troupes restantes)
                    newAllyMoves.add(thisTurnMoves);
                    alternatives.add(new Node(impliedBoard, newAllyMoves, humansEaten, this.humansEatenByOpponent));
                }
            }
        } else {
            for (Position opp: board.getOpponents()) {
                for (String strategy: MOVEMENT_TYPES) {
                    goalMoves.put(opp, this.findBestMoveForStrategy(strategy, opp));
                }
                logger.info("Enemy wants to reach : " + goalMoves.get(opp));
            }

            for (Position opp: goalMoves.keySet()) {
                for (Position goal: goalMoves.get(opp)) {
                    int oppPop = board.getCells()[opp.getX()][opp.getY()].getPopulation();
                    int goalPop = board.getCells()[goal.getX()][goal.getY()].getPopulation();
                    Position nextMoveFromGoal = Utils.findNextMove(board, opp, goal);
                    Cell nextMoveCell = board.getCells()[nextMoveFromGoal.getX()][nextMoveFromGoal.getY()];
                    int humansEaten = this.humansEatenByOpponent;
                    if (nextMoveCell.getPopulation() > 0 && nextMoveCell.getKind().equals("humans")) {
                        humansEaten += nextMoveCell.getPopulation();
                    }
                    // On choisit quelle quantité de population on déplace pour ce groupe, ce qui peut le diviser
                    int movedPopulation = Math.min(oppPop, goalPop);
                    Board impliedBoard = board.simulateMove(opp, nextMoveFromGoal, movedPopulation);
                    alternatives.add(new Node(impliedBoard, this.allyMoves, this.humansEaten, humansEaten));
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

    private ArrayList<Position> findBestMoveForStrategy(String movement, Position position) {
        switch (movement) {
            case "attack":
            case "transform":
                int population = board.getCells()[position.getX()][position.getY()].getPopulation();
                ArrayList<Position> keptPositions = new ArrayList<>();
                double minRatio = Double.POSITIVE_INFINITY;
                for (Position human: board.getHumans()) {
                    int humanPop = board.getCells()[human.getX()][human.getY()].getPopulation();
                    if (population >= humanPop) {
                        double ratio = (double) humanPop / (double) Utils.minDistance(human, position);
                        if (keptPositions.size() < 3) {
                            keptPositions.add(human);
                            minRatio = Math.min(minRatio, ratio);
                        } else {
                            if (ratio > minRatio) {
                                ArrayList<Position> removedPositions = new ArrayList<>();
                                double newMinRatio = Double.POSITIVE_INFINITY;
                                for (Position pos: keptPositions) {
                                    int posPop = board.getCells()[pos.getX()][pos.getY()].getPopulation();
                                    double posRatio = (double) posPop / (double) Utils.minDistance(pos, position);
                                    if (posRatio == minRatio) {
                                        removedPositions.add(pos);
                                    } else {
                                        newMinRatio = Math.min(newMinRatio, posRatio);
                                    }
                                }
                                keptPositions.removeAll(removedPositions);
                                keptPositions.add(human);
                                minRatio = Math.min(newMinRatio, ratio);
                            }
                        }
                    }
                }
                return keptPositions;
            case "escape":
        }
        return new ArrayList<>();
    }

    public ArrayList<ArrayList<Result>> getAllyMoves() {
        return allyMoves;
    }

    public int getHumansEaten() {
        return humansEaten;
    }

    public int getHumansEatenByOpponent() {
        return humansEatenByOpponent;
    }
}
