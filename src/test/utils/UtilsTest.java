package utils;

import board.Board;
import board.Cell;
import board.Player;
import board.Position;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void minDistance() {
        assertEquals(Utils.minDistance(new Position(2, 2), new Position(5, 6)), 4);
        assertEquals(Utils.minDistance(new Position(0, 0), new Position(0, 6)), 6);
    }

    @Test
    void findNextMove() {
        Board board = this.generateFakeMap();
        assertEquals(Utils.findNextMove(board, new Position(2, 0), new Position(2, 3), 2), new Position(2, 1));

        board.getCells()[2][1].fill("werewolves", 4);
        Position nextMoveEnemyInFront = Utils.findNextMove(board, new Position(2, 0), new Position(2, 3), 6);
        assertTrue(nextMoveEnemyInFront.equals(new Position(1, 1)) || nextMoveEnemyInFront.equals(new Position(1, 3)));

        board.getCells()[2][1].fill("werewolves", 3);
        Position nextMoveWeakInFront = Utils.findNextMove(board, new Position(2, 0), new Position(2, 3), 6);
        assertEquals(nextMoveWeakInFront, new Position(2, 1));

        board.getCells()[2][1].fill("humans", 3);
        Position nextMoveHuman = Utils.findNextMove(board, new Position(2, 0), new Position(2, 3), 2);
        assertTrue(nextMoveHuman.equals(new Position(1, 1)) || nextMoveHuman.equals(new Position(1, 3)));

        board.getCells()[2][1].fill("humans", 3);
        Position nextMoveHumanWeak = Utils.findNextMove(board, new Position(2, 0), new Position(2, 3), 3);
        assertEquals(nextMoveHumanWeak, new Position(2, 1));
    }

    private Cell[][] generateFakeMap(int cols, int rows) {
        Cell[][] map = new Cell[cols][rows];

        map[0][0] = new Cell();
        map[0][1] = new Cell("humans", 3);
        map[0][2] = new Cell("humans", 3);

        map[1][0] = new Cell();
        map[1][1] = new Cell();
        map[1][2] = new Cell();
        map[1][3] = new Cell();

        map[2][0] = new Cell("vampires", 3);
        map[2][1] = new Cell();
        map[2][2] = new Cell();
        map[2][3] = new Cell("werewolves", 3);

        map[3][0] = new Cell();
        map[3][1] = new Cell();
        map[3][2] = new Cell();
        map[3][3] = new Cell();

        map[4][0] = new Cell();
        map[4][1] = new Cell("humans", 2);
        map[4][2] = new Cell("humans", 2);
        map[4][3] = new Cell();

        return map;
    }

    private Board generateFakeMap() {
        Board map = new Board();
        map.setUs(new Player("vampires"));
        map.setOpponent(new Player("werewolves"));
        map.setCurrentPlayer(map.getUs());
        map.setCols(5);
        map.setRows(4);
        map.setAllies(new ArrayList<>(Collections.singletonList(new Position(2, 2))));
        map.setOpponents(new ArrayList<>(Collections.singletonList(new Position(0, 0))));
        map.setHumans(new ArrayList<>(Arrays.asList(
                new Position(0, 1),
                new Position(0, 3),
                new Position(4, 1)
        )));
        map.setCells(generateFakeMap(map.getCols(), map.getRows()));
        return map;
    }
}