package main;

import board.Board;
import board.Cell;
import board.Position;
import board.Player;

import java.util.*;

public class TestProject {

    public static void main(String[] args) throws Exception {
        Board map = new Board();
        map.setUs(new Player("vampires"));
        map.setOpponent(new Player("werewolves"));
        map.setCols(5);
        map.setRows(4);
        map.setAllies(new ArrayList<>(Collections.singletonList(new Position(2, 2))));
        map.setOpponents(new ArrayList<>(Collections.singletonList(new Position(0, 0))));
        map.setHumans(new ArrayList<>(Arrays.asList(
                new Position(0, 1),
                new Position(0, 3),
                new Position(4, 1)
        )));

        map.setCells(new Cell[map.getCols()][map.getRows()]);

        map.getCells()[0][0] = new Cell("werewolves", 3);
        map.getCells()[0][1] = new Cell("humans", 4);
        map.getCells()[0][2] = new Cell();
        map.getCells()[0][3] = new Cell("humans", 3);

        map.getCells()[1][0] = new Cell();
        map.getCells()[1][1] = new Cell();
        map.getCells()[1][2] = new Cell();
        map.getCells()[1][3] = new Cell();

        map.getCells()[2][0] = new Cell();
        map.getCells()[2][1] = new Cell();
        map.getCells()[2][2] = new Cell("vampires", 3);
        map.getCells()[2][3] = new Cell();

        map.getCells()[3][0] = new Cell();
        map.getCells()[3][1] = new Cell();
        map.getCells()[3][2] = new Cell();
        map.getCells()[3][3] = new Cell();

        map.getCells()[4][0] = new Cell();
        map.getCells()[4][1] = new Cell("humans", 2);
        map.getCells()[4][2] = new Cell();
        map.getCells()[4][3] = new Cell();

        ArrayList<byte[]> result = map.chooseMove();
        System.out.println(Arrays.toString(result.get(0)));
        System.out.println(Arrays.deepToString(map.getCells()));
    }

}

