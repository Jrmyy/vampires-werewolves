package utils;

import algorithm.Result;
import board.Position;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

class UtilsTest {

    @Test
    void dropDuplicates() {
        ArrayList<Result> list = new ArrayList<>();
        list.add(new Result(new Position(1, 2), 2, new Position(4, 4)));
        list.add(new Result(new Position(1, 3), 2, new Position(4, 4)));
        list.add(new Result(new Position(1, 2), 2, new Position(4, 4)));
        list.add(new Result(new Position(1, 2), 2, new Position(4, 5)));

        ArrayList<Result> expected = new ArrayList<>();
        expected.add(new Result(new Position(1, 2), 2, new Position(4, 4)));
        expected.add(new Result(new Position(1, 3), 2, new Position(4, 4)));
        expected.add(new Result(new Position(1, 2), 2, new Position(4, 5)));

        assertEquals(expected, Utils.dropDuplicates(list));
    }
}