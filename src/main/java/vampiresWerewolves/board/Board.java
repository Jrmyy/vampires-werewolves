package board;

import utils.Utils;

import algorithm.AlphaBeta;
import algorithm.Node;
import algorithm.Result;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
     * On initialise les dimensions de la carte, sans remplir avec les données (après reception de la commande SET par
     * le serveur).
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

    public boolean isGameOver() {
        return this.allies.size() == 0 || this.opponents.size() == 0;
    }

    /**
     * Récupère la position de départ de notre joueur (après réception de la commande HME)
     * @param home
     */
    public void setHome(byte[] home) {
        // On récupère les coordonnées et on ajoute cette position à la liste des positions alliées connues
        int x = (int) home[0];
        int y = (int) home[1];
        allies.add(new Position(x, y));
    }

    public int getHomesByKind(String kind) {
        switch (kind) {
            case "humans":
                return this.humans.size();
            case "vampires":
                if (this.us.getRace().equals("vampires")) {
                    return this.allies.size();
                }
                return this.opponents.size();
            case "werewolves":
                if (this.us.getRace().equals("werewolves")) {
                    return this.allies.size();
                }
                return this.opponents.size();
        }
        return 0;
    }

    /**
     * On va remplir la map avec les différentes espèces aux différentes cases (ou updater le carte) (après reception
     * des commandes MAP ou UPD par le serveur).
     * @param content
     */
    public void fillOrUpdateMap(byte[][] content) {
        int x;
        int y;
        int humans;
        int vampires;
        int werewolves;

        // Pour chaque case reçue par le serveur...
        for (byte[] aContent : content) {

            // On récupère la positiononnée (x,y), le nombre d'espèces, et on le met dans notre matrice de Cell
            x = (int) aContent[0];
            y = (int) aContent[1];
            humans = (int) aContent[2];
            vampires = (int) aContent[3];
            werewolves = (int) aContent[4];
            Position position = new Position(x, y);

            // On définit l'espèce sous forme d'une string et la quantité de population.
            String kind = "empty";
            int population = Math.max(humans, Math.max(vampires, werewolves));
            if (humans > 0) {
                kind = "humans";
            } else if (vampires > 0) {
                kind = "vampires";
            } else if (werewolves > 0) {
                kind = "werewolves";
            }

            if (kind.equals("humans") && !this.getHumans().contains(position)) {
                /* S'il y a des humains et que la position donnée n'est pas dans la liste des humains, on ajoute cette case
                 à la liste des cases humaines connues. */
                this.getHumans().add(position);
            } else if (kind.equals("vampires") || kind.equals("werewolves")) {

                /* Si on ne connait pas encore la race que l'on joue, mais que la case actuelle contient un vampire
                 ou un loup-garou, on détermine laquelle des deux races on joue en comparant cette case avec notre
                 case de départ (déjà inscrite dans la liste des positions alliées). */
                if (this.getUs() == null) {
                    if ((vampires > 0 && this.getAllies().get(0).equals(position))
                            || (werewolves > 0 && !this.getAllies().get(0).equals(position))
                        ) {
                        // Cas où on joue vampire
                        this.setUs(new Player("vampires"));
                        this.setOpponent(new Player("werewolves"));
                    } else {
                        // Cas où on joue loup-garou
                        this.setUs(new Player("werewolves"));
                        this.setOpponent(new Player("vampires"));
                    }
                }

                /* Une fois la race que l'on joue connue, on peut mettre à jour la liste des positions alliées ou
                 ennemies avec la case reçue.
                 */
                if (this.getUs().getRace().equals(kind)) {
                    // Cas d'une case alliée

                    // On ajoute la positiononnée que si elle n'existe pas encore
                    if (!this.getAllies().contains(position)) {
                        this.getAllies().add(position);
                    }

                    // Si la positiononnée est dans la liste des positions de l'adversaire, on la supprime parce que ça
                    // veut dire qu'on l'a récupérée
                    this.getOpponents().remove(position);
                    // Idem pour les humains
                    this.getHumans().remove(position);

                } else {
                    // Cas d'une case ennemie

                    // On ajoute la position donnée que si elle n'existe pas encore
                    if (!this.getOpponents().contains(position)) {
                        this.getOpponents().add(position);
                    }

                    // Si la position donnée est dans notre liste de positions, on la supprime parce que ça veut dire
                    // que l'adversaire nous l'a prise
                    this.getAllies().remove(position);
                    // Idem pour les humains
                    this.getHumans().remove(position);
                }
            } else if (kind.equals("empty")) {
                // Enfin, si la case reçue est vide, on retire cette case de toutes les listes de positions connues.
                this.allies.remove(position);
                this.opponents.remove(position);
                this.humans.remove(position);
            }
            this.getCells()[x][y].fill(kind, population);
        }
    }

    /**
     * Crée une copie de la carte, avant la simulation de plusieurs mouvements
     * @param moves
     * @return
     */
    public Board simulateMoves(ArrayList<Result> moves) {

        // Node.logger.info("Simulating moves " + moves + " on map " + this.toString());

        // On crée d'abord une copie de notre carte
        Board simulated = Board.copy(this);

        ArrayList<Position> newAllies = new ArrayList<>(this.getAllies());
        ArrayList<Position> newHumans = new ArrayList<>(this.getHumans());
        ArrayList<Position> newOpponents = new ArrayList<>(this.getOpponents());

        for (Result move: moves) {
            Position from = move.getSource();
            Position to = move.getDestination();

            // On récupère les cellules de départ et d'arrivée de ce déplacement
            Cell originCell = simulated.cells[from.getX()][from.getY()];
            Cell toCell = simulated.cells[to.getX()][to.getY()];

            // Si la cellule d'origine nous appartient, on ajoute la cellule de destination à nos position, on la retire
            // des positions ennemies et humaines
            if (originCell.getKind().equals(this.getUs().getRace())) {
                if (!newAllies.contains(to)) {
                    newAllies.add(to);
                }
                if (originCell.getPopulation() == move.getItemsMoved()) {
                    // Si la population déplacée égale la population présente au départ, la cellule est alors vide et on
                    // la retire de notre liste
                    newAllies.remove(from);
                }
                newHumans.remove(to);
                newOpponents.remove(to);

                // Sinon ça veut dire qu'elle appartient à l'adversaire
            } else {
                if (!newOpponents.contains(to)) {
                    newOpponents.add(to);
                }
                if (originCell.getPopulation() == move.getItemsMoved()) {
                    newOpponents.remove(from);
                }
                newHumans.remove(to);
                newAllies.remove(to);
            }

            // Les listes changées sont ajoutées à la nouvelle carte
            simulated.setHumans(newHumans);
            simulated.setOpponents(newOpponents);
            simulated.setAllies(newAllies);

            // On va maintenant changer la quantité de population présente dans les cases

            // Population de la cellule d'arrivée
            if (toCell.getKind().equals("humans") || toCell.getKind().equals("empty") || toCell.getKind().equals(originCell.getKind())) {
                // Si la cellule d'arrivée est vide, alliées ou remplit d'humains, on ajoute la population déplacée à la
                // population déjà présente (les captures d'humains sont forcément effectuée avec certitude de victoire).
                simulated.cells[to.getX()][to.getY()] = new Cell(
                        originCell.getKind(), move.getItemsMoved() + toCell.getPopulation()
                );
            } else {
                // Sinon c'est une attaque sur l'ennemi, seule la population déplacée est présente sur la cellule finale.
                simulated.cells[to.getX()][to.getY()] = new Cell(originCell.getKind(), move.getItemsMoved());
            }

            // Population de la cellule de départ
            if (originCell.getPopulation() > move.getItemsMoved()) {
                // Si seulement une partie des troupes est déplacée, la population de départ est réduite
                simulated.cells[from.getX()][from.getY()] = new Cell(
                        originCell.getKind(), originCell.getPopulation() - move.getItemsMoved()
                );
            } else {
                // Si toutes les troupes sont déplacées, la cellule de départ est vide
                simulated.cells[from.getX()][from.getY()] = new Cell();
            }
        }

        // C'est au tour de l'autre joueur de jouer.
        simulated.setCurrentPlayer(this.getCurrentPlayer().equals(this.getUs()) ? this.getOpponent() : this.getUs());
        // Node.logger.info("Simulated map is " + simulated.toString());

        if (!simulated.checkConsistency()) {
            System.out.println("/!\\ Board not coherent :\n" + simulated.toString());
        }

        return simulated;
    }

    /**
     * Crée une copie d'un Board (comme ça on ne touche pas à l'original)
     * @param orig
     * @return
     */
    public static Board copy(Board orig) {
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
     * Calcule la population alliées sur la carte
     * @return
     */
    public int alliesPopulation() {
        return this.countPopulation(this.getAllies());
    }

    /**
     * Calcule la population ennemie sur la carte
     * @return
     */
    public int opponentsPopulation() {
        return this.countPopulation(this.getOpponents());
    }

    /**
     * Calcule le population humaine sur la carte
     * @return
     */
    public int humansPopulation() {
        return this.countPopulation(this.getHumans());
    }

    /**
     * Calcule la population dans une liste de cases données
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

    /**
     * Appelle l'algorithme alpha-beta pour choisir un coup à jouer et renvoit une réponse compréhensible par le serveur.
     * @return
     */
    public ArrayList<byte[]> chooseMove() {
        // On appelle l'algorithme alpha-beta pour choisir notre coup à jouer
        this.setCurrentPlayer(this.getUs());
        AlphaBeta ab = new AlphaBeta(this);
        // Profondeur en fonction du nombre de groupes actuels
        int depth = Math.max(3, 8 - this.getAllies().size() - this.getOpponents().size());
        ArrayList<Result> results = ab.algorithm(depth);
        this.setCurrentPlayer(this.getOpponent());
        // On parse le résultat obtenu dans un format reconnu par le serveur
        ArrayList<byte[]> parsedResults = new ArrayList<>();
        results.forEach(move -> parsedResults.add(move.parse()));
        return parsedResults;
    }

    /**
     * Déplace aléatoirement un des groupes alliés. Cette fonction n'est à appeler qu'en dernier recours.
     * @return
     */
    public ArrayList<byte[]> chooseRandomMove() {
        Random randomGenerator = new Random();
        Position randomPosition = this.allies.get(randomGenerator.nextInt(this.allies.size()));
        int population = this.getCells()[randomPosition.getX()][randomPosition.getY()].getPopulation();
        ArrayList<Position> destinations = Utils.findAdjacentCells(this.cols, this.rows, randomPosition);
        Position randomDestination = destinations.get(randomGenerator.nextInt(destinations.size()));
        byte[] result = new byte[5];
        result[0] = (byte) randomPosition.getX();
        result[1] = (byte) randomPosition.getY();
        result[2] = (byte) population;
        result[3] = (byte) randomDestination.getX();
        result[4] = (byte) randomDestination.getY();
        ArrayList<byte[]> resultList = new ArrayList<>();
        resultList.add(result);
        return resultList;
    }

    /**
     * Vérifie si la carte actuelle est cohérente
     * @return
     */
    private boolean checkConsistency() {
        for (Position human: this.getHumans()) {
            if (!this.getCells()[human.getX()][human.getY()].getKind().equals("humans")) {
                System.out.println("Not coherent because of humans at " + human.toString());
                return false;
            }
        }
        for (Position allies: this.getAllies()) {
            if (!this.getCells()[allies.getX()][allies.getY()].getKind().equals(this.getUs().getRace())) {
                System.out.println("Not coherent because of allies at " + allies.toString());
                return false;
            }
        }
        for (Position opp: this.getOpponents()) {
            if (!this.getCells()[opp.getX()][opp.getY()].getKind().equals(this.getOpponent().getRace())) {
                System.out.println("Not coherent because of opponents at " + opp.toString());
                return false;
            }
        }
        return true;
    }

    private int getMinPop(ArrayList<Position> specie) {
        int minPop = Integer.MAX_VALUE;
        for (Position pos: specie) {
            minPop = Math.min(this.cells[pos.getX()][pos.getY()].getPopulation(), minPop);
        }
        return minPop;
    }

    public int getMinHumanPop() {
        return getMinPop(this.humans);
    }

    public int getMinOppPop() {
        return getMinPop(this.opponents);
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

    @Override
    public int hashCode() {
        return 24 * currentPlayer.hashCode() + 21 * Arrays.deepHashCode(cells);
    }

    @Override
    public String toString() {
        return "Current player is " + this.getCurrentPlayer() + "\n" + "We have allies at : " + this.allies + "\n" +
                "We have opponents at : " + this.opponents + "\n" + "We have humans at : " + this.humans + "\n"
                + "Board is : " + Arrays.deepToString(this.cells);
    }
}
