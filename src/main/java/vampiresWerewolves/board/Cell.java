package board;

import java.io.Serializable;

/**
 * Même but que pour la classe Position, le but c'est d'éviter de manipuler des tableaux, pour ne pas se tromper avec les
 * indices
 */
public class Cell implements Serializable {

    private String kind;
    private int population;

    public Cell() {
        this.kind = "empty";
        this.population = 0;
    }

    public Cell(String kind, int population) {
        this.kind = kind;
        this.population = population;
    }

    public void fill(String kind, int population) {
        this.population = population;
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "Kind: " + kind + ", population: " + population;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Cell) {
            Cell cell = (Cell) obj;
            return this.kind.equals(cell.kind) && this.population == cell.population;
        }

        return false;
    }

    public String getKind() {
        return kind;
    }

    public int getPopulation() {
        return population;
    }

}
