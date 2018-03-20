package board;

import org.junit.Before;
import org.junit.Test;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class BoardTest {

    private Board board;

    @Test
    public void hashCodeTest() {
        this.setFakeMap();
        assertEquals(board.hashCode(), board.hashCode());
        Board newBoard = Board.copy(board);
        newBoard.setCurrentPlayer(board.getOpponent());
        assertNotEquals(newBoard.hashCode(), board.hashCode());
        newBoard = Board.copy(board);
        newBoard.getCells()[0][0].fill("empty", 0);
        newBoard.getCells()[1][1].fill("werewolves", 3);
        assertNotEquals(newBoard.hashCode(), board.hashCode());
        newBoard = Board.copy(board);
        newBoard.setCurrentPlayer(board.getOpponent());
        newBoard.setCurrentPlayer(board.getUs());
        assertEquals(board.hashCode(), newBoard.hashCode());
    }

    private Cell[][] generateFakeMap(int cols, int rows) {
        Cell[][] map = new Cell[cols][rows];

        map[0][0] = new Cell("werewolves", 3);
        map[0][1] = new Cell("humans", 4);
        map[0][2] = new Cell();
        map[0][3] = new Cell("humans", 3);

        map[1][0] = new Cell();
        map[1][1] = new Cell();
        map[1][2] = new Cell();
        map[1][3] = new Cell();

        map[2][0] = new Cell();
        map[2][1] = new Cell();
        map[2][2] = new Cell("vampires", 3);
        map[2][3] = new Cell();

        map[3][0] = new Cell();
        map[3][1] = new Cell();
        map[3][2] = new Cell();
        map[3][3] = new Cell();

        map[4][0] = new Cell();
        map[4][1] = new Cell("humans", 2);
        map[4][2] = new Cell();
        map[4][3] = new Cell();

        return map;
    }

    private void setFakeMap() {
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
        this.board = map;
    }
}