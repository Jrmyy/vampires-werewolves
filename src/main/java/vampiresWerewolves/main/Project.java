package main;

import board.Board;
import tcp.TCPClient;

import java.util.ArrayList;
import java.util.logging.LogManager;

public class Project {

    public static void main(String[] args) throws Exception {

        LogManager.getLogManager().reset();

        TCPClient client = new TCPClient(args[0], Integer.parseInt(args[1]));

        Board map = new Board();
        client.sendName();
        boolean gameRunning = true;
        String order;
        while (gameRunning) {
            order = client.listen();
            switch (order) {
                case "SET":
                    byte[] dimensions = client.listenSET();
                    map.setMapDimensions(dimensions);
                    break;
                case "HUM":
                    byte[][] humanHouses = client.listenHUM();
                    break;
                case "HME":
                    byte[] home = client.listenHME();
                    map.setHome(home);
                    break;
                case "MAP":
                    byte[][] content = client.listenMAP();
                    map.fillOrUpdateMap(content);
                    break;
                case "UPD":
                    byte[][] update = client.listenMAP();
                    map.fillOrUpdateMap(update);
                    ArrayList<byte[]> moves = map.chooseMove();
                    client.sendMove(moves);
                    break;
                case "BYE":
                    gameRunning = false;
                    break;
            }
        }
    }

}
