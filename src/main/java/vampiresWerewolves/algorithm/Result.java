package algorithm;

import board.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Classe qui représente un résultat de mouvement, que l'on peut ensuite parser pour l'envoyer au serveur
 */
public class Result {

    private Position source;
    private int itemsMoved;
    private Position destination;

    public Result(Position source, int itemsMoved, Position destination) {
        this.source = source;
        this.itemsMoved = itemsMoved;
        this.destination = destination;
    }

    public byte[] parse() {
        byte[] result = new byte[5];
        result[0] = (byte) this.source.getX();
        result[1] = (byte) this.source.getY();
        result[2] = (byte) this.itemsMoved;
        result[3] = (byte) this.destination.getX();
        result[4] = (byte) this.destination.getY();
        return result;
    }

    public Position getSource() {
        return source;
    }

    public int getItemsMoved() {
        return itemsMoved;
    }

    public Position getDestination() {
        return destination;
    }

    /**
     * Supprime les duplicats d'une liste de résultats
     * @param undeduped
     * @return
     */
    public static ArrayList<Result> dropDuplicates(ArrayList<Result> undeduped) {
        // Le set va supprimer les duplicats du tableau
        Set<Result> listWithoutDuplicates = new LinkedHashSet<>(undeduped);
        // On vide le tableau initial
        undeduped.clear();
        // On le re remplit avec les valeurs restantes
        undeduped.addAll(listWithoutDuplicates);
        return undeduped;
    }

    /**
     * Retourne si oui ou non le mouvement est circulaire (par circulaire on entend que les sources et les destinations sont différentes)
     * @param results
     * @return
     */
    public static boolean isCircular(ArrayList<Result> results) {
        ArrayList<Position> sources = new ArrayList<>();
        ArrayList<Position> destinations = new ArrayList<>();
        // On récupère toutes les sources et les destinations
        results.forEach(res -> {
            sources.add(res.getSource());
            destinations.add(res.getDestination());
        });
        // On trie les listes (on trie par X puis par Y)
        Collections.sort(sources);
        Collections.sort(destinations);
        // On regarde si les listes sont égales
        return sources.equals(destinations);
    }

    @Override
    public String toString() {
        return "Moving " + this.itemsMoved + " from " + this.source.toString() + " to " + this.destination.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Result) {
            Result res = (Result) obj;
            return res.getSource().equals(this.getSource()) && this.getDestination().equals(res.getDestination()) && this.getItemsMoved() == res.getItemsMoved();
        }

        return false;
    }

    /**
     * Utilisé pour identifier de manière unique une position
     */
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.itemsMoved);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + this.getSource().hashCode() + 29 * this.getDestination().hashCode() + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
