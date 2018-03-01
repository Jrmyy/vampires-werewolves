public class MapManager {

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
    public void setInitialCoord(byte[] home) {
        // On récupère les données et on les met dans source
        int x = (int) home[0];
        int y = (int) home[1];
        source.x = x;
        source.y = y;
    }

    public byte[][] chooseMove() {
        try {
            Thread.sleep(800);
            Coord destination = new Coord(source.x, source.y);
            /*
             * On n'arrête pas tant que l'on n'a pas un résultat correct c'est à dire:
             *  - destination != source
             *  - destination n'est pas valide
             */
            while (
                    (destination.x == source.x && destination.y == source.y) ||
                            destination.x < 0 || destination.x >= map.length ||
                            destination.y < 0 || destination.y >= map[0].length) {

                // IA Logic here

            }
            byte[][] res = new byte[1][5];
            res[0][0] = (byte) source.x;
            res[0][1] = (byte) source.y;
            res[0][2] = (byte) (map[source.x][source.y].werewolves + map[source.x][source.y].vampires);
            res[0][3] = (byte) destination.x;
            res[0][4] = (byte) destination.y;
            source.x = destination.x;
            source.y = destination.y;
            return res;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new byte[0][0];
    }

}
