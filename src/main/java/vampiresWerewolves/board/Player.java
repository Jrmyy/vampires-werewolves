package board;

import java.io.Serializable;

/**
 * Objet pour représenter un joueur par sa race jouée
 */
public class Player implements Serializable {

    private String race;

    public Player(String race) {
        this.race = race;
    }

    public String getRace() {
        return race;
    }

    /* equals et hashCode nécessaires pour faire des hashmap sur la classe Cell */

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && this.race.equals(((Player) obj).getRace());
    }

    @Override
    public String toString() {
        return this.race;
    }

    @Override
    public int hashCode() {
        return race.hashCode();
    }
}

