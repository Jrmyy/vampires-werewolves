import main.java.MapManager;

import java.util.Random;

public class Project {

    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        MapManager map = new MapManager();
        client.sendName();
        boolean gameRunning = true;
        String order = "";
        while (gameRunning) {
            order = client.listen();
            if (order.equals("SET")) {
                byte[] dimensions = client.listenSET();
                map.setMapDimensions(dimensions);
            } else if (order.equals("HUM")) {
                byte[][] humanHouses = client.listenHUM();
            } else if (order.equals("HME")) {
                byte[] home = client.listenHME();
                map.setHome(home);
            } else if (order.equals("MAP")) {
                byte[][] content = client.listenMAP();
                map.fillMap(content);
                map.setRace();
            } else if (order.equals("UPD")) {
                byte[][] update = client.listenMAP();
                map.fillMap(update);
                byte[][] move = map.chooseMove();
                client.sendMove(move);
            } else if (order.equals("BYE")) {
                gameRunning = false;
            }
        }
    }

}

