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

    /**@Before
    public void setUp() {
        this.board = new Board();
    }

    @Test
    public void setMapDimensions() {
        board.setMapDimensions(new byte[] {(byte) 4, (byte) 5});
        assertEquals(board.getCols(), 5);
        assertEquals(board.getRows(), 4);
    }

    @Test
    public void chooseMove() throws IOException {
        this.setFakeMap();
        ArrayList<byte[]> moves = this.board.chooseMove();
        assertEquals(moves.size(), 1);
        byte[] move = moves.get(0);
        assertEquals(move.length, 5);
        assertEquals(move[0], 2);
        assertEquals(move[1], 2);
        assertEquals(move[2], 3);
        assertEquals(move[3], 3);
        assertEquals(move[4], 2);
    }

    @Test
    public void setHome() {
        board.setHome(new byte[] {(byte) 2, (byte) 1});
        assertEquals(board.getAllies().size(), 1);
        assertEquals(board.getAllies().contains(new Position(2, 1)), true);
    }

    @Test
    public void population() {
        this.setFakeMap();
        assertEquals(this.board.alliesPopulation(), 3);
    }

    @Test
    public void opponentPopulation() {
        this.setFakeMap();
        assertEquals(this.board.opponentsPopulation(), 3);
    }

    @Test
    public void humanPopulation() {
        this.setFakeMap();
        System.out.println(this.board.getHumans());
        System.out.println(Arrays.deepToString(this.board.getCells()));
        assertEquals(this.board.humansPopulation(), 9);
    }

    /**@Test
    public void cloneWithMovement() {
        this.setFakeMap();
        assertThat(
                Utils.findNextMove(this.board, new Position(0, 0), new Position(4, 1)),
                anyOf(is(new Position(1, 0)), is(new Position(1, 1)))
        );
        Board clonedMap = this.board.simulateMove(new Position(0, 0), new Position(1, 1));
        assertEquals(this.board.getAllies(), new ArrayList<>(Collections.singletonList(new Position(2, 2))));
        assertEquals(clonedMap.getAllies(), new ArrayList<>(Collections.singletonList(new Position(2, 2))));
        assertEquals(this.board.getOpponents(), new ArrayList<>(Collections.singletonList(new Position(0, 0))));
        assertEquals(clonedMap.getOpponents(), new ArrayList<>(Collections.singletonList(new Position(1, 1))));
        assertEquals(clonedMap.getHumans(), this.board.getHumans());
        Cell[][] fakeMap = generateFakeMap(this.board.getCols(), this.board.getRows());
        Cell[][] fakeMoveMap = generateFakeMoveMap(clonedMap.getCols(), clonedMap.getRows());
        assertArrayEquals(this.board.getCells(), fakeMap);
        assertArrayEquals(clonedMap.getCells(), fakeMoveMap);
    }

    @Test
    public void flip() {
        this.setFakeMap();
        Board flippedMap = this.board.flip();
        Cell[][] fakeMap = generateFakeMap(this.board.getCols(), this.board.getRows());
        assertArrayEquals(this.board.getCells(), fakeMap);
        assertEquals(flippedMap.getAllies(), this.board.getOpponents());
        assertEquals(flippedMap.getOpponents(), this.board.getAllies());
        assertEquals(flippedMap.getHumans(), this.board.getHumans());
        assertEquals(flippedMap.getUs(), this.board.getOpponent());
        assertEquals(flippedMap.getOpponent(), this.board.getUs());
    }

    @Test
    public void chooseMoveWithOpponent() throws IOException {
        this.setFakeMap();
        Board flipped = this.board.flip();
        ArrayList<byte[]> moves = flipped.chooseMove();
        byte[] move = moves.get(0);
        assertEquals(moves.size(), 1);
        assertEquals(move.length, 5);
        assertEquals(move[0], 0);
        assertEquals(move[1], 0);
        assertEquals(move[2], 3);
        assertEquals(move[3], 1);
        assertEquals(move[4], 0);
    }

    @Test
    public void findNextMove() {
        this.setFakeMap();
        assertThat(
                Utils.findNextMove(this.board, new Position(2, 2), new Position(0, 3)),
                anyOf(is(new Position(1, 3)), is(new Position(1, 2)))
        );
        assertEquals(
                Utils.findNextMove(this.board, new Position(2, 2), new Position(0, 2)),
                new Position(1,2)
        );
        assertEquals(
                Utils.findNextMove(this.board, new Position(2, 2), new Position(1, 1)),
                new Position(1,1)
        );
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

    private Cell[][] generateFakeMoveMap(int cols, int rows) {
        Cell[][] map = new Cell[cols][rows];

        map[0][0] = new Cell("empty", 0);
        map[0][1] = new Cell("humans", 4);
        map[0][2] = new Cell();
        map[0][3] = new Cell("humans", 3);

        map[1][0] = new Cell();
        map[1][1] = new Cell("werewolves", 3);
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
    }**/
}