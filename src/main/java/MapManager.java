package main.java;

import java.util.Random;
import java.lang.*;

public class MapManager {

    /* Played race id (Vampires: 1 / Werewolves: 2). */
    private int race;
    /* List of coordinates for the played race. */
    private int[][] positions;
    /* List of move to send to the server, using the same format as understood by the server,
    ie a list of moves, where each move is a list of 5 elements: initial x position, initial y position, number of creatures to move, final x position and final y position.
     */
    private int[][] move;
    /* List of coordinates for the opponent race. */
    private int[][] adv_positions;
    /* List of coordinates for the humans. */
    private int[][] human_positions;
    /* Current map state: same format as defined by the server, ie indexed on x position, y position and race id.
     * Contains number of creatures in each cell. */
    private int[][][] map;

    /* After "SET" order from the server, set the map dimensions. */
    public void setMapDimensions(byte[] dimensions) {
        int rows = (int) dimensions[0];
        int cols = (int) dimensions[1];
        setMap(new int[cols][rows][3]);
        for(int i=0; i<cols; i++){
            for(int j=0; j<rows; j++){
                map[i][j] = new int[] {0, 0, 0};
            }
        }
    }

    /* After "MAP" or "UPD" order, update creatures' amount in the map. */
    public void fillMap(byte[][] content) {
        int x;
        int y;
        int humans;
        int vampires;
        int werewolves;
        int nbCells = content.length;
        for(int i=0; i<nbCells; i++){
            x = (int) content[i][0];
            y = (int) content[i][1];
            humans = (int) content[i][2];
            vampires = (int) content[i][3];
            werewolves = (int) content[i][4];
            map[x][y][0] = humans;
            map[x][y][1] = vampires;
            map[x][y][2] = werewolves;
        }
    }

    /* After "HME" order, initialize list of positions for the played race with the initial position. */
    public void setHome(byte[] home) {
        int x = (int) home[0];
        int y = (int) home[1];
        positions = new int[1][2];
        positions[0][0] = x;
        positions[0][1] = y;
    }

    /* After map has been initialized and home has been defined, identify which race is played. */
    public void setRace() {
        try {
            int x = this.positions[0][0];
            int y = this.positions[0][1];
            if (this.map[x][y][1] > this.map[x][y][2]) {
                this.race = 1;
            } else {
                this.race = 2;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /* Before playing our turn, looks map to update all positions lists. */
    private void defPositions() {
        int[][] humans = new int[0][2];
        int[][] vampires = new int[0][2];
        int[][] werewolves = new int[0][2];
        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                /* For each cell, if one race is present, add this cell coordinates to this race's positions list. */
                if (this.map[i][j][0] > 0) {
                    int[][] new_positions = new int[humans.length + 1][2];
                    System.arraycopy(humans, 0, new_positions, 0, humans.length);
                    new_positions[humans.length][0] = i;
                    new_positions[humans.length][1] = j;
                    humans = new_positions;
                } else if (this.map[i][j][1] > 0) {
                    int[][] new_positions = new int[vampires.length + 1][2];
                    System.arraycopy(vampires, 0, new_positions, 0, vampires.length);
                    new_positions[vampires.length][0] = i;
                    new_positions[vampires.length][1] = j;
                    vampires = new_positions;
                } else if (this.map[i][j][2] > 0) {
                    int[][] new_positions = new int[werewolves.length + 1][2];
                    System.arraycopy(werewolves, 0, new_positions, 0, werewolves.length);
                    new_positions[werewolves.length][0] = i;
                    new_positions[werewolves.length][1] = j;
                    werewolves = new_positions;
                }
            }
        }
        /* Update positions lists depending on which race is played. */
        this.human_positions = humans;
        if (this.race == 1) {
            this.positions = vampires;
            this.adv_positions = werewolves;
        } else {
            this.positions = werewolves;
            this.adv_positions = vampires;
        }
    }

    /* Play our turn. */
    public byte[][] chooseMove() {
        /* Update positions */
        this.defPositions();
        /* Call methods to choose a move */
        return this.randomMove();
    }

    /* For test only: a random move function. */
    public byte[][] randomMove() {
        try {
            Thread.sleep(600); /* To represent calculation time. */
            byte[][] res = new byte[0][5];
            /* For each position where our race is present, choose a random move. */
            for (int i = 0; i < this.positions.length; i++) {
                int[] destination = new int[2];
                int[] coord = this.positions[i];
                destination[0] = coord[0];
                destination[1] = coord[1];
                /* Choose randomly an adjacent cell. Try again while this cell is outside of the map. */
                while ((destination[0] == coord[0] && destination[1] == coord[1]) || destination[0] < 0 || destination[0] >= map.length || destination[1] < 0 || destination[1] >= map[0].length) {
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
                /* To represent that group may split up, choose a random number between 1 and 100.
                 * If this number is lower than number of creatures in the current cell, only this number of creatures will move. */
                Random randomGenerator = new Random();
                int nb_to_move = Math.min((map[coord[0]][coord[1]][1] + map[coord[0]][coord[1]][2]) , randomGenerator.nextInt(100) + 1);
                /* Add this move to the move list. */
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

    /* Setters and getters */

    public int[][][] getMap() {
        return map;
    }

    public void setMap(int[][][] map) {
        this.map = map;
    }

}



/*

public class Species {

}


public class Human extends Species{



}


public class Surnatural extends Species{



} */