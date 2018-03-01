/**
 * Même but que pour la classe Coord, le but c'est d'éviter de manipuler des tableaux, pour ne pas se tromper avec les
 * indices
 */
public class Cell {

    public int humans;
    public int werewolves;
    public int vampires;

    Cell(int humans, int werewolves, int vampires) {
        this.humans = humans;
        this.werewolves = werewolves;
        this.vampires = vampires;
    }

    Cell() {
        this.humans = 0;
        this.werewolves = 0;
        this.vampires = 0;
    }

    public void fill(int humans, int werewolves, int vampires) {
        this.humans = humans;
        this.werewolves = werewolves;
        this.vampires = vampires;
    }


}
