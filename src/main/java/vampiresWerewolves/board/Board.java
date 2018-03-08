package board;

import algorithm.AlphaBeta;
import algorithm.Result;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Board implements Serializable {

    private Player us = null;
    private Player opponent = null;
    private Player currentPlayer = null;
    private int cols;
    private int rows;
    private ArrayList<Position> allies = new ArrayList<>();
    private ArrayList<Position> opponents = new ArrayList<>();
    private ArrayList<Position> humans = new ArrayList<>();
    private Cell[][] cells;

    /**
     * On initialise les dimensions de la carte, sans remplir avec les données
     * @param dimensions
     */
    public void setMapDimensions(byte[] dimensions) {
        // On récupère le nombre de columns et de lignes
        int rows = (int) dimensions[0];
        int cols = (int) dimensions[1];
        this.rows = rows;
        this.cols = cols;

        // On crée une matrice d'objets Cell
        this.setCells(new Cell[cols][rows]);

        // Finalement on va instantier un nouvel objet cell dans chaque cellule de la matrice
        for(int i = 0; i < cols; i++){
            for(int j = 0; j < rows; j++){
                this.getCells()[i][j] = new Cell();
            }
        }
    }

    /**
     * On va remplir la map avec les différentes espèces aux différentes cases (ou updater le carte)
     * @param content
     */
    public void fillOrUpdateMap(byte[][] content) {
        int x;
        int y;
        int humans;
        int vampires;
        int werewolves;

        // Pour chaque case
        for (byte[] aContent : content) {

            // On récupère la positiononnée (x,y), le nombre d'espèces, et on le met dans notre matrice de Cell
            x = (int) aContent[0];
            y = (int) aContent[1];
            humans = (int) aContent[2];
            vampires = (int) aContent[3];
            werewolves = (int) aContent[4];
            Position position = new Position(x, y);
            String kind = "empty";
            int population = Math.max(humans, Math.max(vampires, werewolves));

            if (humans > 0) {
                kind = "humans";
            } else if (vampires > 0) {
                kind = "vampires";
            } else if (werewolves > 0) {
                kind = "werewolves";
            }

            // S'il y a des humains et que la positiononnée n'est pas dans la liste des humains, on remplit
            if (kind.equals("humans") && !this.getHumans().contains(position)) {
                this.getHumans().add(position);
            } else if (kind.equals("vampires") || kind.equals("werewolves")) {
                if (this.getUs() == null) {
                    // Si la race est nulle (on est dans le premier remplissage) et que l'on a une créature sur la case
                    // On regarde si on a des vampires et que l'on est sur la positiononnée de départ
                    if (vampires > 0 && this.getAllies().get(0).equals(position) || werewolves > 0 && !this.getAllies().get(0).equals(position)) {
                        // On assigne la race vampire
                        this.setUs(new Player("vampires"));
                        this.setOpponent(new Player("werewolves"));
                    } else {
                        // Dans tous les autres cas, nous sommes les loup-garous et on met la position dans la liste
                        // des positions adverses
                        this.setUs(new Player("werewolves"));
                        this.setOpponent(new Player("vampires"));
                    }
                }
                // Si on a la race, on est dans une assignation update et on assigne juste en fonction de la race
                if (this.getUs().getRace().equals(kind)) {

                    // On ajoute la positiononnée que si elle n'existe pas encore
                    if (!this.getAllies().contains(position)) {
                        this.getAllies().add(position);
                    }

                    // Si la positiononnée est dans la liste des positions de l'adversaire, on la supprime parce que ça
                    // veut dire qu'on l'a récupéré
                    this.getOpponents().remove(position);
                    // Idem pour les humains
                    this.getHumans().remove(position);

                } else {

                    // On ajoute la position donnée que si elle n'existe pas encore
                    if (!this.getOpponents().contains(position)) {
                        this.getOpponents().add(position);
                    }

                    // Si la positiononnée est dans notre liste de positions, on la supprime parce que ça veut dire que
                    // l'adversaire nous l'a prise
                    this.getAllies().remove(position);
                    // Idem pour les humains
                    this.getHumans().remove(position);
                }
            } else if (kind.equals("empty")) {
                this.allies.remove(position);
                this.opponents.remove(position);
                this.humans.remove(position);
            }
            this.getCells()[x][y].fill(kind, population);
        }
    }

    /**
     * On crée une nouvelle carte en inversant les races. Nos positions deviennent celles de l'adversaire et les
     * positions de l'adversaire deviennent les notres. Utilisée pour la partie min du AlphaBeta
     * @return
     */
    public Board flip() {
        Board flipped = Board.copy(this);
        flipped.us = this.opponent;
        flipped.opponent = this.us;
        flipped.allies = this.opponents;
        flipped.opponents = this.allies;
        flipped.setUs(this.getOpponent());
        flipped.setOpponent(this.getUs());
        return flipped;
    }

    /**
     * Crée une copie de la carte, avant la simulation d'un mouvement de l'origine vers le next move qui mène à notre
     * goal
     * @param from
     * @param to
     * @return
     */
    public Board simulateMove(Position from, Position to) {
        // On crée d'abord une copie de notre carte
        Board simulated = Board.copy(this);

        // On trouve la prochaine positiononnée qui nous permet d'arriver au goal en partant de origin
        Cell originCell = simulated.cells[from.getX()][from.getY()];

        // Si la cellule d'origine nous appartient
        if (originCell.getKind().equals(this.getUs().getRace())) {
            // On ajoute la next position à la liste de nos positions
            simulated.getAllies().add(to);
            // On la supprime des humains et des ennemies (on ne sait pas où elle est)
            simulated.getHumans().remove(to);
            simulated.getOpponents().remove(to);
            // On déplace toutes les troupes pour le moment donc on supprime la cellule d'origine de nos positions sur
            // la carte clonée
            simulated.getAllies().remove(from);
            // Sinon ça veut dire qu'elle appartient à l'adversaire
        } else {
            // On ajoute la next position à la liste des positions ennemies
            simulated.getOpponents().add(to);
            // On la supprime des humains et des notres
            simulated.getHumans().remove(to);
            simulated.getAllies().remove(to);
            // On supprime la cellule d'origine des ennemies
            simulated.getOpponents().remove(from);
        }
        // Comme on bouge toutes les troupes, la cellule d'origine est vide
        simulated.cells[from.getX()][from.getY()] = new Cell();
        // La next cellule devient remplie des valeurs de la cellule d'origine
        simulated.cells[to.getX()][to.getY()] = originCell;
        simulated.setCurrentPlayer(this.getCurrentPlayer().equals(this.getUs()) ? this.getOpponent() : this.getUs());
        return simulated;
    }

    /**
     * Crée une copie d'une MapManager (comme ça on ne touche pas à l'original)
     * @param orig
     * @return
     */
    private static Board copy(Board orig) {
        Object obj = null;
        try {
            // On écrit l'objet dans un Byte Array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // On crée un InputStream à partir du byte array et on lit de telle sorte à faire une copie
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        // On caste l'objet et on le retourne
        return (Board) obj;
    }

    /**
     * Récupère la positiononnée initiale de notre joueur
     * @param home
     */
    public void setHome(byte[] home) {
        // On récupère les données et on les met dans source
        int x = (int) home[0];
        int y = (int) home[1];
        allies.add(new Position(x, y));
    }

    /**
     * Calcule notre nombre d'éléments
     * @return
     */
    protected int alliesPopulation() {
        return this.countPopulation(this.getAllies());
    }

    /**
     * Calcule le nombre d'éléments de l'adversaire
     * @return
     */
    protected int opponentsPopulation() {
        return this.countPopulation(this.getOpponents());
    }

    /**
     * Calcule le nombre d'humains
     * @return
     */
    protected int humansPopulation() {
        return this.countPopulation(this.getHumans());
    }

    /**
     * @param positions
     * @return
     */
    private int countPopulation(ArrayList<Position> positions) {
        int pop = 0;
        for (Position position: positions) {
            pop += this.cells[position.getX()][position.getY()].getPopulation();
        }
        return pop;
    }

    public ArrayList<byte[]> chooseMove() {
        this.setCurrentPlayer(this.getUs());
        AlphaBeta ab = new AlphaBeta(this);
        Result result = ab.algorithm(3);
        this.setCurrentPlayer(this.getOpponent());
        return new ArrayList<>(Collections.singleton(result.parse()));
    }

    public Player getUs() {
        return us;
    }

    public void setUs(Player us) {
        this.us = us;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public ArrayList<Position> getAllies() {
        return allies;
    }

    public void setAllies(ArrayList<Position> allies) {
        this.allies = allies;
    }

    public ArrayList<Position> getOpponents() {
        return opponents;
    }

    public void setOpponents(ArrayList<Position> opponents) {
        this.opponents = opponents;
    }

    public ArrayList<Position> getHumans() {
        return humans;
    }

    public void setHumans(ArrayList<Position> humans) {
        this.humans = humans;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
