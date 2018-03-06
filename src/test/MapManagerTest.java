import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class MapManagerTest {

    private MapManager mapManager;

    @BeforeEach
    void setUp() {
        this.mapManager = new MapManager();
    }

    @Test
    void setMapDimensions() {
        mapManager.setMapDimensions(new byte[] {(byte) 4, (byte) 5});
        assertEquals(mapManager.cols, 5);
        assertEquals(mapManager.rows, 4);
    }

    @Test
    void chooseMove() {
        this.setFakeMap();
        ArrayList<byte[]> moves = this.mapManager.chooseMove();
        assertEquals(moves.size(), 1);
        byte[] move = moves.get(0);
        assertEquals(move.length, 5);
        assertEquals(move[0], 2);
        assertEquals(move[1], 2);
        assertEquals(move[2], 3);
        assertEquals(move[3], 1);
        assertEquals(move[4], 3);
    }

    @Test
    void setHome() {
        mapManager.setHome(new byte[] {(byte) 2, (byte) 1});
        assertEquals(mapManager.home, new Coord(2, 1));
        assertEquals(mapManager.positions.size(), 1);
        assertEquals(mapManager.positions.contains(new Coord(2, 1)), true);
    }

    @Test
    void population() {
        this.setFakeMap();
        assertEquals(this.mapManager.population(), 3);
    }

    @Test
    void opponentPopulation() {
        this.setFakeMap();
        assertEquals(this.mapManager.opponentPopulation(), 3);
    }

    @Test
    void humanPopulation() {
        this.setFakeMap();
        assertEquals(this.mapManager.humanPopulation(), 9);
    }

    @Test
    void cloneWithMovement() {
        this.setFakeMap();
        assertEquals(Utils.findNextMove(this.mapManager, new Coord(0, 0), new Coord(4, 1)), new Coord(1, 1));
        MapManager clonedMap = this.mapManager.cloneWithMovement(new Coord(0, 0), new Coord(1, 1));
        assertEquals(this.mapManager.positions, new ArrayList<>(Collections.singletonList(new Coord(2, 2))));
        assertEquals(clonedMap.positions, new ArrayList<>(Collections.singletonList(new Coord(2, 2))));
        assertEquals(this.mapManager.opponentPositions, new ArrayList<>(Collections.singletonList(new Coord(0, 0))));
        assertEquals(clonedMap.opponentPositions, new ArrayList<>(Collections.singletonList(new Coord(1, 1))));
        assertEquals(clonedMap.humanPositions, this.mapManager.humanPositions);
        Cell[][] fakeMap = generateFakeMap(this.mapManager.cols, this.mapManager.rows);
        Cell[][] fakeMoveMap = generateFakeMoveMap(clonedMap.cols, clonedMap.rows);
        assertArrayEquals(this.mapManager.map, fakeMap);
        assertArrayEquals(clonedMap.map, fakeMoveMap);
    }

    @Test
    void flip() {
        this.setFakeMap();
        MapManager flippedMap = this.mapManager.flip();
        Cell[][] fakeMap = generateFakeMap(this.mapManager.cols, this.mapManager.rows);
        assertArrayEquals(this.mapManager.map, fakeMap);
        assertEquals(flippedMap.positions, this.mapManager.opponentPositions);
        assertEquals(flippedMap.opponentPositions, this.mapManager.positions);
        assertEquals(flippedMap.humanPositions, this.mapManager.humanPositions);
    }

    @Test
    void chooseMovWithOpponent() {
        this.setFakeMap();
        MapManager flipped = this.mapManager.flip();
        ArrayList<byte[]> moves = flipped.chooseMove();
        byte[] move = moves.get(0);
        assertEquals(moves.size(), 1);
        assertEquals(move.length, 5);
        assertEquals(move[0], 0);
        assertEquals(move[1], 0);
        assertEquals(move[2], 3);
        assertEquals(move[3], 1);
        assertEquals(move[4], 1);
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
        MapManager map = new MapManager();
        map.race = "vampires";
        map.cols = 5;
        map.rows = 4;
        map.home = new Coord(2, 2);
        map.positions = new ArrayList<>(Collections.singletonList(new Coord(2, 2)));
        map.opponentPositions = new ArrayList<>(Collections.singletonList(new Coord(0, 0)));
        map.humanPositions = new ArrayList<>(Arrays.asList(
                new Coord(0, 1),
                new Coord(0, 3),
                new Coord(4, 1)
        ));
        map.map = generateFakeMap(map.cols, map.rows);
        this.mapManager = map;
    }
}