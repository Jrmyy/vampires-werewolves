import java.io.*;
import java.util.ArrayList;
import java.lang.*;
import java.util.HashMap;


public class MapManager implements Serializable{

    // Peut être vampire ou loup garou
    protected String race = null;
    // Liste des coordonnées pour la race que l'on joue
    protected ArrayList<Coord> positions = new ArrayList<>();
    // Notre première position
    protected Coord home;
    // Liste des coordonées de l'adversaire
    protected ArrayList<Coord> opponentPositions = new ArrayList<>();
    // Liste des coordonnées des humains
    protected ArrayList<Coord> humanPositions = new ArrayList<>();
    // Etat de la carte
    protected Cell[][] map;
    // Nombre de colonnes
    protected int cols;
    // Nombre de lignes
    protected int rows;

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
        this.map = new Cell[cols][rows];

        // Finalement on va instantier un nouvel objet cell dans chaque cellule de la matrice
        for(int i = 0; i < cols; i++){

            for(int j = 0; j < rows; j++){

               this.map[i][j] = new Cell();
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

            // On récupère la coordonnée (x,y), le nombre d'espèces, et on le met dans notre matrice de Cell
            x = (int) aContent[0];
            y = (int) aContent[1];
            humans = (int) aContent[2];
            vampires = (int) aContent[3];
            werewolves = (int) aContent[4];
            Coord coord = new Coord(x, y);
            String kind = "empty";
            int population = Math.max(humans, Math.max(vampires, werewolves));

            if (humans > 0) {
                kind = "humans";
            } else if (vampires > 0) {
                kind = "vampires";
            } else if (werewolves > 0) {
                kind = "werewolves";
            }

            // S'il y a des humains et que la coordonnée n'est pas dans la liste des humains, on remplit
            if (kind.equals("humans") && !this.humanPositions.contains(coord)) {
                this.humanPositions.add(coord);
            } else if (kind.equals("vampires") || kind.equals("werewolves")) {
                if (this.race == null) {
                    // Si la race est nulle (on est dans le premier remplissage) et que l'on a une créature sur la case
                    // On regarde si on a des vampires et que l'on est sur la coordonnée de départ
                    if (vampires > 0 && this.home.equals(coord)) {

                        // On assigne la race vampire
                        this.setRace("vampires");

                    } else {

                        // Dans tous les autres cas, nous sommes les loup-garous et on met la position dans la liste
                        // des positions adverses
                        this.opponentPositions.add(coord);
                        this.setRace("werewolves");

                    }
                } else {
                    // Si on a la race, on est dans une assignation update et on assigne juste en fonction de la race
                    if (this.race.equals(kind)) {

                        // On ajoute la coordonnée que si elle n'existe pas encore
                        if (!this.positions.contains(coord)) {
                            this.positions.add(coord);
                        }

                        // Si la coordonnée est dans la liste des positions de l'adversaire, on la supprime parce que ça
                        // veut dire qu'on l'a récupéré
                        if (this.opponentPositions.contains(coord)) {
                            this.opponentPositions.remove(coord);
                        }

                    } else {

                        // On ajoute la coordonnée que si elle n'existe pas encore
                        if (!this.opponentPositions.contains(coord)) {
                            this.opponentPositions.add(coord);
                        }

                        // Si la coordonnée est dans notre liste de positions, on la supprime parce que ça veut dire que
                        // l'adversaire nous l'a prise
                        if (this.positions.contains(coord)) {
                            this.positions.remove(coord);
                        }
                    }
                }
            }
            this.map[x][y].fill(kind, population);
        }
    }

    /**
     * Récupère la coordonnée initiale de notre joueur
     * @param home
     */
    public void setHome(byte[] home) {
        // On récupère les données et on les met dans source
        int x = (int) home[0];
        int y = (int) home[1];
        this.home = new Coord(x, y);
        positions.add(this.home);
    }

    /**
     * Assigne la bonne race lors de la première boucle de remplissage de la carte
     * @param race
     */
    private void setRace(String race) {
        this.race = race;
    }

    /**
     * On joue notre tour avec l'algo AlphaBeta
     * @return
     */
    public ArrayList<byte[]> chooseMove() {
        ArrayList<byte[]> results = new ArrayList<>();
        HashMap<Coord, Result> algResults = AlphaBeta.getAlphaBetaMove(this);
        for (Coord position: algResults.keySet()) {
            Result res = algResults.get(position);
            results.add(res.parse());
        }
        return results;
    }

    /**
     * Calcule notre nombre d'éléments
     * @return
     */
    protected int population() {
        int pop = 0;
        for (Coord coord: positions) {
            pop += this.map[coord.x][coord.y].population;
        }
        return pop;
    }

    /**
     * Calcule le nombre d'éléments de l'adversaire
     * @return
     */
    protected int opponentPopulation() {
        int pop = 0;
        for (Coord coord: opponentPositions) {
            pop += this.map[coord.x][coord.y].population;
        }
        return pop;
    }

    /**
     * Calcule le nombre d'humains
     * @return
     */
    protected int humanPopulation() {
        int pop = 0;
        for (Coord coord: humanPositions) {
            pop += this.map[coord.x][coord.y].population;
        }
        return pop;
    }

    /**
     * Crée une copie de la carte, avant la simulation d'un mouvement de l'origine vers le next move qui mène à notre
     * goal
     * @param origin
     * @param goal
     * @return
     */
    protected MapManager cloneWithMovement(Coord origin, Coord goal) {
        // On crée d'abord une copie de notre carte
        MapManager clonedMap = MapManager.copy(this);

        // On trouve la prochaine coordonnée qui nous permet d'arriver au goal en partant de origin
        Cell originCell = clonedMap.map[origin.x][origin.y];

        // Si la cellule d'origine nous appartient
        if (originCell.kind.equals(this.race)) {
            // On ajoute la next position à la liste de nos positions
            clonedMap.positions.add(goal);
            // On la supprime des humains et des ennemies (on ne sait pas où elle est)
            clonedMap.humanPositions.remove(goal);
            clonedMap.opponentPositions.remove(goal);
            // On déplace toutes les troupes pour le moment donc on supprime la cellule d'origine de nos positions sur
            // la carte clonée
            clonedMap.positions.remove(origin);
            // Sinon ça veut dire qu'elle appartient à l'adversaire
        } else {
            // On ajoute la next position à la liste des positions ennemies
            clonedMap.opponentPositions.add(goal);
            // On la supprime des humains et des notres
            clonedMap.humanPositions.remove(goal);
            clonedMap.positions.remove(goal);
            // On supprime la cellule d'origine des ennemies
            clonedMap.opponentPositions.remove(origin);
        }
        // Comme on bouge toutes les troupes, la cellule d'origine est vide
        clonedMap.map[origin.x][origin.y] = new Cell();
        // La next cellule devient remplie des valeurs de la cellule d'origine
        clonedMap.map[goal.x][goal.y] = originCell;
        return clonedMap;
    }

    /**
     * On crée une nouvelle carte en inversant les races. Nos positions deviennent celles de l'adversaire et les
     * positions de l'adversaire deviennent les notres. Utilisée pour la partie min du AlphaBeta
     * @return
     */
    protected MapManager flip() {
        MapManager flippedMap = MapManager.copy(this);
        flippedMap.race = this.race.equals("vampires") ? "werewolves" : "vampires";
        flippedMap.positions = this.opponentPositions;
        flippedMap.opponentPositions = this.positions;
        return flippedMap;
    }

    /**
     * Crée une copie d'une MapManager (comme ça on ne touche pas à l'original)
     * @param orig
     * @return
     */
    private static MapManager copy(MapManager orig) {
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
        return (MapManager) obj;
    }
}
