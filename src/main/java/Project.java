public class Project {

    public static void main(String[] args) throws Exception {
        //TCPClient client = new TCPClient(args[0], Integer.parseInt(args[1]));
        TCPClient client = new TCPClient("localhost", 5555);

        MapManager map = new MapManager();
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
                    map.fillMap(content);
                    map.setRace();
                    break;
                case "UPD":
                    byte[][] update = client.listenMAP();
                    map.fillMap(update);
                    byte[][] move = map.chooseMove();
                    client.sendMove(move);
                    break;
                case "BYE":
                    gameRunning = false;
                    break;
            }
        }
    }

}
