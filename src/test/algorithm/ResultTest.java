package algorithm;

import board.Position;
import org.junit.jupiter.api.Test;
import utils.Utils;
import java.util.ArrayList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

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

        assertEquals(expected, Result.dropDuplicates(list));
    }

    @Test
    void isCircular() {
        ArrayList<Result> circularResults = new ArrayList<>();
        circularResults.add(new Result(new Position(0, 0), 3, new Position(1, 1)));
        circularResults.add(new Result(new Position(0, 1), 3, new Position(0, 0)));
        circularResults.add(new Result(new Position(1, 1), 3, new Position(2, 2)));
        circularResults.add(new Result(new Position(2, 2), 3, new Position(0, 1)));
        assertTrue(Result.isCircular(circularResults));

        ArrayList<Result> subCircularResults = new ArrayList<>();
        subCircularResults.add(new Result(new Position(0, 0), 3, new Position(1, 1)));
        subCircularResults.add(new Result(new Position(0, 1), 3, new Position(2, 2)));
        subCircularResults.add(new Result(new Position(1, 1), 3, new Position(0, 0)));
        subCircularResults.add(new Result(new Position(2, 2), 3, new Position(0, 1)));
        assertTrue(Result.isCircular(subCircularResults));

        ArrayList<Result> thirdCircularResults = new ArrayList<>();
        thirdCircularResults.add(new Result(new Position(0, 0), 3, new Position(1, 1)));
        thirdCircularResults.add(new Result(new Position(0, 1), 3, new Position(2, 2)));
        thirdCircularResults.add(new Result(new Position(1, 1), 3, new Position(0, 1)));
        assertFalse(Result.isCircular(thirdCircularResults));
    }
}