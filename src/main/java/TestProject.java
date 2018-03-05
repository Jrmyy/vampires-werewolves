import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TestProject {

    public static void main(String[] args) throws Exception {
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

        map.map = new Cell[map.cols][map.rows];

        map.map[0][0] = new Cell("werewolves", 3);
        map.map[0][1] = new Cell("humans", 4);
        map.map[0][2] = new Cell();
        map.map[0][3] = new Cell("humans", 3);

        map.map[1][0] = new Cell();
        map.map[1][1] = new Cell();
        map.map[1][2] = new Cell();
        map.map[1][3] = new Cell();

        map.map[2][0] = new Cell();
        map.map[2][1] = new Cell();
        map.map[2][2] = new Cell("vampires", 3);
        map.map[2][3] = new Cell();

        map.map[3][0] = new Cell();
        map.map[3][1] = new Cell();
        map.map[3][2] = new Cell();
        map.map[3][3] = new Cell();

        map.map[4][0] = new Cell();
        map.map[4][1] = new Cell("humans", 2);
        map.map[4][2] = new Cell();
        map.map[4][3] = new Cell();

        ArrayList<byte[]> result = map.chooseMove();
        System.out.println(Arrays.toString(result.get(0)));
        System.out.println(Arrays.deepToString(map.map));
    }

}

