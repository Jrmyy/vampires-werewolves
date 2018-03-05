/**
 * Classe qui représente un résultat de mouvement, que l'on peut ensuite parser pour l'envoyer au serveur
 */
public class Result {

    public Coord source;
    public int itemsMoved;
    public Coord destination;

    Result(Coord source, int itemsMoved, Coord destination) {
        this.source = source;
        this.itemsMoved = itemsMoved;
        this.destination = destination;
    }

    public byte[] parse() {
        byte[] result = new byte[5];
        result[0] = (byte) this.source.x;
        result[1] = (byte) this.source.y;
        result[2] = (byte) this.itemsMoved;
        result[3] = (byte) this.destination.x;
        result[4] = (byte) this.destination.y;
        return result;
    }
}
