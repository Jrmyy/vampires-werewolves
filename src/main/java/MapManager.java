import java.util.Random;
import java.lang.*;


public class MapManager {

    private String race;
    private int[][] positions;
    private int[][] move;
    private int[][] adv_positions;
    private int[][] human_positions;
    private Coord source = new Coord();
    private Cell[][] map;

    /**
     * Set map dimensions (without filling it)
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
        source.x = x;
        source.y = y;
    }

    public void setRace() {
        try {
            int x = this.positions[0][0];
            int y = this.positions[0][1];
            if (this.map[x][y].vampires > this.map[x][y].werewolves) {
                this.race = "vampires";
            } else {
                this.race = "werewolves";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void defPositions() {
        int[][] humans = new int[0][2];
        int[][] vampires = new int[0][2];
        int[][] werewolves = new int[0][2];
        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                if (this.map[i][j].humans > 0) {
                    int[][] new_positions = new int[humans.length + 1][2];
                    System.arraycopy(humans, 0, new_positions, 0, humans.length);
                    new_positions[humans.length][0] = i;
                    new_positions[humans.length][1] = j;
                    humans = new_positions;
                } else if (this.map[i][j].vampires > 0) {
                    int[][] new_positions = new int[vampires.length + 1][2];
                    System.arraycopy(vampires, 0, new_positions, 0, vampires.length);
                    new_positions[vampires.length][0] = i;
                    new_positions[vampires.length][1] = j;
                    vampires = new_positions;
                } else if (this.map[i][j].werewolves > 0) {
                    int[][] new_positions = new int[werewolves.length + 1][2];
                    System.arraycopy(werewolves, 0, new_positions, 0, werewolves.length);
                    new_positions[werewolves.length][0] = i;
                    new_positions[werewolves.length][1] = j;
                    werewolves = new_positions;
                }
            }
        }
        this.human_positions = humans;
        if (this.race == 1) {
            this.positions = vampires;
            this.adv_positions = werewolves;
        } else {
            this.positions = werewolves;
            this.adv_positions = vampires;
        }
    }

    public byte[][] chooseMove() {
        /* Faire appel ici à toutes les méthodes utiles pour déterminer le mouvement à faire */
        this.defPositions();
        return this.randomMove();
    }

    public byte[][] randomMove() {
        try {
            /* Pour modéliser le temps de calcul, à retirer bien évidemment dans la version finales */
            Thread.sleep(600);
            byte[][] res = new byte[0][5];
            for (int i = 0; i < this.positions.length; i++) {
                int[] destination = new int[2];
                int[] coord = this.positions[i];
                destination[0] = coord[0];
                destination[1] = coord[1];
                while (
                        (destination[0] == coord[0] && destination[1] == coord[1]) ||
                                destination[0] < 0 || destination[0] >= map.length ||
                                destination[1] < 0 || destination[1] >= map[0].length
                        ) {
                    Random randomGenerator = new Random();
                    int dir = randomGenerator.nextInt(8);
                    if (dir >= 1 && dir <= 3) {
                        destination[0] = coord[0] + 1;
                    } else if (dir >= 5 && dir <= 7) {
                        destination[0] = coord[0] - 1;
                    }
                    if (dir >= 3 && dir <= 5) {
                        destination[1] = coord[1] + 1;
                    } else if (dir >= 7 || dir <= 1) {
                        destination[1] = coord[1] - 1;
                    }
                }
                Random randomGenerator = new Random();
                int nb_to_move = Math.min((map[coord[0]][coord[1]][1] + map[coord[0]][coord[1]][2]) , randomGenerator.nextInt(100) + 1);
                byte[] move = new byte[5];
                move[0] = (byte) coord[0];
                move[1] = (byte) coord[1];
                move[2] = (byte) nb_to_move;
                move[3] = (byte) destination[0];
                move[4] = (byte) destination[1];
                byte[][] new_res = new byte[res.length + 1][5];
                System.arraycopy(res, 0, new_res, 0, res.length);
                System.arraycopy(move, 0, new_res[res.length], 0, 5);
                res = new_res;
            }
            return res;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new byte[0][0];
    }

}
