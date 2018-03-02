import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.lang.*;


public class MapManager {

    // Peut être vampire ou loup garou
    private String race;
    // Liste des coordonnées pour la race que l'on joue
    private ArrayList<Coord> positions;
    // Liste des mouvements à envoyer au serveur (format 5 bits)
    private ArrayList<Coord> moves;
    // Liste des coordonées de l'adversaire
    private Coord opponentPosition;
    // Liste des coordonnées des humains
    private ArrayList<Coord> humanPositions;
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
    public void fillMap(byte[][] content) {
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
        Coord position = new Coord(x, y);
        positions.add(position);
    }

    public void setRace() {
        try {
            int x = positions.get(0).x;
            int y = positions.get(0).y;
            if (this.map[x][y].vampires > this.map[x][y].werewolves) {
                this.race = "vampires";
            } else {
                this.race = "werewolves";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Avant notre tour, on va mettre à jour notre carte avec les nouvelles positions
     * INTERET ??????
     */
//    private void defPositions() {
//        int[][] humans = new int[0][2];
//        int[][] vampires = new int[0][2];
//        int[][] werewolves = new int[0][2];
//        for (int i = 0; i < this.map.length; i++) {
//            for (int j = 0; j < this.map[i].length; j++) {
//                if (this.map[i][j].humans > 0) {
//                    humanPositions.add(new Coord(i, j));
//                } else if (this.map[i][j].vampires > 0) {
//                    int[][] new_positions = new int[vampires.length + 1][2];
//                    System.arraycopy(vampires, 0, new_positions, 0, vampires.length);
//                    new_positions[vampires.length][0] = i;
//                    new_positions[vampires.length][1] = j;
//                    vampires = new_positions;
//                } else if (this.map[i][j].werewolves > 0) {
//                    int[][] new_positions = new int[werewolves.length + 1][2];
//                    System.arraycopy(werewolves, 0, new_positions, 0, werewolves.length);
//                    new_positions[werewolves.length][0] = i;
//                    new_positions[werewolves.length][1] = j;
//                    werewolves = new_positions;
//                }
//            }
//        }
//        /* Update positions lists depending on which race is played. */
//        this.humanPositions = humans;
//        if (this.race == 1) {
//            this.positions = vampires;
//            this.adv_positions = werewolves;
//        } else {
//            this.positions = werewolves;
//            this.adv_positions = vampires;
//        }
//    }

    /**
     * On joue notre tour
     * @return
     */
    public byte[][] chooseMove() {
        // On fait notre mouvement
        ArrayList<byte[]> moves = this.randomMove();
        return moves.toArray(new byte[0][]);
    }

    /* For test only: a random move function. */
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
                                destination.x < 0 || destination.x >= map.length ||
                                destination.y < 0 || destination.y >= map[0].length
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
