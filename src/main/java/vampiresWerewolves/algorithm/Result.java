package algorithm;

import board.Position;

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

    Result() {
        this.source = null;
        this.itemsMoved = -1;
        this.destination = null;
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

    public void setSource(Position source) {
        this.source = source;
    }

    public int getItemsMoved() {
        return itemsMoved;
    }

    public void setItemsMoved(int itemsMoved) {
        this.itemsMoved = itemsMoved;
    }

    public Position getDestination() {
        return destination;
    }

    public void setDestination(Position destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Moving " + this.itemsMoved + " from " + this.source.toString() + " to " + this.destination.toString();
    }
}
