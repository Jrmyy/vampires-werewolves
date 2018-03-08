package board;

import java.io.Serializable;

public class Player implements Serializable {

    private String race;

    public Player(String race) {
        this.race = race;
    }

    public String getRace() {
        return race;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && this.race.equals(((Player) obj).getRace());
    }
}
