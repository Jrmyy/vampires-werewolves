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
        logger.setUseParentHandlers(false);
        this.board = board;
        this.allyMoves = new ArrayList<>();
    }

    private Node(Board board, ArrayList<ArrayList<Result>> allyMoves, int humansEaten, int humansEatenByOpponent) {
        this.board = board;
        this.allyMoves = allyMoves;
        this.humansEaten = humansEaten;
        this.humansEatenByOpponent = humansEatenByOpponent;
    }

    public List<ArrayList<Result>> computeAllMoves(HashMap<Position, ArrayList<Result>> goalMoves) {
        List<ArrayList<Result>> allMoves = new ArrayList<>();
        int solutions = 1;

        for(ArrayList<Result> results: goalMoves.values()) {
            solutions *= results.size();
        }

        for(int i = 0; i < solutions; i++) {
            ArrayList<Result> combination = new ArrayList<>();
            int j = 1;
            for(ArrayList<Result> res : goalMoves.values()) {
                combination.add(res.get((i/j)%res.size()));
                j *= res.size();
            }
            allMoves.add(combination);
        }

        return allMoves;
    }

    public ArrayList<Node> createAlternatives() {
        ArrayList<Node> alternatives = new ArrayList<>();
        HashMap<Position, ArrayList<Result>> goalMoves = new HashMap<>();

        if (board.getCurrentPlayer().equals(board.getUs())) {
            int minHumanPop = board.getMinHumanPop();
            int minOppPop = board.getMinOppPop();
            for (Position ally: board.getAllies()) {
                int allyPop = board.getCells()[ally.getX()][ally.getY()].getPopulation();
                ArrayList<Result> allMoves = new ArrayList<>();
                for (String strategy: MOVEMENT_TYPES) {
                    if (strategy.equals("transform")) {
                        ArrayList<Result> earlyMoves = this.findBestMoveForStrategy(strategy, ally);
                        ArrayList<Result> earlyMovesSplit = new ArrayList<>();
                        for (Result eMove: earlyMoves) {
                            int minPopToSplit = board.getCells()[eMove.getDestination().getX()][eMove.getDestination().getY()]
                                    .getPopulation();
                            if (minPopToSplit >= Math.min(minHumanPop, 1.5 * minOppPop) &&
                                    (allyPop - minPopToSplit) >= Math.min(minHumanPop, 1.5 * minOppPop)
                                ) {
                                earlyMovesSplit.add(new Result(eMove.getSource(), minPopToSplit, eMove.getDestination()));
                            }
                        }
                        allMoves.addAll(earlyMovesSplit);
                        allMoves.addAll(earlyMoves);
                    } else {
                        allMoves.addAll(this.findBestMoveForStrategy(strategy, ally));
                    }
                }
                goalMoves.put(ally, Utils.dropDuplicates(allMoves));
                logger.info("We want to reach : " + goalMoves.get(ally) + " from " + ally);
            }

            List<ArrayList<Result>> moves = computeAllMoves(goalMoves);

            logger.info("Moves computed for ally are " + moves);

            for (ArrayList<Result> resultedMoves: moves) {
                ArrayList<Result> realMoves = new ArrayList<>();
                int humansEaten = this.humansEaten;
                for (Result res: resultedMoves) {
                    Position nextMoveFromGoal = Utils.findNextMove(board, res.getSource(), res.getDestination(), res.getItemsMoved());
                    Cell nextMoveCell = board.getCells()[nextMoveFromGoal.getX()][nextMoveFromGoal.getY()];
                    if (nextMoveCell.getPopulation() > 0 && nextMoveCell.getKind().equals("humans")) {
                        humansEaten += nextMoveCell.getPopulation();
                    }
                    Result move = new Result(res.getSource(), res.getItemsMoved(), nextMoveFromGoal);
                    realMoves.add(move);
                }
                Board impliedBoard = board.simulateMoves(realMoves);
                ArrayList<ArrayList<Result>> newAllyMoves = new ArrayList<>(this.allyMoves);
                newAllyMoves.add(realMoves);
                alternatives.add(new Node(impliedBoard, newAllyMoves, humansEaten, this.humansEatenByOpponent));
            }

        } else {
            for (Position opp: board.getOpponents()) {
                ArrayList<Result> allMoves = new ArrayList<>();
                for (String strategy: MOVEMENT_TYPES) {
                    allMoves.addAll(this.findBestMoveForStrategy(strategy, opp));
                }
                goalMoves.put(opp, Utils.dropDuplicates(allMoves));
                logger.info("Enemy wants to reach : " + goalMoves.get(opp) + " from " + opp);
            }

            List<ArrayList<Result>> moves = computeAllMoves(goalMoves);

           logger.info("Moves computed for opp are " + moves);

            for (ArrayList<Result> resultedMoves: moves) {
                ArrayList<Result> realMoves = new ArrayList<>();
                int humansEaten = this.humansEatenByOpponent;
                for (Result res: resultedMoves) {
                    Position nextMoveFromGoal = Utils.findNextMove(board, res.getSource(), res.getDestination(), res.getItemsMoved());
                    Cell nextMoveCell = board.getCells()[nextMoveFromGoal.getX()][nextMoveFromGoal.getY()];
                    if (nextMoveCell.getPopulation() > 0 && nextMoveCell.getKind().equals("humans")) {
                        humansEaten += nextMoveCell.getPopulation();
                    }
                    Result move = new Result(res.getSource(), res.getItemsMoved(), nextMoveFromGoal);
                    realMoves.add(move);
                }
                Board impliedBoard = board.simulateMoves(realMoves);
                alternatives.add(new Node(impliedBoard, this.allyMoves, this.humansEatenByOpponent, humansEaten));
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

    private ArrayList<Result> findBestMoveForStrategy(String movement, Position position) {
        int population = board.getCells()[position.getX()][position.getY()].getPopulation();
        ArrayList<Position> keptPositions = new ArrayList<>();
        double minRatio = Double.POSITIVE_INFINITY;
        switch (movement) {
            case "attack":
                // En late game, il va falloir attaquer les ennemis
                Double minOpponentPop = Double.POSITIVE_INFINITY;
                Position minOpponent = null;
                for (Position opp: board.getOpponents()) {
                    int oppPop = board.getCells()[opp.getX()][opp.getY()].getPopulation();
                    if (oppPop < minOpponentPop) {
                        minOpponentPop = (double) oppPop;
                        minOpponent = opp;
                    }
                    if (population > 1.5 * oppPop) {
                        double ratio = (double) oppPop / (double) Utils.minDistance(opp, position);
                        if (keptPositions.size() < 3) {
                            keptPositions.add(opp);
                            minRatio = Math.min(minRatio, ratio);
                        } else if (ratio > minRatio) {
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
                            keptPositions.add(opp);
                            minRatio = Math.min(newMinRatio, ratio);
                        }
                    }
                }
                if (keptPositions.size() == 0 && minOpponentPop > population) {
                    // Si on ne peut pas tuer à coup sûr des ennemis et que notre taille est plus petite que la plus
                    // petite de l'ennemi, on tente le tout pour le tout et on attaque
                    keptPositions.add(minOpponent);
                }
            case "transform":
                // En début de partie, le but sera de grossir le plus possible et donc de manger le plus d'humains
                // possibles
                for (Position human: board.getHumans()) {
                    int humanPop = board.getCells()[human.getX()][human.getY()].getPopulation();
                    if (population >= humanPop) {
                        double ratio = (double) humanPop / (double) Utils.minDistance(human, position);
                        if (keptPositions.size() < 3) {
                            keptPositions.add(human);
                            minRatio = Math.min(minRatio, ratio);
                        } else if (ratio > minRatio) {
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

        ArrayList<Result> moves = new ArrayList<>();
        for (Position kept: keptPositions) {
            moves.add(new Result(position, population, kept));
        }
        return moves;
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
