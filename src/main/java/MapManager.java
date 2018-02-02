package main.java;

import java.util.Random;
import static java.lang.Thread.sleep;

public class MapManager {

    private int[] coord = new int[2];
    private int[][][] map;

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

    public void setInitialCoord(byte[] home) {
        int x = (int) home[0];
        int y = (int) home[1];
        coord[0] = x;
        coord[1] = y;
    }

    public byte[][] chooseMove() {
        try {
            Thread.sleep(800);
            int[] destination = new int[2];
            destination[0] = coord[0];
            destination[1] = coord[1];
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
            byte[][] res = new byte[1][5];
            res[0][0] = (byte) coord[0];
            res[0][1] = (byte) coord[1];
            res[0][2] = (byte) (map[coord[0]][coord[1]][1] + map[coord[0]][coord[1]][2]);
            res[0][3] = (byte) destination[0];
            res[0][4] = (byte) destination[1];
            coord[0] = destination[0];
            coord[1] = destination[1];
            return res;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new byte[0][0];
    }

    public int[][][] getMap() {
        return map;
    }

    public void setMap(int[][][] map) {
        this.map = map;
    }

}



public class Species {

}


public class Human extends Species{



}


public class Surnatural extends Species{



}