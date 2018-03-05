import java.util.ArrayList;
import java.util.Random;
import java.lang.*;


public class MapManager {

    // Peut être vampire ou loup garou
    private String race = null;
    // Liste des coordonnées pour la race que l'on joue
    private ArrayList<Coord> positions = new ArrayList<>();
    private Coord home;
    // Liste des coordonées de l'adversaire
    private ArrayList<Coord> opponentPositions = new ArrayList<>();
    // Liste des coordonnées des humains
    private ArrayList<Coord> humanPositions = new ArrayList<>();
    // Etat de la carte
    private Cell[][] map;

    /**
     * On initialise les dimensions de la carte, sans remplir avec les données
     * @param dimensions
     */
    public void setMapDimensions(byte[] dimensions) {
        // On récupère le nombre de columns et de lignes
        int rows = (int) dimensions[0];
        int cols = (int) dimensions[1];

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
            this.map[x][y].fill(humans, vampires, werewolves);
            Coord coord = new Coord(x, y);

            // S'il y a des humains et que la coordonnée n'est pas dans la liste des humains, on remplit
            if (humans > 0 && !this.humanPositions.contains(coord)) {

                this.humanPositions.add(coord);

            } else if (this.race == null && (vampires > 0 || werewolves > 0)) {

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

            } else if (vampires > 0 || werewolves > 0) {

                // Si on a la race, on est dans une assignation update et on assigne juste en fonction de la race
                if (
                        (vampires > 0 && this.race.equals("vampires"))
                        || (werewolves > 0 && this.race.equals("werewolves"))
                    ) {

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
     * On joue notre tour
     * @return
     */
    public ArrayList<byte[]> chooseMove() {
        // On fait notre mouvement
        return this.randomMove();
    }

    /**
     * Fonction heuristique pour un seul groupe de crétures
     * @param map
     * @param coord
     * @param humanPositions
     * @return
     */
    private double localScoreFunction(Cell[][] map, Coord coord, ArrayList<Coord> humanPositions) {
        int nbCreatures;
        int nbHumans;
        int distance;
        double nbConverted; // Nombre d'humain convertis espérés (avec prise en compte des pertes subies)
        // Facteurs à déterminer expérimentalement
        double a = 1; // Importance dans le score des créatures effectives dans chaque camp
        double b = 1; // Importance dans le score des humains pouvant être convertis
        double phi = 0.8; // Facteur de décroissance pour accorder moins d'importance aux humains éloignés
        double score = 0;
        // Score des créatures alliées sur la case
        nbCreatures = map[coord.x][coord.y].vampires + map[coord.x][coord.y].werewolves;
        score += a * nbCreatures;
        for (Coord humanCoord : humanPositions) {
            // Score des humains à distance des créatures alliées
            nbHumans = map[humanCoord.x][humanCoord.y].humans;
            distance = Math.max(Math.abs(coord.x - humanCoord.x), Math.abs(coord.y - humanCoord.y));
            if (nbCreatures >= nbHumans){
                nbConverted = nbHumans;
            } else {
                nbConverted = (nbCreatures / 2) - ((2 * nbHumans - nbCreatures) * nbCreatures / (2 * nbHumans));
            }
            score += b * Math.pow(phi, distance - 1) * nbConverted;
        }
        return score;
    }

    /**
     * Fonction heuristique pour évaluer une situation donnée de la carte
     * @param map
     * @param positions
     * @param humanPositions
     * @param opponentPositions
     * @return
     */
    private double scoreFunction(Cell[][] map, ArrayList<Coord> positions, ArrayList<Coord> opponentPositions,
                                 ArrayList<Coord> humanPositions) {
        double score = 0;
        for (Coord coord : positions) {
            score += this.localScoreFunction(map, coord, humanPositions);
        }
        for (Coord opponentCoord : opponentPositions) {
            score -= this.localScoreFunction(map, opponentCoord, humanPositions);
        }
        return score;
    }

    /**
     * Mouvement aléatoire pour le moment
     * @return
     */
    private ArrayList<byte[]> randomMove() {
        try {
            // Pour modéliser le temps de calcul, à retirer bien évidemment dans la version finales
            Thread.sleep(600);
            ArrayList<byte[]> results = new ArrayList<>();
            // Pour chacune de nos positions, faire un mouvement aléatoire
            for (Coord source : this.positions) {
                Coord destination = new Coord(source.x, source.y);
                while (
                        (source.x == destination.x && source.y == destination.y) ||
                                destination.x < 0 || destination.x >= this.map.length ||
                                destination.y < 0 || destination.y >= this.map[0].length
                        ) {
                    Random randomGenerator = new Random();
                    int dir = randomGenerator.nextInt(8);
                    if (dir >= 1 && dir <= 3) {
                        destination.x = source.x + 1;
                    } else if (dir >= 5 && dir <= 7) {
                        destination.x = source.x - 1;
                    }
                    if (dir >= 3 && dir <= 5) {
                        destination.y = source.y + 1;
                    } else if (dir >= 7 || dir <= 1) {
                        destination.y = source.y - 1;
                    }
                }

                // On représente la possibilité de se séparer en prenant un nombre aléatoire entre 1 et 100. Si ce
                // nombre est plus petit que le nombre de créature, seulement ce nombre est bougé, sinon on bouge tout
                Random randomGenerator = new Random();
                int nbMoves = Math.min(
                        map[source.x][source.y].vampires + map[source.x][source.y].werewolves,
                        randomGenerator.nextInt(100) + 1
                );

                // On ajoute ce mouvement à la liste des mouvements à faire
                byte[] move = new byte[5];
                move[0] = (byte) source.x;
                move[1] = (byte) source.y;
                move[2] = (byte) nbMoves;
                move[3] = (byte) destination.x;
                move[4] = (byte) destination.y;
                results.add(move);
            }
            return results;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
